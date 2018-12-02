package actors;

import DomainObjects.Article;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import com.lightbend.akka.sample.Greeter;

import java.util.Date;
import java.util.List;

public class DJPredictor extends AbstractActor implements IPredictor {

    public static class Headers{
        public final List<Article> articles;

        public Headers(List<Article> articles){
            this.articles = articles;
        }
    }

    public static class Predict{
        public final Date predictionDate;

        public Predict(Date predictionDate){
            this.predictionDate = predictionDate;
        }
    }



    static public Props props() {
        return Props.create(DJPredictor.class, () -> null);
    }

    @Override
    public void ReadIndexHistory() {

    }

    @Override
    public void Predict() {

    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
