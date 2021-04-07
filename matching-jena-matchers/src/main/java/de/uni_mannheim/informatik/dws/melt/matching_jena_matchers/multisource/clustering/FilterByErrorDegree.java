package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;

/**
 * This filter of correspondences is based on the community structure of the correspondences.
 * E.g. if many entities are fully connected, then this indicates that all of those correspondences are correct.
 * But if tehre are some weakly connected entites, then this might indicate wrong correspondences.
 */
public class FilterByErrorDegree implements IMatcherMultiSource<Object, Alignment, Object>, Filter{
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(FilterByErrorDegree.class);
    
    private double threshold;
    private ModularityAlgorithm algorithm;
    private int modularityFunction;
    private double resolution;
    private long randomSeed;
    private int nRandomStarts;
    private int nIterations;
    

    /**
     * Does not filter but just add the confidence with the standard algorithm.
     */
    public FilterByErrorDegree() {
        this(-0.5);
    }
    
    public FilterByErrorDegree(double threshold) {
        this(threshold, ModularityAlgorithm.LOUVRAIN);
    }
    
    /**
     * Does not filter but just add the confidence.
     * @param algorithm the algorithm to use.
     */
    public FilterByErrorDegree(ModularityAlgorithm algorithm) {
        this(-0.5, algorithm);
    }
    
    /**
     * Constructor with threshold and algorithm.
     * @param threshold set the threshold below 0 to not filter.
     * @param algorithm the algorithm to use
     */
    public FilterByErrorDegree(double threshold, ModularityAlgorithm algorithm) {
        this(threshold, algorithm, 1, 1.0, 0, 1, 5);
    }

    /**
     * Constructor with all parameters. Set threshold smaller zero to not filter.
     * @param threshold the threshold
     * @param algorithm algorithm
     * @param modularityFunction modularityFunction
     * @param resolution resolution
     * @param randomSeed randomSeed
     * @param nRandomStarts nRandomStarts
     * @param nIterations nIterations
     */
    public FilterByErrorDegree(double threshold, ModularityAlgorithm algorithm, int modularityFunction, double resolution, long randomSeed, int nRandomStarts, int nIterations) {
        this.threshold = threshold;
        this.algorithm = algorithm;
        this.modularityFunction = modularityFunction;
        this.resolution = resolution;
        this.randomSeed = randomSeed;
        this.nRandomStarts = nRandomStarts;
        this.nIterations = nIterations;
    }
    
    @Override
    public Alignment match(List<Object> models, Alignment inputAlignment, Object parameters) throws Exception {
        if(threshold < 0){
            return addConfidence(inputAlignment);
        }else{
            return filter(inputAlignment);
        }
    }
    
    public Alignment filter(Alignment alignment){
        Alignment newAlignment = new Alignment(alignment, false);
        ComputeErrDegree<String> errDegree = new ComputeErrDegree<>();
        for(Correspondence c : alignment){
            errDegree.addEdge(c.getEntityOne(), c.getEntityTwo(), c.getConfidence());
        }
        Map<Entry<String,String>, Double> map = errDegree.computeLinkError(modularityFunction, resolution, randomSeed, nRandomStarts, nIterations, algorithm);
        for(Correspondence correspondence : alignment){
            Double err = getErrorValue(map, correspondence);
            if(err <= threshold){
                correspondence.addAdditionalConfidence(FilterByErrorDegree.class, err);
                newAlignment.add(correspondence);
            }
        }
        return newAlignment;
    }
    
    public Alignment addConfidence(Alignment alignment){
        ComputeErrDegree<String> errDegree = new ComputeErrDegree<>();
        for(Correspondence c : alignment){
            errDegree.addEdge(c.getEntityOne(), c.getEntityTwo(), c.getConfidence());
        }
        Map<Entry<String,String>, Double> map = errDegree.computeLinkError(modularityFunction, resolution, randomSeed, nRandomStarts, nIterations, algorithm);
        for(Correspondence correspondence : alignment){
            correspondence.addAdditionalConfidence(FilterByErrorDegree.class, getErrorValue(map, correspondence));
        }
        return alignment;
    }
    
    private static Double getErrorValue(Map<Entry<String,String>, Double> map, Correspondence correspondence){
        Double d = map.get(new SimpleEntry<>(correspondence.getEntityOne(), correspondence.getEntityTwo()));
        if(d != null)
            return d;
        d = map.get(new SimpleEntry<>(correspondence.getEntityTwo(), correspondence.getEntityOne()));
        if(d != null)
            return d;
        return 0.0d;
    }
    
    /**
     * Write a tsv file which contains the err value in the first column and the number of correspondences with this value as a second column.
     * This helps to find out the right threshold.
     * @param alignment the alignemnt to analyze
     * @param file the file to write to.
     */
    public void analyzeErrDistribution(Alignment alignment, File file){
        ComputeErrDegree<String> errDegree = new ComputeErrDegree<>();
        for(Correspondence c : alignment){
            errDegree.addEdge(c.getEntityOne(), c.getEntityTwo(), c.getConfidence());
        }
        Map<Entry<String,String>, Double> map = errDegree.computeLinkError(modularityFunction, resolution, randomSeed, nRandomStarts, nIterations, algorithm);
        Counter<Double> counter = new Counter<>(map.values());
        
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            bw.write("ErrValue\tCountCorrespondences"); bw.newLine();
            List<Entry<Double, Integer>> l = counter.mostCommon();
            l.sort(Entry.comparingByKey(Comparator.reverseOrder()));            
            for(Entry<Double, Integer> e : l){
                bw.write(e.getKey() + "\t" + e.getValue()); bw.newLine();
            }
        } catch (IOException ex) {
            LOGGER.error("Could not write the analysis file", ex);
        }
    }
}
