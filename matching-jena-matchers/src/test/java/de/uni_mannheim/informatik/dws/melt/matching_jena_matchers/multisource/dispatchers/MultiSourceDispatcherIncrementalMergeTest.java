package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MultiSourceDispatcherIncrementalMergeTest {
    @Test
    public void noSourceIsModifiedTest() throws Exception{
        MultiSourceDispatcherIncrementalMerge merger = new matcherTest();
        
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
    private Model getModel(String domain){
        Model m = ModelFactory.createDefaultModel();
        m.add(
                m.createResource("http://" + domain + "/s"), 
                m.createProperty("http://" + domain + "/p"), 
                "object"
        );
        return m;
    }
    
    
    
    @Test
    public void checkComparatorWorks() throws Exception{
        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(generate("10", 10, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("20", 20, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("30", 30, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("40", 40, 0))));
        models.add(new HashSet<>(Arrays.asList(generate("50", 50, 0))));
        
        Collections.shuffle(models, new Random(1324));
        
        SaveOrder oneToOneMatcher = new SaveOrder(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
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
        
        
        oneToOneMatcher = new SaveOrder(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        merger = new MultiSourceDispatcherIncrementalMergeByOrder(oneToOneMatcher, 
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
    
    private void assertAllOrders(SaveOrder oneToOneMatcher, List<Entry<String, Integer>> counts){
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

class matcherTest extends MultiSourceDispatcherIncrementalMerge{

    public matcherTest() {
        super(new NoOp());
    }

    @Override
    public int[][] getMergeTree(List<Set<Object>> models, Object parameters) {
        int[][] array = {
            {0,1},
            {3,2}
        };
        return array;
    }
}

class NoOp implements IMatcher<Object, Object, Object> {
    @Override
    public Object match(Object source, Object target, Object inputAlignment, Object parameters) throws Exception {
        return new Alignment();
    }
}


class SaveOrder implements IMatcher<OntModel, Object, Object> {
    private List<Entry<Counter<String>,Counter<String>>> order;
    private DatasetIDExtractor extractor;
    public SaveOrder(DatasetIDExtractor extractor){
        this.order = new ArrayList<>();
        this.extractor = extractor;
    }
    
    @Override
    public Object match(OntModel source, OntModel target, Object inputAlignment, Object parameters) throws Exception {
        this.order.add(new AbstractMap.SimpleEntry<>(
                getDatasetIdDistribution(source),
                getDatasetIdDistribution(target)
        ));        
        return inputAlignment;
    }
    private Counter<String> getDatasetIdDistribution(OntModel m){
        Counter<String> counter = new Counter<>();
        ExtendedIterator<OntClass> i = m.listClasses();
        while(i.hasNext()){
            String uri = i.next().getURI();
            if(uri != null)
                counter.add(extractor.getDatasetID(uri));
        }
        return counter;
    }
    
    public void clear(){
        this.order.clear();
    }
    
    public void assertOrder(int step, Counter<String> one, Counter<String> two){
        Counter<String> source = order.get(step).getKey();
        Counter<String> target = order.get(step).getValue();
        assertTrue((source.equals(one) && target.equals(two)) || (source.equals(two) && target.equals(one)));
    }
}