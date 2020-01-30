package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter.CardinalityFilter;
import de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter.InstanceFilterBasedOnCommonProperties;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.io.IOException;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class InstanceFilterBasedOnCommonPropertiesTest {
    private static final String namespaceSource = "http://ontmatching.dws.informatik.uni-mannheim.de/source/";
    private static final String namespaceTarget = "http://ontmatching.dws.informatik.uni-mannheim.de/target/";
    
    @Test
    void testFilter() throws IOException {
        
        OntModel source = generate(namespaceSource);
        OntModel target = generate(namespaceTarget);
        
        Alignment m = new Alignment();
        m.add(namespaceSource + "instanceOne", namespaceTarget + "instanceOne", 0.9);
        m.add(namespaceSource + "instanceTwo", namespaceTarget + "instanceTwo", 0.9);
        m.add(namespaceSource + "propOne", namespaceTarget + "propOne", 0.9);
        
        Alignment filtered = new InstanceFilterBasedOnCommonProperties().filter(source, target, m);
        
        assertEquals(2, filtered.size());
    }
    
    
    
    private OntModel generate(String namespace){
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);        
        Resource r = model.createResource(namespace + "instanceOne");
        r.addProperty(model.createProperty(namespace + "propOne"), "Test");
        r.addProperty(model.createProperty(namespace + "propOne"), "Test");
        
        Resource s = model.createResource(namespace + "instanceTwo");
        s.addProperty(model.createProperty(namespace + "propTwo"), "Test");
        s.addProperty(model.createProperty(namespace + "propThree"), "Test");
        
        return model;
    }
    
}
