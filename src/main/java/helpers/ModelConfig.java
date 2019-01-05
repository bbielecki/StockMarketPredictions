package helpers;

public class ModelConfig {

    private int maxCrawlers = 5;
    private String modelPath;
    private String indexHistoryPath;

    public int getMaxCrawlers() {
        return maxCrawlers;
    }

    public void setMaxCrawlers(int maxCrawlers) {
        this.maxCrawlers = maxCrawlers;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getIndexHistoryPath() {
        return indexHistoryPath;
    }

    public void setIndexHistoryPath(String indexHistoryPath) {
        this.indexHistoryPath = indexHistoryPath;
    }
}
