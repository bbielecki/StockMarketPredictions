package actors;

import DomainObjects.*;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import helpers.CrawlerConfigReader;
import helpers.IndexBehaviourPredictionModel;
import helpers.IndexHistoryReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Collections.nCopies;
import static java.util.stream.Collectors.toList;

public class DJPredictor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private ModelConfig predictorConfig;
    private BlockingQueue<Article> receivedArticles = new LinkedBlockingQueue<>();
    private BlockingQueue<LocalDate> predictionRequests = new LinkedBlockingQueue<>();
    private static List<ActorRef> children;
    private static int activeChildrenCounter;
    private ActorRef manager;
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.ofMinutes(1),
                    DeciderBuilder
                            .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                            .match(IOException.class, e -> SupervisorStrategy.restart())
                            .match(FileNotFoundException.class, e-> {
                                activeChildrenCounter--;
                                return SupervisorStrategy.stop();
                            })
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());

    //confirmation that crawler has started it's job. It may be extended to inform Predictor that crawler keeps his job going on or etc.
    public static class CrawlerConfirmation {
        public CrawlerConfirmation() {
        }
    }
    public static class Headers {
        public final List<Article> articles;

        public Headers(List<Article> articles) {
            this.articles = articles;
        }
    }
    public static class NoNewHeaders {
        public NoNewHeaders() {
        }
    }
    public static class StartPrediction {
        public final scala.concurrent.duration.Duration predictionTimeout;
        public final LocalDate predictionDate;

        public StartPrediction(LocalDate predictionDate, scala.concurrent.duration.Duration predictionTimeout) {
            this.predictionDate = predictionDate;
            this.predictionTimeout = predictionTimeout;
        }
    }
    public static class CrawlingSourceUnavailable {
        public final CrawlerConfig config;

        public CrawlingSourceUnavailable(CrawlerConfig config) {
            this.config = config;
        }
    }
    public static class CrawlingException {
        public final CrawlerConfig config;

        public CrawlingException(CrawlerConfig config) {
            this.config = config;
        }
    }



    private List<Article> getArticlesByDate(LocalDate articlesDate) {
        return receivedArticles.stream().filter(article -> article.getDate().isEqual(articlesDate)).collect(toList());
    }

    private List<IndexDescriptor> getIndexHistoryByDate(LocalDate dateOfPrediction) {
        return IndexHistoryReader.readHistory(predictorConfig.getIndexHistoryPath(), dateOfPrediction, 1);
    }

    private void predict(List<Article> articles, List<IndexDescriptor> indexHistory, LocalDate predictionDate) {
        try {
            log.info("DJ Predictor " + getSelf().path() + " is trying to communicate with model.");
            List<Prediction> predictions = getModelPredictions(articles, indexHistory);
            PortfolioManager.PredictionResult predictionResult = new PortfolioManager.PredictionResult(predictionDate, predictions, getSelf().path());
            manager.tell(predictionResult, getSelf());

        } catch (Exception e) {
            log.error("During communicating with model in DJ Predictor " + getSelf().path() + e.getClass() + " has occurred");
            manager.tell(new PortfolioManager.DJPredictorModelCommunicationError(predictorConfig), getSelf());
        }
    }

    private void crawlArticles(LocalDate dateOfPrediction) {
        log.info("DJ Predictor " + getSelf().path() + " is collecting articles for prediction.");
        children.forEach(x -> x.tell(new RedditCrawler.StartCrawling(dateOfPrediction), getSelf()));
    }

    private void startPrediction() {
        log.info("DJ Predictor " + getSelf().path() + " is starting prediction job.");

        new Thread(() -> {
            try {
                LocalDate predictionDate = predictionRequests.take();
                List<Article> articleForPrediction = getArticlesByDate(predictionDate);
                List<IndexDescriptor> indexHistoryForPrediction = getIndexHistoryByDate(predictionDate);
                predict(articleForPrediction, indexHistoryForPrediction, predictionDate);

            } catch (Exception e) {
                log.error("In DJ Predictor " + getSelf().path() + " an error " + e.getMessage() + "has occurred. Sending information to Manager.");
                manager.tell(new PortfolioManager.DJPredictionException(predictorConfig), getSelf());
                log.error(e.getMessage());
            }
        }).start();

    }

    private List<CrawlerConfig> readCrawlerConfigs() {
        List<CrawlerConfig> configs = new ArrayList<>();

        try {
            configs = CrawlerConfigReader.getAsList();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("DJ Predictor " + getSelf().path() + " cannot read all properties files.");
            manager.tell(new PortfolioManager.DJPredictorCrawlersException(predictorConfig), getSelf());
        }

        return configs;
    }

    private List<Prediction> getModelPredictions(List<Article> articles, List<IndexDescriptor> indexHistory) {
        List<Prediction> predictionsToReturn = new ArrayList<>();
        try {
            IndexBehaviourPredictionModel model = new IndexBehaviourPredictionModel(
                    "127.0.0.1", // TODO Read from config
                    10000, // TODO Read from config
                    nCopies(5, (double) activeChildrenCounter / predictorConfig.getMaxCrawlers()));
            predictionsToReturn.addAll(model.predict(articles, indexHistory));
        } catch (IOException e) {
            System.out.println("Reading predictions from model failed");
            manager.tell(new PortfolioManager.DJPredictorModelCommunicationError(predictorConfig), getSelf());
            e.printStackTrace();
        }

        return predictionsToReturn;
    }

    private DJPredictor(ActorRef manager, ModelConfig modelConfig) {
        log.info("Creating DJ Predictor");
        children = new ArrayList<>();
        activeChildrenCounter = 0;
        this.manager = manager;
        this.predictorConfig = modelConfig;

        List<CrawlerConfig> crawlerConfigs = readCrawlerConfigs();

        if(crawlerConfigs.size() == 0){
            log.error("DJ Predictor " + getSelf().path() + " can't create any RedditCrawler due to configuration files problem.");
            manager.tell(new PortfolioManager.DJPredictorCrawlersException(predictorConfig), getSelf());
        }

        int i = 1;
        for (CrawlerConfig config : crawlerConfigs) {
            children.add(getContext().actorOf(RedditCrawler.props(getSelf(), config), RedditCrawler.class.getSimpleName() + i++));
            activeChildrenCounter++;
        }
    }



    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public static Props props(ActorRef manager, ModelConfig modelConfig) {
        return Props.create(DJPredictor.class, () -> new DJPredictor(manager, modelConfig));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Headers.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received request " + Headers.class);

                    if (receivedArticles.size() == 0)
                        x.articles.forEach(a -> {
                            try {
                                receivedArticles.put(a);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        });
                })
                .match(NoNewHeaders.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received request " + NoNewHeaders.class);
                })
                .match(StartPrediction.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received request " + StartPrediction.class);

                    //start prediction request triggers DJ predictor timeout which will be cancelled only if all prediction request will be handled.
                    getContext().setReceiveTimeout(x.predictionTimeout);
                    //add prediction date to prediction job queue.
                    predictionRequests.put(x.predictionDate);
                    crawlArticles(x.predictionDate);
                })
                .match(CrawlingSourceUnavailable.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received message " + CrawlingSourceUnavailable.class + ". Killing a child which failed...");

                    getSender().tell(Kill.getInstance(), getSelf());
                    activeChildrenCounter--;
                })
                .match(CrawlingException.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received message " + CrawlingSourceUnavailable.class + ". Restarting a child which failed...");
                    getSender().tell(Kill.getInstance(), getSelf());
                    getContext().actorOf(RedditCrawler.props(getSelf(), x.config));
                })
                .match(ReceiveTimeout.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received message " + ReceiveTimeout.class + ". Starting prediction.");
                    if (predictionRequests.size() <= 1)
                        getContext().setReceiveTimeout(scala.concurrent.duration.Duration.Undefined());
                    startPrediction();
                })
                .build();
    }
}
