package actors;

import DomainObjects.InvestmentRisk;
import DomainObjects.Prediction;
import Exceptions.InvestorHelperException;
import akka.ConfigurationException;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import DomainObjects.ModelConfig;

import helpers.InvestorHelper;
import helpers.PredictionClassTranslator;
import helpers.PredictorConfigReader;
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
    private InvestmentRisk userRisk;
    private int moneyToInvest;

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
    public static class StartPrediction{
        public final LocalDate predictionDate;
        public StartPrediction(LocalDate predictionDate){
            this.predictionDate = predictionDate;
        }
    }
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
    public static class SetupUserPreferences{
        public SetupUserPreferences(InvestmentRisk userRisk, int moneyToInvest) {
            this.userRisk = userRisk;
            this.moneyToInvest = moneyToInvest;
        }

        public final InvestmentRisk userRisk;
        public final int moneyToInvest;

    }


    private void handlePredictorError(ModelConfig config) {
        log.info("Portfolio Manager " + getSelf().path() + " received message " + DJPredictionException.class + ". Killing a child which failed...");
        getSender().tell(Kill.getInstance(), getSelf());
        if (getContext().receiveTimeout() != Duration.Undefined()) {
            log.info("Portfolio Manager restarts killed child with the same config.");
            getContext().getSystem().actorOf(DJPredictor.props(getSelf(), config));
        }
    }

    private void handlePredictionResult(BlockingQueue<PredictionResult> predictionResults){
        if(predictionResults.size() == 0){
            log.info("No Prediction Results. Portfolio Manager finished its job.");
            return;
        }
        List<PredictionResult> results = new ArrayList<>();
        List<Prediction> predictions = new ArrayList<>();

        //take all predictions from blocked queue.
        PredictionResult temp;
        while ((temp = predictionResults.poll()) != null) results.add(temp);
        //aggregate predictions from all Predictors
        results.forEach(x -> predictions.addAll(x.predictions));

        presentResultOnConsole( getFinalClass(predictions) );

    }

    private void presentResultOnConsole(Map.Entry<Integer, Double> theBestResult){
        System.out.println();
        System.out.println();
        System.out.println("The best model predicted that Dow Jones Index Value will have " + PredictionClassTranslator.ToDescription(theBestResult.getKey()));
        System.out.println("This prediction was made with " + theBestResult.getValue() + "% confidence.");
        try {
            System.out.println("Prediction system recommendation is: " + InvestorHelper.getInvestmentActionType(moneyToInvest, userRisk, theBestResult.getKey(), theBestResult.getValue()));
        } catch (InvestorHelperException e) {
            System.out.println("System cannot recommend the best action. Probably user did not provide amount of money to invest and preferred risk.");
            e.printStackTrace();
        }
        System.out.println();
        System.out.println();
    }

    private PortfolioManager() {
        log.info("Creating Portfolio manager");
        predictionResults = new LinkedBlockingQueue<>();
        modelConfigs = new ArrayList<>();
        models = new ArrayList<>();

        modelConfigs = PredictorConfigReader.getAsList();
        if(modelConfigs == null || modelConfigs.size() == 0) throw new ConfigurationException("Portfolio Manager cannot read DJ Predictors configuration.");
    }

    private void createChildren() {
        int i = 1;
        for (ModelConfig config : modelConfigs) {
            models.add(getContext().actorOf(DJPredictor.props(getSelf(), config), DJPredictor.class.getSimpleName() + i++) );
        }
    }

    public static AbstractMap.SimpleEntry<Integer, Double> getFinalClass(List<Prediction> predictions) {
        Map<Integer, Double> weightedPredictions = predictions.stream().collect(Collectors.toMap(
                Prediction::getPredictionClass, p -> p.getProbability() * p.getWeight() * p.getPredictorWeight(), (oldValue, newValue) -> oldValue + newValue));

        Integer finalClass = Collections.max(weightedPredictions.entrySet(), Map.Entry.comparingByValue()).getKey();

        Double probability = predictions.stream().filter(p -> p.getPredictionClass() == finalClass)
                .mapToDouble(Prediction::getProbability).max().orElseThrow(NoSuchElementException::new);

        return new AbstractMap.SimpleEntry<>(finalClass, probability);
    }

    static public Props props() {
        return Props.create(PortfolioManager.class, PortfolioManager::new);
    }

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(StartPrediction.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " is starting prediction on date: " + LocalDate.now());
                    getContext().setReceiveTimeout(Duration.create(6, TimeUnit.SECONDS));
                    Duration timeout = Duration.create(5, TimeUnit.SECONDS);

                    log.info("Portfolio Manager " + getSelf().path() + " is creating all related predictors.");
                    createChildren();
                    models.forEach(model -> model.tell(new DJPredictor.StartPrediction(x.predictionDate, timeout), getSelf()));
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
                    log.info("Prtfolio Manager " + getSelf().path() + " received request " + ReceiveTimeout.class);
                    getContext().setReceiveTimeout(Duration.Undefined());
                    handlePredictionResult(predictionResults);

                    log.info("Portfolio Manager is going destroy itself.");
                    getContext().stop(getSelf());
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
