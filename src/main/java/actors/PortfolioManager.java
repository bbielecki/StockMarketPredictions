package actors;

import DomainObjects.Prediction;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import helpers.CrawlerConfig;
import scala.concurrent.duration.Duration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PortfolioManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private ActorRef predictor;
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

    @Override
    public Receive createReceive() {

        return receiveBuilder()
                .match(StartPrediction.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " is starting prediction on date: " + LocalDate.now());
                    predictor = getContext().getSystem().actorOf( DJPredictor.props(getSelf(), readCrawlerConfigs()) );
                    predictor.tell(new DJPredictor.StartPrediction(LocalDate.now(), Duration.create(20, TimeUnit.SECONDS)), getSelf());
                })
                .match(PredictionResult.class, x->{
                    log.info("Portfolio Manager " + getSelf().path() + " received prediction result.");

                })
                .build();
    }
}
