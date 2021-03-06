package actors;

import DomainObjects.Article;
import DomainObjects.CrawlerConfig;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import helpers.ArticleFileParser;
import scala.concurrent.duration.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
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
        final LocalDate articlesDate;

        public StartCrawling(LocalDate articlesDate){
            this.articlesDate = articlesDate;
        }
    }

    private RedditCrawler(ActorRef predictor, CrawlerConfig config){
        log.info("Creating Reddit Crawler");
        this.predictor = predictor;
        this.config = config;
    }

    //should get the configuration which determine the range of the articles (first 5, random 5 etc...)
    private void crawlDataSource(LocalDate articlesDate){
        try{
            List<Article> newArticles = new ArrayList<>();
            for(int i = 0; i < config.getWindowSize(); i++)
                newArticles.addAll(ArticleFileParser.readArticles(articlesDate.minusDays(i), config.getReadModulo(), config.getPathToFile()));

            //automatically send crawled articles to subscribed index predictor
            if (!newArticles.isEmpty())
                predictor.tell(new DJPredictor.Headers(newArticles, articlesDate), getSelf());
            else
                predictor.tell(new DJPredictor.NoNewHeaders(), getSelf());

            getContext().setReceiveTimeout(Duration.Undefined());
        }
        catch (Exception e){
            getContext().setReceiveTimeout(Duration.Undefined());
            predictor.tell(new Status.Failure(e), getSelf());
        }
    }

    static public Props props( ActorRef predictor, CrawlerConfig config ) {
        return Props.create(RedditCrawler.class, () -> new RedditCrawler(predictor, config));
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(StartCrawling.class, x ->{
                    log.info("Reddit Crawler " + getSelf().path() + " received request " + StartCrawling.class);
                    this.predictor.tell(new DJPredictor.CrawlerConfirmation(), getSelf());
                    getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
                    crawlDataSource(x.articlesDate);
                })
                .match(ReceiveTimeout.class, x -> {
                    log.info("Reddit Crawler " + getSelf().path() + " received request " + ReceiveTimeout.class);
                    predictor.tell(new DJPredictor.CrawlingSourceUnavailable(this.config), getSelf());
                    getContext().setReceiveTimeout(Duration.Undefined());
                })
                .build();
    }

    @Override
    public void postStop() throws Exception {
        log.info("Reddit Crawler " + getSelf().path() + " was stopped.");
        super.postStop();
    }
}
