package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.relationprediction;

public class RelationTypePredictionResult {
    
    private double confidence;
    private int clazz;

    public RelationTypePredictionResult(double confidence, int clazz) {
        this.confidence = confidence;
        this.clazz = clazz;
    }

    public double getConfidence() {
        return confidence;
    }

    public int getClazz() {
        return clazz;
    }
    
}
