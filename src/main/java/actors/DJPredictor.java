package actors;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import Exceptions.CrawlingSourceUnavailableException;
import Exceptions.EndOfFileException;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import helpers.CrawlerConfig;
import helpers.IndexHistoryReader;
import helpers.ModelConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DJPredictor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static final int minimumWorkingChildren = 2;

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
    public static class CrawlingSourceUnavailable{
        public final CrawlerConfig config;

        public CrawlingSourceUnavailable(CrawlerConfig config){
            this.config = config;
        }
    }
    public static class CrawlingException{
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
            manager.tell(new Status.Failure(e), getSelf());
        }
    }

    private void crawlArticles(LocalDate dateOfPrediction){
        log.info("DJ Predictor " + getSelf().path() + " is collecting articles for prediction.");
        children.forEach(x -> x.tell(new RedditCrawler.StartCrawling(dateOfPrediction), getSelf()));
    }

    private void startPrediction() {
        log.info("DJ Predictor " + getSelf().path() + " is starting prediction job.");

        new Thread(() -> {
            try{
                LocalDate dateOfPrediction = predictionRequests.take();
                List<Article> articleForPrediction = getArticlesByDate(dateOfPrediction);

                predict(articleForPrediction, IndexHistoryReader.readHistory(dateOfPrediction, 1, TimeUnit.DAYS));
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }).start();

    }

    private DJPredictor(ActorRef manager, ModelConfig modelConfig) {
        log.info("Creating DJ Predictor");
        children = new ArrayList<>();
        List<CrawlerConfig> crawlerConfigs = new ArrayList<>();
        this.manager = manager;

        //todo: load crawler configs
        crawlerConfigs.add(new CrawlerConfig());
        for (CrawlerConfig config : crawlerConfigs) {

            children.add( getContext().getSystem().actorOf(RedditCrawler.props(getSelf(), config)) );
            childrenCounter++;

        }
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
    }

    public static Props props(ActorRef manager, ModelConfig config) {
        return Props.create(DJPredictor.class, () -> new DJPredictor(manager, config));
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

                    getContext().setReceiveTimeout(x.predictionTimeout);
                    predictionRequests.add(x.predictionDate);
                    crawlArticles(x.predictionDate);
                })
                .match(CrawlingSourceUnavailable.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received message " + CrawlingSourceUnavailable.class + ". Killing a child which failed...");

                    childrenCounter--;
                    getSender().tell(Kill.getInstance(), getSelf());
                })
                .match(CrawlingException.class, x->{
                    log.info("DJ Predictor " + getSelf().path() + " received message " + CrawlingSourceUnavailable.class + ". Restarting a child which failed...");
                    getSender().tell(Kill.getInstance(), getSelf());
                    getContext().actorOf( RedditCrawler.props(getSelf(), x.config));
                })
                .match(ReceiveTimeout.class, x -> {
                    log.info("DJ Predictor " + getSelf().path() + " received message " + ReceiveTimeout.class + ". Starting prediction.");
                    if(predictionRequests.size() <= 1) getContext().setReceiveTimeout(scala.concurrent.duration.Duration.Undefined());
                    startPrediction();
                })
                .build();
    }


    private void getModelPredictions(List<Article> articles){
        String s = null;

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "cmd.exe", "/c", "cd \"src\\main\\models\" && python test.py Hello");
            builder.redirectErrorStream(true);
            Process p = builder.start();
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            //todo: match output format from model
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        }
        catch (IOException e) {
            System.out.println("Reading predictions from model failed");
            e.printStackTrace();
        }

    }

}