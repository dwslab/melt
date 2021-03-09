package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

/**
 * This class represents a lookup service for Semantic Web prefixes.
 */
public class PrefixLookup {


    /**
     * Default Prefix Lookup (independent of a particular ontology). 
     */
    public static PrefixLookup DEFAULT = new PrefixLookup(initPrefixMapping());
    
    public static PrefixLookup EMPTY = new PrefixLookup(new HashMap<>());
     
    private Map<String,String> mapping;
    //private Trie<String, String> mapping; // map from 
    
    public PrefixLookup(Map<String,String> mapping){
        this.mapping = mapping;
    }
    
    public PrefixLookup(Set<String> uris){
        this.mapping = getMappingFromUris(uris, initPrefixMapping());
    }
    
    public PrefixLookup(TestCase tc, boolean isleftPrefixMap){
        if(isleftPrefixMap){
            this.mapping = getMappingFromOntModel(tc.getSourceOntology(OntModel.class));
        }else{
            this.mapping = getMappingFromOntModel(tc.getTargetOntology(OntModel.class));
        }
    }
    
    public PrefixLookup(OntModel m){
        this.mapping = getMappingFromOntModel(m);
    }
    
    private Map<String,String> getMappingFromOntModel(OntModel m){
        Set<String> uris = new HashSet<>();
        ResIterator i =m.listSubjects();
        while(i.hasNext()){
            Resource r = i.next();
            if(r.isURIResource() == false)
                continue;
            uris.add(r.getURI());
        }
        Map<String, String> mapping = initPrefixMapping();
        
        mapping.putAll(m.getNsPrefixMap());
        return getMappingFromUris(uris, mapping);
    }
    
    
    private static Pattern splitBySlashOrHashtag = Pattern.compile("(?<=\\/|#)");
    private Map<String,String> getMappingFromUris(Set<String> uris, Map<String, String> initialMapping){
        Set<String> knownPrefixes = new HashSet<>(initialMapping.values());
        
        //map from possible prefixes to number of matched urls
        Map<String, Integer> distribution = new HashMap<>();
        for(String uri : uris){  
            String[] splits = splitBySlashOrHashtag.split(uri);
            StringBuilder prefixBuilder = new StringBuilder();
            // do not use the last part of url because it is always specific to that url and not a prefix
            for(int i=0; i<splits.length - 1; i++){ 
                prefixBuilder.append(splits[i]);
                String prefix = prefixBuilder.toString();
                distribution.put(prefix, distribution.getOrDefault(prefix, 0) + 1);
            }
        }
        distribution.remove("http://");
        distribution.remove("http:/");
        for(String s : knownPrefixes){
            distribution.remove(s);
        }
        
        if(distribution.isEmpty())
            return initialMapping;
        
        Entry<String, Integer> maxPrefix = Collections.max(distribution.entrySet(), Comparator.comparing(Entry::getValue));
        initialMapping.put("", maxPrefix.getKey());
        
        //List<Entry<String, Integer>> l = new ArrayList<>(distribution.entrySet());
        //l.sort(Comparator.comparing(Entry::getValue));
        //Collections.reverse(l);
        
        //maybe also normalize         
        //if(distribution.isEmpty())
        //    return initialMapping;
        //double longestPrefix = distribution.keySet().stream().mapToInt(String::length).max().orElse(0);
        //double highestValue = Collections.max(distribution.values());
        
        //List<Entry<String, Double>> prefixOrder = new ArrayList();
        //for(Entry<String, Integer> prefix : distribution.entrySet()){
        //    double normalizedPrefixLength = prefix.getKey().length() / longestPrefix;
        //    double normalizedValue = (double)prefix.getValue() / highestValue;
        //    prefixOrder.add(new SimpleEntry(prefix.getKey(), 1.2 * normalizedPrefixLength + normalizedValue));
        //}        
        //prefixOrder.sort(Comparator.comparing(Entry::getValue));
        //Collections.reverse(prefixOrder);
        
        
        //mapping.setNsPrefix(":", commonPrefix);
        return initialMapping;
    }
    
    public Map<String,String> getPrefixMap(){
        return mapping;
    }
    
    
    /**
     * Given a URI, this method returns the prefixed URI. If the prefix is available in mapping, the short name will
     * be returned (e.g. 'daml:someConcept' rather than 'http://www.daml.org/2001/03/daml+oil#someConcept').
     * If the prefix is not available in the mapping, the full string will be returned.
     * @param uriString The URI of which the prefix shall be obtained.
     * @return Prefixed URI as String.
     */
    public String getPrefix(String uriString){
        for(Entry<String,String> prefix : this.mapping.entrySet()){
            if(uriString.startsWith(prefix.getValue())){
                return uriString.replace(prefix.getValue(), prefix.getKey() + ":");
            }
        }
        return uriString;
    }
    
    private static Map<String,String> initPrefixMapping(){
        Map<String,String> map = new HashMap<>();
                
        //jena standard 
        map.put( "rdfs",  "http://www.w3.org/2000/01/rdf-schema#" );
        map.put( "rdf",   "http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
        map.put( "dc",    "http://purl.org/dc/elements/1.1/" );
        map.put( "owl",   "http://www.w3.org/2002/07/owl#" );
        map.put( "xsd",   "http://www.w3.org/2001/XMLSchema#" );
        //jena extended
        map.put( "rss",   "http://purl.org/rss/1.0/" );
        map.put( "vcard", "http://www.w3.org/2001/vcard-rdf/3.0#" );
        map.put( "ja",    "http://jena.hpl.hp.com/2005/11/Assembler#");
        map.put( "eg",    "http://www.example.org/" );

        //general prefixes
        map.put( "skos",  "http://www.w3.org/2004/02/skos/core#" );
        map.put( "prov",  "http://www.w3.org/ns/prov#" );
        map.put( "dbr",   "http://dbpedia.org/resource/" );
        map.put( "dbo",   "http://dbpedia.org/ontology/" );
        map.put( "wd",    "http://www.wikidata.org/entity/" );
        map.put( "dct",    "http://purl.org/dc/terms/" );
        map.put( "foaf",  "http://xmlns.com/foaf/0.1/" );
        map.put( "schema","http://schema.org/" );
        map.put( "dul",   "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#" );

        //OAEI related:
        map.put( "p1","http://www.owl-ontologies.com/assert.owl#" );
        map.put( "xsp","http://www.owl-ontologies.com/2005/08/07/xsp.owl#" );
        map.put( "oboInOwl","http://www.geneontology.org/formats/oboInOwl#" );
        map.put( "oboRel","http://www.obofoundry.org/ro/ro.owl#" );
        map.put( "snomed","http://www.ihtsdo.org/snomed#" );
        map.put( "swrlb","http://www.w3.org/2003/11/swrlb#" );
        map.put( "swrl","http://www.w3.org/2003/11/swrl#" );
        map.put( "protege","http://protege.stanford.edu/plugins/owl/protege#" );
        map.put( "daml","http://www.daml.org/2001/03/daml+oil#" );
        map.put( "Thesaurus","http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" );

        // OAEI Track related:
        map.put("cmt", "http://cmt#");
        map.put("conference", "http://conference#");
        map.put("edas", "http://edas#");
        map.put("sigkdd", "http://sigkdd#");
        map.put("confof", "http://confOf#");
        map.put("ekaw", "http://ekaw#");
        map.put("iasted", "http://iasted#");
        map.put("mouse", "http://mouse.owl#");
        map.put("human", "http://human.owl#");
        
        return map;
    }
    
    /*
    private static void analyseDefaultOAEIPrefixes(){
        List<TestCase> l = new ArrayList();
        l.addAll(TrackRepository.Anatomy.Default.getTestCases());
        l.addAll(TrackRepository.Conference.V1.getTestCases());
        l.addAll(TrackRepository.Largebio.V2016.FMA_NCI_SMALL.getTestCases());
        l.addAll(TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL.getTestCases());
        
        Map<String, String> prefixes = new HashMap<>();
        for(TestCase tc : l){
            prefixes.putAll(tc.getSourceOntology(OntModel.class).getNsPrefixMap());
            prefixes.putAll(tc.getTargetOntology(OntModel.class).getNsPrefixMap());
        }
        
        for(Entry<String, String> entry : prefixes.entrySet()){
            System.out.println(".setNsPrefix( \"" + entry.getKey() + "\",\"" + entry.getValue() + "\" )");
        }
    }
    
    public static void main(String[] args) {
        analyseDefaultOAEIPrefixes();
    }
    */
    
    
}
