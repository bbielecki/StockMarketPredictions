package actors;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.lightbend.akka.sample.Greeter;
import helpers.CrawlerConfig;
import helpers.IndexHistoryReader;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class DJPredictor extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private BlockingQueue<Article> receivedArticles = new LinkedBlockingQueue<>();

    private List<Article> getArticlesByDate(LocalDate articlesDate){
        //todo: take articles with specified date from receivedArticles lsit;
        throw new NotImplementedException();
    }

    private void predict(List<Article> articles, List<IndexDescriptor> indexHistory){
        throw new NotImplementedException();
    }

    private void startPrediction(LocalDate dateOfPrediction){
        //todo: create many crawlers with different configs
        CrawlerConfig config = new CrawlerConfig();
        ActorRef crawlers = getContext().getSystem().actorOf(RedditCrawler.props(getSelf(), config));
        crawlers.tell(new RedditCrawler.StartCrawling(1, TimeUnit.DAYS), getSelf());

        new Thread(() -> {
            predict(getArticlesByDate(dateOfPrediction), IndexHistoryReader.readHistory(dateOfPrediction,1, TimeUnit.DAYS));
        }).start();

    }

    //confirmation that crawler has started it's job. It may be extended to inform Predictor that crawler keeps his job going on or etc.
    public static class CrawlerConfirmation{
        public CrawlerConfirmation(){}
    }

    public static class Headers{
        public final List<Article> articles;

        public Headers(List<Article> articles){
            this.articles = articles;
        }
    }

    public static class StartPrediction{
        public final LocalDate predictionDate;

        public StartPrediction(LocalDate predictionDate){
            this.predictionDate = predictionDate;
        }
    }

    static public Props props() {
        return Props.create(DJPredictor.class, () -> new DJPredictor());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Headers.class, x -> x.articles.forEach(a -> {
                    try {
                        receivedArticles.put(a);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }))
                .match(StartPrediction.class, x -> {
                    startPrediction(x.predictionDate);
                })
                .build();
    }
}
