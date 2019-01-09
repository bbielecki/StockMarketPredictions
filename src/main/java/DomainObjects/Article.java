package DomainObjects;

import org.json.simple.JSONObject;

import java.time.LocalDate;

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

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("header", header);
        jsonObject.put("date", date.toString());
        return jsonObject;
    }
}
