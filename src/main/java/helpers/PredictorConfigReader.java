package helpers;

import DomainObjects.ModelConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PredictorConfigReader {
    public static List<ModelConfig> getAsList(){
        List<ModelConfig> configs = new ArrayList<>();
        List<String> configFilesRelativePaths = new ArrayList<>();
        configFilesRelativePaths.add("PredictorConfigs/predictor1.properties");
        configFilesRelativePaths.add("PredictorConfigs/predictor2.properties");
        configFilesRelativePaths.add("PredictorConfigs/predictor3.properties");

        for (String path : configFilesRelativePaths){
            configs.add(getConfig(path));
        }

        return configs;
    }

    private static ModelConfig getConfig(String configRelativePath){
        try {
            InputStream is = ClassLoader.getSystemResourceAsStream(configRelativePath);
            Properties prop = new Properties();
            prop.load(is);

            int maxCrawlers = Integer.parseInt(prop.getProperty("maxCrawlers"));
            int historyWindow = Integer.parseInt(prop.getProperty("historyWindow"));
            String modelServiceAddress = prop.getProperty("modelServiceAddress");
            int modelServicePort = Integer.parseInt(prop.getProperty("modelServicePort"));
            String indexHistoryPath = prop.getProperty("indexHistoryPath");

            ModelConfig mc = new ModelConfig();
            mc.setMaxCrawlers(maxCrawlers);
            mc.setModelServiceAddress(modelServiceAddress);
            mc.setModelServicePort(modelServicePort);
            mc.setIndexHistoryPath(indexHistoryPath);
            mc.setHistoryWindow(historyWindow);

            return mc;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ModelConfig();
    }
}
