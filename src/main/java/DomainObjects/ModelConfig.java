package DomainObjects;

public class ModelConfig {

    private int maxCrawlers = 5;
    private String indexHistoryPath;
    private String modelServiceAddress;
    private int modelServicePort;


    public String getModelServiceAddress() {
        return modelServiceAddress;
    }

    public void setModelServiceAddress(String modelServiceAddress) {
        this.modelServiceAddress = modelServiceAddress;
    }

    public int getModelServicePort() {
        return modelServicePort;
    }

    public void setModelServicePort(int modelServicePort) {
        this.modelServicePort = modelServicePort;
    }
    public int getMaxCrawlers() {
        return maxCrawlers;
    }

    public void setMaxCrawlers(int maxCrawlers) {
        this.maxCrawlers = maxCrawlers;
    }

    public String getIndexHistoryPath() {
        return indexHistoryPath;
    }

    public void setIndexHistoryPath(String indexHistoryPath) {
        this.indexHistoryPath = indexHistoryPath;
    }
}
