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


    //This is yet to be tested
    public static List<Article> ReadArticles(LocalDate date){
        BufferedReader br = null;
        String line = "";

        List<Article> articlesToReturn = new ArrayList<>();

        try {
            br = new BufferedReader(new FileReader(ARTICLES_LOCATION));
            while ((line = br.readLine()) != null) {
                List<String> articlesByDay = Arrays.asList(line.split(CSV_SPLIT_BY));
                LocalDate day = LocalDate.parse(articlesByDay.remove(0));

                if (date.isEqual(day))
                    articlesToReturn.addAll(articlesByDay.stream().map(a -> new Article(a, date)).collect(Collectors.toList()));
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return articlesToReturn;
        } catch (IOException e) {
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
