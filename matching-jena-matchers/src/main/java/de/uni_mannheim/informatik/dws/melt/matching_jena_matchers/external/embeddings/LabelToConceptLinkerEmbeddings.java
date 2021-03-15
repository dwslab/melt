package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.LeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.nGramTokenizers.MaxGramLeftToRightTokenizer;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.TokenizeConcatUnderscoreCapitalizeModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.TokenizeConcatUnderscoreLowercaseModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.TokenizeConcatUnderscoreModifier;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.StringModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * LabelToConceptLinker with some additional functions required for embedding approaches.
 */
public abstract class LabelToConceptLinkerEmbeddings implements LabelToConceptLinker {


    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelToConceptLinkerEmbeddings.class);

    /**
     * The list of operations that is performed to find a concept in the dictionary.
     */
    LinkedList<StringModifier> stringModificationSequence;

    /**
     * Constructor
     *
     * @param entityFile File containing the entities that are available in the background knowledge source. One entity per line. UTF-8 encoded.
     */
    public LabelToConceptLinkerEmbeddings(File entityFile) {
        if (entityFile.isDirectory() || !entityFile.exists()) {
            LOGGER.error("The given file is a directory or does not exist. The linker will not work.");
        }
        lookupMap = readFileIntoHashMap(entityFile);
        stringModificationSequence = new LinkedList<>();
        stringModificationSequence.add(new TokenizeConcatUnderscoreModifier());
        stringModificationSequence.add(new TokenizeConcatUnderscoreCapitalizeModifier());
        stringModificationSequence.add(new TokenizeConcatUnderscoreLowercaseModifier());
    }

    /**
     * Constructor
     *
     * @param filePathToEntityFile The file path to the entity file as string.
     */
    public LabelToConceptLinkerEmbeddings(String filePathToEntityFile) {
        this(new File(filePathToEntityFile));
    }

    /**
     * Data lookup.
     */
    public Map<String, String> lookupMap;

    /**
     * Normalization
     *
     * @param stringToBeNormalized The String that shall be normalized.
     * @return Normalized version of the String.
     */
    public abstract String normalize(String stringToBeNormalized);

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        if (labelToBeLinked == null || labelToBeLinked.equals("")) return null;
        String lookupKey = normalize(labelToBeLinked);

        // plain lookup
        if (lookupMap.containsKey(lookupKey)) {
            return lookupMap.get(lookupKey);
        }

        // advanced lookup
        String modifiedConcept;
        for (StringModifier modifier : stringModificationSequence) {
            modifiedConcept = modifier.modifyString(labelToBeLinked);
            if (lookupMap.containsKey(modifiedConcept)) {
                return lookupMap.get(modifiedConcept);
            }
        }
        return null;
    }

    /**
     * Splits the labelToBeLinked in ngrams up to infinite size and tries to link components.
     * This corresponds to a MAXGRAM_LEFT_TO_RIGHT_TOKENIZER or NGRAM_LEFT_TO_RIGHT_TOKENIZER OneToManyLinkingStrategy.
     *
     * @param labelToBeLinked Label that shall be linked.
     * @return A set of concept URIs that were found.
     */
    private Set<String> linkLabelToTokensLeftToRight(String labelToBeLinked) {
        if (labelToBeLinked == null) return null;
        String[] tokens = StringOperations.tokenizeBestGuess(labelToBeLinked);
        LeftToRightTokenizer tokenizer = new MaxGramLeftToRightTokenizer(tokens, " ");
        HashSet<String> result = new HashSet<>();
        String resultingConcept = "";
        String token = tokenizer.getInitialToken();
        while (token != null) {
            resultingConcept = linkToSingleConcept(token);
            if (resultingConcept == null || resultingConcept.length() == 0) {
                token = tokenizer.getNextTokenNotSuccessful();
            } else {
                result.add(resultingConcept);
                token = tokenizer.getNextTokenSuccessful();
            }
        }
        return result;
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        if (labelToBeLinked == null || labelToBeLinked.equals("")) return null;
        Set<String> result = linkLabelToTokensLeftToRight(labelToBeLinked);
        int possibleConceptParts = StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(labelToBeLinked)).length;
        int actualConceptParts = 0;
        for (String s : result) {
            String normalized = this.normalize(s);
            actualConceptParts = actualConceptParts + StringOperations.clearArrayFromStopwords(StringOperations.tokenizeBestGuess(normalized)).length;
        }
        if (possibleConceptParts <= actualConceptParts) {
            return result;
        }
        return null;
    }

    /**
     * Read the HashSet of concepts/entities from file.
     *
     * @param file The file must be UTF-8 encoded.
     * @return The contents of the file as HashSet.
     */
    private Map<String, String> readFileIntoHashMap(File file) {
        HashMap<String, String> result = new HashMap<>();
        if (!file.exists()) {
            LOGGER.error("The specified file: " + file.getAbsolutePath() + " does not exist.");
            return result;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            String readLine;
            while ((readLine = reader.readLine()) != null) {
                String key = normalize(readLine);
                if (result.containsKey(key)) {
                    LOGGER.error("Clash on key " + key + " (concepts: " + readLine + " | " + result.get(key) + "\nResolution: Overwrite with first concept.");
                }
                result.put(key, readLine);
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found exception occurred while reading the file into a HashMap.", e);
        } catch (IOException e) {
            LOGGER.error("An IOException occurred while reading the file into a HashMap.", e);
        }
        return result;
    }

    public LinkedList<StringModifier> getStringModificationSequence() {
        return stringModificationSequence;
    }

    public void setStringModificationSequence(LinkedList<StringModifier> stringModificationSequence) {
        this.stringModificationSequence = stringModificationSequence;
    }
}
