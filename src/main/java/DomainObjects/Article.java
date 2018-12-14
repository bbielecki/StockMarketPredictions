package DomainObjects;

import java.time.LocalDate;
import java.util.Date;

public class Article {
    private String header;
    private LocalDate date;

    public String getHeader() {
        return header;
    }

    public LocalDate getDate() {
        return date;
    }

    public Article(String header, LocalDate date){
        this.header = header;
        this.date = date;
    }
}
