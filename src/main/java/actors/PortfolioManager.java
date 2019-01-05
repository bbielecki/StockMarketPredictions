package actors;

import DomainObjects.Prediction;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import helpers.ModelConfig;

import scala.concurrent.duration.Duration;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import java.util.stream.Collectors;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static BlockingQueue<PredictionResult> predictionResults;
    private static List<ActorRef> models;
    private static List<ModelConfig> modelConfigs;

    public static class PredictionResult{
        public final LocalDate predictionDate;
        public final List<Prediction> predictions;
        public final ActorPath predictorPath;

        public PredictionResult(LocalDate predictionDate, List<Prediction> predictions, ActorPath predictorPath){
            this.predictionDate = predictionDate;
            this.predictions = predictions;
            this.predictorPath = predictorPath;
        }

        @Override
        public boolean equals(Object o){
            if(o == this) return true;
            if(!(o instanceof PredictionResult)) return false;

            PredictionResult pr = (PredictionResult) o;
            return predictionDate.equals(pr.predictionDate) && predictorPath.equals(pr.predictorPath);
        }
    }
    public static class StartPrediction{ }
    public static class DJPredictionException{
        public final ModelConfig config;

        public DJPredictionException(ModelConfig config){
            this.config = config;
        }
    }
    public static class DJPredictorModelCommunicationError {
        public final ModelConfig config;

        public DJPredictorModelCommunicationError(ModelConfig config){
            this.config = config;
        }
    }
    public static class DJPredictorCrawlersException{
        public final ModelConfig config;

        public DJPredictorCrawlersException(ModelConfig config){
            this.config = config;
        }
    }


    private void handlePredictorError(ModelConfig config) {
        log.info("Portfolio Manager " + getSelf().path() + " received message " + DJPredictionException.class + ". Killing a child which failed...");
        getSender().tell(Kill.getInstance(), getSelf());
        if (getContext().receiveTimeout() != Duration.Undefined()) {
            log.info("Portfolio Manager restarts killed child with the same config.");
            getContext().getSystem().actorOf(DJPredictor.props(getSelf(), config));
        }
    }

    static public Props props() {
        return Props.create(PortfolioManager.class, PortfolioManager::new);
    }

    public static AbstractMap.SimpleEntry<Integer, Double> getFinalClass(List<Prediction> predictions) {
        Map<Integer, Double> weightedPredictions = predictions.stream().collect(Collectors.toMap(
                Prediction::getPredictionClass, p -> p.getProbability() * p.getWeight(), (oldValue, newValue) -> oldValue + newValue));

        Integer finalClass = Collections.max(weightedPredictions.entrySet(), Map.Entry.comparingByValue()).getKey();

        Double probability = predictions.stream().filter(p -> p.getPredictionClass() == finalClass)
                .mapToDouble(Prediction::getProbability).max().orElseThrow(NoSuchElementException::new);

        return new AbstractMap.SimpleEntry<>(finalClass, probability);
    }

    private PortfolioManager() {
        log.info("Creating Portfolio manager");
        predictionResults = new LinkedBlockingQueue<>();
        modelConfigs = new ArrayList<>();
        models = new ArrayList<>();

        modelConfigs.add(new ModelConfig());
        for (ModelConfig config : modelConfigs) {
            models.add(getContext().getSystem().actorOf(DJPredictor.props(getSelf(), config)) );
        }
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(StartPrediction.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " is starting prediction on date: " + LocalDate.now());
                    Duration timeout = Duration.create(100, TimeUnit.MILLISECONDS);
                    models.forEach(model -> model.tell(new DJPredictor.StartPrediction(LocalDate.now(), timeout), getSelf()));
                })
                .match(PredictionResult.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " received prediction result.");
                    log.info("predictionResults size = " + predictionResults.size());
                    if(!predictionResults.contains(x)){
                        predictionResults.put(x);
                        log.info("Prediction result was added to queue.");
                    }
                })
                .match(ReceiveTimeout.class, x -> {
                    log.info("Reddit Crawler " + getSelf().path() + " received request " + ReceiveTimeout.class);
                    getContext().setReceiveTimeout(Duration.Undefined());
                    //todo: call negotiations method and reset timeout...
                })
                .match(DJPredictionException.class, x -> handlePredictorError(x.config))
                .match(DJPredictorModelCommunicationError.class, x -> handlePredictorError(x.config))
                .match(DJPredictorCrawlersException.class, x -> {
                    log.info("Portfolio Manager " + getSelf().path() + " received message " + DJPredictorCrawlersException.class + ". Killing a child which failed...");
                    getSender().tell(Kill.getInstance(), getSelf());
                })
                .build();
    }
}
