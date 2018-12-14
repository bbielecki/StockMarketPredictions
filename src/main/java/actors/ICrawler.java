package actors;

import DomainObjects.Article;
import akka.actor.ActorRef;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ICrawler {
    List<Article> Search(LocalDate date);
    void SendResultTo(ActorRef predictor);
}
