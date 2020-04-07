package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;


import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Arrays;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ScalableStringProcessingMatcherTest {
    
    @Test
    void matcherTest() throws Exception {
        OntModel source = ModelFactory.createOntologyModel();
        source.createIndividual("http://source.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "This is a test");
        
        source.createIndividual("http://source.de/two", OWL.Thing)
                .addLiteral(RDFS.label, "Two")
                .addLiteral(SKOS.altLabel, "foo bar");
        
        //---------
        
        OntModel target = ModelFactory.createOntologyModel();
        target.createIndividual("http://target.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "This is a test")
                .addLiteral(SKOS.altLabel, "foo bar");
        
        ScalableStringProcessingMatcher matcher = new ScalableStringProcessingMatcher(Arrays.asList(
                new PropertySpecificStringProcessing(text -> text, 1.0, RDFS.label)
        ));
        
        //test simple match
        Alignment a = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(a.contains(new Correspondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE)));
        assertEquals(1, a.size());
        
        //test no match because no processing
        matcher = new ScalableStringProcessingMatcher(Arrays.asList());
        a = matcher.match(source, target, new Alignment(), new Properties());
        assertEquals(0, a.size());
        
        //test easrly stopping
        matcher = new ScalableStringProcessingMatcher(Arrays.asList(
            new PropertySpecificStringProcessing(text -> text, 1.0, RDFS.label),
            new PropertySpecificStringProcessing(text -> text, 0.9, SKOS.altLabel)
        ));
        matcher.setEarlyStopping(true);
        a = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(a.contains(new Correspondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE)));
        assertEquals(1, a.size());
        
        matcher.setEarlyStopping(false);
        a = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(a.contains(new Correspondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE)));
        assertTrue(a.contains(new Correspondence("http://source.de/two", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE)));
        assertEquals(2, a.size());
    }
    
    @Test
    void testProcessing() throws Exception {
        OntModel source = ModelFactory.createOntologyModel();
        source.createIndividual("http://source.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "everything");        
        //---------
        OntModel target = ModelFactory.createOntologyModel();
        target.createIndividual("http://target.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "EVERYTHING");
        
        ScalableStringProcessingMatcher matcher = new ScalableStringProcessingMatcher(Arrays.asList(
                new PropertySpecificStringProcessing(text -> text, 1.0, RDFS.label)
        ));
        Alignment a = matcher.match(source, target, new Alignment(), new Properties());
        assertEquals(0, a.size());
        
        matcher = new ScalableStringProcessingMatcher(Arrays.asList(
                new PropertySpecificStringProcessing(text -> text, 1.0, RDFS.label),
                new PropertySpecificStringProcessing(text -> text.toLowerCase(), 0.99, RDFS.label)
        ));
        a = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(a.contains(new Correspondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE)));
        assertEquals(1, a.size());
    }
}
