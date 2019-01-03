import actors.DJPredictor;
import actors.PortfolioManager;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("StockMarketPredictions");
        final ActorRef printerActor = system.actorOf(PortfolioManager.props(), "printerActor");
        printerActor.tell(new PortfolioManager.StartPrediction(), ActorRef.noSender());

    }
}
