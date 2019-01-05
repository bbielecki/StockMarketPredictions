package DomainObjects;

import java.math.BigDecimal;
import java.util.Objects;

public class IndexDescriptor {

    private BigDecimal openValue;
    private BigDecimal closeValue;

    public IndexDescriptor(Double openValue, Double closeValue) {
        this.openValue = BigDecimal.valueOf(openValue);
        this.closeValue = BigDecimal.valueOf(closeValue);
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
        return openValue.equals(that.openValue) &&
                closeValue.equals(that.closeValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openValue, closeValue);
    }

    @Override
    public String toString() {
        return "[" + openValue + ", " + closeValue + "]";
    }
}
