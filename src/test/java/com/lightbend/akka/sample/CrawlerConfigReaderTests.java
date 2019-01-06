package com.lightbend.akka.sample;

import DomainObjects.CrawlerConfig;
import helpers.CrawlerConfigReader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class CrawlerConfigReaderTests {
    @Test
    public void readConfigFileTest() throws IOException {
        //prepare
        CrawlerConfig config;
        String relativePath = "CrawlerConfigs/crawler1.properties";

        //act
        config = CrawlerConfigReader.getConfig(relativePath);

        //assert
        Assert.assertNotNull(config);
        Assert.assertTrue(config.getReadModulo() > -1);
    }

    @Test
    public void readConfigFilesTest() throws IOException {
        //prepare
        List<CrawlerConfig> configs;

        //act
        configs = CrawlerConfigReader.getAsList();

        //assert
        Assert.assertEquals(configs.size(), 5);
    }
}
