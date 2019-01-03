package actors;

import DomainObjects.Prediction;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
<<<<<<< HEAD
import helpers.ModelConfig;
=======
import helpers.CrawlerConfig;
import scala.concurrent.duration.Duration;
>>>>>>> dd5e95adbf78765c1ed29934c10353778bb400bd

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import scala.concurrent.duration.Duration;

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
