package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorAllAnnotationProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValueExtractorAllAnnotationPropertiesTest {


    @Test
    void extract() {
        ValueExtractorAllAnnotationProperties extractor = new ValueExtractorAllAnnotationProperties();


        // assert not null condition
        assertNotNull(extractor.extract(null));
    }
}