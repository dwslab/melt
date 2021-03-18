package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;


public class MultiSourceDispatcherSomePairsOrderBasedTest {
    @Test
    public void noSourceIsModifiedTest() throws Exception{
        
        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(generate("a", 10, 60))));
        models.add(new HashSet<>(Arrays.asList(generate("b", 20, 50))));
        models.add(new HashSet<>(Arrays.asList(generate("c", 30, 40))));
        models.add(new HashSet<>(Arrays.asList(generate("d", 40, 30))));
        models.add(new HashSet<>(Arrays.asList(generate("e", 50, 20))));
        models.add(new HashSet<>(Arrays.asList(generate("f", 60, 10))));
        
        Collections.shuffle(models, new Random(1234));
        
        SaveOrderMatcherForTest innerMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        MultiSourceDispatcherSomePairsOrderBased merger = new MultiSourceDispatcherSomePairsOrderBased(innerMatcher, 
                MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_DECENDING, true);
        merger.match(models, null, null);
        
        innerMatcher.assertOrder(0, getCounter("f", 60), getCounter("e", 50));
        innerMatcher.assertOrder(1, getCounter("f", 60), getCounter("d", 40));
        innerMatcher.assertOrder(2, getCounter("f", 60), getCounter("c", 30));
        innerMatcher.assertOrder(3, getCounter("f", 60), getCounter("b", 20));
        innerMatcher.assertOrder(4, getCounter("f", 60), getCounter("a", 10));
        
        
        
        innerMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        merger = new MultiSourceDispatcherSomePairsOrderBased(innerMatcher, 
                MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_DECENDING, false);
        merger.match(models, null, null);
        
        innerMatcher.assertOrder(0, getCounter("f", 60), getCounter("e", 50));
        innerMatcher.assertOrder(1, getCounter("e", 50), getCounter("d", 40));
        innerMatcher.assertOrder(2, getCounter("d", 40), getCounter("c", 30));
        innerMatcher.assertOrder(3, getCounter("c", 30), getCounter("b", 20));
        innerMatcher.assertOrder(4, getCounter("b", 20), getCounter("a", 10));
        
        
        //sort by instances:
        
        innerMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        merger = new MultiSourceDispatcherSomePairsOrderBased(innerMatcher, 
                MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_INSTANCES_DECENDING, true);
        merger.match(models, null, null);
        
        innerMatcher.assertOrder(0, getCounter("a", 10), getCounter("b", 20));
        innerMatcher.assertOrder(1, getCounter("a", 10), getCounter("c", 30));
        innerMatcher.assertOrder(2, getCounter("a", 10), getCounter("d", 40));
        innerMatcher.assertOrder(3, getCounter("a", 10), getCounter("e", 50));
        innerMatcher.assertOrder(4, getCounter("a", 10), getCounter("f", 60));
        
        
        innerMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        merger = new MultiSourceDispatcherSomePairsOrderBased(innerMatcher, 
                MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_INSTANCES_DECENDING, false);
        merger.match(models, null, null);
        
        innerMatcher.assertOrder(0, getCounter("a", 10), getCounter("b", 20));
        innerMatcher.assertOrder(1, getCounter("b", 20), getCounter("c", 30));
        innerMatcher.assertOrder(2, getCounter("c", 30), getCounter("d", 40));
        innerMatcher.assertOrder(3, getCounter("d", 40), getCounter("e", 50));
        innerMatcher.assertOrder(4, getCounter("e", 50), getCounter("f", 60));
    }
    
    private Counter<String> getCounter(String s, int count){
        Counter c = new Counter<>();
        c.add(s, count);
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
