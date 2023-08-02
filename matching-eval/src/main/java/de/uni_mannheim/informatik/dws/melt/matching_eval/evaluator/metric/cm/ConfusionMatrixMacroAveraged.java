package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

/**
 * Data Structure for an individual confusion matrix.
 *
 * @author Sven Hertling, Jan Portisch
 */
public class ConfusionMatrixMacroAveraged extends ConfusionMatrix {

    protected double fmeasure;
    
    public ConfusionMatrixMacroAveraged(Alignment truePositive, Alignment falsePositive, Alignment falseNegative, double precision, double recall, double fmeasure){
        this(truePositive, falsePositive, falseNegative, truePositive.size() + falsePositive.size(), precision, recall, fmeasure);
    }

    public ConfusionMatrixMacroAveraged(Alignment truePositive, Alignment falsePositive, Alignment falseNegative, int numberOfCorrespondences, double precision, double recall, double fmeasure){
        super(truePositive, falsePositive, falseNegative, numberOfCorrespondences, precision, recall);
        this.fmeasure = fmeasure;
    }

    @Override
    public double getF1measure() {
        return fmeasure;
    }
    
    @Override
    public double getFbetaMeasure(double beta){
        if(beta == 1.0)
            return fmeasure;
        throw new IllegalArgumentException("macro averaged confusion matrix can not return fbeta score.");
    }
}
