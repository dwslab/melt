package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

import de.uni_mannheim.informatik.dws.melt.matching_base.DataStore;
import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorProperty;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * Extracts corpus dependent stopwords from instances, classes and properties.
 */
public class StopwordExtraction extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopwordExtraction.class);
    
    /**
     * Literal extractors to choose which literal/properties should be used.
     */
    private List<TextExtractor> valueExtractors;
    /**
     * Tokenizer function.
     */
    private Function<String, Collection<String>> tokenizer;
    /**
     * If true, counts only tokens only once (even if it appears in one literal multiple times or multiple times in different literals).
     */
    private boolean countDistinctTermsPerResource;
    /**
     * Extracts the N top most tokensa s stopwords.
     */
    private int topNStopwords;
    /**
     * The percentage how many resources this token must have to count as a stopword. Range between zero and one.
     */
    private double stopwordsPercentage;

    /**
     * Extracts the stopwords based on two criteria.
     * 1) top most occuring tokens 2) percentage.
     * It will stop if one of the two critia is fullfilled.
     * @param tokenizer tokenizer
     * @param countDistinctTermsPerResource If true, counts only tokens only once (even if it appears in one literal multiple times or multiple times in different literals).
     * @param topNStopwords how many stopswords to extract
     * @param stopwordsPercentage the percentage of how often a token should appear.
     * @param valueExtractors Literal extractors to choose which literal/properties should be used.
     */
    public StopwordExtraction(Function<String, Collection<String>> tokenizer, boolean countDistinctTermsPerResource, int topNStopwords, double stopwordsPercentage, List<TextExtractor> valueExtractors) {
        this.valueExtractors = valueExtractors;
        this.tokenizer = tokenizer;
        this.countDistinctTermsPerResource = countDistinctTermsPerResource;
        this.topNStopwords = topNStopwords;
        this.stopwordsPercentage = stopwordsPercentage;
    }
    
    /**
     * Extracts the stopwords based on two criteria.
     * 1) top most occuring tokens 2) percentage.
     * It will stop if one of the two critia is fullfilled.
     * @param tokenizer tokenizer
     * @param countDistinctTermsPerResource If true, counts only tokens only once (even if it appears in one literal multiple times or multiple times in different literals).
     * @param topNStopwords how many stopswords to extract
     * @param stopwordsPercentage the percentage of how often a token should appear.
     * @param valueExtractors Literal extractors to choose which literal/properties should be used.
     */
    public StopwordExtraction(Function<String, Collection<String>> tokenizer, boolean countDistinctTermsPerResource, int topNStopwords, double stopwordsPercentage, TextExtractor... valueExtractors) {
        this(tokenizer, countDistinctTermsPerResource, topNStopwords, stopwordsPercentage, Arrays.asList(valueExtractors));
    }
    
    /**
     * Extracts the stopwords based on the top most occuring tokens.
     * @param tokenizer tokenizer
     * @param topNStopwords how many stopswords to extract
     * @param properties the properies which should be used for extracting the literals (text).
     */
    public StopwordExtraction(Function<String, Collection<String>> tokenizer, int topNStopwords, Property... properties){
        this(tokenizer, true, topNStopwords, 0.0d, TextExtractorProperty.wrapExtractor(properties));
    }
    
    /**
     * Extracts the stopwords based on the percentage (should be between 0 and 1).
     * E.g. a token is a stopword if it occurs in more than 3 percent (0.03) of all resources.
     * @param tokenizer tokenizer
     * @param stopwordsPercentage the percentage of how often a token should appear.
     * @param properties the properies which should be used for extracting the literals (text).
     */
    public StopwordExtraction(Function<String, Collection<String>> tokenizer, double stopwordsPercentage, Property... properties){
        this(tokenizer, true, 0, stopwordsPercentage, TextExtractorProperty.wrapExtractor(properties));
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired()){
            storeExtractedStopwords(source.listClasses(), "stopwords_source_classes");
            storeExtractedStopwords(target.listClasses(), "stopwords_target_classes");
        }    
        if(OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired()){
            storeExtractedStopwords(source.listAllOntProperties(), "stopwords_source_properties");
            storeExtractedStopwords(target.listAllOntProperties(), "stopwords_target_properties");
        }
        if(OaeiOptions.isMatchingInstancesRequired()){
            storeExtractedStopwords(source.listIndividuals(), "stopwords_source_instances");
            storeExtractedStopwords(target.listIndividuals(), "stopwords_target_instances");
        }
        return inputAlignment;
    }
    
    public void storeExtractedStopwords(Iterable<? extends Resource> resources, String key){
        DataStore.getGlobal().put(key, extractStopwords(resources));
    }
    
    public void storeExtractedStopwords(Iterator<? extends Resource> resources, String key){
        DataStore.getGlobal().put(key, extractStopwords(resources));
    }
    
    public Set<String> extractStopwords(Iterable<? extends Resource> resources){
        return extractStopwords(resources.iterator());
    }
    
    public Set<String> extractStopwords(Iterator<? extends Resource> resources){
        int countResources = 0;
        Map<String, Integer> tokenCounter = new HashMap<>();
        while (resources.hasNext()) {
            Resource res = resources.next();
            Set<String> extractedLiterals = new HashSet();
            for(TextExtractor extractor : this.valueExtractors){
                extractedLiterals.addAll(extractor.extract(res));
            }
            if(!extractedLiterals.isEmpty())
                countResources++;
            
            Collection<String> allTokens = new ArrayList();            
            for(String literal : extractedLiterals){
                if(literal.isEmpty())
                    continue;
                for(String token : this.tokenizer.apply(literal)){
                    if(!token.isEmpty()){
                        allTokens.add(token);
                    }
                }
            }
            if(countDistinctTermsPerResource)
               allTokens = new HashSet(allTokens);
            
            for(String token : allTokens){
                tokenCounter.put(token, tokenCounter.getOrDefault(token, 0) + 1);
            }
        }
        
        Set<String> finalStopwords = new HashSet();
        if(tokenCounter.isEmpty())
            return finalStopwords;
        
        List<Entry<String, Integer>> sortedTokenCount = tokenCounter.entrySet().stream()
                .sorted(Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
        
        LOGGER.debug("Sorted tokens (#resources: {}): {}", countResources, sortedTokenCount.stream().limit(30).collect(Collectors.toList()));
        
        for(Entry<String, Integer> tokenCount : sortedTokenCount){
            if(this.topNStopwords != 0 && finalStopwords.size() >= this.topNStopwords)
                break;
            double frequency = (double)tokenCount.getValue() / (double) countResources;
            if(this.stopwordsPercentage != 0.0d && frequency <= this.stopwordsPercentage)
                break;
            //TODO: check and or or
            finalStopwords.add(tokenCount.getKey());
        }
        LOGGER.debug("Extracted stopwords: {}", finalStopwords);
        
        return finalStopwords;
    }
    
}
