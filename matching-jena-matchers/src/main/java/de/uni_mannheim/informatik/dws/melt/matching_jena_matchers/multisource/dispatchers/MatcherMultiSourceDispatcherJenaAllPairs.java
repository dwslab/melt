package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MultiSourceDispatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MatcherMultiSourceDispatcherJenaAllPairs extends MatcherMultiSourceURL implements MultiSourceDispatcher, IMatcherMultiSourceCaller{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherMultiSourceDispatcherJenaAllPairs.class);
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Object oneToOneMatcher;

    public MatcherMultiSourceDispatcherJenaAllPairs(Object oneToOneMatcher) {
        this.oneToOneMatcher = oneToOneMatcher;
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
        int combinations = (models.size() * (models.size() - 1)) / 2;
        LOGGER.info("Match {} one to one matches", combinations);
        Alignment finalAlignment = new Alignment();
        int counter = 1;
        for(int i = 0; i < models.size() - 1; i++){
            Set<Object> left = models.get(i);
            for(int j = i + 1; j < models.size(); j++){
                Set<Object> right = models.get(j);
                LOGGER.info("Match combination {} out of {}", counter++, combinations);
                //to make sure that all matchers gets the same input alignment and proeprties we make a deep copy of them.
                Object copiedInputAlignment = objectMapper.readValue(objectMapper.writeValueAsString(inputAlignment), Object.class);
                Object copiedParameters = objectMapper.readValue(objectMapper.writeValueAsString(parameters), Object.class);
                AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(this.oneToOneMatcher, left, right, copiedInputAlignment, copiedParameters);
                Alignment a = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
                if(a == null){
                    LOGGER.warn("Tranformation of the alignment was not succesfull. One matching alignment will not be in the result.");
                }else{
                    finalAlignment.addAll(a);
                }
                //finalAlignment.addAll(oneToOneMatcherJena.match(left, right, inputAlignment, new Properties()));
            }
        }
        //return finalAlignment;
        return new AlignmentAndParameters(finalAlignment, parameters);
    }
    
    @Override
    public boolean needsTransitiveClosureForEvaluation(){
        return false;
    }
    
}
