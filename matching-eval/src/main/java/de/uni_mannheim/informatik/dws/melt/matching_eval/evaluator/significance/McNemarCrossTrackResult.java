package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

/**
 * Local data structure.
 * To be used for testing.
 */
class McNemarCrossTrackResult {


    public String matcherName1;
    public String matcherName2;
    public double alpha;

    public McNemarCrossTrackResult(String matcherName1, String matcherName2, double alpha) {
        this.matcherName1 = matcherName1;
        this.matcherName2 = matcherName2;
        this.alpha = alpha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof McNemarCrossTrackResult)) return false;

        McNemarCrossTrackResult that = (McNemarCrossTrackResult) o;
        return this.matcherName1.equals(that.matcherName1) &&
                this.matcherName2.equals(that.matcherName2) &&
                this.alpha == that.alpha;
    }

    @Override
    public int hashCode() {
        return matcherName1.hashCode() + matcherName2.hashCode() + (int) (alpha * 10);
    }

    @Override
    public String toString() {
        return this.matcherName1 + "," + this.matcherName2 + "," + alpha;
    }
}