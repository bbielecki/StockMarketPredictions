package actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.util.Date;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static class PredictionResult{
        public final Date predictionDate;

        public PredictionResult(Date predictionDate){
            this.predictionDate = predictionDate;
        }
    }

    @Override
    public Receive createReceive() {
        return null;
    }
}
