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
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MultiSourceDispatcherTransitivePairsTextBasedTest {
    @Test
    public void noSourceIsModifiedTest() throws Exception{
        
        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(getModel("a", "Cities are very big and huge"))));
        models.add(new HashSet<>(Arrays.asList(getModel("b", "Some parts in cities are big and bigger"))));
        models.add(new HashSet<>(Arrays.asList(getModel("c", "There are animals like cats and dogs. Cities"))));
        models.add(new HashSet<>(Arrays.asList(getModel("d", "Dogs are interesting animals , similar to cats and pets in general."))));
        models.add(new HashSet<>(Arrays.asList(getModel("e", "Many pupils run to the school every morning.Cities"))));
        models.add(new HashSet<>(Arrays.asList(getModel("f", "School starts early in the morning when all pupils wake up."))));
        
        Collections.shuffle(models, new Random(1234));
        
        SaveOrderMatcherForTest innerMatcher = new SaveOrderMatcherForTest(DatasetIDExtractor.CONFERENCE_TRACK_EXTRACTOR);
        MultiSourceDispatcherTransitivePairsTextBased merger = new MultiSourceDispatcherTransitivePairsTextBased(innerMatcher);
        
        merger.match(models, null, null);
        
        assertEquals(5, innerMatcher.numberOfMatches());
        
        innerMatcher.assertOrder(0, getCounter("e"), getCounter("f"));
        innerMatcher.assertOrder(1, getCounter("c"), getCounter("d"));
        innerMatcher.assertOrder(2, getCounter("a"), getCounter("b"));
        innerMatcher.assertOrder(3, getCounter("a"), getCounter("e"));
        innerMatcher.assertOrder(4, getCounter("a"), getCounter("c"));
    }
    
    private Counter<String> getCounter(String... s){
        return new Counter<>(Arrays.asList(s));
    }
    
    private OntModel getModel(String domain, String literal){
        OntModel m = ModelFactory.createOntologyModel();
        m.add(
                m.createClass("http://" + domain + "/s"), 
                RDFS.label, 
                literal
        );
        return m;
    }
}
