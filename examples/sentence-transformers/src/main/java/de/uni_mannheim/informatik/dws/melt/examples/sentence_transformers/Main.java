package de.uni_mannheim.informatik.dws.melt.examples.sentence_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherPipelineSequential;
import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorBasic;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorRank;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ConfidenceCombiner;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardAlwaysMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives.AddNegativesViaAlignment;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
        String mode = cliOptions.getMode();
        switch (mode) {
            case "bi-encoder":
            case "biencoder":
                LOGGER.info("Mode: BI-ENCODER");
                biEncoder(cliOptions);
                break;
            case "tcfintune":
            case "tcfinetuned":
            case "tc_finetune":
            case "tc_finetuned":
            case "finetunedpertestcase":
                LOGGER.info("Mode: TC_FINETUNE");
                fineTunedPerTestCase(cliOptions);
                break;            
            default:
                LOGGER.warn("Mode '{}' not found.", mode);
                System.exit(1);
        }
        LOGGER.info("DONE");
    }
    

    /**
     * The model is fine-tuned per testcase.
     *
     * @param gpu               The GPU to be used.
     * @param tracks            Tracks to be evaluated.
     * @param transformerModels Models (Strings) to be used.
     * @param transformersCache Cache for transformers.
     * @param targetDir         Where the models shall be written to.
     */
    static void fineTunedPerTestCase(CLIOptions cliOptions) {
        ExecutionResultSet ers = new ExecutionResultSet();
        
        String gpu = cliOptions.getGPU();
        File transformersCache = cliOptions.getTransformersCache();
        File targetDir = cliOptions.getTargetDirectoryForModels();
        
        //inititialize biEncoder with the optimal configuration found earlier
        //this is also our "recall" matcher
        SentenceTransformersMatcher biEncoder = new SentenceTransformersMatcher(
            TextExtractor.appendStringPostProcessing(new TextExtractorSet(), StringProcessing::normalizeOnlyCamelCaseAndUnderscore),
            "all-MiniLM-L6-v2"
        );
        biEncoder.setMultipleTextsToMultipleExamples(true);
        biEncoder.setCudaVisibleDevices(gpu);
        biEncoder.setTopK(5);
        biEncoder.setTransformersCache(transformersCache);

        for (TestCase testCase : cliOptions.getTestCases()) {
            for(Entry<String, TestCase> trainingCase : cliOptions.getTestCaseWithPositives(testCase)){
                for(TextExtractor textExtractor : cliOptions.getTextExtractors()){
                    for(boolean additionallySwitchSourceTarget : cliOptions.getAdditionallySwitchSourceTarget()){
                        for(boolean isMultipleTextsToMultipleExamples : cliOptions.getMultiText()){
                            for (String model : cliOptions.getTransformerModels()) {
                                
                                String configurationName = "ftTestCase_" + model + "_" + trainingCase.getKey() + "_" + textExtractor.getClass().getSimpleName() +
                                        "_isMulti_" + isMultipleTextsToMultipleExamples + "_isSwitch_" + additionallySwitchSourceTarget +
                                        "_" + testCase.getName();
                                configurationName = configurationName.replaceAll(" ", "_");
                                File finetunedModelFile = new File(targetDir, configurationName);
                                
                                //generate the recall alignment once for the test case
                                ExecutionResultSet testCaseResults = Executor.run(testCase, biEncoder, configurationName + "_RecallMatcher");
                                Alignment recallAlignment = testCaseResults.get(testCase, configurationName + "_RecallMatcher").getSystemAlignment();
                                
                                //auto threshold for initial recall alignment
                                double bestConfidenceF1 = ConfidenceFinder.getBestConfidenceForFmeasure(trainingCase.getValue().getParsedInputAlignment(),
                                    recallAlignment,
                                    GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
                                Executor.runMatcherOnTop(testCaseResults, configurationName + "_RecallMatcher", 
                                        new ConfidenceFilter(bestConfidenceF1), configurationName + "_RecallMatcherCutConfidence");                                
                                Executor.runMatcherOnTop(testCaseResults, configurationName + "_RecallMatcherCutConfidence", 
                                        new MaxWeightBipartiteExtractor(), configurationName + "_RecallMatcherCutConfidenceOneOne");
                                
                                // Step 1: Training
                                // ----------------

                                

                                TextExtractor modifiedTextExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);

                                TransformersFineTuner fineTuner = new TransformersFineTuner(modifiedTextExtractor, model, finetunedModelFile);
                                fineTuner.setAdditionallySwitchSourceTarget(additionallySwitchSourceTarget);
                                fineTuner.setCudaVisibleDevices(gpu);
                                fineTuner.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
                                fineTuner.setTransformersCache(transformersCache);

                                //CrossEncoderTrainingPipeline trainingPipeline = new CrossEncoderTrainingPipeline(fineTuner, recallAlignment);
                                                                
                                Executor.run(trainingCase.getValue(),
                                        new MatcherPipelineSequential(new AddNegativesViaAlignment(recallAlignment), fineTuner));

                                // Step 1.2: Fine-Tuning the Model
                                try {
                                    fineTuner.finetuneModel();
                                } catch (Exception e) {
                                    LOGGER.warn("Exception during training:", e);
                                }

                                // Step 2: Apply Model
                                // -------------------
                                
                                TransformersFilter transformersFilter = new TransformersFilter(modifiedTextExtractor, finetunedModelFile.getAbsolutePath());
                                transformersFilter.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
                                transformersFilter.setCudaVisibleDevices(gpu);
                                transformersFilter.setTransformersCache(transformersCache);                                
                                if(recallAlignment.size() > 50_000){
                                    transformersFilter.setOptimizeAll(true);
                                }
                                
                                testCaseResults.addAll(Executor.run(testCase,
                                        new MatcherPipelineSequential(
                                                new ForwardAlwaysMatcher(recallAlignment), 
                                                transformersFilter,
                                                new ConfidenceCombiner(TransformersFilter.class)
                                        ), 
                                        configurationName + "_CrossEncoder"));
                                                                
                                //auto threshold for initial recall alignment
                                double bestConfidenceCrossEncoderF1 = ConfidenceFinder.getBestConfidenceForFmeasure(trainingCase.getValue().getParsedInputAlignment(),
                                    testCaseResults.get(testCase, configurationName + "_CrossEncoder").getSystemAlignment(),
                                    GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);
                                Executor.runMatcherOnTop(testCaseResults, configurationName + "_CrossEncoder", 
                                        new ConfidenceFilter(bestConfidenceCrossEncoderF1), configurationName + "_CrossEncoderCutConfidence");                                
                                Executor.runMatcherOnTop(testCaseResults, configurationName + "_CrossEncoderCutConfidence", 
                                        new MaxWeightBipartiteExtractor(), configurationName + "_CrossEncoderCutConfidenceOneOne");
                                
                                ers.addAll(testCaseResults);
                            }
                        }
                    }
                }
            }
        }
        
        File resultsDir = Evaluator.getDirectoryWithCurrentTime();
        new EvaluatorBasic(ers).writeToDirectory(resultsDir);
        new EvaluatorCSV(ers).writeToDirectory(resultsDir);
        new EvaluatorRank(ers).writeToDirectory(resultsDir);
    }


    /**
     * Performs a zero shot evaluation on the given models using the provided tracks.
     *
     * @param gpu               The GPU to be used.
     * @param transformerModels Models (Strings) to be used.
     * @param transformersCache Cache for transformers.
     * @param tracks            Tracks to be evaluated.
     * @throws Exception General exception.
     */
    static void biEncoder(CLIOptions cliOptions) {
        List<TestCase> testCases = cliOptions.getTestCases();

        ExecutionResultSet ers = new ExecutionResultSet();
        String gpu = cliOptions.getGPU();
        File transformersCache = cliOptions.getTransformersCache();
        for (String transformerModel : cliOptions.getTransformerModels()) {
            for(TextExtractor textExtractor : cliOptions.getTextExtractors()){
                for(Boolean isMultipleTextsToMultipleExamples : cliOptions.getMultiText()){
                    String configurationName =
                        "zero_" + transformerModel + "_isMulti_" + isMultipleTextsToMultipleExamples +
                                    "_" + textExtractor.getClass().getSimpleName();
                    TextExtractor modifiedTextExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);
                    SentenceTransformersMatcher matcher = new SentenceTransformersMatcher(modifiedTextExtractor, transformerModel);
                    matcher.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
                    matcher.setCudaVisibleDevices(gpu);
                    matcher.setTopK(5);
                    matcher.setTransformersCache(transformersCache);
                    
                    Map<String, Object> matchers = new HashMap<>();
                    matchers.put(configurationName, matcher);
                    ers.addAll(Executor.run(testCases, matchers));
                }
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }

}
