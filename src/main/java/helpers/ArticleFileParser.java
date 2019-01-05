package helpers;

import DomainObjects.Article;

import java.time.LocalDate;
import java.util.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class ArticleFileParser {

    private static String ARTICLES_LOCATION = "";
    private static String CSV_SPLIT_BY = ",";
    private static int CRAWLERS_NUMBER = 5;


    public static List<Article> readArticles(LocalDate date, int readModulo){
        BufferedReader br = null;
        String line = "";
        int articlesCount = 0;

        List<Article> articlesToReturn = new ArrayList<>();

        try {
            br = new BufferedReader(new FileReader(ARTICLES_LOCATION));

            //skip headers
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] articleWithDay = line.split(CSV_SPLIT_BY,2);
                LocalDate day = LocalDate.parse(articleWithDay[0]);

                if (date.isEqual(day)) {
                    if ((articlesCount % CRAWLERS_NUMBER) == readModulo)
                        articlesToReturn.add(new Article(articleWithDay[1], date));
                    articlesCount++;
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
