package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import com.github.liblevenshtein.transducer.Algorithm;
import com.github.liblevenshtein.transducer.ITransducer;
import com.github.liblevenshtein.transducer.factory.TransducerBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matcher which uses different String Matching approaches (stored in PropertySpecificStringProcessing) with a specific confidence.
 * Multiple normalization are possible as shown below.
 * The highest confidence is taken at the end.
 * <pre>{@code 
 * Function<String, Object> equality = (text) -> text;
 * Function<String, Object> lowercase = (text) -> text.toLowerCase();
 * ScalableStringProcessingMatcher matcherOne = new ScalableStringProcessingMatcher(Arrays.asList(
 *              new PropertySpecificStringProcessing(equality, 1.0, RDFS.label),
 *              new PropertySpecificStringProcessing(lowercase, 0.9, RDFS.label)
 *              new PropertySpecificStringProcessing(equality, 0.7, SKOS.altLabel),
 *              new PropertySpecificStringProcessing(lowercase, 0.6, SKOS.altLabel)
 * ));
 * }</pre>
 */
public class ScalableStringProcessingMatcher extends MatcherYAAAJena{

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalableStringProcessingMatcher.class);
    
    protected Iterable<PropertySpecificStringProcessing> processingElements;
    protected Set<ValueExtractor> usedValueExtractors;
    
    protected boolean matchClasses = true;
    protected boolean matchProperties = true;
    protected boolean matchInstances = true;    
    protected boolean earlyStopping = true;
    
    
    public ScalableStringProcessingMatcher(Iterable<PropertySpecificStringProcessing> processingElements, boolean earlyStopping){
        this.earlyStopping = earlyStopping;
        this.processingElements = processingElements;
        this.usedValueExtractors = new HashSet<>();
        for(PropertySpecificStringProcessing p: processingElements){
            this.usedValueExtractors.addAll(p.getValueExtractors());
        }
    }
    
    public ScalableStringProcessingMatcher(Iterable<PropertySpecificStringProcessing> processingElements){
        this(processingElements, true);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired() && matchClasses){
            LOGGER.debug("Match classes");
            matchResources(source.listClasses(), target.listClasses(), inputAlignment);  
        }
        if((OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired()) && matchProperties){
            LOGGER.debug("Match properties");
            matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);
        }
        if(OaeiOptions.isMatchingInstancesRequired() && matchInstances){
            LOGGER.debug("Match instances");
            matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
        }
        LOGGER.debug("Finished");
        return inputAlignment;
    }
    
    
    public void matchResources(Iterator<? extends Resource> sourceResources, Iterator<? extends Resource> targetResources, Alignment alignment) {
        //processing -> tokens/ids -> (list of resources)
        Map<PropertySpecificStringProcessing, Map<Object, Set<String>>> index = new HashMap<>();
        

        //source
        while (sourceResources.hasNext()) {
            Resource source = sourceResources.next();
            if(source.isURIResource() == false)
                continue;
            String sourceURI = source.getURI();            
            Map<ValueExtractor, Set<String>> valueMap = extractAllValues(source);
            for(PropertySpecificStringProcessing processing : this.processingElements){
                Map<Object, Set<String>> tokenIndex = index.computeIfAbsent(processing, k->new HashMap<>());
                for(String sourceLabels : getLiterals(processing, valueMap)){
                    if(StringUtils.isBlank(sourceLabels))
                        continue;
                    Object o = processing.getProcessing().apply(sourceLabels);
                    if(isObjectEmpty(o))
                        continue;
                    tokenIndex.computeIfAbsent(o, k-> new HashSet<>()).add(sourceURI);
                }
            }
        }
        
        Map<PropertySpecificStringProcessing, ITransducer> levenshteinIndex = buildLevenshteinIndex(index);
        
        while (targetResources.hasNext()) {
            Resource target = targetResources.next();
            if(target.isURIResource() == false)
                continue;
            String targetURI = target.getURI();
            
            Map<ValueExtractor, Set<String>> valueMap = extractAllValues(target);
            for(PropertySpecificStringProcessing processing : this.processingElements){
                Map<Object, Set<String>> tokenIndex = index.get(processing);
                if(tokenIndex == null)
                    continue;
                boolean findMatch = false;
                for(String targetLabel : getLiterals(processing, valueMap)){
                    if(StringUtils.isBlank(targetLabel))
                        continue;
                    Object o = processing.getProcessing().apply(targetLabel);
                    
                    Set<Object> searchObjects = new HashSet<>();
                    if(o == null)
                        continue;
                    searchObjects.add(o);
                    if(o instanceof String){
                        String oString = (String)o;
                        if(StringUtils.isBlank(oString))
                            continue;
                        ITransducer transducer = levenshteinIndex.get(processing);
                        if(transducer != null){
                            for(Object s : transducer.transduce(oString)){
                                searchObjects.add(s);
                            }
                        }
                    }
                    for(Object object : searchObjects){
                        for(String sourceURI : tokenIndex.getOrDefault(object, new HashSet<>())){
                            findMatch = true;
                            alignment.addOrUseHighestConfidence(sourceURI, targetURI, processing.getConfidence());
                        }
                    }
                }
                if(findMatch && earlyStopping)
                    break;
            }
        }
    }
    
    private Map<PropertySpecificStringProcessing, ITransducer> buildLevenshteinIndex(Map<PropertySpecificStringProcessing,Map<Object, Set<String>>> index){
        //choose all processing with levenshtein
        Set<PropertySpecificStringProcessing> levenshteinProcessings = new HashSet();
        for(PropertySpecificStringProcessing processing : this.processingElements){
            if(processing.getMaxLevenshteinDistance() > 0){
                levenshteinProcessings.add(processing);
            }
        }
        
        Map<PropertySpecificStringProcessing, ITransducer> levenshteinIndex = new HashMap<>();
        for(PropertySpecificStringProcessing processsing : levenshteinProcessings){
            List<String> texts = new ArrayList<>();
            int minLength = processsing.getMinLengthForLevenshtein();
            for(Object o : index.get(processsing).keySet()){
                if(o instanceof String){
                    String text = (String)o;
                    if(text.length() > minLength){
                        texts.add(text);
                    }
                }
            }
            ITransducer transducer = new TransducerBuilder()
                .dictionary(texts)
                .isSorted(false)
                .algorithm(Algorithm.TRANSPOSITION)
                .defaultMaxDistance(processsing.getMaxLevenshteinDistance())
                .includeDistance(false)
                .build();
            levenshteinIndex.put(processsing, transducer);
        }
        return levenshteinIndex;
    }
    
    
    protected Set<String> getLiterals(PropertySpecificStringProcessing processing, Map<ValueExtractor, Set<String>> valueMap){
        Set<String> values = new HashSet<>();
        for(ValueExtractor extractor : processing.getValueExtractors()){
            values.addAll(valueMap.get(extractor));
        }
        return values;
    }
    
    protected Map<ValueExtractor,Set<String>> extractAllValues(Resource r){
        Map<ValueExtractor, Set<String>> literals = new HashMap<>();
        for(ValueExtractor p : this.usedValueExtractors){
            literals.put(p, p.extract(r));
        }
        return literals;
    }
    
        
    protected boolean isObjectEmpty(Object o){
        if(o == null)
            return true;
        if(o instanceof String){
            if(StringUtils.isBlank((String)o))
                return true;
        }
        return false;
    }

    public boolean isMatchClasses() {
        return matchClasses;
    }

    public void setMatchClasses(boolean matchClasses) {
        this.matchClasses = matchClasses;
    }

    public boolean isMatchProperties() {
        return matchProperties;
    }

    public void setMatchProperties(boolean matchProperties) {
        this.matchProperties = matchProperties;
    }

    public boolean isMatchInstances() {
        return matchInstances;
    }

    public void setMatchInstances(boolean matchInstances) {
        this.matchInstances = matchInstances;
    }

    public boolean isEarlyStopping() {
        return earlyStopping;
    }

    public void setEarlyStopping(boolean earlyStopping) {
        this.earlyStopping = earlyStopping;
    }
}
