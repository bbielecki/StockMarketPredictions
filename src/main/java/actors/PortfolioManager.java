package actors;

import DomainObjects.Prediction;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef predictor;
    public static class PredictionResult{
        public final Date predictionDate;
        public final List<Prediction> predictions;

        public PredictionResult(Date predictionDate, List<Prediction> predictions){
            this.predictionDate = predictionDate;
            this.predictions = predictions;
        }
    }
    public static class StartPrediction{

    }

    static public Props props() {
        return Props.create(PortfolioManager.class, PortfolioManager::new);
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(StartPrediction.class, x->{
                    predictor = getContext().getSystem().actorOf( DJPredictor.props(getSelf()) );
                    predictor.tell(new DJPredictor.StartPrediction(LocalDate.now()), getSelf());
                })
                .build();
    }
}
