package com.lightbend.akka.sample;

import helpers.PredictionDatesReader;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

public class PredictionDatesReaderTests {
    @Test
    public void readPredictionDatesFromConfigFileTest(){
        //prepare
        PredictionDatesReader predictionDatesReader = new PredictionDatesReader();

        //act
        List<LocalDate> predictionDates = predictionDatesReader.getAsList();

        //assert
        Assert.assertNotNull(predictionDates);
        Assert.assertTrue(predictionDates.size() > 0);
    }
}
