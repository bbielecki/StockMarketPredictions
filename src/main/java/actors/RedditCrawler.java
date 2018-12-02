package actors;

import DomainObjects.Article;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import helpers.ArticleFileParser;

import java.util.Date;
import java.util.List;

public class RedditCrawler extends AbstractActor implements ICrawler {

    static public Props props(String message, ActorRef printerActor) {
        return Props.create(RedditCrawler.class, () -> null);
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    @Override
    public List<Article> Search(Date date) {
        return ArticleFileParser.ReadArticles(date);
    }

    @Override
    public void SendResultTo(ActorRef predictor) {
        List<Article> readArticles = Search(new Date());

        predictor.tell(new DJPredictor.Headers(readArticles), getSelf());
    }

}
