package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MultiSourceDispatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.GenericMatcherCaller;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherIncrementalMergeByOrder.IDENTITY;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This dispatcher will match multiple ontologies by selecting a few pairs.
 * First the ontologies will be sorted by a given comparator.
 * As an example A, B, C, D.
 * Afterwards two possible matching strategies are possible:
 * <ul>
 * <li>firstVsRest (constructor parameter) is true: it will match (A,B) ; (A,C) ; (A,D)</li>
 * <li>firstVsRest (constructor parameter) is false: it will match (A,B) ; (B,C) ; (C,D)</li>
 * </ul>
 * Some comparators can be found at {@link MultiSourceDispatcherIncrementalMergeByOrder} as static attributes.
 */
public class MultiSourceDispatcherSomePairsOrderBased extends MatcherMultiSourceURL implements MultiSourceDispatcher, IMatcherMultiSourceCaller{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherSomePairsOrderBased.class);    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final Object oneToOneMatcher;    
    private final Comparator<? super ModelAndIndex> comparator;
    private final boolean firstVsRest;
    
    
    public MultiSourceDispatcherSomePairsOrderBased(Object oneToOneMatcher, Comparator<? super ModelAndIndex> comparator, boolean firstVsRest) {
        this.oneToOneMatcher = oneToOneMatcher;
        this.comparator = comparator;
        this.firstVsRest = firstVsRest;
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
        int numberOfModels = models.size();
        if(numberOfModels < 2){
            LOGGER.info("Nothing to match, because to few ontologies. Return input alignment.");
            return new AlignmentAndParameters(inputAlignment, parameters);
        }
        
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        List<ModelAndIndex> inducedOrder = new ArrayList<>(numberOfModels);
        for(int i = 0; i < numberOfModels; i++){
            inducedOrder.add(new ModelAndIndex(models.get(i), i, p));
        }
        
        if(this.comparator != IDENTITY){
            inducedOrder.sort(this.comparator);
        }
        
        int combinations = inducedOrder.size() - 1;
        LOGGER.info("Match {} one to one matches", combinations);
        Alignment finalAlignment = new Alignment();
        
        if(firstVsRest){
            Set<Object> left = inducedOrder.get(0).getModelRepresentations();
            for(int i = 1; i < inducedOrder.size(); i++){
                Set<Object> right = inducedOrder.get(i).getModelRepresentations();
                LOGGER.info("Match combination {} out of {}", i, combinations);
                AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(this.oneToOneMatcher, left, right, 
                        DispatcherHelper.deepCopy(inputAlignment), DispatcherHelper.deepCopy(parameters));
                Alignment a = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
                if(a == null){
                    LOGGER.warn("Tranformation of the alignment was not succesfull. One matching alignment will not be in the result.");
                }else{
                    finalAlignment.addAll(a);
                }
            }
        }else{
            for(int i = 0; i < inducedOrder.size() - 1; i++){
                Set<Object> left = inducedOrder.get(i).getModelRepresentations();
                Set<Object> right = inducedOrder.get(i+1).getModelRepresentations();
                LOGGER.info("Match combination {} out of {}", i + 1, combinations);
                AlignmentAndParameters alignmentAndPrameters = GenericMatcherCaller.runMatcherMultipleRepresentations(this.oneToOneMatcher, left, right, 
                        DispatcherHelper.deepCopy(inputAlignment), DispatcherHelper.deepCopy(parameters));
                Alignment a = TypeTransformerRegistry.getTransformedObject(alignmentAndPrameters.getAlignment(), Alignment.class);
                if(a == null){
                    LOGGER.warn("Tranformation of the alignment was not succesfull. One matching alignment will not be in the result.");
                }else{
                    finalAlignment.addAll(a);
                }
            }
        }
        return new AlignmentAndParameters(finalAlignment, parameters);
    }
    
    @Override
    public boolean needsTransitiveClosureForEvaluation(){
        return true;
    }
    
}
