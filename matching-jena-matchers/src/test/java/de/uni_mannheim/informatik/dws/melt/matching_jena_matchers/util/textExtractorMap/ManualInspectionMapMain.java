package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorMap;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.*;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap.TextExtractorMapSet;
import org.slf4j.LoggerFactory;

/**
 * This class allows to manually inspect the output of a {@link TextExtractor} by writing the results to a file or stdout.
 */
public class ManualInspectionMapMain {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ManualInspectionMain.class);
    public static void main(String[] args){
        
        TextExtractorMap extractor = new TextExtractorMapSet();
        //TextExtractor extractor = new  TextExtractorSet();
        
        ManualInspectionMap inspector = new ManualInspectionMap(extractor);
        
        
        //TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();
        //inspector.describeAll(tc, true, true, true, true);
        
        TestCase tc = TrackRepository.Knowledgegraph.V4.getTestCase("memoryalpha-memorybeta");
        inspector.describeSampleSource(tc, 100, true, true, false, false);
        
    }
    
    
    
}
