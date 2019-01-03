package actors;

import DomainObjects.Prediction;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Date;
import java.util.List;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static class PredictionResult{
        public final Date predictionDate;
        public final List<Prediction> predictions;

        public PredictionResult(Date predictionDate, List<Prediction> predictions){
            this.predictionDate = predictionDate;
            this.predictions = predictions;
        }
    }

    static public Props props() {
        return Props.create(PortfolioManager.class, PortfolioManager::new);
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(DJPredictor.StartPrediction.class, x->{
                    getContext().getSystem().actorOf( DJPredictor.props(getSelf()) );
                })
                .build();
    }
}
