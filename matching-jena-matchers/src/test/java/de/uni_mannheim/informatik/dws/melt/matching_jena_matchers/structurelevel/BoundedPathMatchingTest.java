package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel;


import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class BoundedPathMatchingTest {
    
    private static String sourceNS = "http://source.com/";
    private static String targetNS = "http://target.com/";
    
    @Test
    void testSimple() throws Exception {
        /*
        Zs <----------->  Zt
        |                 | 
        As                At
        |                 |
        Xs <------------> Xt
        
        */        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
    }
    
    @Test
    void testSimpleConfidence() throws Exception {
        /*
        Zs <-----0.9----> Zt
        |                 | 
        As                At
        |                 |
        Xs <-----0.8----> Xt
        
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt", 0.8);
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt", 0.9);
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        Correspondence c = output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE);
        assertTrue(c != null);
        assertTrue(Math.abs(c.getConfidence() - 0.85) < 0.000001);
    }
    
    @Test
    void testMultiplePaths() throws Exception {
        /*
          Zs <---------->   Zt
        /   \               | 
       As    Bs             At
        \   /               |
         Xs <------------>  Xt
        
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Zs"),
                Arrays.asList("Xs", "Bs", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
        assertTrue(output.getCorrespondence(sourceNS +"Bs", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
    }
    
    @Test
    void testMultiplePathsOneLonger() throws Exception {
        /*
          Zs <---------->   Zt
          /  \               |
         /   Cs              |
        /    |               | 
       As    Bs             At
        \   /               |
         Xs <------------>  Xt
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Zs"),
                Arrays.asList("Xs", "Bs", "Cs", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
    }
    
    @Test
    void testMultiplePathsDifferentLength() throws Exception {
        /*
          Zs <---------->   Zt
          |                 |
          Cs                |
          |                 | 
          Bs                At
          |                 |
         Xs <------------>  Xt
        
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "Bs", "Cs", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"Bs", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) == null);
        assertTrue(output.getCorrespondence(sourceNS +"Cs", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) == null);
    }
    
    @Test
    void testLargerPath() throws Exception {
        /*
        Zs <----------->  Zt
        |                 | 
        Bs                Bt
        |                 | 
        As                At
        |                 |
        Xs <------------> Xt
        
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Bs", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Bt", "Zt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) == null);
        assertTrue(output.getCorrespondence(sourceNS +"Bs", targetNS +"Bt", CorrespondenceRelation.EQUIVALENCE) == null);
        
        //change length
        matcher = new BoundedPathMatching();
        matcher.setConfigurations(Arrays.asList(BoundedPathMatchingConfiguration.createClassHierarchyConfiguration(2)));
        output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
        assertTrue(output.getCorrespondence(sourceNS +"Bs", targetNS +"Bt", CorrespondenceRelation.EQUIVALENCE) != null);
    }
    
    @Test
    void testTwoPaths() throws Exception {
        /*

      Ys   Zs           Yt  Zt
      |    |            |    |
      Bs   As           Bt  At
       \  /              \ /
        Xs <------------> Xt
        
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Zs"),
                Arrays.asList("Xs", "Bs", "Ys")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt"),
                Arrays.asList("Xt", "Bt", "Yt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Ys", targetNS + "Yt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
        assertTrue(output.getCorrespondence(sourceNS +"Bs", targetNS +"Bt", CorrespondenceRelation.EQUIVALENCE) != null);
        
        assertTrue(output.getCorrespondence(sourceNS +"Bs", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) == null);
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"Bt", CorrespondenceRelation.EQUIVALENCE) == null);
    }
    
    
    @Test
    void testMultipleCorrespondences() throws Exception {
        /*
        Zs <----------->  Zt  Yt
        |                 |   |
        As                At  Bt
        |                 |  /
        Xs <------------> Xt
        
        */        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Zs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Zt"),
                Arrays.asList("Xt", "Bt", "Yt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Zt");
        inputAlignment.add(sourceNS + "Zs", targetNS + "Yt");
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"At", CorrespondenceRelation.EQUIVALENCE) != null);
        assertTrue(output.getCorrespondence(sourceNS +"As", targetNS +"Bt", CorrespondenceRelation.EQUIVALENCE) != null);
    }
    
    
    @Test
    void testCycle() throws Exception {
        /*
                           Bt
                           |  \
        As                 At  Ct
        \ \                 | /
         Xs <------------>  Xt
        
        */
        
        OntModel source = createClassHierarchy(sourceNS,Arrays.asList(
                Arrays.asList("Xs", "As", "Xs")));
        
        OntModel target = createClassHierarchy(targetNS,Arrays.asList(
                Arrays.asList("Xt", "At", "Bt", "Ct", "Xt")));
                

        Alignment inputAlignment = new Alignment();
        inputAlignment.add(sourceNS + "Xs", targetNS + "Xt");
        // do not use smart exit if too less correspondences available
        inputAlignment.add(sourceNS + "undefined", targetNS + "undefined"); 
        
        BoundedPathMatching matcher = new BoundedPathMatching();
        Alignment output = matcher.match(source, target, inputAlignment, new Properties());
        assertEquals(2, inputAlignment.size());
        
        
    }
    
    
    
    private OntModel createClassHierarchy(String namespace, List<List<String>> hierarchies){
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        for(List<String> hierarchy : hierarchies){
            for(int i = 1; i < hierarchy.size(); i++){
                model.createClass(namespace + hierarchy.get(i - 1))
                    .addSuperClass(model.createClass(namespace + hierarchy.get(i)));
            }
        }
        return model;
    }
}
