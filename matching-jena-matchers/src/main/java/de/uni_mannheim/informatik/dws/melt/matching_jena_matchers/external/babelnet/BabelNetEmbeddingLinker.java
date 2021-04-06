package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SynonymConfidenceCapability;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.LabelToConceptLinkerEmbeddings;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class BabelNetEmbeddingLinker extends LabelToConceptLinkerEmbeddings implements SynonymConfidenceCapability {


    private static final Logger LOGGER = LoggerFactory.getLogger(BabelNetEmbeddingLinker.class);

    /**
     * Name of the linker.
     */
    String nameOfLinker;

    /**
     * Constructor
     * @param entityFile The entity file.
     */
    public BabelNetEmbeddingLinker(File entityFile){
        super(entityFile);
        stringModificationSequence.add(new PlainModifier());
        stringModificationSequence.add(new TokenizeConcatUnderscoreLowercaseModifier());
        stringModificationSequence.add(new TokenizeConcatUnderscoreCapitalizeModifier());
        stringModificationSequence.add(new TokenizeConcatUnderscoreCapitalizeFirstLetterModifier());
    }

    /**
     * A set of string operations that are all performed.
     */
    List<StringModifier> stringModificationSequence = new ArrayList<>();

    /**
     * Constructor
     * @param pathToEntityFile The path to the entity file
     */
    public BabelNetEmbeddingLinker(String pathToEntityFile){
        this(new File(pathToEntityFile));
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        if(labelToBeLinked == null){
            return null;
        }
        for(StringModifier modifier : stringModificationSequence) {
            String modifiedLabel = modifier.modifyString(labelToBeLinked);
            String link = super.getLookupMap().get(normalizeStatic(modifiedLabel));
            if(link != null){
                return link;
            }
        }
        return null;
    }

    @Override
    public String getNameOfLinker() {
        return nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }

    private static final String URI_START_TOKEN = "http://babelnet.org/rdf/";

    /**
     * Normalize strings. Static so that behavior can be easily controlled by unit tests.
     * @param stringToBeNormalized The string that shall be normalized.
     * @return Normalized String.
     */
    public static String normalizeStatic(String stringToBeNormalized){
        String result = stringToBeNormalized;
        if(result.startsWith(URI_START_TOKEN)){
            result = result.substring(URI_START_TOKEN.length());
        }
        result = result.replaceAll(" ", "_");
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
