package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.instancelevel;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class MatchPropBasedOnInstancesTest {
    private static final String namespaceSource = "http://melt.dws.informatik.uni-mannheim.de/source/";
    private static final String namespaceTarget = "http://melt.dws.informatik.uni-mannheim.de/target/";
    
    /*
    @Test
    void testSimpleCase() throws Exception {
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        OntModel target = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        source.createResource(namespaceSource+"John")
                .addProperty(source.createProperty(namespaceSource + "propA"), "test")
                .addProperty(source.createProperty(namespaceSource + "propA"), "bla");
        
        target.createResource(namespaceTarget+"Jonny")
                .addProperty(target.createProperty(namespaceTarget + "propB"), "test")
                .addProperty(target.createProperty(namespaceTarget + "propB"), "bla");
        
        Alignment inputAlignment = new Alignment();
        //instance John - Jonny is matched
        inputAlignment.add(namespaceSource + "John", namespaceTarget + "Jonny", 0.9);
        
        MatchPropBasedOnInstances matcher = new MatchPropBasedOnInstances(new File("test.csv"));
        Alignment a = matcher.match(source, target, inputAlignment, null);
        
        assertEquals(1, a.size());
        Correspondence c = a.iterator().next();
        assertEquals(namespaceTarget + "propA", c.getEntityOne());
        assertEquals(namespaceTarget + "propB", c.getEntityTwo());
        assertEquals(1.0, c.getConfidence());
    }
    */
    
}
