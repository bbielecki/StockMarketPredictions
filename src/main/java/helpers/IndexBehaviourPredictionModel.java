package helpers;

import DomainObjects.Article;
import DomainObjects.IndexDescriptor;
import DomainObjects.Prediction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toSet;

public class IndexBehaviourPredictionModel implements Closeable {

    private Socket socket;
    private List<Double> weights;

    public IndexBehaviourPredictionModel(String modelHost, int modelPort, List<Double> weights) throws IOException {
        if (weights.size() != 5) {
            throw new IllegalArgumentException("There should be only 5 weights (one for each class)!");
        }

        this.socket = new Socket(modelHost, modelPort);
        this.weights = weights;
    }

    public List<Prediction> predict(List<Article> articles, List<IndexDescriptor> indexHistory) throws IOException {
        BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String modelInput = prepareModelInput(articles, indexHistory);
        outputStream.write(modelInput);
        outputStream.flush();
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
        modelInputJsonObject.put("indexHistory", indexHistoryJsonArray);

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
    public void close() throws IOException {
        this.socket.close();
    }
}
