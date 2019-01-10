package helpers;

import DomainObjects.InvestmentActionType;
import DomainObjects.InvestmentRisk;
import Exceptions.InvestorHelperException;

import java.security.InvalidParameterException;

public class InvestorHelper {

    private static final int probabilityToInt = 10;
    private static final int maxMoneyInfluence = 3;

    private static final double normalizationParam = (maxMoneyInfluence * InvestmentRisk.HIGH.getValue() * probabilityToInt);

    public static InvestmentActionType getInvestmentActionType(int moneyToInvest, InvestmentRisk investmentRisk, int predictionClass, double predictionProbability) throws InvestorHelperException {

        if(moneyToInvest < 0 || predictionClass < 0 || predictionClass > 4 || predictionProbability < 0 || predictionProbability > 1) throw new InvalidParameterException();

        //from -2 up to 2 for big increase
        double normalizedPredictionClass = predictionClass - 1;

        double highRiskInvestLevel = 1/normalizedPredictionClass - 0.1;
        double mediumRiskInvestLevel = highRiskInvestLevel - 0.05;
        double lowRiskInvestLevel = mediumRiskInvestLevel - 0.05;

        double highRiskSellLevel = highRiskInvestLevel - 1;
        double mediumRiskSellLevel = mediumRiskInvestLevel - 1;
        double lowRiskSellLevel = lowRiskInvestLevel - 1;

        //the more money investor want to invest, the more risky he is and
        // the higher prediction class is - the lower probability is accepted as
        // enough to invest(for positive classes), but in opposite the higher probability of negative class
        // should cause decision to sell his shares.

        //first step - calculate total user score.
        double userScore = calculateMoneyInfluence(moneyToInvest) * investmentRisk.getValue()  * (predictionProbability * probabilityToInt);
        //normalize this value to the range of (-1; 1)
        double normalizedScore = userScore / normalizationParam;
        //make a decision
        if(investmentRisk == InvestmentRisk.LOW){
            if(normalizedScore > 0)
                return normalizedScore >= lowRiskInvestLevel ? InvestmentActionType.INVEST : InvestmentActionType.DO_NOTHING;
            else
                return normalizedScore <= lowRiskSellLevel ? InvestmentActionType.SELL : InvestmentActionType.DO_NOTHING;

        }else if(investmentRisk == InvestmentRisk.MEDIUM){
            if(normalizedScore > 0)
                return normalizedScore >= mediumRiskInvestLevel ? InvestmentActionType.INVEST : InvestmentActionType.DO_NOTHING;
            else
                return normalizedScore <= mediumRiskSellLevel ? InvestmentActionType.SELL : InvestmentActionType.DO_NOTHING;

        }else if(investmentRisk == InvestmentRisk.HIGH){
            if(normalizedScore > 0)
                return normalizedScore >= highRiskInvestLevel ? InvestmentActionType.INVEST : InvestmentActionType.DO_NOTHING;
            else
                return normalizedScore <= highRiskSellLevel ? InvestmentActionType.SELL : InvestmentActionType.DO_NOTHING;

        }

        throw new InvestorHelperException("Wrong decision. Check algorithm.");
    }

    private static double calculateMoneyInfluence(int money){
        if(money < 100)
            return maxMoneyInfluence * 0.33;
        if(money < 1000)
            return maxMoneyInfluence * 0.66;
        else return maxMoneyInfluence;
    }
}
