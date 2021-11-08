package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MergeExecutorTest {
        
    @Test
    public void testMergesWithoutAlignment(){
        Model source = getSource();
        Model target = getTarget();
        
        assertEquals(3, source.size());
        assertEquals(3, target.size());
        
        //test without any alignment
        MergeExecutor.mergeSourceIntoTarget(source, target, new Alignment(), false);
        
        assertEquals(3, source.size());
        assertEquals(6, target.size());
        
        assertTrue(target.containsAll(source));
        
        source = getSource();
        target = getTarget();        
        MergeExecutor.mergeSourceIntoTarget(source, target, new Alignment(), true);
        
        assertEquals(3, source.size());
        assertEquals(6, target.size());
        
        assertTrue(target.containsAll(source));      
    }
    
    @Test
    public void testMergesWitInstanceAndPropertyAlignmentNoInformationToUnion(){
        Model source = getSource();
        Model target = getTarget();
        
        assertEquals(3, source.size());
        assertEquals(3, target.size());
        
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/alice", "http://target.com/bob");
        alignment.add("http://source.com/eyeColor", "http://target.com/eyeColor");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, false);
        
        assertEquals(3, source.size());
        assertEquals(3, target.size());
        
        Resource bob = target.createResource("http://target.com/bob");
        Property eyecolorTarget = target.createProperty("http://target.com/eyeColor");
        
        assertTrue(target.contains(bob, RDFS.label, "Bob"));
        assertTrue(target.contains(bob, RDFS.comment, "Bob comment"));
        assertTrue(target.contains(bob, eyecolorTarget, "green"));
        assertFalse(target.containsAny(source));
    }
    
    @Test
    public void testMergesWitInstanceAndPropertyAlignmentNoInformationToUnionWithMultipleSources(){
        Model source = getSourceMultipleInstances();
        Model target = getTarget();
        
        assertEquals(6, source.size());
        assertEquals(3, target.size());
        
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/alice", "http://target.com/bob");
        alignment.add("http://source.com/eyeColor", "http://target.com/eyeColor");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, false);

        assertEquals(6, source.size());
        assertEquals(6, target.size());
        
        Resource bob = target.createResource("http://target.com/bob");
        Resource charlie = target.createResource("http://source.com/charlie");
        Property eyecolorTarget = target.createProperty("http://target.com/eyeColor");
        
        assertTrue(target.contains(bob, RDFS.label, "Bob"));
        assertTrue(target.contains(bob, RDFS.comment, "Bob comment"));
        assertTrue(target.contains(bob, eyecolorTarget, "green"));
        
        assertTrue(target.contains(charlie, RDFS.label, "Charlie"));
        assertTrue(target.contains(charlie, RDFS.comment, "Charlie comment"));
        assertTrue(target.contains(charlie, eyecolorTarget, "brown"));
    }
    
    
    
    @Test
    public void testMergesWitInstanceAlignmentAddInformation(){
        Model source = getSource();
        Model target = getTarget();
        
        assertEquals(3, source.size());
        assertEquals(3, target.size());
        
        //test without any alignment        
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/alice", "http://target.com/bob");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, true);
        
        assertEquals(3, source.size());
        assertEquals(6, target.size());
        
        Resource bob = target.createResource("http://target.com/bob");
        Property eyecolorSource = target.createProperty("http://source.com/eyeColor");
        Property eyecolorTarget = target.createProperty("http://target.com/eyeColor");
        
        assertTrue(target.contains(bob, RDFS.label, "Bob"));
        assertTrue(target.contains(bob, RDFS.label, "Alice"));
        assertTrue(target.contains(bob, RDFS.comment, "Bob comment"));
        assertTrue(target.contains(bob, RDFS.comment, "Alice comment"));
        assertTrue(target.contains(bob, eyecolorSource, "blue"));
        assertTrue(target.contains(bob, eyecolorTarget, "green"));
        assertFalse(target.containsAny(source));
    }
    
    
    @Test
    public void testMergesWitInstanceAndPropertyAlignmentAddInformation(){
        Model source = getSource();
        Model target = getTarget();
        
        assertEquals(3, source.size());
        assertEquals(3, target.size());
        
        //test without any alignment        
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/alice", "http://target.com/bob");
        alignment.add("http://source.com/eyeColor", "http://target.com/eyeColor");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, true);
        
        assertEquals(3, source.size());
        assertEquals(6, target.size());
        
        Resource bob = target.createResource("http://target.com/bob");
        Property eyecolorTarget = target.createProperty("http://target.com/eyeColor");
        
        assertTrue(target.contains(bob, RDFS.label, "Bob"));
        assertTrue(target.contains(bob, RDFS.label, "Alice"));
        assertTrue(target.contains(bob, RDFS.comment, "Bob comment"));
        assertTrue(target.contains(bob, RDFS.comment, "Alice comment"));
        assertTrue(target.contains(bob, eyecolorTarget, "blue"));
        assertTrue(target.contains(bob, eyecolorTarget, "green"));
        assertFalse(target.containsAny(source));
    }
    
    
    @Test
    public void testMergesWitInstanceAndPropertyAlignmentAddInformationMultipleInstances(){
        Model source = getSourceMultipleInstances();
        Model target = getTarget();
        
        assertEquals(6, source.size());
        assertEquals(3, target.size());
               
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/alice", "http://target.com/bob");
        alignment.add("http://source.com/eyeColor", "http://target.com/eyeColor");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, true);
        
        
        
        assertEquals(6, source.size());
        assertEquals(9, target.size());
        
        Resource bob = target.createResource("http://target.com/bob");
        Resource charlie = target.createResource("http://source.com/charlie");
        Property eyecolorTarget = target.createProperty("http://target.com/eyeColor");
        
        assertTrue(target.contains(bob, RDFS.label, "Bob"));
        assertTrue(target.contains(bob, RDFS.label, "Alice"));
        assertTrue(target.contains(bob, RDFS.comment, "Bob comment"));
        assertTrue(target.contains(bob, RDFS.comment, "Alice comment"));
        assertTrue(target.contains(bob, eyecolorTarget, "blue"));
        assertTrue(target.contains(bob, eyecolorTarget, "green"));
        
        assertTrue(target.contains(charlie, RDFS.label, "Charlie"));
        assertTrue(target.contains(charlie, RDFS.comment, "Charlie comment"));
        assertTrue(target.contains(charlie, eyecolorTarget, "brown"));
        
    }
    
    
    @Test
    public void testPathNoInformationToUnion(){
        // A  --> B   --> C
        //        |
        //One --> Two --> Three
        Model source = getSourcePath();
        Model target = getTargetPath();
        
        assertEquals(2, source.size());
        assertEquals(2, target.size());
        
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/bob", "http://target.com/two");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, false);
        
        assertEquals(2, target.size());
        
        assertTrue(target.contains(
                target.createResource("http://target.com/one"), 
                FOAF.knows,
                target.createResource("http://target.com/two")));
        
        assertTrue(target.contains(
                target.createResource("http://target.com/two"), 
                FOAF.knows,
                target.createResource("http://target.com/three")));
    }
    
    @Test
    public void testPathWithInformationToUnion(){
        // A  --> B   --> C
        //        |
        //One --> Two --> Three
        Model source = getSourcePath();
        Model target = getTargetPath();
        
        assertEquals(2, source.size());
        assertEquals(2, target.size());
        
        Alignment alignment = new Alignment();
        alignment.add("http://source.com/bob", "http://target.com/two");
        
        MergeExecutor.mergeSourceIntoTarget(source, target, alignment, true);
        
        assertTrue(target.contains(
                target.createResource("http://source.com/alice"), 
                FOAF.knows,
                target.createResource("http://target.com/two")));
        
        assertTrue(target.contains(
                target.createResource("http://target.com/one"), 
                FOAF.knows,
                target.createResource("http://target.com/two")));
        
        assertTrue(target.contains(
                target.createResource("http://target.com/two"), 
                FOAF.knows,
                target.createResource("http://source.com/charlie")));
        
        assertTrue(target.contains(
                target.createResource("http://target.com/two"), 
                FOAF.knows,
                target.createResource("http://target.com/three")));
        
        assertFalse(target.containsAny(source));
    }
    
    
    private static Model getSourcePath(){
        Model source = ModelFactory.createDefaultModel();
        Resource alice = source.createResource("http://source.com/alice");
        Resource bob = source.createResource("http://source.com/bob");
        Resource charlie = source.createResource("http://source.com/charlie");
        alice.addProperty(FOAF.knows, bob);
        bob.addProperty(FOAF.knows, charlie);
        return source;
    }
    
    private static Model getTargetPath(){
        Model target = ModelFactory.createDefaultModel();
        Resource one = target.createResource("http://target.com/one");
        Resource two = target.createResource("http://target.com/two");
        Resource three = target.createResource("http://target.com/three");
        one.addProperty(FOAF.knows, two);
        two.addProperty(FOAF.knows, three);
        return target;
    }
    
    
    //Helper fuctions
    
    private static Model getSource(){
        Model source = ModelFactory.createDefaultModel();
        source.createResource("http://source.com/alice")
                .addProperty(RDFS.label, "Alice")
                .addProperty(RDFS.comment, "Alice comment")
                .addProperty(source.createProperty("http://source.com/eyeColor"), "blue");
        return source;
    }
    
    private static Model getSourceMultipleInstances(){
        Model source = ModelFactory.createDefaultModel();
        source.createResource("http://source.com/alice")
                .addProperty(RDFS.label, "Alice")
                .addProperty(RDFS.comment, "Alice comment")
                .addProperty(source.createProperty("http://source.com/eyeColor"), "blue");
        source.createResource("http://source.com/charlie")
                .addProperty(RDFS.label, "Charlie")
                .addProperty(RDFS.comment, "Charlie comment")
                .addProperty(source.createProperty("http://source.com/eyeColor"), "brown");
        return source;
    }
    
    private static Model getTarget(){
        Model target = ModelFactory.createDefaultModel();
        target.createResource("http://target.com/bob")
                .addProperty(RDFS.label, "Bob")
                .addProperty(RDFS.comment, "Bob comment")
                .addProperty(target.createProperty("http://target.com/eyeColor"), "green");
        return target;
    }
    
    private static void printOutModel(Model m){
        RDFDataMgr.write(System.out, m, Lang.TURTLE);
    }
}
