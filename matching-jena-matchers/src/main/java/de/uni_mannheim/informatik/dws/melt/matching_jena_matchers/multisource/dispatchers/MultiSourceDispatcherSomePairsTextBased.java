package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MultiSourceDispatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.TransitiveClosure;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.math.MathEx;

/**
 * This dispatcher will compare the texts in a model and match the ones which are textually the clostest such that a connection between all ontologies exists.
 * Therefore exactly (number of models)-1 matching operations and no merges are executed.
 */
public class MultiSourceDispatcherSomePairsTextBased extends MatcherMultiSourceURL implements MultiSourceDispatcher, IMatcherMultiSourceCaller{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherSomePairsTextBased.class);
    
    private final Object oneToOneMatcher;
    
    private double mindf;
    private double maxdf;
    
    public MultiSourceDispatcherSomePairsTextBased(Object oneToOneMatcher) {
        this(oneToOneMatcher, 0.0, 1.0);
    }
    
    public MultiSourceDispatcherSomePairsTextBased(Object oneToOneMatcher, double mindf, double maxdf) {
        this.oneToOneMatcher = oneToOneMatcher;
        this.mindf = mindf;
        this.maxdf = maxdf;
    }
    
    
    @Override
    public URL match(List<URL> models, URL inputAlignment, URL parameters) throws Exception {        
        List<Set<Object>> list = new ArrayList<>(models.size());
        for(URL ontology : models){
            list.add(new HashSet<>(Arrays.asList(ontology)));
        }
        AlignmentAndParameters alignmentAndPrameters = match(list, inputAlignment, parameters);
        return TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), URL.class);
    }
    
    @Override
    public AlignmentAndParameters match(List<Set<Object>> models, Object inputAlignment, Object parameters) throws Exception{
        int combinations = models.size() - 1;
        LOGGER.info("Match {} one to one matches", combinations);
        Alignment finalAlignment = new Alignment();
        
        //just get the double[][] of features:
        MultiSourceDispatcherIncrementalMergeByClusterText m = new MultiSourceDispatcherIncrementalMergeByClusterText(null, null, this.mindf, this.maxdf);
        double[][] data = m.getClusterFeatures(models, parameters);
        
        List<MatchingPair> list = new ArrayList<>();
        for(int i = 0; i < data.length - 1; i++){
            double[] left = data[i];
            for(int j = i + 1; j < models.size(); j++){
                double[] right = data[j];
                list.add(new MatchingPair(i, j, MathEx.cos(left, right)));
            }
        }
        list.sort(Comparator.comparing(MatchingPair::getDistance).reversed());
        
        int matcheToBeExecuted = combinations;
        TransitiveClosure<Integer> closure = new TransitiveClosure<>();
        for(MatchingPair p : list){
            if(closure.belongToTheSameCluster(p.left, p.right)){
                continue;
            }
            closure.add(p.left, p.right);
            AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(
                    this.oneToOneMatcher, models.get(p.left), models.get(p.right), 
                    DispatcherHelper.deepCopy(inputAlignment), DispatcherHelper.deepCopy(parameters));
            Alignment a = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
            if(a == null){
                LOGGER.warn("Tranformation of the alignment was not succesfull. One matching alignment will not be in the result.");
            }else{
                finalAlignment.addAll(a);
            }
            
            matcheToBeExecuted--;
            if(matcheToBeExecuted <= 0)
                break; // fast exit
        }
        
        return new AlignmentAndParameters(finalAlignment, parameters);
    }
    
    
    @Override
    public boolean needsTransitiveClosureForEvaluation(){
        return true;
    }
    
    class MatchingPair{
        private int left;
        private int right;
        private double distance;

        public MatchingPair(int left, int right, double distance) {
            this.left = left;
            this.right = right;
            this.distance = distance;
        }

        public int getLeft() {
            return left;
        }

        public int getRight() {
            return right;
        }

        public double getDistance() {
            return distance;
        }
    }
}
