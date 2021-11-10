package de.uni_mannheim.informatik.dws.melt.examples.sentence_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersLoss;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.cli.*;
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
            case "zero":
            case "zeroshot":
            case "zeroshotevaluation":
                LOGGER.info("Mode: ZEROSHOT");
                zeroShotEvaluation(cliOptions);
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
        for (float fraction : cliOptions.getFractions()) {
            for (TestCase testCase : cliOptions.getTestCases()) {
                TestCase trainingCase = TrackRepository.generateTestCaseWithSampledReferenceAlignment(
                        testCase, fraction, 41, false);
                for(TextExtractor textExtractor : cliOptions.getTextExtractors()){
                    for(boolean additionallySwitchSourceTarget : cliOptions.getAdditionallySwitchSourceTarget()){
                        for(boolean isMultipleTextsToMultipleExamples : cliOptions.getMultiText()){
                            for(SentenceTransformersLoss loss : cliOptions.getLoss()){
                                for (String model : cliOptions.getTransformerModels()) {
                                    // Step 1: Training
                                    // ----------------

                                    String configurationName = "ftTestCase_" + model + "_" + fraction + "_" + textExtractor.getClass().getSimpleName() +
                                            "_isMulti_" + isMultipleTextsToMultipleExamples + "_isSwitch_" + additionallySwitchSourceTarget + "_loss_" + loss.toString() +
                                            "_" + testCase.getName();
                                    configurationName = configurationName.replaceAll(" ", "_");
                                    File finetunedModelFile = new File(targetDir, configurationName);
                                    
                                    TextExtractor modifiedTextExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);

                                    // Step 1.1.: Running the test case and generating training examples
                                    SentenceTransformersFineTuner fineTuner = new SentenceTransformersFineTuner(modifiedTextExtractor, model, finetunedModelFile);
                                    fineTuner.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
                                    fineTuner.setCudaVisibleDevices(gpu);
                                    fineTuner.setNumberOfEpochs(5);
                                    fineTuner.setAdditionallySwitchSourceTarget(additionallySwitchSourceTarget);
                                    fineTuner.setTransformersCache(transformersCache);
                                    fineTuner.setLoss(loss);

                                    TrainingPipeline trainingPipeline = new TrainingPipeline(fineTuner);

                                    Executor.run(trainingCase, trainingPipeline);

                                    // Step 1.2: Fine-Tuning the Model
                                    try {
                                        trainingPipeline.getFineTuner().finetuneModel();
                                    } catch (Exception e) {
                                        LOGGER.warn("Exception during training:", e);
                                    }

                                    // Step 2: Apply Model
                                    // -------------------
                                    SentenceTransformersMatcher matcher = new SentenceTransformersMatcher(modifiedTextExtractor, finetunedModelFile.getAbsolutePath());
                                    matcher.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
                                    matcher.setCudaVisibleDevices(gpu);
                                    matcher.setTransformersCache(transformersCache);
                                    
                                    Map<String, Object> matchers = new HashMap<>();
                                    matchers.put(configurationName, matcher);
                                    
                                    ers.addAll(Executor.run(trainingCase, matchers));
                                }
                            }
                        }
                    }
                }
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
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
    static void zeroShotEvaluation(CLIOptions cliOptions) {
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
