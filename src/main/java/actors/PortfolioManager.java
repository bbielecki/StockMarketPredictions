package actors;

import DomainObjects.Prediction;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import helpers.ModelConfig;
import helpers.CrawlerConfig;

import scala.concurrent.duration.Duration;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static List<ActorRef> models;
    private static List<ModelConfig> modelConfigs;
    private int activePredictorsCounter;

    public static class PredictionResult{
        public final LocalDate predictionDate;
        public final List<Prediction> predictions;
        public final String testMessage = "done";

        public PredictionResult(LocalDate predictionDate, List<Prediction> predictions){
            this.predictionDate = predictionDate;
            this.predictions = predictions;
        }
    }
    public static class StartPrediction{

    }

    private List<CrawlerConfig> readCrawlerConfigs(){
        return new ArrayList<>();
    }

    static public Props props() {
        return Props.create(PortfolioManager.class, PortfolioManager::new);
    }

    public static int getFinalClass(List<Prediction> predictions) {
        Map<Integer, Double> weightedPredictions = predictions.stream().collect(Collectors.toMap(
                Prediction::getPredictionClass, p -> p.getProbability() * p.getWeight(), (oldValue, newValue) -> oldValue + newValue));

        return Collections.max(weightedPredictions.entrySet(), Map.Entry.comparingByValue()).getKey();
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
                .match(PredictionResult.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " received prediction result.");

                })
                .build();
    }
}
