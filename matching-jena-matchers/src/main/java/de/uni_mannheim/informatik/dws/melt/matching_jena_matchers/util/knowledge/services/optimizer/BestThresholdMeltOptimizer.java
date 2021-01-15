package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge.services.optimizer;


import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;


import java.io.File;
import java.util.Arrays;
import java.util.Map;

public class BestThresholdMeltOptimizer {

    /*
    public static void main(String[] args) {
        //KnowledgeSourceEmbedding ks = null;
        ExecutionResultSet ers = new ExecutionResultSet();
        String pathToWordnetEntityFile = "/Users/janportisch/Documents/PhD/LREC_2020/Language_Models/wordnet/wordnet_entities.txt";
        String pathToWordnetVectorFile = "/Users/janportisch/Documents/PhD/LREC_2020/Language_Models/wordnet/sg200_wordnet_500_8_df_mc1_it3_reduced_vectors.kv";
        //ks = new KnowledgeSourceEmbedding(pathToWordnetVectorFile,
        //        pathToWordnetEntityFile, 1.0, new WordNetEmbeddingLinker(pathToWordnetEntityFile), "Wordnet");
        //BackgroundMatcher backgroundMatcher = new BackgroundMatcher(ks, ImplementedStrategies.SYNONYMY);
        //ers.addAll(Executor.run(TrackRepository.Anatomy.Default.getFirstTestCase(), backgroundMatcher, "WN_" + threshold));
        //ks.close();


        // initialization
        GridSearch gridSearch = new GridSearch(BackgroundMatcher.class, "Wiktionary Background Matcher");
        KnowledgeSource ks1 = new KnowledgeSourceEmbedding(pathToWordnetVectorFile, pathToWordnetEntityFile, 0.4, new WordNetEmbeddingLinker(pathToWordnetEntityFile), "Wordnet");
        KnowledgeSource ks2 = new KnowledgeSourceEmbedding(pathToWordnetVectorFile, pathToWordnetEntityFile, 0.6, new WordNetEmbeddingLinker(pathToWordnetEntityFile), "Wordnet");
        KnowledgeSource ks3 = new KnowledgeSourceEmbedding(pathToWordnetVectorFile, pathToWordnetEntityFile, 0.8, new WordNetEmbeddingLinker(pathToWordnetEntityFile), "Wordnet");
        //gridSearch.addStaticConstructorParameter((KnowledgeSource) ks, ImplementedStrategies.SYNONYMY);
        gridSearch.addConstructorParameter(Arrays.asList(ks1, ks2, ks3), KnowledgeSource.class);
        gridSearch.addConstructorParameter(Arrays.asList(ImplementedStrategies.SYNONYMY));

        // parameters
        //gridSearch.addParameter("knowledgeSource.threshold", 0.4, 0.6, 0.8);

        Map<String, IOntologyMatchingToolBridge> r = gridSearch.getMatcherConfigurations();

        // evaluation
        EvaluatorCSV evaluator = new EvaluatorCSV(gridSearch.runGridSequential(TrackRepository.Anatomy.Default));
        evaluator.writeToDirectory(new File("./results_test"));

    }
     */

}
