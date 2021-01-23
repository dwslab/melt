package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.ExternalResource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SynonymCapability;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * This class represents a single gensim embedding model.
 * It allows for simplified usage in matching systems.
 */
public class GensimEmbeddingModel implements ExternalResource, SynonymCapability {

    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GensimEmbeddingModel.class);

    /**
     * The desired threshold to declare two concepts as synonymous / same.
     */
    public double threshold;

    /**
     * Gensim instance
     */
    public PythonServer gensim;

    /**
     * File to the vocabulary entries of the model.
     */
    public File entityFile;

    /**
     * Linker
     */
    public LabelToConceptLinker linker;

    /**
     * Required as String in order to build requests.
     */
    public String modelFilePath;

    /**
     * Name of the knowledge source used such as the name of the underlying corpus (can be used to generate matcher name).
     */
    public String knowledgeSourceName;

    /**
     * Constructor
     * @param pathToModelOrVectorFile The file path to the gensim model or gensim vector file.
     * @param pathToEntityFile The path to the vocabulary entries.
     * @param threshold The threshold that shall be used for the synonymy strategy.
     * @param linker The appropriate label to concept linker for the given embedding.
     * @param knowledgeSourceName The name of the knowledge source (will be used as matcher name)
     */
    public GensimEmbeddingModel(String pathToModelOrVectorFile, String pathToEntityFile, double threshold, LabelToConceptLinkerEmbeddings linker, String knowledgeSourceName) {
        this.threshold = threshold;
        File modelFile = new File(pathToModelOrVectorFile);
        if (!modelFile.exists() && !modelFile.isDirectory()) {
            LOGGER.error("The model file specified is either a directory or does not exist. ABORT.");
        }
        try {
            this.modelFilePath = modelFile.getCanonicalPath();
        } catch (IOException e) {
            LOGGER.error("Could not get canonical file path for file: " + pathToModelOrVectorFile + ". Requests will not work.");
        }
        this.entityFile = new File(pathToEntityFile);
        if (!modelFile.exists() && !modelFile.isDirectory()) {
            LOGGER.error("The entity specified is either a directory or does not exist. The linker will not work.");
        }
        this.linker = linker;
        this.knowledgeSourceName = knowledgeSourceName;
        this.gensim = PythonServer.getInstance();
    }

    @Override
    public boolean isInDictionary(String word) {
        return linker.linkToSingleConcept(word) != null;
    }

    @Override
    public HashSet<String> getSynonyms(String linkedConcept) {
        // not to be implemented
        LOGGER.error("Not implemented.");
        return null;
    }

    @Override
    public boolean isSynonymous(String linkedConcept1, String linkedConcept2) {
        return isStrongFormSynonymous(linkedConcept1, linkedConcept2);
    }


    /**
     * Note that the concepts have to be linked.
     * @param linkedWord_1 linked word 1
     * @param linkedWord_2 linked word 2
     * @return True if synonymous, else false.
     */
    @Override
    public boolean isStrongFormSynonymous(String linkedWord_1, String linkedWord_2) {
        if(linkedWord_1 == null || linkedWord_2 ==  null){
            return false;
        }
        double similarity = getSimilarity(linkedWord_1, linkedWord_2);
        return similarity > this.threshold;
    }

    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return this.knowledgeSourceName;
    }

    /**
     * Calculate the similarity given two linked concepts.
     * @param linkedWord_1 Linked concept 1.
     * @param linkedWord_2 Linked concept 2.
     * @return Similarity score. Higher values mean higher similarity.
     */
    public double getSimilarity(String linkedWord_1, String linkedWord_2){
        if(linkedWord_1 == null || linkedWord_2 ==  null){
            // LOGGER.warn("Could not calculate similarity. Will return 0!"); // leads to excessive logging for combined strategies
            return 0.0;
        }
        double similarity = gensim.getSimilarity(linkedWord_1, linkedWord_2, this.modelFilePath);
        return similarity;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
