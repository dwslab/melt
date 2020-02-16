package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.elementlevel;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.OaeiOptions;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Matches resource A (source) to B (target) iff they have at least one label in the same synset.
 * The text used for the resources can be defined (e.g. rdfs:label etc).
 * The processing can also be adjusted by subclassing this class and override method processString.
 */
public class SynonymTextMatcher extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynonymTextMatcher.class);
    
    private Map<String, Long> synonymSet;
    private Collection<Property> properties;
    
    
    public SynonymTextMatcher(Map<String, Long> synonymSet, Collection<Property> properties){
        this.synonymSet = synonymSet;
        this.properties = properties;
    }
    
    public SynonymTextMatcher(Map<String, Long> synonymSet, Property... properties){
        this.synonymSet = synonymSet;
        this.properties = Arrays.asList(properties);
    }
    
    public SynonymTextMatcher(File csvFile, Collection<Property> properties){
        this.synonymSet = parseSynonymCsvFile(csvFile);
        this.properties = properties;
    }

    public SynonymTextMatcher(File csvFile, Property... properties){
        this(csvFile, Arrays.asList(properties));
    }
    
    public SynonymTextMatcher(File csvFile){
        this(csvFile, Arrays.asList(RDFS.label));
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired()){
            LOGGER.info("SynonymMatcher - match classes");
            matchResources(source.listClasses(), target.listClasses(), inputAlignment);    
        }                
        if(OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired()){
            LOGGER.info("SynonymMatcher - match properties");
            matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);      
        }              
        if(OaeiOptions.isMatchingInstancesRequired()){
            LOGGER.info("SynonymMatcher - match instances");
            matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
        }
        LOGGER.info("SynonymMatcher - finished matching");
        return inputAlignment;
    }
    
    
    
    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        Map<Long, Set<String>> synID2URI = new HashMap<>();
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            String sourceURI = source.getURI();
            for(String sourceText : getStringRepresentations(source)){
                Long synID = this.synonymSet.get(sourceText);
                if(synID != null){
                    Set<String> uris = synID2URI.get(synID);
                    if(uris == null){
                        uris = new HashSet<>();
                        synID2URI.put(synID, uris);
                    }
                    uris.add(sourceURI);
                }
            }
        }
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            for(String targetText : getStringRepresentations(target)){
                Long targetsynID = this.synonymSet.get(targetText);
                if(targetsynID != null){
                    Set<String> sourceURIs = synID2URI.get(targetsynID);
                    if(sourceURIs != null){
                        for(String sourceURI : sourceURIs){
                            alignment.add(sourceURI, target.getURI());
                        }
                    }
                }
            }
        }
    }
    
    protected Set<String> getStringRepresentations(Resource r){
        Set<String> values = new HashSet<>();
        if(r.isURIResource() == false)
            return values;
        for(Property p : properties){
            StmtIterator i = r.listProperties(p);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    String processed = processString(n.asLiteral().getLexicalForm());
                    if(StringUtils.isBlank(processed) == false)
                        values.add(processed);
                }
            }
        }
        return values;
    }
    
    /**
     * This method parse a synonym file formatted as a csv file.
     * Each line is a synsetand each cell in a line is a text. 
     * @param f teh file to be parsed
     * @return a map which maps a text to its synset id.
     */
    protected Map<String, Long> parseSynonymCsvFile(File f){
        Map<String, Long> map = new HashMap<>();
        if(f == null || f.exists() == false){
            LOGGER.warn("SynonymCsvFile is null or does not exist. Continue with empty synonym map.");
            return map;
        }
        LOGGER.info("Start loading synonym file");
        long synsetIndex = 0;
        try(Reader in = new FileReader(f)){
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                synsetIndex++;
                for(String text : row){
                    map.put(processString(text), synsetIndex);
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not parse synonym file", ex);
        }
        LOGGER.info("Finished loading synonym file");
        return map;
    }
    
        
    protected String processString(String text){
        return text.toLowerCase().trim();
    }
    
    
    /**
     * Parse a synset file which can be shared across different synonym text matchers.
     * @param f
     * @return 
     */
    public static Map<String, Long> parseCommonSynonymCsvFile(File f){
        Map<String, Long> map = new ConcurrentHashMap<>();
        if(f == null || f.exists() == false){
            LOGGER.warn("SynonymCsvFile is null or does not exist. Continue with empty synonym map.");
            return map;
        }
        LOGGER.info("Start loading synonym file");
        long synsetIndex = 0;
        try(Reader in = new FileReader(f)){
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                synsetIndex++;
                for(String text : row){
                    map.put(text.toLowerCase().trim(), synsetIndex);
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not parse synonym file", ex);
        }
        LOGGER.info("Finished loading synonym file");
        return map;
    }
}
