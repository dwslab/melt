package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.IOException;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class BadHostsFilterTest {
    
    @Test
    void testFilter() throws IOException {
        
        OntModel source = create("example.com", "http://mouse.owl#");
        OntModel target = create("foo.com", "http://target.owl#");
        
        System.out.println(BadHostsFilter.getHostURIOfModel(source)); // might be wrong answer
        assertEquals("mouse.owl", BadHostsFilter.getHostURIOfModelByFullAnalysis(source));
        assertEquals("mouse.owl", BadHostsFilter.getHostURIOfModelBySampling(source, 202));
        assertEquals("mouse.owl", BadHostsFilter.getHostURIOfModelBySampling(source, Integer.MAX_VALUE));
        
        
        Alignment a = new Alignment();
        a.add("http://mouse.owl#123", "http://target.owl#152");
        a.add("http://example.com/foo/start/8465", "http://target.owl#152");
        a.add("http://mouse.owl#123", "http://foo.com/foo/end/6445");
        
        Alignment b = BadHostsFilter.filter(source, target, a, true, (m) -> BadHostsFilter.getHostURIOfModelBySampling(m, 202));
        
        assertEquals(1, b.size());
        
        
    }
    
    private OntModel create(String wrong, String okay){
        OntModel m = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);        
        for(int i = 0; i < 50; i++){
            m.createClass("http://" + wrong + "/foo/start/" + i);
        }
        for(int i = 0; i < 200; i++){
            m.createClass(okay + i);
        }
        for(int i = 0; i < 50; i++){
            m.createClass("http://" + wrong + "/foo/end/" + i);
        }
        return m;
    }
}
