package de.uni_mannheim.informatik.dws.melt.examples.llm_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherPipelineSequential;
import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorBasic;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCopyResults;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorRank;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.HighPrecisionMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.AdditionalConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BadHostsFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.AddAlignmentMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ConfidenceCombiner;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardAlwaysMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.LLMBase;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.LLMBinaryFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.LLMChooseGivenEntityFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is also the main class that will be run when executing the JAR.
 */
public class Main {
    static{ System.setProperty("log4j.skipJansi", "false"); }
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) throws Exception {
        CLIOptions cliOptions = new CLIOptions(args);
        MeltUtil.logWelcomeMessage();
        cliOptions.initializeStaticCmdParameters();
        if(cliOptions.getRecallGeneration().equals("only")){
            runRecallOnly(cliOptions);
        }else{
            run(cliOptions);
        }
        LOGGER.info("DONE");
    }
    
    
    
    private static void run(CLIOptions cliOptions) throws Exception {
        PythonServer.setOverridePythonFiles(false);
        
        ExecutionResultSet ers = new ExecutionResultSet();
        
        String gpu = cliOptions.getGPU();
        File transformersCache = cliOptions.getTransformersCache();
        File targetDir = cliOptions.getTargetDirectoryForModels();
        
        //inititialize biEncoder with the optimal configuration found earlier
        //this is also our "recall" matcher
        SentenceTransformersMatcher biEncoder = new SentenceTransformersMatcher(
            TextExtractor.appendStringPostProcessing(new TextExtractorSet(), StringProcessing::normalizeOnlyCamelCaseAndUnderscore),
            "multi-qa-mpnet-base-dot-v1"//"all-MiniLM-L6-v2"
        );
        biEncoder.setMultipleTextsToMultipleExamples(true);
        biEncoder.setCudaVisibleDevices(gpu);
        int k = cliOptions.getKNeighbours();
        biEncoder.setTopK(k); 
        biEncoder.setTransformersCache(transformersCache);        
        biEncoder.addResourceFilter(SentenceTransformersPredicateBadHosts.class);
        //biEncoder.addResourceFilter(SentenceTransformersPredicateInputAlignment.class);
        
        int maxTokens = cliOptions.getMaxTokens();
        for (TestCase testCase : cliOptions.getTestCases()) {
            String recallGenerationName = "";
            Alignment recallAlignment = null;
            switch(cliOptions.getRecallGeneration()){
                case "smalldummy":{
                    int easyCases = 10;
                    int hardCasesCount = 10;
                    Alignment highPrecision = Executor.runSingle(testCase, new HighPrecisionMatcher()).getSystemAlignment();
                    Alignment reference = testCase.getParsedReferenceAlignment();
                    
                    
                    List<Correspondence> hardCases = Alignment.createOrder(Alignment.subtraction(reference, highPrecision));
                    Collections.shuffle(hardCases, new Random(12345));
                    List<Correspondence> finalPositives = new ArrayList<>(hardCases.subList(0, Math.min(hardCases.size(), hardCasesCount)));
                    finalPositives.addAll(Alignment.createOrder(highPrecision).subList(0, Math.min(highPrecision.size(), easyCases)));
                    
                    File referenceAlignmentFile = File.createTempFile("ref_alignment", ".rdf");
                    referenceAlignmentFile.deleteOnExit();
                    new Alignment(finalPositives).serialize(referenceAlignmentFile);
                    testCase = new TestCase(testCase.getName(), testCase.getSource(), testCase.getTarget(), referenceAlignmentFile.toURI(), testCase.getTrack());
                            
                    Alignment candidates = Executor.runSingle(testCase, biEncoder).getSystemAlignment();
                    recallAlignment = new Alignment();
                    for(Correspondence c : finalPositives){
                        candidates.getCorrespondencesSource(c.getEntityOne()).forEach(recallAlignment::add);
                        candidates.getCorrespondencesSource(c.getEntityTwo()).forEach(recallAlignment::add);
                        recallAlignment.add(c);
                    }
                    recallGenerationName = "SmallDummyMatcher" + k;
                    ers.add(new ExecutionResult(testCase, recallGenerationName, recallAlignment, testCase.getParsedReferenceAlignment()));
                    break;
                }
                case "dummy":{
                    //generate the recall alignment by using only reference alignment and some dummy candidates
                    Alignment candidates = Executor.runSingle(testCase, biEncoder).getSystemAlignment();
                    recallAlignment = new Alignment();
                    for(Correspondence c : testCase.getParsedReferenceAlignment()){
                        candidates.getCorrespondencesSource(c.getEntityOne()).forEach(recallAlignment::add);
                        candidates.getCorrespondencesSource(c.getEntityTwo()).forEach(recallAlignment::add);
                        recallAlignment.add(c);
                    }
                    recallGenerationName = "DummyMatcher" + k;
                    ers.add(new ExecutionResult(testCase, recallGenerationName, recallAlignment, testCase.getParsedReferenceAlignment()));
                    break;
                }
                case "normal":{
                    //with high precision matcher
                    /*
                    ExecutionResultSet init = new ExecutionResultSet();
                    init.addAll(Executor.run(testCase, new MatcherPipelineSequential(new HighPrecisionMatcher(), new BadHostsFilter()), "HighPrecision"));

                    //generate the recall alignment once for the test case
                    Executor.runMatcherOnTop(init, "HighPrecision",
                            biEncoder,
                            "RecallMatcher" + k
                    );
                    recallAlignment = init.get(testCase, "RecallMatcher" + k).getSystemAlignment();
                    ers.addAll(init);
                    */
                    //without high precision matcher
                    
                    recallGenerationName = "RecallMatcher" + k;
                    
                    ExecutionResultSet recallMatcherResults = Executor.run(testCase, biEncoder, recallGenerationName);
                    recallAlignment = recallMatcherResults.get(testCase, recallGenerationName).getSystemAlignment();
                    ers.addAll(recallMatcherResults);
                    
                    break;
                }
                default:{
                    LOGGER.warn("Argument recall (rec) which is set to \"{}\" is not one of smalldummy, dummy, normal.", cliOptions.getRecallGeneration());
                    System.exit(1);
                    break;
                }
            }
            addConfidenceAndOneToOne(ers, testCase, recallGenerationName);
            
            for(Entry<String, TextExtractorMap> textExtractor : cliOptions.getTextExtractors()){
                for(String model : cliOptions.getTransformerModels()) {
                    LLMConfiguration modelConfig = LLMConfiguration.getConfiguration(model);
                    for(Entry<String, String> promt : cliOptions.getPrompts(textExtractor.getValue())) {

                        String configurationName = processModelName(model) + "promt" + promt.getKey() + cliOptions.isReplacePrompt() + cliOptions.isIncludeSystemPrompt() + 
                                "_loading" + cliOptions.isIncludeLoadingArguments() + "_" + textExtractor.getKey() +
                                "_isChoose" + cliOptions.isChoose() + "_" + recallGenerationName;
                        configurationName = configurationName.replaceAll(" ", "_");
                        File modelFolder = new File(targetDir, configurationName);

                        //TextExtractorMap modifiedTextExtractor = TextExtractorMap.appendStringPostProcessing(textExtractor.getValue(), StringProcessing::normalizeOnlyCamelCaseAndUnderscore);

                        String finalPromt = promt.getValue();
                        if(cliOptions.isReplacePrompt()){
                            finalPromt = finalPromt.replace("###", "~~~");
                        }
                        if(cliOptions.isIncludeSystemPrompt()){
                            finalPromt = modelConfig.processPromt(finalPromt);
                        }


                        //LLMBinaryFilter llmTransformersFilter = new LLMBinaryFilter(modifiedTextExtractor, model, finalPromt);
                        LLMBase llmTransformersFilter = cliOptions.isChoose() ? 
                                new LLMChooseGivenEntityFilter(textExtractor.getValue(), model, finalPromt) : 
                                new LLMBinaryFilter(textExtractor.getValue(), model, finalPromt);
                        llmTransformersFilter.setMultipleTextsToMultipleExamples(true);
                        llmTransformersFilter.setCudaVisibleDevices(gpu);
                        llmTransformersFilter.setTransformersCache(transformersCache);
                        if(cliOptions.isDebug()){
                            modelFolder.mkdirs();
                            llmTransformersFilter.setDebugFile(new File(modelFolder, "debug.txt"));
                        }

                        llmTransformersFilter
                                .addGenerationArgument("max_new_tokens", maxTokens)
                                .addGenerationArgument("temperature", 0.0);

                        if(cliOptions.isIncludeLoadingArguments()){
                            llmTransformersFilter.addLoadingArguments(modelConfig.getLoadingArguments());
                        }

                        ExecutionResultSet testCaseResults = Executor.run(testCase,
                                new MatcherPipelineSequential(
                                        new ForwardAlwaysMatcher(recallAlignment), 
                                        llmTransformersFilter,
                                        new ConfidenceCombiner(cliOptions.isChoose() ? LLMChooseGivenEntityFilter.class : LLMBinaryFilter.class)
                                ), 
                                configurationName
                        );


                        ExecutionResult highPrecision = Executor.runSingle(testCase, 
                                new MatcherPipelineSequential(new HighPrecisionMatcher(), new BadHostsFilter()), "HighPrecision");
                        testCaseResults.add(highPrecision);
                        Executor.runMatcherOnTop(testCaseResults, configurationName, 
                                new AddAlignmentMatcher(highPrecision.getSystemAlignment()), 
                                configurationName + "addhighprec"
                        );
                        
                        if(testCase.getTrack().getName().equals("conference")){
                            addFixedConfidenceAndOneToOne(testCaseResults, testCase, configurationName);
                            addFixedConfidenceAndOneToOne(testCaseResults, testCase, configurationName + "addhighprec");
                        }else{
                            addConfidenceAndOneToOne(testCaseResults, testCase, configurationName);
                            addConfidenceAndOneToOne(testCaseResults, testCase, configurationName + "addhighprec");
                        }
                        
                        
                        if(cliOptions.isChoose()){
                            Executor.runMatcherOnTop(testCaseResults, configurationName,
                                    new AdditionalConfidenceFilter(0.5, LLMChooseGivenEntityFilter.BINARY_ADDITIONAL_CONFIDENCE_KEY),
                                    configurationName + "_CutLLMDecision"
                            );
                            double bestConfidence = ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedReferenceAlignment(),
                                    testCaseResults.get(testCase, configurationName + "_CutLLMDecision").getSystemAlignment(),
                                    GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);

                            Executor.runMatcherOnTop(testCaseResults, configurationName + "_CutLLMDecision",
                                    new ConfidenceFilter(bestConfidence),
                                    configurationName + "_CutLLMDecision_CutBestConfidenc" + bestConfidence
                            );
                            
                            double bestConfidenceComplete = ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedReferenceAlignment(),
                                    testCaseResults.get(testCase, configurationName + "_CutLLMDecision").getSystemAlignment(),
                                    GoldStandardCompleteness.COMPLETE);

                            Executor.runMatcherOnTop(testCaseResults, configurationName + "_CutLLMDecision",
                                    new ConfidenceFilter(bestConfidenceComplete),
                                    configurationName + "_CutLLMDecision_CutBestCompleteConfidence" + bestConfidenceComplete
                            );

                            Executor.runMatcherOnTop(testCaseResults, configurationName + "_CutLLMDecision",
                                    new ConfidenceFilter(0.5),
                                    configurationName + "_CutLLMDecision_CutConfidence0.5"
                            );
                            
                        }
                        
                        /*
                        Executor.runMatcherOnTop(testCaseResults, configurationName,
                                new ConfidenceFilter(0.5),
                                configurationName + "_CutConfidence0.5"
                        );

                        Executor.runMatcherOnTop(testCaseResults, configurationName + "_CutConfidence0.5",
                                new MaxWeightBipartiteExtractor(),
                                configurationName + "_CutConfidence0.5_OneOne"
                        );

                        Executor.runMatcherOnTop(testCaseResults, configurationName, 
                                    new AlcomoFilter(), configurationName + "_Alcomo");

                        Executor.runMatcherOnTop(testCaseResults, configurationName + "_Alcomo", 
                                    new MaxWeightBipartiteExtractor(), configurationName + "_Alcomo_OneOne");
                        */
                        ers.addAll(testCaseResults);
                    }
                }
            }
            //OntologyCacheJena.emptyCache();
        }
        
        File resultsDir = Evaluator.getDirectoryWithCurrentTime();
        LOGGER.info("EvaluatorCopyResults");
        new EvaluatorCopyResults(ers).writeResultsToDirectory(resultsDir);
        LOGGER.info("EvaluatorBasic");
        new EvaluatorBasic(ers).writeToDirectory(resultsDir);
        LOGGER.info("EvaluatorRank");
        new EvaluatorRank(ers).writeToDirectory(resultsDir);
        LOGGER.info("EvaluatorCSV");
        new EvaluatorCSV(ers).writeToDirectory(resultsDir);
        LOGGER.info("Finish evaluating");
    }

    
    private static void addFixedConfidenceAndOneToOne(ExecutionResultSet testCaseResults, TestCase testCase, String oldMatcherName){
        Executor.runMatcherOnTop(testCaseResults, oldMatcherName,
                new MaxWeightBipartiteExtractor(),
                oldMatcherName + "_OneOne"
        );
        for(double d : Arrays.asList(0.5, 0.6, 0.7, 0.8, 0.9)){
            Executor.runMatcherOnTop(testCaseResults, oldMatcherName + "_OneOne",
                    new ConfidenceFilter(d),
                    oldMatcherName + "_OneOne_CutConfidence" + d
            );
        }
    }
    
    private static void addConfidenceAndOneToOne(ExecutionResultSet testCaseResults, TestCase testCase, String oldMatcherName){
        
        double bestConfidenceCrossEncoderF1 = ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedReferenceAlignment(),
                testCaseResults.get(testCase, oldMatcherName).getSystemAlignment(),
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);

        Executor.runMatcherOnTop(testCaseResults, oldMatcherName,
                new ConfidenceFilter(bestConfidenceCrossEncoderF1),
                oldMatcherName + "_CutBestConfidence" + bestConfidenceCrossEncoderF1
        );

        Executor.runMatcherOnTop(testCaseResults, oldMatcherName,
                new MaxWeightBipartiteExtractor(),
                oldMatcherName + "_OneOne"
        );

        double bestConfidence = ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedReferenceAlignment(),
                testCaseResults.get(testCase, oldMatcherName + "_OneOne").getSystemAlignment(),
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);

        Executor.runMatcherOnTop(testCaseResults, oldMatcherName + "_OneOne",
                new ConfidenceFilter(bestConfidence),
                oldMatcherName + "_OneOne_CutBestConfidence" + bestConfidence
        );
        
        double bestConfidenceComplete = ConfidenceFinder.getBestConfidenceForFmeasure(testCase.getParsedReferenceAlignment(),
                testCaseResults.get(testCase, oldMatcherName + "_OneOne").getSystemAlignment(),
                GoldStandardCompleteness.COMPLETE);

        Executor.runMatcherOnTop(testCaseResults, oldMatcherName + "_OneOne",
                new ConfidenceFilter(bestConfidence),
                oldMatcherName + "_OneOne_CutBestCompleteConfidence" + bestConfidenceComplete
        );
        
        Executor.runMatcherOnTop(testCaseResults, oldMatcherName + "_OneOne",
                new ConfidenceFilter(0.5),
                oldMatcherName + "_OneOne_CutConfidence0.5"
        );
    }
    
    
    
    private static void runRecallOnly(CLIOptions cliOptions) throws Exception {
        String gpu = cliOptions.getGPU();
        File transformersCache = cliOptions.getTransformersCache();
        ExecutionResultSet ers = new ExecutionResultSet();
        for (TestCase testCase : cliOptions.getTestCases()) {
            for(int k : Arrays.asList(1, 3, 5, 10)){
                for(String model : cliOptions.getTransformerModels()) {
                    SentenceTransformersMatcher biEncoder = new SentenceTransformersMatcher(
                        TextExtractor.appendStringPostProcessing(new TextExtractorSet(), StringProcessing::normalizeOnlyCamelCaseAndUnderscore),
                        model
                    );
                    biEncoder.setMultipleTextsToMultipleExamples(true);
                    biEncoder.setCudaVisibleDevices(gpu);
                    biEncoder.setTopK(k);
                    biEncoder.setTransformersCache(transformersCache);
                    ers.addAll(Executor.run(testCase, biEncoder, model + "_" + k));
                }
            }
        }
        LOGGER.info("EvaluatorCSV");
        new EvaluatorCSV(ers).writeToDirectory();
        LOGGER.info("Finish evaluating");
    }
        
    private static String processModelName(String modelName){
        int lastIndex = modelName.lastIndexOf("/");
        if(lastIndex >= 0){
            modelName = modelName.substring(lastIndex + 1);
        }
        lastIndex = modelName.lastIndexOf("\\");
        if(lastIndex >= 0){
            modelName = modelName.substring(lastIndex + 1);
        }
        return modelName;
    }
}
