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
        for(Object matcher : this.matchers){
            AlignmentAndParameters matcherResult = GenericMatcherCaller.runMatcherMultipleRepresentations(matcher, sourceRepresentations, targetRepresentations, inputAlignment, parameters);
            if(matcherResult.getAlignment() == null){
                throw new IllegalArgumentException("A matcher returned null from the match method. No matcher should do this. Please repair the matcher " + matcher.getClass());
            }
            if(matcherResult.getParameters() == null){
                throw new IllegalArgumentException("A matcher set the parameters object to null. No matcher should do this. Please repair the matcher " + matcher.getClass());
            }
            inputAlignment = matcherResult.getAlignment();
            parameters = matcherResult.getParameters();
        }
        return new AlignmentAndParameters(inputAlignment, parameters);
    }
}
