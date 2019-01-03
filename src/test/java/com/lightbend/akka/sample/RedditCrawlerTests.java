package com.lightbend.akka.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import actors.DJPredictor;
import actors.PortfolioManager;
import actors.RedditCrawler;
import helpers.CrawlerConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;

import java.time.Duration;
import java.time.LocalDate;

public class RedditCrawlerTests {
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
    public void testConfirmationMsg() {
        final TestKit testProbe = new TestKit(system);
        LocalDate now = LocalDate.now();

        final ActorRef redditCrawler = system.actorOf(RedditCrawler.props(testProbe.getRef(), new CrawlerConfig()));
        redditCrawler.tell(new RedditCrawler.StartCrawling(now), ActorRef.noSender());

        DJPredictor.CrawlerConfirmation message = testProbe.expectMsgClass(Duration.ofSeconds(5), DJPredictor.CrawlerConfirmation.class);
        assertNotNull(message);
    }

    @Test
    public void testCrawlingSourceUnavailableException() {
        final TestKit testProbe = new TestKit(system);
        LocalDate now = LocalDate.now();

        final ActorRef redditCrawler = system.actorOf(RedditCrawler.props(testProbe.getRef(), new CrawlerConfig()));
        redditCrawler.tell(new RedditCrawler.StartCrawling(now), ActorRef.noSender());

        DJPredictor.CrawlerConfirmation confirmation = testProbe.expectMsgClass(Duration.ofSeconds(5), DJPredictor.CrawlerConfirmation.class);
        DJPredictor.CrawlingSourceUnavailable errorMsg = testProbe.expectMsgClass(Duration.ofSeconds(5), DJPredictor.CrawlingSourceUnavailable.class);
        assertNotNull(errorMsg);
    }
}
