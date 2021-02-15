package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.resultsSimilarity;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import mdsj.MDSJ;

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
     * @param executionResult_1 The first execution results.
     * @param executionResult_2 The second execution result.
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
    
    
    /**
     * Get coordinates for visualizing the matcher distances in a 2D space.
     * It uses multidimensional scaling for converting the distances to coordinates.
     * This is based on <a href="http://www.dit.unitn.it/~p2p/OM-2013/om2013_poster6.pdf">OM-Poster: Is my ontology matching system similar to yours? - Ernesto Jimenez-Ruiz, Bernardo Cuenca Grau, Ian Horrocks</a>.
     * @see <a href="https://www.inf.uni-konstanz.de/exalgo/software/mdsj/">MDSJ Library from University Konstanz</a>
     * @return a map which maps an execution result to a coordinate.
     */
    public Map<ExecutionResult, Point2D.Double> getCoordinates(){
        List<ExecutionResult> results = this.getExecutionResultsAsList();
        
        //create distance matrix:
        double[][] distance_matrix = new double[results.size()][results.size()];        
        for(int i = 0; i < results.size(); i++){
            ExecutionResult iResult = results.get(i);
            for(int j = 0; j < results.size(); j++){
                if(i == j){
                    distance_matrix[i][j] = 0; // distance between same is zero
                    continue;
                }                    
                ExecutionResult jResult = results.get(j);
                distance_matrix[i][j] = 1.0 - this.getMatcherSimilarity(iResult, jResult); //"1 -" because dissimilarity
            }
        }
        
        //compute coordinates
        double[][] coordinates = MDSJ.stressMinimization(distance_matrix, 2);
        
        double[] xValues = shift(coordinates[0]); //shift if values are below zero
        double[] yValues = shift(coordinates[1]);
        
        Map<ExecutionResult, Point2D.Double> coordinateMap = new HashMap<>();
        for(int i = 0; i < results.size(); i++){
            coordinateMap.put(results.get(i), new Point2D.Double(xValues[i], yValues[i]));
        }
        return coordinateMap;
    }
    
    private static double[] shift(double[] array){
        double min = Double.MAX_VALUE;
        for(Double d : array){
            if(d < min){
                min = d;
            }
        }
        
        double[] shifted = new double[array.length];
        double shift = 0 - min;
        if(shift > 0){
            for(int i = 0; i < array.length; i++){
                shifted[i] = array[i] + shift; 
            }
            return shifted;
        }else{
            return array;
        }
    }
}
