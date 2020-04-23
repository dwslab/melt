package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


public class ConfidenceFilterTest {
    
    @Test
    void testFilter() throws IOException {
        Alignment m = new Alignment();        
        for(double conf=0.1;conf<=1.0;conf+=0.1){
            for(int i=0;i<10;i++){
                m.add(
                        "http://exampleLeftWithALongURI/" + Double.toString(conf) + "_" +  Integer.toString(i), 
                        "http://exampleRightWithALongURI/" + Double.toString(conf) + "_" +  Integer.toString(i), 
                        conf);
            }
        }
        Alignment filtered = new ConfidenceFilter().filter(m, null, null);
        assertEquals(10, filtered.size());
    }
    
    @Test
    void testFilterDifferentThresholds() throws IOException {
        
        
        OntModel source = ModelFactory.createOntologyModel();
        source.createIndividual("http://left.com/Individual", OWL.Thing);
        source.createDatatypeProperty("http://left.com/DatatypeProperty");
        source.createObjectProperty("http://left.com/ObjectProperty");
        source.createClass("http://left.com/Class");
        
        OntModel target = ModelFactory.createOntologyModel();
        target.createIndividual("http://right.com/Individual", OWL.Thing);
        target.createDatatypeProperty("http://right.com/DatatypeProperty");
        target.createObjectProperty("http://right.com/ObjectProperty");
        target.createClass("http://right.com/Class");
        
        Correspondence clazz = new Correspondence("http://left.com/Class","http://right.com/Class", 0.6);
        Correspondence objectProperty = new Correspondence("http://left.com/ObjectProperty","http://right.com/ObjectProperty", 0.7);
        Correspondence datatypeProperty = new Correspondence("http://left.com/DatatypeProperty","http://right.com/DatatypeProperty", 0.8);
        Correspondence individual = new Correspondence("http://left.com/Individual","http://right.com/Individual", 0.9);
        
        List<Correspondence> all = Arrays.asList(clazz, objectProperty, datatypeProperty, individual);
        Alignment a = new Alignment(all);
        
        
        Alignment filtered = new ConfidenceFilter(0.5, 0.6, 0.7, 0.8, 1.0).filter(a, source, target);
        assertEquals(4, filtered.size());
        assertTrue(a.containsAll(all));
        
        filtered = new ConfidenceFilter(0.0, 1.0, 1.0, 1.0, 1.0).filter(a, source, target);
        assertEquals(1, filtered.size());
        assertTrue(a.contains(clazz));
        
        filtered = new ConfidenceFilter(1.0, 0.0, 1.0, 1.0, 1.0).filter(a, source, target);
        assertEquals(1, filtered.size());
        assertTrue(a.contains(objectProperty));
        
        filtered = new ConfidenceFilter(1.0, 1.0, 0.0, 1.0, 1.0).filter(a, source, target);
        assertEquals(1, filtered.size());
        assertTrue(a.contains(datatypeProperty));
        
        filtered = new ConfidenceFilter(1.0, 1.0, 1.0, 0.0, 1.0).filter(a, source, target);
        assertEquals(1, filtered.size());
        assertTrue(a.contains(individual));
    }
}
