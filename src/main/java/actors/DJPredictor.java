package actors;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import DomainObjects.Prediction;
import Exceptions.CrawlingSourceUnavailableException;
import Exceptions.EndOfFileException;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import helpers.CrawlerConfig;
import helpers.IndexHistoryReader;
import helpers.ModelConfig;

import java.io.*;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DJPredictor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static final int minimumWorkingChildren = 2;

    private ModelConfig predictorConfig;
    private static int childrenCounter = 0;
    private BlockingQueue<Article> receivedArticles = new LinkedBlockingQueue<>();
    private BlockingQueue<LocalDate> predictionRequests = new LinkedBlockingQueue<>();
    private static List<ActorRef> children;
    private ActorRef manager;
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.ofMinutes(1),
                    DeciderBuilder
                            .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                            .match(CrawlingSourceUnavailableException.class, e -> {
                                if (childrenCounter <= minimumWorkingChildren) return SupervisorStrategy.escalate();
                                else {
                                    childrenCounter--;
                                    return SupervisorStrategy.stop();
                                }
                            })
                            .match(EndOfFileException.class, e -> {
                                if (childrenCounter == 0) return SupervisorStrategy.escalate();
                                else {
                                    childrenCounter--;
                                    return SupervisorStrategy.stop();
                                }
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
        return receivedArticles.stream().filter(article -> article.getDate().isEqual(articlesDate)).collect(Collectors.toList());
    }

    private void predict(List<Article> articles, List<IndexDescriptor> indexHistory) {
        try {
            log.info("DJ Predictor " + getSelf().path() + " is trying to communicate with model.");
            getModelPredictions(articles);


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
                LocalDate dateOfPrediction = predictionRequests.take();

                //todo: delete after tests
                manager.tell(new PortfolioManager.PredictionResult(dateOfPrediction, new ArrayList<>(), getSelf().path()), getSelf());

                List<Article> articleForPrediction = getArticlesByDate(dateOfPrediction);
                predict(articleForPrediction, IndexHistoryReader.readHistory(dateOfPrediction, 1, TimeUnit.DAYS));

            } catch (Exception e) {
                log.error("In DJ Predictor " + getSelf().path() + " an error " + e.getMessage() + "has occurred. Sending information to Manager.");
                manager.tell(new PortfolioManager.DJPredictionException(predictorConfig), getSelf());
                log.error(e.getMessage());
            }
        }).start();

    }

    private List<CrawlerConfig> readCrawlerConfigs() {
        ArrayList<FileInputStream> propertiesStreams = new ArrayList<>();
        ArrayList<CrawlerConfig> configs = new ArrayList<>();

        try {
            propertiesStreams.add(new FileInputStream("crawler1.properties"));
            propertiesStreams.add(new FileInputStream("crawler2.properties"));
            propertiesStreams.add(new FileInputStream("crawler3.properties"));
            propertiesStreams.add(new FileInputStream("crawler4.properties"));
            propertiesStreams.add(new FileInputStream("crawler5.properties"));

            for (FileInputStream fis : propertiesStreams) {
                Properties prop = new Properties();
                prop.load(fis);

                configs.add(new CrawlerConfig(Integer.getInteger(prop.getProperty("readModulo"))));
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("DJ Predictor " + getSelf().path() + " cannot read all properties files.");
            manager.tell(new PortfolioManager.DJPredictorCrawlersException(predictorConfig), getSelf());
        }

        return configs;
    }

    private List<Prediction> getModelPredictions(List<Article> articles) {
        List<Prediction> predictionsToReturn = new ArrayList<>();

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"src\\main\\models\" && python test.py Hello");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            String[] predictions = stdInput.readLine().replaceAll("\\[", "")
                    .replaceAll("\\]","").split(",");

            for (int i = 0; i < predictions.length; i++) {
                predictionsToReturn.add(new Prediction(i, Double.valueOf(predictions[i]), 1));
            }

        } catch (IOException e) {
            System.out.println("Reading predictions from model failed");
            e.printStackTrace();
        }
        return predictionsToReturn;
    }

    private DJPredictor(ActorRef manager, ModelConfig modelConfig) {
        log.info("Creating DJ Predictor");
        children = new ArrayList<>();
        this.manager = manager;
        this.predictorConfig = modelConfig;

        List<CrawlerConfig> crawlerConfigs = readCrawlerConfigs();

        if(crawlerConfigs.size() == 0){
            log.error("DJ Predictor " + getSelf().path() + " can't create any RedditCrawler due to configuration files problem.");
            manager.tell(new PortfolioManager.DJPredictorCrawlersException(predictorConfig), getSelf());
        }

        for (CrawlerConfig config : crawlerConfigs) {
            children.add(getContext().getSystem().actorOf(RedditCrawler.props(getSelf(), config)));
            childrenCounter++;
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

                    childrenCounter--;
                    getSender().tell(Kill.getInstance(), getSelf());
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
