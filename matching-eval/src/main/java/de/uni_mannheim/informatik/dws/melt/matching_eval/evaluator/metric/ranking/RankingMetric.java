package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.ranking;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.Metric;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A metric which computes the NDCG and average precision for a execution result.
 */
public class RankingMetric extends Metric<RankingResult>{
    
    private static Logger LOGGER = LoggerFactory.getLogger(RankingMetric.class);
    
    private static double logOf2 = Math.log(2);    
    protected boolean partialGoldStandard;
    protected SameConfidenceRanking sameConfidenceRanking;
    
    public RankingMetric(boolean partialGoldStandard, SameConfidenceRanking sameConfidenceRanking){
        this.partialGoldStandard = partialGoldStandard;
        this.sameConfidenceRanking = sameConfidenceRanking;
    }

    @Override
    protected RankingResult compute(ExecutionResult executionResult) {
        
        Alignment systemAlignment = executionResult.getSystemAlignment();
        if(this.partialGoldStandard){
            systemAlignment = getSystemResultReducedToGoldStandardEntities(executionResult);
        }
        Alignment referenceAlignment = executionResult.getReferenceAlignment();
        List<Correspondence> correspondenceRanking = sameConfidenceRanking.sortAlignment(systemAlignment, referenceAlignment);
        
        if(correspondenceRanking.isEmpty()){
            LOGGER.info("List of System result is empty. Rank metric is zero.");
            return new RankingResult(0, 0, 0);
        }
            
        //MAP
        List<Double> precision = new ArrayList<>();
        int truePositive = 0;
        //NDCG
        double dcg = 0;
        double idcg = computeIDCG(correspondenceRanking.size());
        for (int i = 0; i < correspondenceRanking.size(); i++) {
            Correspondence correspondence = correspondenceRanking.get(i);            
            if (!referenceAlignment.contains(correspondence))
                continue;
            truePositive++;
            precision.add((double)truePositive/(double)(i+1));
            dcg += logOf2 / Math.log(i + 2); //because rank = i + 1;
        }
        double ndcg = dcg / idcg;        
        return new RankingResult(dcg, ndcg, getAverage(precision));
    }
    
    
    protected double computeIDCG(int n) {
        double idcg = 0;
        for (int i = 0; i < n; i++){
                idcg += logOf2 / Math.log(i + 2);
        }
        return idcg;
    }
    
    
    /**
     * Return the system alignment but only with correspondences where the source or the target appears also in the gold standard
     * @param executionResult execution result to use
     * @return reduced system alignment
     */
    protected Alignment getSystemResultReducedToGoldStandardEntities(ExecutionResult executionResult){
        Alignment systemAlignment = executionResult.getSystemAlignment();
        Alignment referenceAlignment = executionResult.getReferenceAlignment();
        
        Set<String> referenceSources = makeSet(referenceAlignment.getDistinctSources());
        Set<String> referenceTargets = makeSet(referenceAlignment.getDistinctTargets());
        
        Alignment reducedSystemAlignment = new Alignment(systemAlignment);        
        for(Correspondence c : systemAlignment){
            if(referenceSources.contains(c.getEntityOne()) == false && referenceTargets.contains(c.getEntityTwo())){
                reducedSystemAlignment.remove(c);
            }
        }
        return reducedSystemAlignment;
    }
    
    
    protected static <T> Set<T> makeSet(Iterable<T> iterable) {
        Set<T> set = new HashSet<>();
        for(T element : iterable){
            set.add(element);
        }
        return set;
    }
    
    protected double getAverage(List<Double> list){
        double sum = 0.0;
        for(Double d : list)
            sum += d;
        return sum / (double) list.size();
    }
    
}
