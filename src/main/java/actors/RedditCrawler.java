package actors;

import DomainObjects.Article;
import Exceptions.CrawlingSourceUnavailableException;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import helpers.ArticleFileParser;
import helpers.CrawlerConfig;
import scala.collection.script.Start;
import scala.concurrent.duration.Duration;

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
        log.info("Creating Reddit Crawler");
        this.predictor = predictor;
        this.config = config;
    }

    //should get the configuration which determine the range of the articles (first 5, random 5 etc...)
    private void crawlDataSource(){
        log.error("In Reddit Crawler " + getSelf().path() + CrawlingSourceUnavailableException.class + " has occurred");
        predictor.tell(new Status.Failure(new CrawlingSourceUnavailableException()), getSelf());
        //get articles by date.
//        try{
//            //todo: use config
//            List<Article> newArticles = ArticleFileParser.readArticles(LocalDate.now());
//
//            //automatically send crawled articles to subscribed index predictor
//            if (!newArticles.isEmpty())
//                predictor.tell(new DJPredictor.Headers(newArticles), getSelf());
//            else
//                predictor.tell(new DJPredictor.NoNewHeaders(), getSelf());
//
//            getContext().setReceiveTimeout(Duration.Undefined());
//        }
//        catch (Exception e){
//            getContext().setReceiveTimeout(Duration.Undefined());
//            predictor.tell(new Status.Failure(e), getSelf());
//        }
    }

    //starting crawler work. It will periodically send articles to subscribed predictor (which had created this Crawler)
    private void startCrawling(StartCrawling startCrawlingMessage) {
        log.info("Reddit Crawler " + getSelf().path() + " is starting crawling job.");
        scheduler.scheduleAtFixedRate(this::crawlDataSource, 0, startCrawlingMessage.timeSpanBetweenCrawlings, startCrawlingMessage.timeUnit);
    }

    static public Props props( ActorRef predictor, CrawlerConfig config ) {
        return Props.create(RedditCrawler.class, () -> new RedditCrawler(predictor, config));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartCrawling.class, x ->{
                    log.info("Reddit Crawler " + getSelf().path() + " received request " + StartCrawling.class);
                    getContext().setReceiveTimeout(Duration.create(10, TimeUnit.SECONDS));
                    startCrawling(x);
                    this.predictor.tell(new DJPredictor.CrawlerConfirmation(), getSelf());
                })
                .match(ReceiveTimeout.class, x -> {
                    log.info("Reddit Crawler " + getSelf().path() + " received request " + ReceiveTimeout.class);
                    predictor.tell(new Status.Failure(new CrawlingSourceUnavailableException()), getSelf());
                })
                .build();
    }

//    @Override
//    public void postRestart(){
//
//    }
}
