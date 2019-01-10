package com.lightbend.akka.sample;

import DomainObjects.Article;
import DomainObjects.ModelConfig;
import actors.DJPredictor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DJPredictorTests {
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
    public void StartAddHeaders(){
        //prepare
        final TestKit testProbe = new TestKit(system);
        ActorRef dj = system.actorOf(DJPredictor.props(testProbe.getRef(), new ModelConfig()));

        List<Article> articles = new ArrayList<>();
        articles.add(new Article("test", LocalDate.now()));

        //act
        dj.tell(new DJPredictor.Headers(articles), testProbe.getRef());


    }

}
