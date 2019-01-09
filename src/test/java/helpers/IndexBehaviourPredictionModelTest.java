package helpers;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import DomainObjects.Prediction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.nCopies;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class IndexBehaviourPredictionModelTest {

    private static final String RUN_COMMAND = "python3";
    private static final String MODEL_FILE = "/helpers/IndexBehaviourPredictionTestModel.py";
    private static final String MODEL_HOST = "127.0.0.1";
    private static final int MODEL_PORT = 10000;

    private static Process modelProcess;

    @BeforeClass
    public static void setUp() throws IOException {
        String modelPath = IndexBehaviourPredictionModel.class.getResource(MODEL_FILE).getPath();
        modelProcess = new ProcessBuilder().command(RUN_COMMAND, modelPath).redirectErrorStream(true).start();
        BufferedReader modelOutputStream = new BufferedReader(new InputStreamReader(modelProcess.getInputStream()));

        String expectedEnd = String.format("Model running on %s:%s", MODEL_HOST, MODEL_PORT);
        assertThat(modelOutputStream.readLine().endsWith(expectedEnd), is(true));
    }

    @Test
    public void predict() throws IOException {
        // given
        LocalDate predictionDate = LocalDate.of(2016, 7, 1);
        IndexBehaviourPredictionModel model = new IndexBehaviourPredictionModel(MODEL_HOST, MODEL_PORT, nCopies(5, 1.0));
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

    @AfterClass
    public static void tearDown() {
        modelProcess.destroy();
    }
}