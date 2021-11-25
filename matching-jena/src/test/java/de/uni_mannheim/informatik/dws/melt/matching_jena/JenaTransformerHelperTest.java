package de.uni_mannheim.informatik.dws.melt.matching_jena;

import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import java.io.File;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import org.apache.jena.ontology.OntModel;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class JenaTransformerHelperTest {
    
    @Test
    void getModelRepresentationTest() throws MalformedURLException {
        File ontologyFile = new File("./src/test/resources/cmt.owl");
        OntModel model = OntologyCacheJena.get(ontologyFile.toURI().toURL());
        assertNotNull(model);

        assertEquals("Jena Model{ default namespace (prefix): http://cmt# top 3 domains (of 200 resources): http://cmt#(88) }",
                JenaTransformerHelper.getModelRepresentation(new HashSet<>(Arrays.asList(model))));
        
        assertTrue(JenaTransformerHelper.getModelRepresentation(new HashSet<>(Arrays.asList(model, ontologyFile.toURI().toURL())))
                .endsWith("src/test/resources/cmt.owl"));
    }
}
