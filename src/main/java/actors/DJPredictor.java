package actors;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import Exceptions.CrawlingSourceUnavailableException;
import Exceptions.EndOfFileException;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.DeciderBuilder;
import com.lightbend.akka.sample.Greeter;
import helpers.CrawlerConfig;
import helpers.IndexHistoryReader;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
    private static List<ActorRef> children;
    private ActorRef manager;
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(10, Duration.ofMinutes(1),
                    DeciderBuilder
                            .match(CrawlingSourceUnavailableException.class, e -> SupervisorStrategy.restart())
                            .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                            .match(IllegalArgumentException.class, e -> {
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
        public final LocalDate predictionDate;

        public StartPrediction(LocalDate predictionDate) {
            this.predictionDate = predictionDate;
        }
    }


    private List<Article> getArticlesByDate(LocalDate articlesDate) {
        return receivedArticles.stream().filter(article -> article.getDate().isEqual(articlesDate)).collect(Collectors.toList());
    }

    private void predict(List<Article> articles, List<IndexDescriptor> indexHistory) {
        try {
            log.info("DJ Predictor " + getSelf().path() + " is trying to communicate with model.");
            //todo: communicate with model

        } catch (Exception e) {
            log.error("During communicating with model in DJ Predictor " + getSelf().path() + e.getClass() + " has occurred");
            manager.tell(new Status.Failure(e), getSelf());
        }
    }

    private void startPrediction(LocalDate dateOfPrediction) {
        log.info("DJ Predictor " + getSelf().path() + " is starting prediction job.");

        children.forEach(x -> x.tell(new RedditCrawler.StartCrawling(1, TimeUnit.DAYS), getSelf()));

        new Thread(() -> {
            List<Article> articleForPrediction = getArticlesByDate(dateOfPrediction);
            predict(articleForPrediction, IndexHistoryReader.readHistory(dateOfPrediction, 1, TimeUnit.DAYS));
        }).start();

    }

    private DJPredictor(ActorRef manager) {
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

    public static Props props(ActorRef manager) {
        return Props.create(DJPredictor.class, () -> new DJPredictor(manager));
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

                    startPrediction(x.predictionDate);
                })
                .build();
    }
}
