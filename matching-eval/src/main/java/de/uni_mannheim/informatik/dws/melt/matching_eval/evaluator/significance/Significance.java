package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

/**
 * Enumeration for Significance
 */
enum Significance {


    SIGNIFICANT,
    NOT_SIGNIFICANT,
    CANNOT_BE_DETERMINED;

    public static Significance getSignificance(Double p, double alpha) {
        if (Double.isNaN(p)) {
            return CANNOT_BE_DETERMINED;
        }
        if (p < alpha) {
            return SIGNIFICANT;
        } else return NOT_SIGNIFICANT;
    }
}