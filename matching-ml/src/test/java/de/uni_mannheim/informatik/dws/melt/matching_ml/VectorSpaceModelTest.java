package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.VectorSpaceModelMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class VectorSpaceModelTest {

    private static Logger LOGGER = LoggerFactory.getLogger(VectorSpaceModelTest.class);

    /**
     * This test requires that the cache contains the anatomy files, else this test will not execute.
     */
    @Test
    void isConfidenceCorrectlySet() {
        String separator = File.separator;
        String ontologyCache = System.getProperty("user.home") + separator + "oaei_track_cache" + separator + "oaei.webdatacommons.org" + separator;
        String anatomyTestCase = ontologyCache + "anatomy_track" + separator + "anatomy_track-default" + separator + "mouse-human-suite" + separator;

        File sourceFile = new File(anatomyTestCase + separator + "source.rdf");
        File targetFile = new File(anatomyTestCase + separator + "target.rdf");

        if(!sourceFile.exists()){
            LOGGER.error("Source file not found:" + sourceFile.getAbsolutePath() + "\nTest will not be executed.");
            return;
        }
        if(!targetFile.exists()){
            LOGGER.error("Target file not found:" + targetFile.getAbsolutePath() + "\nTest will not be executed.");
            return;
        }

        Alignment inputAlignment = new Alignment();
        inputAlignment.add("http://mouse.owl#MA_0000253", "http://human.owl#NCI_C33127");
        inputAlignment.add("http://mouse.owl#MA_0000253", "http://human.owl#NCI_C12292");

        VectorSpaceModelMatcher vsmm = new VectorSpaceModelMatcher();
        try {
            Alignment result = vsmm.match(getOntModel(sourceFile.getAbsolutePath()), getOntModel(targetFile.getAbsolutePath()), inputAlignment, new Properties());
            for (Correspondence c : result) {
                assertNotEquals(1.0, c.getConfidence());
            }
        } catch (Exception e) {
            LOGGER.error("An error occurred during the match operation.", e);
            fail();
        }
    }

    /**
     * Helper method to quickly load the ontology from the file path.
     * @param filePath The file path to the ontology.
     * @return Parsed OntModel.
     */
    private static OntModel getOntModel(String filePath){
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, null);
        ontModel.add(RDFDataMgr.loadModel(filePath));
        return ontModel;
    }

}
