package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


public class MultiSourceDispatcherIncrementalMergeByClusterTextTest {
    @Test
    public void testLowMemoryConsumption() throws MalformedURLException{        
        List<Set<Object>> models = getModels();
        MultiSourceDispatcherIncrementalMergeByClusterText merger = new MultiSourceDispatcherIncrementalMergeByClusterText(null);
        merger.setRemoveUnusedJenaModels(true);
        merger.getMergeTree(models, new Properties());
        for(Set<Object> model : models){
            for(Object o : model){
                if(o instanceof Model){
                    assertTrue(false, "There is an ontmodel available but RemoveUnusedJenaModels is set to true");
                }
            }
        }
    }
    
    @Test
    public void testOntModelStillInModelsWhenMemoryDoesntMatter() throws MalformedURLException{        
        List<Set<Object>> models = getModels();
        MultiSourceDispatcherIncrementalMergeByClusterText merger = new MultiSourceDispatcherIncrementalMergeByClusterText(null);
        merger.setRemoveUnusedJenaModels(false);
        merger.getMergeTree(models, new Properties());
        for(Set<Object> model : models){
            boolean hasModel = false;
            for(Object o : model){
                if(o instanceof Model){
                    hasModel = true;
                }
            }
            assertTrue(hasModel, "OntModel is not contained in models even though RemoveUnusedJenaModels is false");
        }
    }
    
    private List<Set<Object>> getModels() throws MalformedURLException{
        TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();

        List<Set<Object>> models = new ArrayList<>();
        models.add(new HashSet<>(Arrays.asList(tc.getSource().toURL())));
        models.add(new HashSet<>(Arrays.asList(tc.getTarget().toURL())));
        models.add(new HashSet<>(Arrays.asList(tc.getSource().toURL())));
        return models;
    }
}
