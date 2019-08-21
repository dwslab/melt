package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.shared.PrefixMapping;

/**
 * This class represents a lookup service for Semantic Web prefixes.
 */
public class PrefixLookup {

    private static PrefixMapping mapping = initPrefixMapping();
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
            .lock();
    }
    
    public static PrefixMapping getPrefixMapping(){
        return mapping;
    }
    public static Map<String, String> getPrefixMap(){
        return mapping.getNsPrefixMap();
    }
    public static String getPrefix(String uriStr){
        int idx = splitIdx(uriStr) ;
        if (idx >= 0)
        {
            String x = uriStr.substring(0,idx+1);
            String prefix = mapping.getNsURIPrefix(x);
            if (prefix != null)
            {
                return prefix + ':' + uriStr.substring(idx+1);
            }
        }
        return uriStr;
    }
    
    private static int splitIdx(String uriStr)
    {
        int idx = uriStr.lastIndexOf('#') ;
        if ( idx >= 0 )
            return idx ;
        idx = uriStr.lastIndexOf('/') ;
        return idx ;
    }
    
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
    
    /*
    public static void main(String[] args) {
        analyseDefaultOAEIPrefixes();
    }
    */
}
