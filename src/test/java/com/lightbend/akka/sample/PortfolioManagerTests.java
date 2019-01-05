package com.lightbend.akka.sample;

import actors.DJPredictor;
import actors.PortfolioManager;
import akka.actor.*;
import akka.testkit.javadsl.TestKit;
import helpers.ModelConfig;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
    public void HandlePredticionResultTest(){
        final TestKit testProbe = new TestKit(system);
        ActorRef actor = system.actorOf(DJPredictor.props(testProbe.getRef(), new ModelConfig()));
        BlockingQueue<PortfolioManager.PredictionResult> queue = new LinkedBlockingQueue<>();
        List<PortfolioManager.PredictionResult> list = new ArrayList<>();
        try {
            queue.put(new PortfolioManager.PredictionResult(LocalDate.now(), new ArrayList<>(), actor.path()));
            queue.put(new PortfolioManager.PredictionResult(LocalDate.now(), new ArrayList<>(), actor.path()));
            queue.put(new PortfolioManager.PredictionResult(LocalDate.now(), new ArrayList<>(), actor.path()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        PortfolioManager.PredictionResult pr;
        while ((pr = queue.poll()) != null){
            list.add(pr);
        }
        Assert.assertNotNull(list);
        Assert.assertTrue(list.size() == 3);
    }
}
