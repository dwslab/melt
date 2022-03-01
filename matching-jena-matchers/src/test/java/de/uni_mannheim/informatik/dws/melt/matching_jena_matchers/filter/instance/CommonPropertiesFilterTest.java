package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance.CommonPropertiesFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 * InstanceFilterBasedOnCommonPropertiesTest
 */
public class CommonPropertiesFilterTest {
    private static final String namespaceSource = "http://melt.dws.informatik.uni-mannheim.de/source/";
    private static final String namespaceTarget = "http://melt.dws.informatik.uni-mannheim.de/target/";
    
    @Test
    void testFilter() throws IOException {
        
        OntModel source = generate(namespaceSource);
        OntModel target = generate(namespaceTarget);
        
        Alignment m = new Alignment();
        m.add(namespaceSource + "instanceOne", namespaceTarget + "instanceOne", 0.9);
        m.add(namespaceSource + "instanceTwo", namespaceTarget + "instanceTwo", 0.9);
        m.add(namespaceSource + "propOne", namespaceTarget + "propOne", 0.9);
        
        Alignment filtered = new CommonPropertiesFilter().filter(source, target, m);
        
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
