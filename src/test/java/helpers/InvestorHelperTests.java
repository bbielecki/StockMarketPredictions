package helpers;

import DomainObjects.InvestmentActionType;
import DomainObjects.InvestmentRisk;
import Exceptions.InvestorHelperException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

//@RunWith(Parameterized.class)
public class InvestorHelperTests {
//    @Parameterized.Parameters
//    public static Collection<Object[]> predictionProbToAction(){
//        return Arrays.asList(new Object[][] {
//                {0.1, InvestmentActionType.DO_NOTHING},
//                {0.2, InvestmentActionType.DO_NOTHING},
//                {0.3, InvestmentActionType.DO_NOTHING},
//                {0.1, InvestmentActionType.DO_NOTHING},
//                {0.1, InvestmentActionType.DO_NOTHING}
//        });
//    }

    @Test
    public void HighDoNothingDecisionTest() throws InvestorHelperException {
        //prepare
        int money = 100;
        InvestmentRisk risk = InvestmentRisk.HIGH;
        int predictionClass = 4;
        double predictionProbability = 0.5;

        //act
        InvestmentActionType action = InvestorHelper.getInvestmentActionType(money, risk, predictionClass, predictionProbability);

        //assert
        Assert.assertSame(action, InvestmentActionType.DO_NOTHING);
    }

    @Test
    public void MediumDoNothingDecisionTest() throws InvestorHelperException {
        //prepare
        int money = 100;
        InvestmentRisk risk = InvestmentRisk.MEDIUM;
        int predictionClass = 4;
        double predictionProbability = 0.8;

        //act
        InvestmentActionType action = InvestorHelper.getInvestmentActionType(money, risk, predictionClass, predictionProbability);

        //assert
        Assert.assertSame(action, InvestmentActionType.DO_NOTHING);
    }

    @Test
    public void LowDoNothingDecisionTest() throws InvestorHelperException {
        //prepare
        int money = 100;
        InvestmentRisk risk = InvestmentRisk.LOW;
        int predictionClass = 4;
        double predictionProbability = 1;

        //act
        InvestmentActionType action = InvestorHelper.getInvestmentActionType(money, risk, predictionClass, predictionProbability);

        //assert
        Assert.assertSame(action, InvestmentActionType.DO_NOTHING);
    }

    @Test
    public void HighSellDecisionTest() throws InvestorHelperException{
        //prepare
        int money = 100;
        InvestmentRisk risk = InvestmentRisk.HIGH;
        int predictionClass = 1;
        double predictionProbability = 0.6;

        //act
        InvestmentActionType action = InvestorHelper.getInvestmentActionType(money, risk, predictionClass, predictionProbability);

        //assert
        Assert.assertSame(action, InvestmentActionType.SELL);
    }
}
