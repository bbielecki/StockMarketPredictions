package helpers;

import DomainObjects.Article;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ArticleFileParserTest {

    @Test
    public void readArticlesFromOneDay() {
        String filePath = getClass().getResource("/helpers/ArticleFileParserTestFile.csv").getPath();
        int modulo = 0;
        LocalDate date = LocalDate.parse("2016-07-01");

        List<Article> articles = ArticleFileParser.readArticles(date, modulo, filePath);

        assertTrue(articles.size() > 0);
        assertEquals(date, articles.get(0).getDate());
    }
}
