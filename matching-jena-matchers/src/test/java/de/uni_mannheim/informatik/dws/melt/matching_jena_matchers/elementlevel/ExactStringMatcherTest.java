package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ExactStringMatcherTest {

    @Test
    public void executeMatch(){
        ExactStringMatcher bsm = new ExactStringMatcher();
        assertNotNull(bsm.getTransformationFunction());
    }

}