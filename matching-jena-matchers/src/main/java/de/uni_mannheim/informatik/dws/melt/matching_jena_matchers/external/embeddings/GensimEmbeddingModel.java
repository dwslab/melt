package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.MultiConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SynonymConfidenceCapability;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * This class represents a single gensim embedding model.
 * It allows for simplified usage in matching systems.
 */
public class GensimEmbeddingModel extends SemanticWordRelationDictionary implements SynonymConfidenceCapability {


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

    public boolean isInDictionary(String word) {
        return linker.linkToSingleConcept(word) != null;
    }

    @Override
    public Set<String> getSynonymsLexical(String linkedConcept) {
        // not to be implemented
        LOGGER.error("Not implemented.");
        return null;
    }

    @Override
    public Set<String> getHypernyms(String linkedConcept) {
        // not to be implemented
        LOGGER.error("Not implemented.");
        return null;
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public boolean isSynonymous(String linkedConcept1, String linkedConcept2) {
        return isStrongFormSynonymous(linkedConcept1, linkedConcept2);
    }

    /**
     * Given two sets, save for each concept in the first set the highest similarity that can be found by comparing
     * it with all concepts in the other set. Average the highest similarities.<br><br>
     * Example:<br>
     * Set 1: A, B; Set 2: C, D;<br>
     * sim(A, C) = 0.75<br>
     * sim(A, D) = 0.10<br>
     * sim(B, C) = 0.25<br>
     * sim(B, D) = 0.05<br>
     * This method will return (0.75 + 0.25)/2 = 0.5
     *
     *
     * @param links1 Set of links 1.
     * @param links2 Set of links 2.
     * @return Best average.
     */
    public double getBestCrossAverage(Set<String> links1, Set<String> links2){
        double totalSimilarity = 0.0;
        for(String link1 : links1){
            double similarity = 0.0;
            for(String link2 : links2){
                double checkSimilarity = gensim.getSimilarity(link1, link2, this.modelFilePath);
                if(checkSimilarity > similarity){
                    similarity = checkSimilarity;
                }
            }
            totalSimilarity += similarity;
        }
        return totalSimilarity / links1.size();
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
        double similarity = getSynonymyConfidence(linkedWord_1, linkedWord_2);
        return similarity > this.threshold;
    }

    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName() {
        return this.knowledgeSourceName;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    /**
     * If we have two multi-concept links, the similarity of the best combination is returned.
     *
     * Example:<br>
     * Set 1: A, B; Set 2: C, D;<br>
     * sim(A, C) = 0.75<br>
     * sim(A, D) = 0.10<br>
     * sim(B, C) = 0.25<br>
     * sim(B, D) = 0.05<br>
     * This method will return 0.75.
     *
     *
     * @param linkedConcept1 Link 1.
     * @param linkedConcept2 Link 2.
     * @return Confidence.
     */
    @Override
    public double getSynonymyConfidence(String linkedConcept1, String linkedConcept2) {
        if(linkedConcept1 == null || linkedConcept2 ==  null){
            return 0.0;
        }
        if(linker instanceof MultiConceptLinker){
            Set<String> uris1 = ((MultiConceptLinker) linker).getUris(linkedConcept1);
            Set<String> uris2 = ((MultiConceptLinker) linker).getUris(linkedConcept2);
            double bestScore = 0.0;

            for(String uri1 : uris1){
                for(String uri2 : uris2){
                    double score = gensim.getSimilarity(uri1, uri2, this.modelFilePath);
                    if(score > bestScore){
                        bestScore = score;
                    }
                }
            }
            return bestScore;
        } else {
            return gensim.getSimilarity(linkedConcept1, linkedConcept2, this.modelFilePath);
        }
    }

    @Override
    public double getStrongFormSynonymyConfidence(String linkedConcept1, String linkedConcept2) {
        return getSynonymyConfidence(linkedConcept1, linkedConcept2);
    }
}
