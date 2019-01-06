package helpers;

import DomainObjects.CrawlerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CrawlerConfigReader {
    public static List<CrawlerConfig> getAsList() throws IOException {
        List<CrawlerConfig> configs = new ArrayList<>();
        List<String> configFilesRelativePaths = new ArrayList<>();
        configFilesRelativePaths.add("CrawlerConfigs/crawler1.properties");
        configFilesRelativePaths.add("CrawlerConfigs/crawler2.properties");
        configFilesRelativePaths.add("CrawlerConfigs/crawler3.properties");
        configFilesRelativePaths.add("CrawlerConfigs/crawler4.properties");
        configFilesRelativePaths.add("CrawlerConfigs/crawler5.properties");

        for (String path : configFilesRelativePaths) {
            configs.add(getConfig(path));
        }

        return configs;
    }
    public static CrawlerConfig getConfig(String configRelativePath) throws IOException {
        InputStream is = ClassLoader.getSystemResourceAsStream(configRelativePath);
        Properties prop = new Properties();
        prop.load(is);

        int readModulo = Integer.parseInt(prop.getProperty("readModulo"));
        return new CrawlerConfig(readModulo);
    }
}
