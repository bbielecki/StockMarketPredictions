package helpers;

public class PredictionClassTranslator {
    public static String ToDescription(int predictionClass){
        switch (predictionClass){
            case 0: return "big drop";
            case 1: return "drop";
            case 2: return "no changes";
            case 3: return "increase";
            case 4: return "big increase";
            default: return "";
        }
    }
}
