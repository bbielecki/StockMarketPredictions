package actors;

import akka.actor.AbstractActor;

import java.util.Date;

public class PortfolioManager extends AbstractActor {

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
