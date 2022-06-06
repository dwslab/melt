package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.AlignmentAndParameters;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BaselineStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.TransitiveClosure;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.VOID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


public class MultiSourceDispatcherIncrementalMergeTest {
    
    @Test
    public void noSourceIsModifiedTest() throws Exception{
        MultiSourceDispatcherIncrementalMerge merger = new MatcherFixedMergeTree();
        merger.setCopyMode(CopyMode.COPY_IN_MEMORY);
        List<Set<Object>> models = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            models.add(new HashSet<>(Arrays.asList(getModel("domain" + Integer.toString(i)))));
        }
        
        merger.match(models, null, null);
        
        for(Set<Object> model : models){
            assertEquals(1, model.size());
            Model m = (Model)model.iterator().next();
            assertEquals(1, m.size());
        }        
    }
    
    @Test
    public void sourceIsModifiedTest() throws Exception{
        MultiSourceDispatcherIncrementalMerge merger = new MatcherFixedMergeTree();
        merger.setCopyMode(CopyMode.NONE);
        List<Set<Object>> models = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            models.add(new HashSet<>(Arrays.asList(getModel("domain" + Integer.toString(i)))));
        }
        
        merger.match(models, null, null);
        
        boolean greaterThanOne = false;
        for(Set<Object> model : models){
            assertEquals(1, model.size());
            Model m = (Model)model.iterator().next();
            if(m.size() > 1){
                greaterThanOne = true;
            }
        }
        assertTrue(greaterThanOne, "All models are still the same even though CopyMode is set to None");
    }
    
    private Model getModel(String domain){
        Model m = ModelFactory.createDefaultModel();
        m.add(
                m.createResource("http://" + domain + "/s"), 
                m.createProperty("http://" + domain + "/p"), 
                "object"
        );
        return m;
    }
    
    @ParameterizedTest
    @ValueSource(ints  = {1,2})
    public void checkEverthingIsMerged(int threads) throws Exception{

        List<Set<Object>> models = new ArrayList<>();
        List<String> sameEntities = new ArrayList<>();
        for(int i=0; i < 7; i++){            
            OntModel m = ModelFactory.createOntologyModel();
            Resource r = m.createResource("http://example.com/" + i + "#individual");
            sameEntities.add("http://example.com/" + i + "#individual");
            r.addProperty(RDF.type, OWL.Thing);
            r.addProperty(RDFS.label, "Hello");
            models.add(new HashSet<>(Arrays.asList(m)));
        }

        MultiSourceDispatcherIncrementalMerge merger = new MatcherFixedMergeTree(new BaselineStringMatcher(), MergeOrderTest.getLeftSkewed(7, true));
        merger.setNumberOfThreads(threads);
        AlignmentAndParameters result = merger.match(models, null, null);
        Alignment a = result.getAlignment(Alignment.class);

        //due to the fact that model 0 is merged into model 1 and then all others are merged to it,
        //everthing will be mapped to model 1 
        assertContainsAnyDirection(a, "http://example.com/1#individual", "http://example.com/0#individual");
        assertContainsAnyDirection(a, "http://example.com/1#individual", "http://example.com/2#individual");
        assertContainsAnyDirection(a, "http://example.com/1#individual", "http://example.com/3#individual");
        assertContainsAnyDirection(a, "http://example.com/1#individual", "http://example.com/4#individual");
        assertContainsAnyDirection(a, "http://example.com/1#individual", "http://example.com/5#individual");
        assertContainsAnyDirection(a, "http://example.com/1#individual", "http://example.com/6#individual");

        TransitiveClosure<String> closure = new TransitiveClosure<>();
        for(Correspondence c : a){
            closure.add(c.getEntityOne(), c.getEntityTwo());
        }

        assertTrue(closure.belongToTheSameCluster(sameEntities));
        
    }
    private static void assertContainsAnyDirection(Alignment a, String one, String two){
        assertTrue(a.contains(new Correspondence(one, two)) || 
                a.contains(new Correspondence(two, one)));
    }
    
    
    @Test
    public void checkComparatorAscendingWorks() throws Exception{
        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(generate("10", 10, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("20", 20, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("30", 30, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("40", 40, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("50", 50, 0))));
        
        Collections.shuffle(models, new Random(1324));
        
        SaveOrderMatcherForTest oneToOneMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        MultiSourceDispatcherIncrementalMerge merger = new MultiSourceDispatcherIncrementalMergeByOrder(oneToOneMatcher, 
                MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_ASCENDING);
        
        merger.match(models, new Alignment(), new Properties());
        assertAllOrders(oneToOneMatcher,Arrays.asList(
                new SimpleEntry<>("10", 10), 
                new SimpleEntry<>("20", 20),
                new SimpleEntry<>("30", 30), 
                new SimpleEntry<>("40", 40),
                new SimpleEntry<>("50", 50)
        ));
    }
    
    @Test
    public void checkComparatorDecendingWorks() throws Exception{
        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(generate("10", 10, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("20", 20, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("30", 30, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("40", 40, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("50", 50, 0))));
        
        Collections.shuffle(models, new Random(1324));
        
        SaveOrderMatcherForTest oneToOneMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        MultiSourceDispatcherIncrementalMergeByOrder merger = new MultiSourceDispatcherIncrementalMergeByOrder(oneToOneMatcher, 
                MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_DECENDING);
        merger.match(models, new Alignment(), new Properties());
        
        assertAllOrders(oneToOneMatcher,Arrays.asList(
                new SimpleEntry<>("50", 50),
                new SimpleEntry<>("40", 40),
                new SimpleEntry<>("30", 30), 
                new SimpleEntry<>("20", 20),
                new SimpleEntry<>("10", 10)
        ));
    }
    
    
    @Test
    public void checkForAdaptiveThresholdWithGoldStandard() throws Exception{
        Model zero = ModelFactory.createDefaultModel();
        zero.add(zero.createResource("http://zero.com/"), RDF.type, VOID.Dataset);
        zero.add(zero.createResource("http://zero.com/a"), RDFS.label, "a");
        zero.add(zero.createResource("http://zero.com/b"), RDFS.label, "b");
        zero.add(zero.createResource("http://zero.com/c"), RDFS.label, "c");
        zero.add(zero.createResource("http://zero.com/x"), RDFS.label, "x");
        
        Model one = ModelFactory.createDefaultModel();
        one.add(zero.createResource("http://one.com/"), RDF.type, VOID.Dataset);
        one.add(one.createResource("http://one.com/a"), RDFS.label, "a");
        one.add(one.createResource("http://one.com/b"), RDFS.label, "b");
        one.add(one.createResource("http://one.com/c"), RDFS.label, "c");
        one.add(one.createResource("http://one.com/y"), RDFS.label, "y");
        
        Model two = ModelFactory.createDefaultModel();
        two.add(zero.createResource("http://two.com/"), RDF.type, VOID.Dataset);
        two.add(two.createResource("http://two.com/a"), RDFS.label, "a");
        two.add(two.createResource("http://two.com/b"), RDFS.label, "b");
        two.add(two.createResource("http://two.com/c"), RDFS.label, "c");
        two.add(two.createResource("http://two.com/z"), RDFS.label, "z");
        
                
        Alignment gold = new Alignment();
        gold.add("http://zero.com/a", "http://one.com/a", 1.0);
        gold.add("http://zero.com/b", "http://one.com/b", 1.0);        
        /*
        MatcherYAAAJena matcher = new MatcherYAAAJena() {
            @Override
            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                List<Resource> left = source.listSubjectsWithProperty(RDF.type, VOID.Dataset).toList();
                List<Resource> right = target.listSubjectsWithProperty(RDF.type, VOID.Dataset).toList();
                if(left.size() == 1 && right.size() == 1){
                    
                }
                
            }
        };
        
        MultiSourceDispatcherIncrementalMerge m = new MatcherFixedMergeTree(matcher, new int[][]{
            {0,1},
            {3,2}
        });
        
        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(zero)));
        models.add(new HashSet<>(Arrays.asList(one)));
        models.add(new HashSet<>(Arrays.asList(two)));
        m.match(models, new Properties(), new Alignment());
        */
    }
    
    
    private void assertAllOrders(SaveOrderMatcherForTest oneToOneMatcher, List<Entry<String, Integer>> counts){
        Counter<String> aggregated = new Counter<>();
        
        oneToOneMatcher.assertOrder(0, 
                getCounter(counts.get(0)),
                getCounter(counts.get(1)));
        
        aggregated.add(counts.get(0).getKey(), counts.get(0).getValue());
        aggregated.add(counts.get(1).getKey(), counts.get(1).getValue());
        for(int i = 2; i < counts.size();  i++){
            oneToOneMatcher.assertOrder(i-1, 
                aggregated,
                getCounter(counts.get(i)));
            
            aggregated.add(counts.get(i).getKey(), counts.get(i).getValue());
        }
    }
        
    private Counter<String> getCounter(Entry<String, Integer> count){
        Counter<String> c = new Counter<>();
        c.add(count.getKey(), count.getValue());
        return c;
    }
    
    private OntModel generate(String name, int classes, int individuals){
        OntModel m = ModelFactory.createOntologyModel();
        for(int i=0; i<classes; i++){
            m.add(m.createResource("http://" + name + "#class" + i), RDF.type, OWL.Class);
        }
        
        for(int i=0; i<individuals; i++){
            m.add(m.createResource("http://" + name + "#individual" + i), RDF.type, OWL.Thing);
        }
        return m;
    }
    
}