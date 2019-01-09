package helpers;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import DomainObjects.Prediction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toSet;

public class IndexBehaviourPredictionModel implements Closeable {

    private static final String COMMAND = "python";

    private List<Double> weights;
    private Process process;

    public IndexBehaviourPredictionModel(String modelPath, List<Double> weights) throws IOException {
        if (weights.size() != 5) {
            throw new IllegalArgumentException("There should be only 5 weights (one for each class)!");
        }

        this.weights = weights;
        this.process = new ProcessBuilder(COMMAND, modelPath)
                .redirectErrorStream(true)
                .start();
    }

    public List<Prediction> predict(List<Article> articles, List<IndexDescriptor> indexHistory) throws IOException {
        // TODO Don't call a new process on every call!
        BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // We add a newline character here because models read from standard input line by line
        String modelInput = prepareModelInput(articles, indexHistory);
        outputStream.write(modelInput);
        String modelOutput = inputStream.readLine();
        return new ArrayList<>(parseModelOutput(modelOutput));
    }

    private String prepareModelInput(List<Article> articles, List<IndexDescriptor> indexHistory) {
        JSONObject modelInputJsonObject = new JSONObject();

        JSONArray articlesJsonArray = new JSONArray();
        articlesJsonArray.addAll(articles.stream().map(Article::toJsonObject).collect(toSet()));
        modelInputJsonObject.put("articles", articlesJsonArray);

        JSONArray indexHistoryJsonArray = new JSONArray();
        indexHistoryJsonArray.addAll(indexHistory.stream().map(IndexDescriptor::toJsonObject).collect(toSet()));
        modelInputJsonObject.put("indexHistory", indexHistory);

        return modelInputJsonObject.toJSONString();
    }

    private List<Prediction> parseModelOutput(String modelOutput) {
        String[] predictionStrings = modelOutput
                .replaceAll("\\[", "")
                .replaceAll("\\]","")
                .split(",");
        List<Prediction> predictions = new ArrayList<>();
        for (int i = 0; i < predictionStrings.length; i++) {
            predictions.add(new Prediction(
                    i,
                    Double.valueOf(predictionStrings[i]),
                    1,
                    weights.get(i)));
        }
        return predictions;
    }

    @Override
    public void close() {
        this.process.destroy();
    }
}
