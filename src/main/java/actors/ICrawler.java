package actors;

import DomainObjects.Article;
import akka.actor.ActorRef;

import java.util.Date;
import java.util.List;

public interface ICrawler {
    List<Article> Search(Date date);
    void SendResultTo(ActorRef predictor);
}
