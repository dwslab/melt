package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

/**
 * Data Structure for an individual confusion matrix.
 *
 * @author Sven Hertling, Jan Portisch
 */
public class ConfusionMatrix {

    
    private Alignment truePositive;
    private Alignment falsePositive;
    private Alignment falseNegative;
    
    private double precision;
    private double recall;

    /**
     * The number of correspondences. Typically, this number is {@code truePositive + falsePositive} but this is not
     * the case for partial gold standards!
     */
    private int numberOfCorrespondences;


    /**
     * Constructor to fill confusion matrix. The number of correspondences is assumed to be {@code truePositive + falsePositive}.
     * @param truePositive True positive mapping.
     * @param falsePositive False positive mapping.
     * @param falseNegative False negative mapping
     * @param precision Precision as double [0, 1].
     * @param recall Recall as double [0, 1].
     */
    public ConfusionMatrix(Alignment truePositive, Alignment falsePositive, Alignment falseNegative, double precision, double recall){
        this.truePositive = truePositive;
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
        this.precision = precision;
        this.recall = recall;
        this.numberOfCorrespondences = truePositive.size() + falsePositive.size();
    }

    /**
     * Constructor to fill confusion matrix.
     * @param truePositive True positive mapping.
     * @param falsePositive False positive mapping.
     * @param falseNegative False negative mapping
     * @param numberOfCorrespondences The number of correspondences. Note that this number can deviate from {@code truePositive + falsePositive}
     *                                in case of partial gold standards.
     * @param precision Precision as double [0, 1].
     * @param recall Recall as double [0, 1].
     */
    public ConfusionMatrix(Alignment truePositive, Alignment falsePositive, Alignment falseNegative, int numberOfCorrespondences, double precision, double recall){
        this.truePositive = truePositive;
        this.falsePositive = falsePositive;
        this.falseNegative = falseNegative;
        this.precision = precision;
        this.recall = recall;
        this.numberOfCorrespondences = numberOfCorrespondences;
    }

    /**
     * Alignments which are in the reference mapping and are also found by the matcher.
     * @return found and correct mappings (correct)
     */
    public Alignment getTruePositive() {
        return truePositive;
    }
    
    public int getTruePositiveSize() {
        return truePositive.size();
    }

    /**
     * Alignments which are not correct but found by the matcher
     * @return found but not correct mappings (too much)
     */
    public Alignment getFalsePositive() {
        return falsePositive;
    }
    
    public int getFalsePositiveSize() {
        return falsePositive.size();
    }

    /**
     * Alignments which are correct but not found by the matcher
     * @return correct but not found by the matcher (should be found)
     */
    public Alignment getFalseNegative() {
        return falseNegative;
    }
    
    public int getFalseNegativeSize() {
        return falseNegative.size();
    }

    public double getPrecision() {
        return precision;
    }

    public double getRecall() {
        return recall;
    }

    public double getF1measure() {
        return getFbetaMeasure(1.0);
    }
    
    public double getFbetaMeasure(double beta){
        double betaSquared = Math.pow(beta, 2);
        double numerator = (1 + betaSquared) * (precision * recall);
        double denominator = ((betaSquared * precision) + recall);
        if(denominator == 0){
            return 0;
        }else{
            return numerator / denominator;
        }
    }

    public int getNumberOfCorrespondences() {
        return numberOfCorrespondences;
    }

    public void setNumberOfCorrespondences(int numberOfCorrespondences) {
        this.numberOfCorrespondences = numberOfCorrespondences;
    }

    /**
     * Returns a new confusion matrix where tp, fp, fn are subtracted from the other confusion matrix.
     * @param other the other confusion matrix.
     * @return a new confusion matrix which is the set difference.
     */
    public ConfusionMatrix subtract(ConfusionMatrix other){
        
        Alignment subTruePositive = new Alignment(this.truePositive);
        subTruePositive.removeAll(other.truePositive);
        
        Alignment subFalsePositive = new Alignment(this.falsePositive);
        subFalsePositive.removeAll(other.falsePositive);
        
        Alignment subFalseNegative = new Alignment(this.falseNegative);
        subFalseNegative.removeAll(other.falseNegative);

        int numberOfCorrespondences = this.getNumberOfCorrespondences();
        numberOfCorrespondences = numberOfCorrespondences - other.getNumberOfCorrespondences();
        
        return ConfusionMatrixMetric.calculateConfusionMatrixFromMappings(subTruePositive, subFalsePositive, subFalseNegative, numberOfCorrespondences);
    }

    @Override
    public String toString() {
        return "ConfusionMatrix: TP:" + truePositive.size() + 
                ", FP=" + falsePositive.size() + ", FN=" + falseNegative.size() + 
                ", PREC=" + String.format("%.4f", precision) + 
                ", REC=" + String.format("%.4f", recall) + 
                ", F1=" + String.format("%.4f", getF1measure());
    }
}
