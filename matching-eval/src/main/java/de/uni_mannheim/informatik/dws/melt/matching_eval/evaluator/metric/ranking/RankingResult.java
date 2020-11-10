package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

/**
 * Result of the {@link RankingMetric}.
 */
public class RankingResult {

    /**
     * Discounted cumulative gain, see <a href="https://en.wikipedia.org/wiki/Discounted_cumulative_gain">Wikipedia</a>.
     */
    protected double dcg;

    /**
     * Normalized DCG, see <a href="https://en.wikipedia.org/wiki/Discounted_cumulative_gain#Normalized_DCG">Wikipedia</a>.
     */
    protected double ndcg;

    /**
     * Average precision, see <a href="https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#Average_precision">Wikipedia</a>.
     */
    protected double averagePrecision;

    /**
     * HITS@K.
     * Using double to allow for averaging HITS@K.
     */
    protected double hitsAtK;

    /**
     * The precision at k, also known as P@K.
     * You can read more on <a href="https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#Precision_at_K">Wikipedia</a>.
     */
    protected double precisionAtK;

    /**
     * The recall at k, also known as R@K.
     * You can more <a href="https://medium.com/@m_n_malaeb/recall-and-precision-at-k-for-recommender-systems-618483226c54">here</a>.
     */
    protected double recallAtK;

    /**
     * The K of HITS@K-based KPIs.
     * The hits are saved in {@link RankingResult#hitsAtK} and
     */
    protected int kOfHitsAtK;

    /**
     * The reciprocal rank, see <a href="https://en.wikipedia.org/wiki/Mean_reciprocal_rank">Wikipedia</a>.
     */
    protected double reciprocalRank;

    /**
     * R-Precision, see <a href="https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#R-Precision">Wikipedia</a>.
     */
    protected double rPrecision;

    /**
     * Default Constructor
     */
    protected RankingResult(){
    }

    /**
     * Constructor
     * @param dcg Value to be set.
     * @param ndcg Value to be set.
     * @param averagePrecision Value to be set.
     * @param reciprocalRank Value to be set.
     * @param rPrecision Value to be set.
     * @param hitsAtK Value to be set.
     * @param precisionAtK Value to be set.
     * @param recallAtK Value to be set.
     * @param kOfHitsAtK Value to be set.
     */
    public RankingResult(double dcg, double ndcg, double averagePrecision, double reciprocalRank, double rPrecision, double hitsAtK, double precisionAtK, double recallAtK, int kOfHitsAtK) {
        this.dcg = dcg;
        this.ndcg = ndcg;
        this.averagePrecision = averagePrecision;
        this.hitsAtK = hitsAtK;
        this.kOfHitsAtK = kOfHitsAtK;
        this.reciprocalRank = reciprocalRank;
        this.precisionAtK = precisionAtK;
        this.rPrecision = rPrecision;
        this.recallAtK = recallAtK;
    }

    /**
     * Adds all scores of {@code otherResult}.
     * @param otherResult Scores to be added. The {@code otherResult} instance will stay untouched.
     */
    void addScores(RankingResult otherResult){
        this.dcg += otherResult.dcg;
        this.ndcg += otherResult.ndcg;
        this.averagePrecision += otherResult.averagePrecision;
        this.hitsAtK += otherResult.hitsAtK;
        this.reciprocalRank += otherResult.reciprocalRank;
        this.precisionAtK += otherResult.precisionAtK;
        this.rPrecision += otherResult.rPrecision;
        this.recallAtK += otherResult.recallAtK;
    }

    /**
     * Normalize everything with a given factor.
     * @param normalizationFactor The factor by which all scores shall be normalized, e.g. if
     * {@code normalizationFactor=5.0}, then all individual scores (dcg, ndcg, etc.) will be divided by {@code 5.0}.
     */
    void normalizeAllScores(double normalizationFactor){
        this.dcg = this.dcg / normalizationFactor;
        this.ndcg = this.ndcg / normalizationFactor;
        this.averagePrecision = this.averagePrecision / normalizationFactor;
        this.hitsAtK = this.hitsAtK / normalizationFactor;
        this.reciprocalRank = this.reciprocalRank / normalizationFactor;
        this.precisionAtK = this.precisionAtK / normalizationFactor;
        this.rPrecision = this.rPrecision / normalizationFactor;
        this.recallAtK = this.recallAtK / normalizationFactor;
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

    public double getHitsAtK() {
        return hitsAtK;
    }

    public int getkOfHitsAtK() {
        return kOfHitsAtK;
    }

    public double getPrecisionAtK() {
        return precisionAtK;
    }


    /**
     * Get the reciprocal rank, see <a href="https://en.wikipedia.org/wiki/Mean_reciprocal_rank">Wikipedia</a>.
     * @return Reciprocal Rank as double.
     */
    public double getReciprocalRank() {
        return reciprocalRank;
    }

    /**
     * Get R-Precision,
     * see <a href="https://en.wikipedia.org/wiki/Evaluation_measures_(information_retrieval)#R-Precision">Wikipedia</a>.
     * @return rPrecision as double.
     */
    public double getrPrecision() {
        return rPrecision;
    }

    public double getRecallAtK() {
        return recallAtK;
    }

    public double getF1AtK(){
        return (2 * getPrecisionAtK() * getRecallAtK()) / (getPrecisionAtK() + getRecallAtK());
    }

    public double getFmeasureAtK(double beta){
        return ((beta * beta + 1) * getPrecisionAtK() * getRecallAtK()) / ((beta * beta) * getPrecisionAtK() + getRecallAtK());
    }
}
