package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.elementlevel;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.OaeiOptions;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
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


public class SynonymTextMatcher extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(SynonymTextMatcher.class);
    
    private Map<String, Long> synonymSet;
    private Long synsetIndex; //initialized to 0
    private Collection<Property> properties;
    
    
    public SynonymTextMatcher(File csvFile, Collection<Property> properties){
        this.synsetIndex = 0L;
        this.synonymSet = new HashMap<>();
        this.properties = properties;
        parseSynonymCsvFile(csvFile);        
    }
    
    public SynonymTextMatcher(Collection<Property> properties){
        this(null, properties);
    }
    
    public SynonymTextMatcher(File csvFile, Property... properties){
        this(csvFile, Arrays.asList(properties));
    }
    
    public SynonymTextMatcher(Property... properties){
        this(null, Arrays.asList(properties));
    }
    
    public SynonymTextMatcher(File csvFile){
        this(csvFile, Arrays.asList(RDFS.label));
    }
    
    public SynonymTextMatcher(){
        this(null, Arrays.asList(RDFS.label));
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired())
            matchResources(source.listClasses(), target.listClasses(), inputAlignment);        
        if(OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired())
            matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);        
        if(OaeiOptions.isMatchingInstancesRequired())
            matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
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
    
    
        
    protected void parseSynonymCsvFile(File f){
        if(f == null || f.exists() == false){
            return;
        }        
        LOGGER.debug("Start loading synonym file");
        try(Reader in = new FileReader(f)){
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                addSynonymSet(row);
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not parse transitive closure", ex);
        }
        LOGGER.debug("Finished loading synonym file");
    }
    
    protected void addSynonymSet(Iterable<String> synset){
        this.synsetIndex++;
        for(String text : synset){
            this.synonymSet.put(processString(text), this.synsetIndex);
        }
    }
    
    protected String processString(String text){
        return text.toLowerCase().trim();
    }
    
    
}
