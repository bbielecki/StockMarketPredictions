package com.lightbend.akka.sample;

import DomainObjects.Prediction;
import actors.DJPredictor;
import actors.PortfolioManager;
import akka.actor.*;
import akka.testkit.javadsl.TestKit;
import DomainObjects.ModelConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PortfolioManagerTests {
    static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void PredictionResultEqualsMethodTest() {
        final TestKit testProbe = new TestKit(system);
        LocalDate now = LocalDate.now();
        LocalDate notNow = LocalDate.MIN;
        ActorRef actor_1 = system.actorOf(DJPredictor.props(testProbe.getRef(), new ModelConfig()));
        ActorRef actor_2 = system.actorOf(DJPredictor.props(testProbe.getRef(), new ModelConfig()));

        List<PortfolioManager.PredictionResult> predictionResults = new ArrayList<>();
        PortfolioManager.PredictionResult pr_1 = new PortfolioManager.PredictionResult(now, new ArrayList<>(),actor_1.path());
        PortfolioManager.PredictionResult pr_2 = new PortfolioManager.PredictionResult(now, new ArrayList<>(),actor_1.path());

        PortfolioManager.PredictionResult pr_3 = new PortfolioManager.PredictionResult(now, new ArrayList<>(),actor_2.path());
        PortfolioManager.PredictionResult pr_4 = new PortfolioManager.PredictionResult(notNow, new ArrayList<>(),actor_2.path());
        PortfolioManager.PredictionResult pr_5 = new PortfolioManager.PredictionResult(notNow, new ArrayList<>(),actor_1.path());

        predictionResults.add(pr_1);
        Assert.assertTrue(predictionResults.contains(pr_1));
        Assert.assertTrue(predictionResults.contains(pr_2));

        Assert.assertTrue(!predictionResults.contains(pr_3));
        Assert.assertTrue(!predictionResults.contains(pr_4));
        Assert.assertTrue(!predictionResults.contains(pr_5));
    }

    @Test
    public void testPredictingFinalClass() {
        Prediction p1_0 = new Prediction(0, 0.5, 1, 1);
        Prediction p1_1 = new Prediction(1, 0.5, 2, 1);
        Prediction p2_0 = new Prediction(0, 0.4, 1, 1);
        Prediction p2_1 = new Prediction(1, 0.6, 2, 1);

        List<Prediction> predictions = new ArrayList<>(Arrays.asList(p1_0, p1_1, p2_0, p2_1));

        AbstractMap.SimpleEntry<Integer, Double> result = PortfolioManager.getFinalClass(predictions);

        Assert.assertEquals(new AbstractMap.SimpleEntry<>(1, 0.6), result);
    }
}
