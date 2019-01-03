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

    private final int predictionClass;
    private final double probability;
    private final int weight;

    public Prediction(int predictionClass, double probability, int weight){
        this.predictionClass = predictionClass;
        this.probability= probability;
        this.weight = weight;
    }
}
