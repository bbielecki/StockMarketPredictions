package DomainObjects;

public class CrawlerConfig {
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
}
