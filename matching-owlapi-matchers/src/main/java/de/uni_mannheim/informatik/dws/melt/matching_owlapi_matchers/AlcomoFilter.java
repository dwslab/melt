package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers;

import de.uni_mannheim.informatik.dws.alcomo.ExtractionProblem;
import de.uni_mannheim.informatik.dws.alcomo.Settings;
import de.uni_mannheim.informatik.dws.alcomo.exceptions.PCFException;
import de.uni_mannheim.informatik.dws.alcomo.mapping.Correspondence;
import de.uni_mannheim.informatik.dws.alcomo.mapping.Mapping;
import de.uni_mannheim.informatik.dws.alcomo.mapping.SemanticRelation;
import de.uni_mannheim.informatik.dws.alcomo.ontology.IOntology;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.MatcherYAAAOwlApi;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Properties;

import static de.uni_mannheim.informatik.dws.alcomo.Settings.BlackBoxReasoner.PELLET;
import static de.uni_mannheim.informatik.dws.alcomo.Settings.BlackBoxReasoner;


// TODO refactor to URL
public class AlcomoFilter extends MatcherYAAAOwlApi implements Filter {


    private static final Logger LOGGER = LoggerFactory.getLogger(AlcomoFilter.class);

    /**
     * True if a one-to-one alignment is required. Access only through getter/setter.
     */
    private boolean isOneToOneAlignment;
    private static final boolean IS_ONE_TO_ONE_ALIGNMENT_DEFAULT_VALUE = true;

    private ExtractionProblem extractionProblem;

    /**
     * The reasoner that is to be used. Access only through getter/setter.
     */
    private BlackBoxReasoner reasoner;
    private static final BlackBoxReasoner DEFAULT_REASONER = PELLET;

    /**
     * Constructor. Will set default values.
     */
    public AlcomoFilter() {
        setOneToOneAlignment(IS_ONE_TO_ONE_ALIGNMENT_DEFAULT_VALUE);
        setReasoner(DEFAULT_REASONER);
        try {
            extractionProblem = new ExtractionProblem(
                    ExtractionProblem.ENTITIES_CONCEPTSPROPERTIES,
                    ExtractionProblem.METHOD_OPTIMAL,
                    ExtractionProblem.REASONING_EFFICIENT
            );
        } catch (PCFException pe) {
            LOGGER.error("A problem occurred while trying to initialize the extraction problem. " +
                    "The matcher is likely not functional. You can set the extraction problem manually again " +
                    "before running the match method.");
        }

    }


    public Alignment match(OWLOntology source, OWLOntology target, Alignment inputAlignment, Properties p) throws Exception {
        String ontology1path = serializeOntologyToTemporaryFile(source);
        String ontology2path = serializeOntologyToTemporaryFile(target);
        String alignmentPath = serializeAlignmentToTemporaryFile(inputAlignment);

        // load ontologies
        IOntology sourceOntology = new IOntology(ontology1path);
        IOntology targetOntology = new IOntology(ontology2path);

        // load the alignment
        Mapping mapping = new Mapping(alignmentPath);

        // extraction problem has already been created; let's bind ontologies and alignment
        extractionProblem.bindSourceOntology(sourceOntology);
        extractionProblem.bindTargetOntology(targetOntology);
        extractionProblem.bindMapping(mapping);

        // let's solve the problem
        extractionProblem.solve();

        // return the filtered alignment
        Mapping resultingAlcomoMapping = extractionProblem.getExtractedMapping();

        return null;
    }

    public static Alignment alcomoAlignmentToYaaaAlignment(Mapping alcomoAlignment) {
        Alignment result = new Alignment();
        Iterator<Correspondence> iterator = alcomoAlignment.iterator();
        while (iterator.hasNext()) {
            Correspondence alcomoCorrespondence = iterator.next();
            result.add(
                    alcomoCorrespondence.getSourceEntityUri(),
                    alcomoCorrespondence.getTargetEntityUri(),
                    alcomoCorrespondence.getConfidence(),
                    alcomoCorrespondence.getRelation()
            );
        }
    }

    public static CorrespondenceRelation alcomoSemanticRelationToYaaaCorrespondenceRelation(int alcomoSemanticRelationCode) {
        switch (alcomoSemanticRelationCode) {
            case SemanticRelation.EQUIV:
                return CorrespondenceRelation.EQUIVALENCE;
            case SemanticRelation.SUB:
                return CorrespondenceRelation.SUBSUMED;
            case SemanticRelation.SUPER:
                return CorrespondenceRelation.SUBSUME;
            case SemanticRelation.DIS:
                return CorrespondenceRelation.INCOMPAT;
            case SemanticRelation.NA:
            default:
                return CorrespondenceRelation.UNKNOWN;
        }
    }


    /**
     * Serialize the alignment to a file and return its path.
     *
     * @param alignment The alignment to be serialized.
     * @return The path (as String) to the serialized alignment.
     * @throws AlcomoException Thrown if there are problems with the file.
     */
    static String serializeAlignmentToTemporaryFile(Alignment alignment) throws AlcomoException {
        if (alignment == null) {
            alignment = new Alignment();
        }
        try {
            // create a temporary file
            File alignmentFile = Files.createTempFile("alignmentSerializationForAlcomo", null).toFile();
            alignmentFile.deleteOnExit();

            // serialize alignment
            alignment.serialize(alignmentFile);

            // return path
            return alignmentFile.getAbsolutePath();
        } catch (IOException e) {
            final String errorMessage = "A problem occurred while trying to serialize the alignment to file.";
            LOGGER.error(errorMessage, e);
            throw new AlcomoException(errorMessage, e);
        }
    }

    /**
     * Serializes the ontology to a file and returns its path.
     *
     * @param ontology The ontology of which the file path shall be obtained.
     * @return String file path.
     * @throws AlcomoException Risen if there occurs an exception in the process.
     */
    static String serializeOntologyToTemporaryFile(OWLOntology ontology) throws AlcomoException {
        if (ontology == null) {
            throw new AlcomoException("The ontology does not exist.", null);
        }
        try {
            // create a temporary file
            File ontologyFile = Files.createTempFile("ontologyForAlcomo", null).toFile();
            ontologyFile.deleteOnExit();

            // serialize ontology
            ontology.getOWLOntologyManager().saveOntology(ontology, new FileOutputStream(ontologyFile));

            // return path
            return ontologyFile.getAbsolutePath();
        } catch (IOException e) {
            final String errorMessage = "A problem occurred while trying to serialize the alignment to file.";
            LOGGER.error(errorMessage, e);
            throw new AlcomoException(errorMessage, e);
        } catch (OWLOntologyStorageException e) {
            final String errorMessage = "Could not save ontology to file.";
            LOGGER.error(errorMessage, e);
            throw new AlcomoException(errorMessage, e);
        }
    }

    public boolean isOneToOneAlignment() {
        return isOneToOneAlignment;
    }

    public void setOneToOneAlignment(boolean oneToOneAlignment) {
        isOneToOneAlignment = oneToOneAlignment;
        Settings.ONE_TO_ONE = oneToOneAlignment;
    }

    public BlackBoxReasoner getReasoner() {
        return reasoner;
    }

    public void setReasoner(BlackBoxReasoner reasoner) {
        this.reasoner = reasoner;
        Settings.BLACKBOX_REASONER = reasoner;
    }

    public ExtractionProblem getExtractionProblem() {
        return extractionProblem;
    }

    public void setExtractionProblem(ExtractionProblem extractionProblem) {
        this.extractionProblem = extractionProblem;
    }
}
