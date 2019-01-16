package DomainObjects;

public class ModelConfig {

    private int actorId;
    private int maxCrawlers = 5;
    private int historyWindow;
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

    public int getHistoryWindow() {
        return historyWindow;
    }

    public void setHistoryWindow(int historyWindow) {
        this.historyWindow = historyWindow;
    }

    public int getActorId() {
        return actorId;
    }

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }
}
