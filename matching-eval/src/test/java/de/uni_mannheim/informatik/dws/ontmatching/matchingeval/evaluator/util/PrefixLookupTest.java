package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;


public class PrefixLookupTest {
    @Test
    void defaultPrefixCheck() {
        assertEquals("protege:", PrefixLookup.getPrefix("http://protege.stanford.edu/plugins/owl/protege#"));
        assertEquals("protege:Test", PrefixLookup.getPrefix("http://protege.stanford.edu/plugins/owl/protege#Test"));
        assertEquals("owl:Class", PrefixLookup.getPrefix("http://www.w3.org/2002/07/owl#Class"));       
    }

}
