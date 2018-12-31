package actors;

import DomainObjects.Article;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import helpers.ArticleFileParser;
import helpers.CrawlerConfig;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RedditCrawler extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private final ActorRef predictor;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final CrawlerConfig config;

    public static class StartCrawling{
        final int timeSpanBetweenCrawlings;
        final TimeUnit timeUnit;

        public StartCrawling(int timeSpanBetweenCrawlings, TimeUnit timeUnit){
            this.timeSpanBetweenCrawlings = timeSpanBetweenCrawlings;
            this.timeUnit = timeUnit;
        }
    }




    private RedditCrawler(ActorRef predictor, CrawlerConfig config){
        this.predictor = predictor;
        this.config = config;
    }

    //should get the configuration which determine the range of the articles (first 5, random 5 etc...)
    private void crawlDataSource(){
        //get articles by date.
        //todo: use config
        List<Article> newArticles = ArticleFileParser.readArticles(LocalDate.now());

        //automatically send crawled articles to subscribed index predictor
        if (!newArticles.isEmpty())
            predictor.tell(new DJPredictor.Headers(newArticles), getSelf());
        else
            predictor.tell(new DJPredictor.NoNewHeaders(), getSelf());
    }

    //starting crawler work. It will periodically send articles to subscribed predictor (which had created this Crawler)
    private void startCrawling(StartCrawling startCrawlingMessage){
        scheduler.scheduleAtFixedRate(this::crawlDataSource, 0, startCrawlingMessage.timeSpanBetweenCrawlings, startCrawlingMessage.timeUnit);
    }

    static public Props props( ActorRef predictor, CrawlerConfig config ) {
        return Props.create(RedditCrawler.class, () -> new RedditCrawler(predictor, config));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartCrawling.class, x ->{
                    startCrawling(x);
                    this.predictor.tell(new DJPredictor.CrawlerConfirmation(), getSelf());
                })
                .build();
    }
}
