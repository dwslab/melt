package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.resultsSimilarity;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;

/**
 * Resulting object of the {@link MatcherSimilarityMetric} calculation.
 *
 * @author Jan Portisch
 */
public class MatcherSimilarity {


    /**
     * Data structure holding the matcher similarity result.
     */
    private HashMap<ExecutionResultTuple, Double> matcherSimilaritySet;

    /**
     * Constructor
     */
    public MatcherSimilarity(){
        matcherSimilaritySet = new HashMap<>();
    }

    void add(ExecutionResult executionResult_1, ExecutionResult executionResult_2, double similarity){
        matcherSimilaritySet.put(new ExecutionResultTuple(executionResult_1, executionResult_2), similarity);
    }

    /**
     * Returns the matcher similarity between the two execution results.
     * @param executionResult_1
     * @param executionResult_2
     * @return Similarity as double.
     */
    public double getMatcherSimilarity(ExecutionResult executionResult_1, ExecutionResult executionResult_2){
        return matcherSimilaritySet.get(new ExecutionResultTuple(executionResult_1, executionResult_2));
    }

    public HashMap<ExecutionResultTuple, Double> getMatcherSimilaritySet() {
        return matcherSimilaritySet;
    }


    /**
     * Obtain all Execution results that are used in the {@link MatcherSimilarity#matcherSimilaritySet} as ArrayList.
     * @return ArrayList with all execution results.
     */
    public ArrayList<ExecutionResult> getExecutionResultsAsList(){
        HashSet<ExecutionResult> executionResultsSet = new HashSet<>();
        for(ExecutionResultTuple tuple : matcherSimilaritySet.keySet()){
            executionResultsSet.add(tuple.result1);
            executionResultsSet.add(tuple.result2);
        }
        ArrayList<ExecutionResult> result = new ArrayList<>();
        result.addAll(executionResultsSet);
        Collections.sort(result, ExecutionResult.getMatcherNameComparator());
        return result;
    }


    /**
     * Get the median similarity. Note that the median of the whole matrix is calculated including self-comparisons
     * (matrix diagonal) such as sim(LogMap, Logmap).
     * @return Median as double.
     */
    public double getMedianSimiarity(){
        ArrayList<Double> similarities = new ArrayList<>();
        similarities.addAll(this.matcherSimilaritySet.values());
        return median(similarities);
    }


    /**
     * Get the median similarity ignoring self-comparisons (i.e. the similarity betwen two matchers that are equal
     * such as sim(LogMap, Logmap).
     * @return Median as double.
     */
    public double getMedianSimilariyWithoutSelfSimilarity(){
        ArrayList<Double> similarities = new ArrayList<>();
        for(HashMap.Entry<ExecutionResultTuple, Double> entry : matcherSimilaritySet.entrySet()){
            if(!entry.getKey().result1.equals(entry.getKey().result2)){
                similarities.add(entry.getValue());
            }
        }
        return median(similarities);
    }


    /**
     * Given a list of double values, obtain the median value.
     * @param valueList The list whose median shall be determined.
     * @return Median value as double.
     */
    public static double median(List<Double> valueList){
        Collections.sort(valueList);
        if(valueList.size() % 2 == 0){
            int firstPosition = (valueList.size() / 2 ) - 1;
            return ( valueList.get(firstPosition) + valueList.get(firstPosition + 1) ) / 2.0 ;
        } else {
            return valueList.get(valueList.size() / 2);
        }
    }


    @Override
    public String toString(){
        // transform to list to ensure constant order
        ArrayList<ExecutionResult> executionResults = this.getExecutionResultsAsList();
        HashMap<ExecutionResultTuple, Double> matcherSimilaritySet = this.getMatcherSimilaritySet();

        StringBuffer resultBuffer = new StringBuffer();
        resultBuffer.append("x");

        for(ExecutionResult executionResultTop : executionResults){
            resultBuffer.append("," + executionResultTop.getMatcherName());
        }
        resultBuffer.append("\n");

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        decimalFormatSymbols.setGroupingSeparator(',');
        DecimalFormat df2 = new DecimalFormat("#.##");
        df2.setDecimalFormatSymbols(decimalFormatSymbols);

        for(ExecutionResult executionResultOuter : executionResults){
            resultBuffer.append(executionResultOuter.getMatcherName());
            for(ExecutionResult executionResultInner : executionResults){
                Double similarity = matcherSimilaritySet.get(new ExecutionResultTuple(executionResultOuter, executionResultInner));
                resultBuffer.append("," + df2.format(similarity));
            }
            resultBuffer.append("\n");
        }
        return resultBuffer.toString();
    }


}
