package DomainObjects;

import java.util.Date;

public class Article {
    private String header;
    private Date date;

    public String getHeader() {
        return header;
    }

    public Date getDate() {
        return date;
    }

    public Article(String header, Date date){
        this.header = header;
        this.date = date;
    }
}
