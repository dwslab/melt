package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherMultiSourceCaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes all multi source matchers one after the other.
 */
public class MultiSourcePipelineSequential implements IMatcherMultiSourceCaller {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourcePipelineSequential.class);
    
    protected List<Object> matchers;
        
    public MultiSourcePipelineSequential(Object... matchers){
        this.matchers = Arrays.asList(matchers);
    }
    
    public MultiSourcePipelineSequential(Iterable<Object> matchers){
        this.matchers = new ArrayList<>();
        matchers.forEach(this.matchers::add);
    }

    @Override
    public AlignmentAndParameters match(List<Set<Object>> models, Object inputAlignment, Object parameters) throws Exception {
        Object tmpAlignment = inputAlignment;
        Object tmpParameters = parameters;
        for(Object matcher : this.matchers){
            AlignmentAndParameters matcherResult = GenericMatcherMultiSourceCaller.runMatcherMultiSourceMultipleRepresentations(matcher, models, tmpAlignment, tmpParameters);
            if(matcherResult.getAlignment() == null){
                LOGGER.warn("Matcher could not be called or returned null object. The matcher sequence will stop here and return null.");
                return null;
            }
            if(matcherResult.getParameters() == null){
                LOGGER.warn("Parameters is null. Use old parameters as fallback.");
            }else{
                tmpParameters = matcherResult.getParameters();
            }
        }
        return new AlignmentAndParameters(tmpAlignment, tmpParameters);
    }

    @Override
    public boolean needsTransitiveClosureForEvaluation() {
        //if at least one matcher needs a transitive closure, then the whole pipeline needs transitive closure.
        for(Object matcher : this.matchers){
            if(GenericMatcherMultiSourceCaller.needsTransitiveClosureForEvaluation(matcher))
                return true;
        }
        return false;
    }
    
    public void addMatcher(Object matcher){
        this.matchers.add(matcher);
    }
}
