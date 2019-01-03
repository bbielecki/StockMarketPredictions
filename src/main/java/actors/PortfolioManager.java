package actors;

import DomainObjects.Prediction;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import helpers.ModelConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import scala.concurrent.duration.Duration;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static List<ActorRef> models;
    private static List<ModelConfig> modelConfigs;
    private int activePredictorsCounter;

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

    public PortfolioManager() {
        log.info("Creating Portfolio manager");
        models = new ArrayList<>();
        modelConfigs = new ArrayList<>();
        activePredictorsCounter = 0;

        modelConfigs.add(new ModelConfig());
        for (ModelConfig config : modelConfigs) {
            models.add(getContext().getSystem().actorOf(DJPredictor.props(getSelf(), config)) );
            activePredictorsCounter++;
        }
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(StartPrediction.class, x->{
                    Duration timeout = new Duration(100, "millis");
                    models.forEach(model -> model.tell(new DJPredictor.StartPrediction(LocalDate.now(), timeout), getSelf()));
                })
                .match(PredictionResult.class, x -> {

                })
                .build();
    }
}
