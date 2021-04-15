package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This filter keeps only the top X correspondences according to confidence.
 * The filter can be configured to be source-based (keep only the top X correspondences for each source node).
 * The filter can be configured to be target-based (keep only the top X correspondences for each target node).
 * The filter can be configured to be size-based (based on the smaller or larger side of the alignment).
 */
public class TopXFilter extends MatcherYAAAJena implements Filter {


    /**
     * Default Constructor
     * @param x X
     * @param filterMode The filter mode.
     * @param threshold The desired threshold. Use 0.0d if you do not want to use threshold filtering.
     */
    public TopXFilter(int x, TopFilterMode filterMode, double threshold){
        setX(x);
        setThreshold(threshold);
        setFilterMode(filterMode);
    }

    public TopXFilter(int x){
        this(x, DEFAULT_FILTER_MODE, DEFAULT_THRESHOLD);
    }

    public TopXFilter(int x, double threshold){
        this(x, DEFAULT_FILTER_MODE, threshold);
    }

    public TopXFilter(int x, TopFilterMode mode){
        this(x, mode, DEFAULT_THRESHOLD);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TopXFilter.class);

    private double threshold;
    public static final double DEFAULT_THRESHOLD = 0.0;

    private int x;
    public static final int DEFAULT_X = 1;

    private TopFilterMode filterMode;
    public static TopFilterMode DEFAULT_FILTER_MODE = TopFilterMode.SMALLEST;

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment);
    }

    /**
     * Filters the given alignment such that there are only the top X correpondences (according to confidence) for
     * every source node.
     * @param alignment The initial alignment.
     * @return The filtered alignment.
     */
    public Alignment filter(Alignment alignment) {
        if(alignment == null){
            return null;
        }
        Alignment result = new Alignment(alignment, false);
        int sourceSize, targetSize;
        switch (this.getFilterMode()){
            case SOURCE:
                for(String source : alignment.getDistinctSources()){
                    result.addAll(filterTopX(alignment.getCorrespondencesSource(source).iterator()));
                }
                break;
            case TARGET:
                for(String target : alignment.getDistinctTargets()){
                    result.addAll(filterTopX(alignment.getCorrespondencesTarget(target).iterator()));
                }
                break;
            case LARGEST:
                sourceSize = getIteratorSize(alignment.getDistinctSources().iterator());
                targetSize = getIteratorSize(alignment.getDistinctTargets().iterator());
                if(sourceSize >= targetSize){
                    for(String source : alignment.getDistinctSources()){
                        result.addAll(filterTopX(alignment.getCorrespondencesSource(source).iterator()));
                    }
                } else {
                    for(String target : alignment.getDistinctTargets()){
                        result.addAll(filterTopX(alignment.getCorrespondencesTarget(target).iterator()));
                    }
                }
                break;
            case SMALLEST:
                sourceSize = getIteratorSize(alignment.getDistinctSources().iterator());
                targetSize = getIteratorSize(alignment.getDistinctTargets().iterator());
                if(sourceSize <= targetSize){
                    for(String source : alignment.getDistinctSources()){
                        result.addAll(filterTopX(alignment.getCorrespondencesSource(source).iterator()));
                    }
                } else {
                    for(String target : alignment.getDistinctTargets()){
                        result.addAll(filterTopX(alignment.getCorrespondencesTarget(target).iterator()));
                    }
                }
        }

        return result;
    }

    private <T> int getIteratorSize(Iterator<T> iterator){
        if (iterator == null){
            return 0;
        }
        int result = 0;
        while(iterator.hasNext()){
            result++;
            iterator.next();
        }
        return result;
    }

    private Alignment filterTopX(Iterator<Correspondence> iterator){
        Alignment result = new Alignment();
        if(iterator == null){
            return result;
        }

        Set<Correspondence> correspondences = new HashSet<>();
        while(iterator.hasNext()){
            correspondences.add(iterator.next());
        }

        result.addAll(correspondences
                .stream()
                .filter(x -> x.getConfidence() > this.threshold)
                .sorted(Comparator.reverseOrder())
                .limit(this.x)
                .collect(Collectors.toSet()));

        return result;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        if(x < 1){
            LOGGER.error("x cannot be less than 1. Using default: 1");
            this.x = DEFAULT_X;
            return;
        }
        this.x = x;
    }

    public TopFilterMode getFilterMode() {
        return filterMode;
    }

    public void setFilterMode(TopFilterMode filterMode) {
        this.filterMode = filterMode;
    }

    /**
     * Filter mode.
     */
    public enum TopFilterMode {

        /**
         * Keep the top X correspondences for the source.
         */
        SOURCE,

        /**
         * Keep the top X correspondences for the target.
         */
        TARGET,

        /**
         * Keep the top X correspondences for the smaller side in the alignment.
         */
        SMALLEST,

        /**
         * Keep the top X correspondences for the larger side in the alignment.
         */
        LARGEST;
    }
}
