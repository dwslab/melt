package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates a CSV (first element is source - all others are synonyms) based on a
 * <a href="http://kaiko.getalp.org/about-dbnary/">DBnary</a> dump file.
 * You can read more about DBnary in the following article:
 * <a href="http://www.semantic-web-journal.net/system/files/swj648.pdf">Sérasset Gilles (2014).
 * DBnary: Wiktionary as a Lemon-Based Multilingual Lexical Resource in RDF. to appear in Semantic Web Journal
 * (special issue on Multilingual Linked Open Data).</a>
 */
public class WiktionarySynsetCSV {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiktionarySynsetCSV.class);
    
    private static final String dbnary = "http://kaiko.getalp.org/dbnary#";
    
    private static final Property describes = ResourceFactory.createProperty(dbnary +"describes");
    private static final Property synonym = ResourceFactory.createProperty(dbnary +"synonym");
    private static final Property sense = ResourceFactory.createProperty("http://www.w3.org/ns/lemon/ontolex#sense");
    private static final Resource Page = ResourceFactory.createResource(dbnary +"Page");
    
    /*
    public static void main(String[] args){
        //generateCSVFile("en_dbnary_ontolex.ttl", "synoyms.csv");
        
        //TDB packed
        //Model m = TDBFactory.createDataset("dbnary_tdb").getDefaultModel();
        //generateCSVFile(m, new File("synoyms.csv"));
        
        //Map<String, Set<String>> map = loadCsvFile("synoyms.csv");
        //LOGGER.info("Size of map: {}", map.size());
        //LOGGER.info("Synonym of blindfish: {}", map.getOrDefault("blindfish", new HashSet()));
    }
    */
    
    
    /**
     * Extracts synonyms from a dbnary dump and writes the synonyms to a csv file.
     * @param path path to the the dbnary dump
     * @param csvFile csv file which contains the synonyms as source, syn1, syn2, syn3
     */
    public static void generateCSVFile(String path, String csvFile){
        Model m = ModelFactory.createDefaultModel();
        LOGGER.info("Load dbnary file");
        m.read(path);
        LOGGER.info("Process dbnary model");
        generateCSVFile(m, new File(csvFile));
    }
    /**
     * Extracts synsonyms from a jena model of the dbnary dump and writes the synonyms to a csv file
     * @param m a jena model of the dbnary dump (can be tdb packed or not)
     * @param csvFile csv file which contains the synonyms as source, syn1, syn2, syn3
     */
    public static void generateCSVFile(Model m, File csvFile){
        try (CSVPrinter printer = CSVFormat.DEFAULT.print(csvFile, StandardCharsets.UTF_8)){
            ResIterator pages = m.listResourcesWithProperty(RDF.type, Page);
            while(pages.hasNext()){
                Resource r = pages.next();
                //LOGGER.info("Process page {}", r.getURI());
                if(r.getURI().substring(31).startsWith("eng/") == false){
                    LOGGER.info("Found non englisch page in wiktionary dump - page {} is skipped", r.getURI());
                    continue;
                }
                String sourceWord = getLemmaFromURI(r.getURI());
                
                Set<String> synonyms = new HashSet();                
                NodeIterator descriptionConcepts = m.listObjectsOfProperty(r, describes);
                while(descriptionConcepts.hasNext()){
                    RDFNode descriptionConcept = descriptionConcepts.next();
                    if(descriptionConcept.isResource() == false){
                        LOGGER.warn("Skipping descriptionConcept which is not a resource");
                        continue;
                    }
                    addSynonyms(synonyms, descriptionConcept.asResource());
                    
                    //continue to look for senses
                    NodeIterator senses = m.listObjectsOfProperty(descriptionConcept.asResource(), sense);
                    while(senses.hasNext()){
                        RDFNode senseNode = senses.next();
                        if(senseNode.isResource() == false){
                            LOGGER.warn("Skipping sense which is not a resource");
                            continue;
                        }
                        addSynonyms(synonyms, senseNode.asResource());
                    }
                }
                if(synonyms.size() > 0){
                    List<String> record = new ArrayList();
                    record.add(sourceWord);
                    record.addAll(synonyms);
                    printer.printRecord(record);
                }
            }
        }catch (IOException ex) {
            LOGGER.info("Could not write to file", ex);
        }
    }
    
    
    public static Map<String, Set<String>> loadCsvFile(String csvFile){
        return loadCsvFile(new File(csvFile));
    }
    
    /**
     * Load the csv file and returns a map with key as original word and value as a set of synonyms.
     * @param csvFile the csv file to load
     * @return map of original word to synonyms
     */
    public static Map<String, Set<String>> loadCsvFile(File csvFile){
        Map<String, Set<String>> synset = new HashMap();
        try(Reader in = new InputStreamReader(new FileInputStream(csvFile), "UTF-8")){
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                if(row.size() > 1){
                    Set<String> synonyms = new HashSet();
                    Iterator<String> i = row.iterator();
                    String source = i.next();
                    while(i.hasNext()){
                        synonyms.add(i.next());
                    }
                    synset.put(source, synonyms);
                }
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not read csv file", ex);
        }
        return synset;
    }
    
    /**
     * Load a csv file of synonyms and returns a map where the key is a word/phrase and the value is the corresponding sysnset representative.
     * Usually this is named as synset_id.
     * If a word appears multiple times, then multiple values appear in the set.
     * @param csvFile the csv file to load
     * @return map where the key is a word/phrase and the value is the corresponding sysnset representative 
     */
    public static Map<String, Set<String>> loadCsvFileAsReplacementMapWithAllKeys(File csvFile){
        return loadCsvFileAsReplacementMapWithAllKeys(csvFile, cell->cell.toLowerCase(Locale.ENGLISH).trim());
    }
    
    /**
     * Load a csv file of synonyms and returns a map where the key is a word/phrase and the value is the corresponding sysnset representative.
     * Usually this is named as synset_id.
     * If a word appears multiple times, then multiple values appear in the set.
     * @param csvFile the csv file to load
     * @param preprocessing the function which is applied to every synonym
     * @return map where the key is a word/phrase and the value is the corresponding sysnset representative 
     */
    public static Map<String, Set<String>> loadCsvFileAsReplacementMapWithAllKeys(File csvFile, Function<String, String> preprocessing){
        Map<String, Set<String>> wordToSynset = new HashMap();
        try(Reader in = new InputStreamReader(new FileInputStream(csvFile), "UTF-8")){
            int lineNumber = 1;
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                for(String cell : row){
                    String cellNormalized = preprocessing.apply(cell);
                    if(cellNormalized.length() > 0){
                        wordToSynset.computeIfAbsent(cellNormalized, __->new HashSet<>()).add("synset_" + lineNumber);
                    }
                }
                lineNumber++;
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not read csv file", ex);
        }
        return wordToSynset;
    }
    
    /**
     * Load a csv file of synonyms and returns a map where the key is a word/phrase and the value is the corresponding sysnset representative.
     * Usually this is named as synset_id.
     * If a word appears multiple times, then only the last occurance is used.
     * Therefore it is recommendable to load a transitive closure of a synonym file.
     * @param csvFile the csv file to load
     * @return map where the key is a word/phrase and the value is the corresponding sysnset representative 
     */
    public static Map<String, String> loadCsvFileAsReplacementMap(File csvFile){
        Map<String, String> wordToSynset = new HashMap();
        try(Reader in = new InputStreamReader(new FileInputStream(csvFile), "UTF-8")){
            int lineNumber = 1;
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                for(String cell : row){
                    String cellNormalized = cell.trim();
                    if(cellNormalized.length() > 0){
                        wordToSynset.put(cellNormalized, "synset_" + lineNumber);
                    }
                }
                lineNumber++;
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not read csv file", ex);
        }
        return wordToSynset;
    }
    
    /**
     * Add synonyms from a resource which has a synonyms relation
     * @param synonyms the set in which all new synoyms are added
     * @param r resource
     */
    private static void addSynonyms(Set<String> synonyms, Resource r) {
        StmtIterator statements = r.listProperties(synonym);
        while(statements.hasNext()){
            Statement s = statements.next();
            if(s.getObject().isURIResource()){
                synonyms.add(getLemmaFromURI(s.getObject().asResource().getURI()));
            }
        }
    }
    
    
    /**
     * Given a resource URI, this method will transform it to a lemma.
     *
     * @param uri Resource URI to be transformed.
     * @return Lemma.
     */
    private static String getLemmaFromURI(String uri) {
        return uri.substring(35, uri.length()).replace("_", " ");
    }
    
}
