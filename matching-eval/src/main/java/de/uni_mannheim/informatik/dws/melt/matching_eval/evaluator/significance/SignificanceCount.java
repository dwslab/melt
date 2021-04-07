package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

/**
 * Simple count for significance statistics.
 */
class SignificanceCount {


    public int significantlyDifferent = 0;
    public int notSignificantlyDifferent = 0;
    public int notDefined = 0;

    public SignificanceCount(Significance significance) {
        increment(significance);
    }

    void increment(Significance significance) {
        switch (significance) {
            case SIGNIFICANT:
                significantlyDifferent++;
                return;
            case NOT_SIGNIFICANT:
                notSignificantlyDifferent++;
                return;
            case CANNOT_BE_DETERMINED:
                notDefined++;
        }
    }
}