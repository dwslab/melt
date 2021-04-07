package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

/**
 * Local data structure.
 */
public class McNemarTrackResult {


    public String matcherName1;
    public String matcherName2;
    public String trackName;
    public double alpha;

    public McNemarTrackResult(String matcherName1, String matcherName2, String trackName, double alpha) {
        this.matcherName1 = matcherName1;
        this.matcherName2 = matcherName2;
        this.trackName = trackName;
        this.alpha = alpha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof McNemarTrackResult)) return false;

        McNemarTrackResult that = (McNemarTrackResult) o;
        return this.matcherName1.equals(that.matcherName1) &&
                this.matcherName2.equals(that.matcherName2) &&
                this.trackName.equals(that.trackName) &&
                this.alpha == that.alpha;
    }

    @Override
    public int hashCode() {
        return matcherName1.hashCode() + matcherName2.hashCode() + trackName.hashCode() + (int) (alpha * 10);
    }

    @Override
    public String toString() {
        return this.trackName + "," + this.matcherName1 + "," + this.matcherName2 + "," + alpha;
    }
}