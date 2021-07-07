package de.uni_mannheim.informatik.dws.melt.matching_owlapi;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.semanticweb.owlapi.model.OWLOntology;

public class OntologyCacheOwlApiTest {
    private static final int NUMBER_MODEL_CLASSES = 30;

    @Test
    public void testTwoTimesSameFileContent() {
        File ontologyFile = new File("./src/test/resources/cmt.owl");
        OWLOntology modelCmt = OntologyCacheOwlApi.get(ontologyFile);
        assertNotNull(modelCmt);
        assertEquals(NUMBER_MODEL_CLASSES, modelCmt.classesInSignature().count());
        
        OWLOntology cachedModel = OntologyCacheOwlApi.get(ontologyFile); //should be loaded by cache
        assertEquals(modelCmt, cachedModel); 
        
        
        File ontologyFile2 = new File("./src/test/resources/cmt2.owl"); //no cache but same file content
        OWLOntology model2 = OntologyCacheOwlApi.get(ontologyFile2);
        assertNotNull(model2);
        assertEquals(modelCmt, model2);
    }
}

/*
class MyMatcher extends MatcherYAAAOwlApi{
    @Override
    public Alignment match(OWLOntology source, OWLOntology target, Alignment inputAlignment, Properties p) throws Exception {
        System.out.println("Match " + source.getOntologyID().toString() + " with " + target.getOntologyID().toString());
        return new Alignment();        
    }
    
    //private void matchResources(Stream<? extends OWLLogicalEntity> sourceResources, Stream<? extends OWLLogicalEntity> targetResources, Alignment alignment) {
    //    Map<String, Set<String>> text2URI = new HashMap<>();
    //    sourceResources.forEach(entity -> entity.accept(visitor));
    //}
}
*/