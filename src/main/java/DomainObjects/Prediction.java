package DomainObjects;

public class Prediction {
    public int getPredictionClass() {
        return predictionClass;
    }

    public double getProbability() {
        return probability;
    }

    public int getWeight() {
        return weight;
    }

    public double getPredictorWeight() {
        return predictorWeight;
    }

    private final int predictionClass;
    private final double probability;
    private final int weight;
    private double predictorWeight;

    public Prediction(int predictionClass, double probability, int weight, double predictorWeight){
        this.predictionClass = predictionClass;
        this.probability= probability;
        this.weight = weight;
        this.predictorWeight = predictorWeight;
    }
}
