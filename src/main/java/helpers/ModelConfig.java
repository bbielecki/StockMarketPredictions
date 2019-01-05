package helpers;

public class ModelConfig {
    private int maxCrawlers = 5;

    private String modelPath;

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public int getMaxCrawlers() {
        return maxCrawlers;
    }

    public void setMaxCrawlers(int maxCrawlers) {
        this.maxCrawlers = maxCrawlers;
    }
}
