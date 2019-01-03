package com.lightbend.akka.sample;

import actors.DJPredictor;
import actors.PortfolioManager;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import helpers.ModelConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
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
}
