package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

/**
 * Local data structure.
 * To be used for testing.
 */
class McNemarIndividualResult {


    public String matcherName1;
    public String matcherName2;
    public String testCaseName;
    public String trackName;
    public double alpha;

    public McNemarIndividualResult(String matcherName1, String matcherName2, String testCaseName, String trackName, double alpha) {
        this.matcherName1 = matcherName1;
        this.matcherName2 = matcherName2;
        this.testCaseName = testCaseName;
        this.trackName = trackName;
        this.alpha = alpha;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof McNemarIndividualResult)) return false;

        McNemarIndividualResult that = (McNemarIndividualResult) o;
        return this.matcherName1.equals(that.matcherName1) &&
                this.matcherName2.equals(that.matcherName2) &&
                this.testCaseName.equals(that.testCaseName) &&
                this.trackName.equals(that.trackName) &&
                this.alpha == that.alpha;
    }

    @Override
    public String toString() {
        return this.trackName + "," + this.testCaseName + "," + this.matcherName1 + "," + this.matcherName2 + "," + alpha;
    }

    public McNemarTrackResult getTrackResult() {
        return new McNemarTrackResult(matcherName1, matcherName2, trackName, alpha);
    }

    public McNemarCrossTrackResult getCrossTrackResult(){
        return new McNemarCrossTrackResult(matcherName1, matcherName2, alpha);
    }
}