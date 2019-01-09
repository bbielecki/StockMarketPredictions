package helpers;

import DomainObjects.IndexDescriptor;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IndexHistoryReaderTest {

    @Test
    public void readHistoryOneDay() {
        // given
        String filePath = getClass().getResource("/helpers/IndexHistoryReaderTestFile.csv").getPath();
        LocalDate predictionDate = LocalDate.of(2016, 7, 1);
        int windowSize = 1;

        // when
        List<IndexDescriptor> actual = IndexHistoryReader.readHistory(filePath, predictionDate, windowSize);
        List<IndexDescriptor> expected = singletonList(
                new IndexDescriptor(predictionDate.minusDays(1), 17712.759766, 17929.990234)
        );

        // then
        assertThat(actual, is(expected));
    }

    @Test
    public void readHistoryThreeDays() {
        // given
        String filePath = getClass().getResource("/helpers/IndexHistoryReaderTestFile.csv").getPath();
        LocalDate predictionDate = LocalDate.of(2016, 7, 1);
        int windowSize = 3;

        // when
        List<IndexDescriptor> actual = IndexHistoryReader.readHistory(filePath, predictionDate, windowSize);
        List<IndexDescriptor> expected = asList(
                new IndexDescriptor(predictionDate.minusDays(1), 17712.759766, 17929.990234),
                new IndexDescriptor(predictionDate.minusDays(2), 17456.019531, 17694.679688),
                new IndexDescriptor(predictionDate.minusDays(3), 17190.509766, 17409.720703)
        );

        // then
        assertThat(actual, is(expected));
    }
}