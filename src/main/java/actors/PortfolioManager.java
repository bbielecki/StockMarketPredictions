package actors;

import DomainObjects.InvestmentRisk;
import DomainObjects.Prediction;
import Exceptions.InvestorHelperException;
import akka.ConfigurationException;
import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import DomainObjects.ModelConfig;

import akka.japi.pf.DeciderBuilder;
import helpers.InvestorHelper;
import helpers.PredictionClassTranslator;
import helpers.PredictorConfigReader;
import scala.concurrent.duration.Duration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import java.util.stream.Collectors;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static HashMap<LocalDate, BlockingQueue<PredictionResult>> predictionResults;
    private static List<ActorRef> models;
    private static List<ModelConfig> modelConfigs;
    private InvestmentRisk userRisk;
    private int moneyToInvest;
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(10, java.time.Duration.ofMinutes(1),
                    DeciderBuilder
                            .match(NullPointerException.class, e -> SupervisorStrategy.restart())
                            .match(IOException.class, e -> SupervisorStrategy.restart())
                            .match(FileNotFoundException.class, e-> SupervisorStrategy.stop() )
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());

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
    public static class ConfirmationMessage{
    }


    private void handlePredictorError(ModelConfig config, ActorRef errorSender) {
        log.info("Portfolio Manager " + getSelf().path() + " received message " + DJPredictionException.class + ". Killing a child which failed...");
        errorSender.tell(Kill.getInstance(), getSelf());
        if (getContext().receiveTimeout() != Duration.Undefined()) {
            Timer resurectionTimer = new Timer();
            resurectionTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(errorSender.isTerminated()){
                        log.info("Portfolio Manager restarts killed child with the same config.");
                        getContext().getSystem().actorOf(DJPredictor.props(getSelf(), config), DJPredictor.class.getSimpleName() + config.getActorId());
                        resurectionTimer.cancel();
                    }
                }
            }, 50L);
        }
    }

    private void handlePredictionResult(Map<LocalDate, BlockingQueue<PredictionResult>> predictionResults, LocalDate predictionDate){
        if(predictionResults.size() == 0){
            log.info("No Prediction Results!! Portfolio Manager finished its job.");
            return;
        }
        try{

            BlockingQueue<PredictionResult> results;
            List<PredictionResult> resultsAsList = new ArrayList<>();
            List<Prediction> aggregatedPredictions = new ArrayList<>();

            if(predictionDate == null) results = predictionResults.entrySet().iterator().next().getValue();
            else results = predictionResults.get(predictionDate);

            //take all predictions from blocked queue.
            PredictionResult temp;
            while ((temp = results.poll()) != null) resultsAsList.add(temp);
            //aggregated predictions from all results on provided prediction date.
            resultsAsList.forEach(x -> aggregatedPredictions.addAll(x.predictions));

            try{

                presentResultOnConsole( getFinalClass(aggregatedPredictions) );
                //clear prediction result queue
                predictionResults.remove(predictionDate);

            }catch (Exception e){
                e.printStackTrace();
                log.error("In PortfolioManager an error has occurred in voting algorithm. Cannot present valid prediction result, please check errors stack.");
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("In PortfolioManager an error has occurred during taking all predictions from received results.");
        }
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
        predictionResults = new HashMap<>();
        modelConfigs = new ArrayList<>();
        models = new ArrayList<>();

        modelConfigs = PredictorConfigReader.getAsList();
        if(modelConfigs == null || modelConfigs.size() == 0) throw new ConfigurationException("Portfolio Manager cannot read DJ Predictors configuration.");
    }

    private void createChildren() {
        //do not create children if already exists.
        if(models.size() > 0) return;
        int i = 1;
        for (ModelConfig config : modelConfigs) {
            config.setActorId(i++);
            models.add(getContext().actorOf(DJPredictor.props(getSelf(), config), DJPredictor.class.getSimpleName() + config.getActorId()) );
        }
    }

    private boolean checkIfReceivedAllResults(LocalDate predictionDate){
        return predictionResults.get(predictionDate).size() == modelConfigs.size();
    }


    @Override
    public SupervisorStrategy supervisorStrategy() {
        return strategy;
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
                    log.info("Portfolio Manager " + getSelf().path() + " received message " + StartPrediction.class);
                    if(moneyToInvest == 0) {
                        log.error("Money to invest is not provided. Cannot start prediction.");
                        return;
                    }
                    if(userRisk == null) {
                        log.error("Money to invest is not provided. Cannot start prediction.");
                        return;
                    }
                    log.info("Portfolio Manager " + getSelf().path() + " is starting prediction on date: " + x.predictionDate);
                    // TODO Eliminate race - DONE. timeout is set only on first request. Then the timeout handle all request one by one
                    // and reset timeout when all requests are handled. Disadvantage of this solution is that the next request will be handled
                    // later the timeout interval if any request is scheduled.
                    if(!predictionResults.containsKey(x.predictionDate)){
                        getContext().setReceiveTimeout(Duration.create(15, TimeUnit.SECONDS));
                        log.info("Portfolio Manager " + getSelf().path() + " is creating all related predictors.");
                        createChildren();
                    }

                    Duration timeout = Duration.create(10, TimeUnit.SECONDS);
                    models.forEach(model -> model.tell(new DJPredictor.StartPrediction(x.predictionDate, timeout), getSelf()));
                })
                .match(PredictionResult.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " received prediction result.");
                    log.info("predictionResults size = " + predictionResults.size());
                    if(!predictionResults.containsKey(x.predictionDate)){
                        BlockingQueue<PredictionResult> results = new LinkedBlockingQueue<>();
                        results.add(x);
                        predictionResults.put(x.predictionDate, results);
                    }else {
                        predictionResults.get(x.predictionDate).add(x);
                    }
                    log.info("Prediction result was added to queue.");

                    //if PortfolioManager received all 3 prediction results from all DJPredictors then it can start voting and decision process.
                    if(checkIfReceivedAllResults(x.predictionDate)) handlePredictionResult(predictionResults, x.predictionDate);
                    if(predictionResults.size() == 0) {
                        log.info("PredictionManager removes timeout.");
                        getContext().setReceiveTimeout(Duration.Undefined());
                    }
                })
                .match(ReceiveTimeout.class, x -> {
                    log.info("Prtfolio Manager " + getSelf().path() + " received request " + ReceiveTimeout.class);
                    getContext().setReceiveTimeout(Duration.Undefined());
                    handlePredictionResult(predictionResults, null);

                    log.info("Portfolio Manager is going destroy itself.");
                    getContext().stop(getSelf());
                })
                .match(DJPredictionException.class, x -> handlePredictorError(x.config, getSender()))
                .match(DJPredictorModelCommunicationError.class, x -> handlePredictorError(x.config, getSender()))
                .match(DJPredictorCrawlersException.class, x -> {
                    log.info("Portfolio Manager " + getSelf().path() + " received message " + DJPredictorCrawlersException.class + ". Killing a child which failed...");
                    getSender().tell(Kill.getInstance(), getSelf());
                })
                .match(SetupUserPreferences.class, x -> {
                    log.info("Portfolio Manager " + getSelf().path() + " received message " + SetupUserPreferences.class + ". You can start predictions.");
                    userRisk = x.userRisk;
                    moneyToInvest = x.moneyToInvest;
                    if(getSender() != null && getSender() != ActorRef.noSender()) getSender().tell(new ConfirmationMessage(), getSelf());
                })
                .build();
    }
}
