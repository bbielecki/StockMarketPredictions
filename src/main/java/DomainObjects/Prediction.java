package DomainObjects;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prediction that = (Prediction) o;
        return predictionClass == that.predictionClass &&
                Double.compare(that.probability, probability) == 0 &&
                weight == that.weight &&
                Double.compare(that.predictorWeight, predictorWeight) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(predictionClass, probability, weight, predictorWeight);
    }
}
