package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
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
}

class matcherTest extends MultiSourceDispatcherIncrementalMerge{

    public matcherTest() {
        super(new NoOp());
    }

    @Override
    public int[][] getMergeTree(List<Set<Object>> models, Object inputAlignment, Object parameters) {
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