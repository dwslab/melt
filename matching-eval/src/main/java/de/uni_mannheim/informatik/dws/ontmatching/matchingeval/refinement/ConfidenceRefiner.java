package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;

public class ConfidenceRefiner implements Refiner {

    protected double confidence;
    
    public ConfidenceRefiner(double confidence){
        this.confidence = confidence;
    }
    
    @Override
    public ExecutionResult refine(ExecutionResult toBeRefined) {        
        return new ExecutionResult(toBeRefined, toBeRefined.getSystemAlignment().cut(confidence), toBeRefined.getReferenceAlignment(), this);
    }

    public double getConfidence() {
        return confidence;
    }

    //hashcode and equals on confidence value

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (int) (Double.doubleToLongBits(this.confidence) ^ (Double.doubleToLongBits(this.confidence) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ConfidenceRefiner other = (ConfidenceRefiner) obj;
        if (Double.doubleToLongBits(this.confidence) != Double.doubleToLongBits(other.confidence)) {
            return false;
        }
        return true;
    }
    
}
