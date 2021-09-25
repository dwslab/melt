package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BaselineStringMatcherTest {

    @Test
    void normalizeString() {
        assertEquals(BaselineStringMatcher.normalize("HelloWorld"), BaselineStringMatcher.normalize("hello_world"));
        assertEquals(BaselineStringMatcher.normalize("Hello World"), BaselineStringMatcher.normalize("hello_world"));
        assertEquals(BaselineStringMatcher.normalize("HelloWorld"), BaselineStringMatcher.normalize("hello_world"));
    }

    @Test
    void getLabelOrFragment(){
        OntModel model = ModelFactory.createOntologyModel();
        OntResource r = model.createOntResource(null);
        assertNull(BaselineStringMatcher.getLabelOrFragment(r));
    }
}