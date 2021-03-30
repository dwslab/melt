package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.significance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public McNemarIndividualResult(String matcherName1, String matcherName2, String testCaseName, String trackName,
                                   double alpha) {
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

    /**
     * Given a map from McNemarIndividualResult to Double (=pvalue), this method extracts the tracks.
     * @param pValues The p values (map).
     * @return Track names as String in set.
     */
    public static Set<String> getDistinctTracks(Map<McNemarIndividualResult, Double> pValues){
        Set<String> result = new HashSet<>();
        for(McNemarIndividualResult mir : pValues.keySet()){
            result.add(mir.trackName);
        }
        return result;
    }

    /**
     * Given a map of p values, determine the absolute number of test cases (as identified by their name).
     * @param pValues Map of p values.
     * @return Integer stating the number of test cases.
     */
    public static int getNumberOfTestCases(Map<McNemarIndividualResult, Double> pValues){
        Set<String> testCases = new HashSet<>();
        for(McNemarIndividualResult mir : pValues.keySet()){
            testCases.add(mir.testCaseName);
        }
        return testCases.size();
    }

    /**
     * Given a map from McNemarIndividualResult to Double (=pvalue) and a track, a sub-map is created with the
     * entries that are from that particular track.
     * @param pValues The p values (map).
     * @param track Track as String.
     * @return Sub-Map.
     */
    public static Map<McNemarIndividualResult, Double> getEntriesForTrack(Map<McNemarIndividualResult, Double> pValues,
                                                                    String track){
        Map<McNemarIndividualResult, Double> result = new HashMap<>();
        for (Map.Entry<McNemarIndividualResult, Double> entry : pValues.entrySet()){
            if(entry.getKey().trackName.equals(track)){
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}