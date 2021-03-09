package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

//import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorProperty;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.vocabulary.RDFS;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StopwordExtractionTest {
    
    /*
    @Test
    void extractionTest() {
        OntModel anatomySource = TrackRepository.Anatomy.Default.getFirstTestCase().getSourceOntology(OntModel.class);
        
        StopwordExtraction extractor = new StopwordExtraction(s->Arrays.asList(s.toLowerCase(Locale.ENGLISH).split(" ")), 5, RDFS.label);
        assertEquals(5, extractor.extractStopwords(anatomySource.listClasses()).size());
        
        extractor = new StopwordExtraction(s->Arrays.asList(s.toLowerCase(Locale.ENGLISH).split(" ")), 0.03, RDFS.label);
        assertEquals(9, extractor.extractStopwords(anatomySource.listClasses()).size());
        
        extractor = new StopwordExtraction(s->Arrays.asList(s.toLowerCase(Locale.ENGLISH).split(" ")), false, 5, 0.03, new ValueExtractorProperty(RDFS.label));
        assertEquals(5, extractor.extractStopwords(anatomySource.listClasses()).size());
        
        extractor = new StopwordExtraction(s->Arrays.asList(s.toLowerCase(Locale.ENGLISH).split(" ")), false, 5, 0.06, new ValueExtractorProperty(RDFS.label));
        assertEquals(2, extractor.extractStopwords(anatomySource.listClasses()).size());
    }
*/
    
}
