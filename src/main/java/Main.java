import DomainObjects.InvestmentRisk;
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
        ActorRef portfolioManager = null;
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
                if(portfolioManager == null || portfolioManager.isTerminated()) portfolioManager = system.actorOf(PortfolioManager.props(), "PortfolioManager");
                System.out.println();
                System.out.println("Please enter the amount of money to invest:");
                int money = readUserMoney(input);
                System.out.println();
                System.out.println("How risky will you play?");
                InvestmentRisk risk = readUserRisk(input);
                PortfolioManager.SetupUserPreferences preferences = new PortfolioManager.SetupUserPreferences(risk, money);
                portfolioManager.tell(preferences, ActorRef.noSender());
                portfolioManager.tell(new PortfolioManager.StartPrediction(predictionDates.remove(0)), ActorRef.noSender());

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
        system.terminate();
    }

    private static int readUserMoney(Scanner input){
        while(true){
            String response = input.nextLine();
            try{
                int amount = Integer.parseInt(response);
                if(amount >= 0) {
                    return amount;
                }

                System.out.println("Provided amount must be greater than 0.");
            }catch (Exception e){
                System.out.println("Not valid amount, please try once again.");
            }
        }
    }

    private static InvestmentRisk readUserRisk(Scanner input){
        while (true){
            System.out.println("Enter number if you will play:");
            System.out.println("1 - without risk");
            System.out.println("2 - normally");
            System.out.println("3 - very risky");
            try{
                int risk = Integer.parseInt(input.nextLine());
                if(risk<1 || risk>3) throw new Exception();
                switch (risk){
                    case 1: return InvestmentRisk.LOW;
                    case 2: return InvestmentRisk.MEDIUM;
                    case 3: return InvestmentRisk.HIGH;
                }
            }catch (Exception e){
                System.out.println("Please enter number from range 1-3");
            }
        }
    }
}
