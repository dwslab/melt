package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.OntologyCacheOwlApi;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;

import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class AlcomoFilterTest {


    private static final Logger LOGGER = LoggerFactory.getLogger(AlcomoFilterTest.class);

    @Test
    void urlTest() {
        try {
            File sourceFile = loadFile("cmt.owl");
            assertNotNull(sourceFile);
            File targetFile = loadFile("ekaw.owl");
            assertNotNull(targetFile);
            File referenceFile = loadFile("cmt-ekaw.rdf");
            assertNotNull(referenceFile);
            File systemFile = loadFile("csa-cmt-ekaw.rdf");
            assertNotNull(systemFile);
            Alignment systemAlignment = AlignmentParser.parse(systemFile);

            Correspondence wrongCorrespondence = new Correspondence("http://cmt#assignedByReviewer",
                    "http://ekaw#writtenBy");
            assertTrue(systemAlignment.contains(wrongCorrespondence));

            int alignmentSizeBefore = systemAlignment.size();

            AlcomoFilter filter = new AlcomoFilter();
            URL filteredAlignmentUrl = filter.match(sourceFile.toURI().toURL(),
                    targetFile.toURI().toURL(),
                    systemFile.toURI().toURL());
            Alignment filteredAlignment = AlignmentParser.parse(filteredAlignmentUrl);
            assertTrue(filteredAlignment.size() < alignmentSizeBefore);
            assertFalse(filteredAlignment.contains(wrongCorrespondence));

        } catch (Exception e){
            fail(e);
        }
    }

    @Test
    void owlApiTest() {
        try {
            File sourceFile = loadFile("cmt.owl");
            assertNotNull(sourceFile);
            File targetFile = loadFile("ekaw.owl");
            assertNotNull(targetFile);
            File referenceFile = loadFile("cmt-ekaw.rdf");
            assertNotNull(referenceFile);
            File systemFile = loadFile("csa-cmt-ekaw.rdf");
            assertNotNull(systemFile);
            Alignment systemAlignment = AlignmentParser.parse(systemFile);
            systemAlignment.addExtensionValue("http://www.jan-portisch.eu/example", "myValue");

            TestCase tc = new TestCase("TEST_CMT_EKAW", sourceFile.toURI(), targetFile.toURI(), referenceFile.toURI(),
                    null, systemFile.toURI(), GoldStandardCompleteness.COMPLETE, null);

            int alignmentSizeBefore = systemAlignment.size();

            AlcomoFilter filter = new AlcomoFilter();
            Alignment filteredAlignment = filter.match(OntologyCacheOwlApi.get(sourceFile.toURI().toURL()),
                    OntologyCacheOwlApi.get(targetFile.toURI().toURL()),
                    systemAlignment, null);

            assertTrue(filteredAlignment.size() < alignmentSizeBefore);
            assertEquals("myValue",filteredAlignment.getExtensions().get("http://www.jan-portisch.eu/example"));

        } catch (Exception e){
            fail(e);
        }
    }

    @Test
    void serializeAlignmentToTemporaryFile() {
        try {
            assertNotNull(AlcomoFilter.serializeAlignmentToTemporaryFile(new Alignment()));
            assertNotNull(AlcomoFilter.serializeAlignmentToTemporaryFile(null));
        } catch (AlcomoException e) {
            LOGGER.error("AlcomoException occurred.", e);
            fail("Exception must not be thrown.");
        }
    }

    @Test
    void serializeOntologyToTemporaryFile() {
        TestCase tc = TrackRepository.Conference.V1.getFirstTestCase();
        try {
            String o1path = AlcomoFilter.serializeOntologyToTemporaryFile(tc.getSourceOntology(OWLOntology.class));
            String o2path = AlcomoFilter.serializeOntologyToTemporaryFile(tc.getTargetOntology(OWLOntology.class));
            assertNotNull(o1path);
            assertNotNull(o2path);
            assertNotEquals(o1path, o2path);
        } catch (AlcomoException e) {
            LOGGER.error("AlcomoException occurred.", e);
            fail("Exception must not be thrown.");
        }
    }

    /**
     * Helper function to load files in class path that contain spaces.
     * @param fileName Name of the file.
     * @return File in case of success, else null.
     */
    private File loadFile(String fileName){
        try {
            URL resultUri =  this.getClass().getClassLoader().getResource(fileName);
            assertNotNull(resultUri);
            File result = FileUtils.toFile(resultUri.toURI().toURL());
            assertTrue(result.exists(), "Required resource not available.");
            return result;
        } catch (URISyntaxException | MalformedURLException exception){
            exception.printStackTrace();
            fail("Could not load file.", exception);
            return null;
        }
    }
}