package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.OntologyCacheJena;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.instancelevel.MatchClassBasedOnInstances;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.instancelevel.SimInstanceMetric;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.structurelevel.MatchPropBasedOnClass;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class MatchClassBasedOnInstancesTest {
    private static final String namespaceSource = "http://ontmatching.dws.informatik.uni-mannheim.de/source/";
    private static final String namespaceTarget = "http://ontmatching.dws.informatik.uni-mannheim.de/target/";
    
    
    private OntModel source = generate(namespaceSource, 20);
    private OntModel target = generate(namespaceTarget, 50);
    
    @Test
    void axioms() throws IOException {
        Alignment inputAlignment = new Alignment();
        //1 instance is matched
        inputAlignment.add(namespaceSource + "instance0", namespaceTarget + "instance0", 0.5);
        
        assertCorrespondenceBetweenClass(0.5, SimInstanceMetric.BASE, 0.0, inputAlignment, 1.0);
        assertEmpty(0.5, SimInstanceMetric.BASE, 0.6, inputAlignment);        
        assertCorrespondenceBetweenClass(1.0, SimInstanceMetric.MATCH_BASED, 0.0, inputAlignment, 1.0);
        
    }

    
    private void assertCorrespondenceBetweenClass(double metricConfidence, SimInstanceMetric metric, double instanceMinConfidence, Alignment inputAlignment, double expectedConfidence) throws IOException{
        
        Alignment a = new MatchClassBasedOnInstances(metricConfidence, metric, instanceMinConfidence).getClassMatches(source, target, inputAlignment);
        
        assertEquals(1, a.size());
        Correspondence c = a.iterator().next();
        assertEquals(namespaceSource + "myclass", c.getEntityOne());
        assertEquals(namespaceTarget + "myclass", c.getEntityTwo());
        assertEquals(expectedConfidence, c.getConfidence());
    }
    
    private void assertEmpty(double metricConfidence, SimInstanceMetric metric, double instanceMinConfidence, Alignment inputAlignment) throws IOException{
        Alignment a = new MatchClassBasedOnInstances(metricConfidence, metric, instanceMinConfidence).getClassMatches(source, target, inputAlignment);
        assertEquals(0, a.size());
    }
    
    private OntModel generate(String namespace, int instances){
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);        
        OntClass clazz = model.createClass(namespace + "myclass");        
        for(int i=0; i < instances; i++){
            Resource r = model.createResource(namespace + "instance" + i);
            r.addProperty(RDF.type, clazz);       
        }
        return model;
    }
    
}
