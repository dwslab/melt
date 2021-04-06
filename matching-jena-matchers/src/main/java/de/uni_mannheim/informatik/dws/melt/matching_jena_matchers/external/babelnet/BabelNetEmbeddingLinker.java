package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SynonymConfidenceCapability;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.LabelToConceptLinkerEmbeddings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class BabelNetEmbeddingLinker extends LabelToConceptLinkerEmbeddings implements SynonymConfidenceCapability {


    private static final Logger LOGGER = LoggerFactory.getLogger(BabelNetEmbeddingLinker.class);

    /**
     * The file where the entities/concepts can be found.
     */
    File entityFile;

    /**
     * Name of the linker.
     */
    String nameOfLinker;

    /**
     * Set of all available entities.
     */
    Map<String, String> entities;

    /**
     * Constructor
     * @param entityFile The entity file.
     */
    public BabelNetEmbeddingLinker(File entityFile){
        super(entityFile);
    }

    /**
     * Constructor
     * @param pathToEntityFile The path to the entity file
     */
    public BabelNetEmbeddingLinker(String pathToEntityFile){
        this(new File(pathToEntityFile));
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        return this.entities.get(normalizeStatic(labelToBeLinked));
    }

    @Override
    public String getNameOfLinker() {
        return nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }

    /**
     * Normalize strings. Static so that behavior can be easily controlled by unit tests.
     * @param stringToBeNormalized The string that shall be normalized.
     * @return Normalized String.
     */
    public static String normalizeStatic(String stringToBeNormalized){
        String result = stringToBeNormalized.replaceAll(" ", "_");
        result = result.replaceAll("\\.", "_");
        result = result.replaceAll("-", "_");
        if(result.startsWith("bn:")){
            result = result.substring(3);
        }
        if(result.endsWith("_EN")){
            result = result.substring(0, result.length() - 5);
        }
        result = result.replaceAll(":", "_");
        return result;
    }

    @Override
    public String normalize(String stringToBeNormalized) {
        return normalizeStatic(stringToBeNormalized);
    }

    @Override
    public double getSynonymyConfidence(String linkedConcept1, String linkedConcept2) {
        return 0;
    }

    @Override
    public double getStrongFormSynonymyConfidence(String linkedConcept1, String linkedConcept2) {
        return 0;
    }
}
