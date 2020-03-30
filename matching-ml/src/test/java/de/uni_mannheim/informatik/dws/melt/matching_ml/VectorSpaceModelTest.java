package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VectorSpaceModelTest {
    
    @Test
    void isConfidenceCorrectlySet() throws Exception {
        TestCase testCase = TrackRepository.Anatomy.Default.getFirstTestCase();

        Alignment inputAlignment = new Alignment();
        inputAlignment.add("http://mouse.owl#MA_0000253", "http://human.owl#NCI_C33127");
        inputAlignment.add("http://mouse.owl#MA_0000253", "http://human.owl#NCI_C12292");
        
        VectorSpaceModelMatcher vsmm = new VectorSpaceModelMatcher();
        Alignment result = vsmm.match(testCase.getSourceOntology(OntModel.class), testCase.getTargetOntology(OntModel.class), inputAlignment, new Properties());
        for(Correspondence c : result){
            assertNotEquals(1.0, c.getConfidence()); 
        }
    }
    
}
