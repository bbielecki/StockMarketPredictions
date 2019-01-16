package DomainObjects;

public class CrawlerConfig {

    private int actorId;
    private int readModulo;
    private String pathToFile;

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

    public int getActorId(){return actorId;}

    public void setActorId(int actorId) {
        this.actorId = actorId;
    }
}
