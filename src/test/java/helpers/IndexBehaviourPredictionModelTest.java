package helpers;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import DomainObjects.Prediction;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IndexBehaviourPredictionModelTest {

    @Test
    public void predict() throws IOException {
        // given
        String modelPath = getClass().getResource("/helpers/IndexBehaviourPredictionTestModel.py").getPath();
        LocalDate predictionDate = LocalDate.of(2016, 7, 1);
        IndexBehaviourPredictionModel model = new IndexBehaviourPredictionModel(modelPath, nCopies(5, 1.0));
        List<Article> articles = asList(
                new Article("Test header 1", predictionDate),
                new Article("Test header 2", predictionDate.minusDays(1)),
                new Article("Test header 3", predictionDate.minusDays(2))
        );
        List<IndexDescriptor> indexHistory = asList(
                new IndexDescriptor(predictionDate, 1.0, 2.0),
                new IndexDescriptor(predictionDate.minusDays(1), 2.0, 3.0)
        );

        // when
        List<Prediction> actual = model.predict(articles, indexHistory);
        List<Prediction> expected = asList(
                new Prediction(0, 0.5, 1, 1),
                new Prediction(1, 0.5, 1, 1),
                new Prediction(2, 0.5, 1, 1),
                new Prediction(3, 0.5, 1, 1),
                new Prediction(4, 0.5, 1, 1)
        );

        // then
        assertThat(actual, is(expected));
    }
}