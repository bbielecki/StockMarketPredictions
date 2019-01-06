package DomainObjects;

public class CrawlerConfig {
    private int readModulo;

    public CrawlerConfig(int readModulo){
        this.readModulo = readModulo;
    }

    public int getReadModulo() {
        return readModulo;
    }
}
