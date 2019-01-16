package DomainObjects;

public class CrawlerConfig {

    private int actorId;
    private int readModulo;
    private String pathToFile;
    private int windowSize;

    public CrawlerConfig(int readModulo, String pathToFile){
        this.readModulo = readModulo;
        this.pathToFile = pathToFile;
    }

    public int getReadModulo() {
        return readModulo;
    }

    public String getPathToFile() {
        return pathToFile;
    }


    public int getWindowSize() {
        return windowSize;
    }

    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public int getActorId(){return actorId;}

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }
}
