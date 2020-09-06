package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;


import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Arrays;
import java.util.HashSet;
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
    void testCrossIndexMatch() throws Exception {
        OntModel source = ModelFactory.createOntologyModel();
        source.createIndividual("http://source.de/one", OWL.Thing)
                .addLiteral(SKOS.altLabel, "This is a test");
        
        //---------
        
        OntModel target = ModelFactory.createOntologyModel();
        target.createIndividual("http://target.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "This is a test");
        
        
        ScalableStringProcessingMatcher matcher = new ScalableStringProcessingMatcher(Arrays.asList(
                new PropertySpecificStringProcessing(text -> text, 1.0, RDFS.label),
                new PropertySpecificStringProcessing(text -> text, 0.9, SKOS.altLabel)
        ), false);
        
        //test no cross index
        Alignment a = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(a.isEmpty());
        
        //test cross index
        matcher.setCrossIndexMatch(true);        
        a = matcher.match(source, target, new Alignment(), new Properties());
        
        Correspondence c = a.getCorrespondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE);
        
        assertTrue(c != null);
        assertEquals(0.9, c.getConfidence());
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
    
    @Test
    void testMultipleReturn() throws Exception {
        OntModel source = ModelFactory.createOntologyModel();
        source.createIndividual("http://source.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "a");        
        //---------
        OntModel target = ModelFactory.createOntologyModel();
        target.createIndividual("http://target.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "b");
        
        ScalableStringProcessingMatcher matcher = new ScalableStringProcessingMatcher(Arrays.asList(
                new PropertySpecificStringProcessingMultipleReturn(text -> {
                    if(text.equals("a")){
                        return new HashSet(Arrays.asList("a", "x"));
                    }else if(text.equals("b")){
                        return new HashSet(Arrays.asList("b", "x"));
                    }
                    return new HashSet(Arrays.asList(text));
                }, 1.0, RDFS.label)
        ));
        Alignment a = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(a.contains(new Correspondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE)));
        assertEquals(1, a.size());
        
    }
}
