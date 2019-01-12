package helpers;

import DomainObjects.IndexDescriptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.util.Objects.requireNonNull;

public class IndexHistoryReader {

    private static final String DELIMITER = ",";

    /**
     * @param filePath a path for file with CSV values that have dates in descending order
     * @param predictionDate a date for which we want to forecast using historical data
     * @param windowSize indicates the range of days that would be retrieved from file
     * @return list of index descriptors that dates of would be within range of days defined by windowSize before
     * prediction date and prediction date minus one
     */
    public static List<IndexDescriptor> readHistory(String filePath, LocalDate predictionDate, int windowSize) {

        List<IndexDescriptor> indexDescriptors = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(requireNonNull(getSystemResourceAsStream(filePath))))) {

            // Skip header
            br.readLine();

            String line;
            LocalDate firstHistoryDate = predictionDate.minusDays(windowSize + 1);
            while ((line = br.readLine()) != null) {

                String[] row = line.split(DELIMITER);
                LocalDate rowDate = LocalDate.parse(row[0]);
                if (rowDate.isAfter(firstHistoryDate) && rowDate.isBefore(predictionDate)) {

                    IndexDescriptor indexDescriptor = new IndexDescriptor(
                            rowDate,
                            Double.valueOf(row[1]),
                            Double.valueOf(row[4]));
                    indexDescriptors.add(indexDescriptor);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return indexDescriptors;
    }
}
