package helpers;

import DomainObjects.Article;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.util.Objects.requireNonNull;

public class ArticleFileParser {

    private static String CSV_SPLIT_BY = ",";
    private static int CRAWLERS_NUMBER = 5;


    public static List<Article> readArticles(LocalDate date, int readModulo, String pathToFile){
        BufferedReader br = null;
        String line = "";
        int articlesCount = 0;

        List<Article> articlesToReturn = new ArrayList<>();

        try {
            br = new BufferedReader(new InputStreamReader(requireNonNull(getSystemResourceAsStream(pathToFile))));

            //skip headers
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] articleWithDay = line.split(CSV_SPLIT_BY,2);
                try {
                    LocalDate day = LocalDate.parse(articleWithDay[0]);

                    if (date.isEqual(day)) {
                        if ((articlesCount % CRAWLERS_NUMBER) == readModulo)
                            articlesToReturn.add(new Article(articleWithDay[1], date));
                        articlesCount++;
                 }
                } catch (DateTimeParseException e) {
                    continue;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return articlesToReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return articlesToReturn;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return articlesToReturn;
                }
            }
        }

        return articlesToReturn;
    }
}
