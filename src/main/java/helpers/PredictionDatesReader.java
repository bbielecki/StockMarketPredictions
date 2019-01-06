package helpers;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PredictionDatesReader {
    public static List<LocalDate> getAsList(){
        return getAsList("MainConfigs/predictionDates.properties");
    }
    public static List<LocalDate> getAsList(String resourcePath){
        List<LocalDate> predictionDates = new ArrayList<>();
        try {
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourcePath);
            Properties prop = new Properties();
            prop.load(inputStream);

            //read startDate from config file
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate startDate = LocalDate.parse(prop.getProperty("startDate"), dateFormat);
            predictionDates.add(startDate);

            //read number of following days to make predictions
            int followingDayCounter = Integer.parseInt(prop.getProperty("numberOfDays"));
            for (int i = 1; i<=followingDayCounter; i++) predictionDates.add(startDate.plusDays(i));

            return predictionDates;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return predictionDates;
    }
}
