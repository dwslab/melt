package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

/**
 * Result of the RankingMetric
 */
public class RankingResult {
    
    protected double dcg;
    protected double ndcg;
    protected double averagePrecision;

    public RankingResult(double dcg, double ndcg, double averagePrecision) {
        this.dcg = dcg;
        this.ndcg = ndcg;
        this.averagePrecision = averagePrecision;
    }

    public double getDcg() {
        return dcg;
    }

    public double getNdcg() {
        return ndcg;
    }

    public double getAveragePrecision() {
        return averagePrecision;
    }

    
    
    
}
