package helpers;

import DomainObjects.InvestmentActionType;
import DomainObjects.InvestmentRisk;
import Exceptions.InvestorHelperException;

import java.security.InvalidParameterException;

public class InvestorHelper {
    private static final double minUserScore = 1 / (3 * 6 * 3 * 3);
    private static final double maxUserScore = 1;

    public static InvestmentActionType getInvestmentActionType(int moneyToInvest, InvestmentRisk investmentRisk, int predictionClass, double predictionProbability) throws InvestorHelperException {

        if(moneyToInvest < 0 || predictionClass < 0 || predictionClass > 4 || predictionProbability < 0 || predictionProbability > 1) throw new InvalidParameterException();

        //if there is no change in index value then DO_NOTHING should be returned
        if(predictionClass == 2){
            return InvestmentActionType.DO_NOTHING;
        }

        //the more money investor want to invest, the more risky he is and
        // the higher prediction class is - the lower probability is accepted as
        // enough to invest(for positive classes), but in opposite the higher probability of negative class
        // should cause decision to sell his shares.

        //first step - calculate total user score.
        double userScore = calculateMoneyInfluence(moneyToInvest) * investmentRisk.getValue()  * calculateClassProbabilityInfluence(predictionProbability) * calculateClassInfluence(predictionClass);
        userScore = 1 / userScore;
        //normalize to (min, max)
        double normalizedUserScore = (userScore - minUserScore)/(maxUserScore - minUserScore);

        if(predictionClass > 2 && normalizedUserScore <= 0.025) return InvestmentActionType.INVEST;
        else if(predictionClass > 2) return InvestmentActionType.DO_NOTHING;
        if(normalizedUserScore <= 0.025) return InvestmentActionType.SELL;
        else return InvestmentActionType.DO_NOTHING;
        }

    private static int calculateMoneyInfluence(int money){
        if(money <= 100)  return 1;
        if(money <= 1000) return 2;
        else             return 3;
    }

    private static int calculateClassProbabilityInfluence(double probability){
        if(probability <= 0.25) return 1;
        if(probability <= 0.5)  return 2;
        if(probability <= 0.7)  return 4;
        if(probability <= 0.85) return 6;
        if(probability <= 1)    return 8;
        else return 1;
    }

    private static int calculateClassInfluence(int predictionClass){
        if(predictionClass == 2)                         return 1;
        if(predictionClass == 0 || predictionClass == 3) return 2;
        if(predictionClass == 1 || predictionClass == 4) return 3;
        else return 0;
    }
}
