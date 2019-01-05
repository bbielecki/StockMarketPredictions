import DomainObjects.Article;
import DomainObjects.Prediction;
import actors.DJPredictor;
import actors.PortfolioManager;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("StockMarketPredictions");
        final ActorRef printerActor = system.actorOf(PortfolioManager.props(), "printerActor");
        printerActor.tell(new PortfolioManager.StartPrediction(), ActorRef.noSender());
    }
}
