package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;

/**
 * This class represents a lookup service for Semantic Web prefixes.
 */
public class PrefixLookup {
    
    /**
     * Default Prefix Lookup (independent of a particular ontology). 
     */
    public static PrefixLookup DEFAULT = new PrefixLookup(initPrefixMapping());
    
    private PrefixMapping mapping;    
    
    public PrefixLookup(PrefixMapping mapping){
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
    
    private PrefixMapping getMappingFromOntModel(OntModel m){
        Set<String> uris = new HashSet<>();
        ResIterator i =m.listSubjects();
        while(i.hasNext()){
            Resource r = i.next();
            if(r.isURIResource() == false)
                continue;
            uris.add(r.getURI());
        }
        PrefixMapping mapping = initPrefixMapping();
        mapping.setNsPrefixes(m);
        return getMappingFromUris(uris, mapping);
    }
    
    
    private PrefixMapping getMappingFromUris(Set<String> uris, PrefixMapping initialMapping){
        Set<String> knownPrefixes = new HashSet<>(initialMapping.getNsPrefixMap().values());
        HashMap<String, Integer> distribution = new HashMap<>();
        for(String uri : uris){
            String key = getBaseUri(uri);
            if(knownPrefixes.contains(key) == false)
                distribution.put(key, distribution.getOrDefault(key, 0) + 1);
        }
        
        List<Entry<String, Integer>> list = new ArrayList<>(distribution.entrySet());
        list.sort(Comparator.comparing(Entry::getValue));
        Collections.reverse(list);
        
        if(list.isEmpty()){
            return initialMapping;
        }
        String commonPrefix = list.get(0).getKey();
        for(Entry<String, Integer> s: list.subList(1, list.size())){
            String newCommonPrefix = greatestCommonPrefix(commonPrefix, s.getKey());
            if(newCommonPrefix.length() > 7){
                commonPrefix = newCommonPrefix;
            }
        }
        
        /*
        Map<String, Integer> bestCommonPrefix = new HashMap<>();
        for(Entry<String, Integer> entry : distribution.entrySet()){
            
            String[] entry.getKey().split("/")
                    
            for(int i=0; i<entry.getKey().length(); i++){
                String key = entry.getKey().substring(0, i);
                bestCommonPrefix.put(key, distribution.getOrDefault(key, 0) + entry.getValue());
            }
        }
        
        int longestPrefix = distribution.keySet().stream().mapToInt(String::length).max().orElse(0);
        int highestValue = bestCommonPrefix.values().stream().mapToInt(x->x).max().orElse(0);
        double sweetSpot = 0;
        for(Entry<String, Integer> entry : bestCommonPrefix.entrySet()){
            
            
            for(int i=0; i<entry.getKey().length(); i++){
                String key = entry.getKey().substring(0, i);
                bestCommonPrefix.put(key, distribution.getOrDefault(key, 0) + entry.getValue());
            }
        }
        */

        mapping.setNsPrefix(":", commonPrefix);
        return mapping;
    }
    
    
    private String greatestCommonPrefix(String a, String b) {
        int minLength = Math.min(a.length(), b.length());
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return a.substring(0, i);
            }
        }
        return a.substring(0, minLength);
    }
    
    
    public PrefixMapping getPrefixMapping(){
        return mapping;
    }
    
    public Map<String, String> getPrefixMap(){
        return mapping.getNsPrefixMap();
    }
    
    
    /**
     * Given a URI, this method returns the prefixed URI. If the prefix is available in mapping, the short name will
     * be returned (e.g. 'daml:someConcept' rather than 'http://www.daml.org/2001/03/daml+oil#someConcept').
     * If the prefix is not available in the mapping, the full string will be returned.
     * @param uriString The URI of which the prefix shall be obtained.
     * @return Prefix as String.
     */
    public String getPrefix(String uriString){
        int idx = splitIdx(uriString);
        if (idx >= 0)
        {
            String baseUriString = uriString.substring(0, idx + 1);
            String prefix = mapping.getNsURIPrefix(baseUriString);
            if (prefix != null)
            {
                return prefix + ':' + uriString.substring(idx + 1);
            } else return uriString;
        }
        return uriString;
    }
    
    /**
     * Given a URI String, the base will be determined (in other word, the fragment will be cut).
     * @param uriString URI of which the base shall be determined.
     * @return The base URI as String.
     */
    public static String getBaseUri(String uriString){
        int idx = splitIdx(uriString);
        if (idx >= 0)
        {
            String baseUriString = uriString.substring(0, idx + 1);
            return baseUriString;
        }
        return uriString;
    }
    
    /**
     * Obtains the index of the hashkey or last slash in order to determine the fragment/prefix of a URI
     * @param uriString URI string.
     * @return Position of splitting character.
     */
    private static int splitIdx(String uriString)
    {
        int idx = uriString.lastIndexOf('#') ;
        if ( idx >= 0 )
            return idx ;
        idx = uriString.lastIndexOf('/') ;
        return idx ;
    }
    
    
    private static PrefixMapping initPrefixMapping(){
        return PrefixMapping.Factory.create()
            //jena standard 
            .setNsPrefix( "rdfs",  "http://www.w3.org/2000/01/rdf-schema#" )
            .setNsPrefix( "rdf",   "http://www.w3.org/1999/02/22-rdf-syntax-ns#" )
            .setNsPrefix( "dc",    "http://purl.org/dc/elements/1.1/" )
            .setNsPrefix( "owl",   "http://www.w3.org/2002/07/owl#" )
            .setNsPrefix( "xsd",   "http://www.w3.org/2001/XMLSchema#" )
            //jena extended
            .setNsPrefix( "rss",   "http://purl.org/rss/1.0/" )
            .setNsPrefix( "vcard", "http://www.w3.org/2001/vcard-rdf/3.0#" )
            .setNsPrefix( "ja",    "http://jena.hpl.hp.com/2005/11/Assembler#")
            .setNsPrefix( "eg",    "http://www.example.org/" )
            
            //general prefixes
            .setNsPrefix( "skos",  "http://www.w3.org/2004/02/skos/core#" )
            .setNsPrefix( "prov",  "http://www.w3.org/ns/prov#" )
            .setNsPrefix( "dbr",   "http://dbpedia.org/resource/" )
            .setNsPrefix( "dbo",   "http://dbpedia.org/ontology/" )
            .setNsPrefix( "wd",    "http://www.wikidata.org/entity/" )
            .setNsPrefix( "dct",    "http://purl.org/dc/terms/" )
            .setNsPrefix( "foaf",  "http://xmlns.com/foaf/0.1/" )
            .setNsPrefix( "schema","http://schema.org/" )
            .setNsPrefix( "dul",   "http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#" )
            
            //OAEI related:
            .setNsPrefix( "p1","http://www.owl-ontologies.com/assert.owl#" )
            .setNsPrefix( "xsp","http://www.owl-ontologies.com/2005/08/07/xsp.owl#" )
            .setNsPrefix( "oboInOwl","http://www.geneontology.org/formats/oboInOwl#" )
            .setNsPrefix( "oboRel","http://www.obofoundry.org/ro/ro.owl#" )
            .setNsPrefix( "snomed","http://www.ihtsdo.org/snomed#" )
            .setNsPrefix( "swrlb","http://www.w3.org/2003/11/swrlb#" )
            .setNsPrefix( "swrl","http://www.w3.org/2003/11/swrl#" )
            .setNsPrefix( "protege","http://protege.stanford.edu/plugins/owl/protege#" )
            .setNsPrefix( "daml","http://www.daml.org/2001/03/daml+oil#" )
            .setNsPrefix( "Thesaurus","http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#" )

            // OAEI Track related:
            .setNsPrefix("cmt", "http://cmt#")
            .setNsPrefix("conference", "http://conference#")
            .setNsPrefix("edas", "http://edas#")
            .setNsPrefix("sigkdd", "http://sigkdd#")
            .setNsPrefix("confof", "http://confOf#")
            .setNsPrefix("ekaw", "http://ekaw#")
            .setNsPrefix("iasted", "http://iasted#")
            .setNsPrefix("mouse", "http://mouse.owl#")
            .setNsPrefix("human", "http://human.owl#");

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
