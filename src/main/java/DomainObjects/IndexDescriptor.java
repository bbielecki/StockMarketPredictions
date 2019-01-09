package DomainObjects;

import org.json.simple.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class IndexDescriptor {

    private LocalDate date;
    private BigDecimal openValue;
    private BigDecimal closeValue;

    public IndexDescriptor(LocalDate date, Double openValue, Double closeValue) {
        this.date = date;
        this.openValue = BigDecimal.valueOf(openValue);
        this.closeValue = BigDecimal.valueOf(closeValue);
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getOpenValue() {
        return openValue;
    }

    public BigDecimal getCloseValue() {
        return closeValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IndexDescriptor that = (IndexDescriptor) o;
        return date.equals(that.date) &&
                openValue.equals(that.openValue) &&
                closeValue.equals(that.closeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, openValue, closeValue);
    }

    @Override
    public String toString() {
        return "IndexDescriptor{" +
                "date=" + date +
                ", openValue=" + openValue +
                ", closeValue=" + closeValue +
                '}';
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("date", date.toString());
        jsonObject.put("openValue", openValue);
        jsonObject.put("closeValue", closeValue);
        return jsonObject;
    }
}
