package de.uni_mannheim.informatik.dws.melt.matching_base;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes all matchers one after the other.
 */
public class MatcherPipelineSequential implements IMatcherCaller {


    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherPipelineSequential.class);
    
    protected List<Object> matchers;
    
    public MatcherPipelineSequential(){
        this.matchers = new ArrayList<>();
    }
    
    public MatcherPipelineSequential(Object... matchers){
        this.matchers = Arrays.asList(matchers);
    }
    
    public MatcherPipelineSequential(Iterable<Object> matchers){
        this.matchers = new ArrayList<>();
        matchers.forEach(this.matchers::add);
    }
    
    @Override
    public AlignmentAndParameters match(Set<Object> sourceRepresentations, Set<Object> targetRepresentations, Object inputAlignment, Object parameters) throws Exception {
        Object tmpAlignment = inputAlignment;
        Object tmpParameters = parameters;
        for(Object matcher : this.matchers){
            AlignmentAndParameters matcherResult = GenericMatcherCaller.runMatcherMultipleRepresentations(matcher, sourceRepresentations, targetRepresentations, tmpAlignment, tmpParameters);
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
}
