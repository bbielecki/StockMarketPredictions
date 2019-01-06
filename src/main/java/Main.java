import actors.PortfolioManager;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import helpers.PredictionDatesReader;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("StockMarketPredictions");
        ActorRef portfolioManager = system.actorOf(PortfolioManager.props(), "PortfolioManager");
        Scanner input = new Scanner(System.in);
        boolean stop = false;
        List<LocalDate> predictionDates = PredictionDatesReader.getAsList();

        if(predictionDates.size() == 0) {
            System.out.println("Configuration error has occurred. Sorry...");
            return;
        }
        System.out.println("Welcome to the best Stock Market predictor!");
        System.out.println();
        System.out.println("Please, follow the instructions...");
        while (!stop){
            System.out.println("Do you want to predict another Dow Jones Index value?");
            System.out.println("Press enter Y(yes) or N(no)");
            String response = input.nextLine();
            if(response.equals("N")) stop=true;
            else if(response.equals("Y")){
                portfolioManager.tell(new PortfolioManager.StartPrediction(predictionDates.remove(0)), ActorRef.noSender());
                System.out.println();
                System.out.println("Please wait for result...");
                System.out.println();
                System.out.println();
            }
            if(predictionDates.size() == 0){
                System.out.println("All configured prediction dates were used.");
                stop=true;
            }
        }

        System.out.println("Enter q to quit.");
        input.nextLine();
    }
}
