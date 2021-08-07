package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherAnatomy;
import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherKgTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorBasic;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorForTransformers;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorShortAndLongTexts;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Text;

/**
 * This class is also the main class that will be run when executing the JAR.
 */
public class Main {


    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);


    public static void main(String[] args) throws Exception {
        //CLI setup:
        Options options = new Options();

        options.addOption(Option.builder("g")
                .longOpt("gpu")
                .hasArg()
                .desc("Which GPUs to use. This can be comma separated. Eg. 0,1 which uses GPU zero and one.")
                .build());

        options.addOption(Option.builder("tc")
                .longOpt("transformerscache")
                .hasArg()
                .desc("The file path to the transformers cache.")
                .build());

        options.addOption(Option.builder("p")
                .longOpt("python")
                .hasArg()
                .desc("The python command to use.")
                .build());

        options.addOption(Option.builder("c")
                .longOpt("cache")
                .hasArg()
                .argName("path")
                .desc("The path to the cache folder for ontologies.")
                .build());

        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print this help message.")
                .build());

        options.addOption(Option.builder("tm")
                .longOpt("transformermodels")
                .hasArgs()
                .valueSeparator(' ')
                .desc("The transformer models to be used, separated by space.")
                .build());

        options.addOption(Option.builder("tracks")
                .longOpt("tracks")
                .required()
                .hasArgs()
                .valueSeparator(' ')
                .desc("The tracks to be used, separated by spaces.")
                .build()
        );

        options.addOption(Option.builder("f")
                .longOpt("fractions")
                .hasArgs()
                .valueSeparator(' ')
                .desc("The training fraction to be used for training, space separated. Example:\n" +
                        "--fractions 0.1 0.2 0.3")
                .build()
        );

        options.addOption(Option.builder("m")
                .longOpt("mode")
                .hasArg()
                .desc("Available modes: BASELINE, BASELINE_LIGHT, ZEROSHOT, TC_FINETUNE, TRACK_FINETUNE, " +
                        "TRACK_FINETUNE_HP, GLOBAL_FINETUNE")
                .required()
                .build()
        );

        options.addOption(Option.builder("mt")
                .longOpt("multitext")
                .desc("Switch, no arguments. If added, multiple text parts for one element will lead " +
                        "to multiple examples.")
                .build()
        );

        options.addOption(Option.builder("sp")
                .longOpt("serverport")
                .hasArg()
                .desc("The port of the python server.")
                .build()
        );

        options.addOption(Option.builder("at")
                .longOpt("autothresholding")
                .desc("Switch, no arguments. If added, an automated threshold will be set.")
                .build());

        // configure the extractor options here...
        List<TextExtractor> extractorList = new ArrayList<>();
        extractorList.add(new TextExtractorSet());
        extractorList.add(new TextExtractorShortAndLongTexts());
        extractorList.add(new TextExtractorForTransformers());

        StringBuilder extractorDescription = new StringBuilder();
        extractorDescription.append("The extractor to be used. Reference by number. Options:\n");
        int i = 0;
        for (TextExtractor te : extractorList) {
            extractorDescription.append("(")
                    .append(i++)
                    .append(") ")
                    .append(te.getClass().getSimpleName())
                    .append("\n");
        }

        options.addOption(Option.builder("te")
                .longOpt("textextractor")
                .desc(extractorDescription.toString())
                .hasArg()
                .build());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }

        if (cmd.hasOption("p")) {
            String p = cmd.getOptionValue("p");
            LOGGER.info("Setting python command to {}", p);
            PythonServer.setPythonCommandBackup(p);
        }

        if (cmd.hasOption("sp")) {
            String p = cmd.getOptionValue("sp");
            try {
                int port = Integer.parseInt(p);
                LOGGER.info("Setting python port to {}", port);
                PythonServer.setPort(port);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Argument serverport (sp) which is set to \"{}\" is not a number.", p);
                System.exit(1);
            }
        }

        if (cmd.hasOption("c")) {
            File cacheFile = new File(cmd.getOptionValue("c"));
            Track.setCacheFolder(cacheFile);
        }

        String gpu = cmd.getOptionValue("g", "");
        File transformersCache = null;
        if (cmd.hasOption("tc")) {
            transformersCache = new File(cmd.getOptionValue("tc"));
        }

        boolean isMultipleTextsToMultipleExamples = cmd.hasOption("mt");
        boolean isAutoThresholding = cmd.hasOption("at");

        transformerModels = cmd.getOptionValues("tm");

        TextExtractor textExtractor;
        if (cmd.hasOption("te")) {
            String teOption = cmd.getOptionValue("te");
            int parseTeOption = 0;
            try {
                parseTeOption = Integer.parseInt(teOption);
            } catch (NumberFormatException ex) {
                LOGGER.warn("Argument textextractor (-te) which is set to \"{}\" is not a number.", teOption);
                System.exit(1);
            }
            //range check
            if(parseTeOption < 0 || parseTeOption >= extractorList.size()){
                LOGGER.warn("Argument textextractor (-te) which is set to \"{}\" is not in the range 0-{}.", teOption, extractorList.size()-1);
                System.exit(1);
            }
            
            try {
                textExtractor = extractorList.get(parseTeOption);
                LOGGER.info("Using text extractor: " + textExtractor.getClass().getSimpleName());
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("Argument textextractor (-te) is set to {} but the index does not correspong to a text extractor.",teOption);
                System.exit(1);
                return; //needed because otherwise it will not compile (variable textExtractor may not be initialized)
            }
        } else {
            textExtractor = extractorList.get(0);
            LOGGER.info("Text extractor (Option -te) not specified. Using default: " +
                    textExtractor.getClass().getSimpleName());
        }

        String[] trackStrings;
        if (cmd.hasOption("tracks")) {
            trackStrings = cmd.getOptionValues("tracks");
            for (String trackString : trackStrings) {
                trackString = trackString.toLowerCase(Locale.ROOT).trim();
                switch (trackString) {
                    case "conference":
                        tracks.add(TrackRepository.Conference.V1);
                        break;
                    case "anatomy":
                        tracks.add(TrackRepository.Anatomy.Default);
                        break;
                    case "kg":
                    case "knowledge-graphs":
                    case "knowledgegraphs":
                    case "knowledgegraph":
                        tracks.add(TrackRepository.Knowledgegraph.V4);
                        break;
                    default:
                        LOGGER.warn("Could not map track: " + trackString);
                        System.exit(1);
                }
            }
        }

        //check track parameter:
        if (tracks == null || tracks.isEmpty()) {
            LOGGER.warn("No tracks specified. ABORTING program.");
            System.exit(1);
        }

        String mode = cmd.getOptionValue("m").toLowerCase(Locale.ROOT).trim();

        File targetDirForModels = new File("./models");

        switch (mode) {
            case "zero":
            case "zeroshot":
            case "zeroshotevaluation":
                checkTransformerParameter();
                LOGGER.info("Mode: ZEROSHOT");
                zeroShotEvaluation(gpu, transformerModels, transformersCache, tracks,
                        isMultipleTextsToMultipleExamples, textExtractor, isAutoThresholding);
                break;
            case "tcfintune":
            case "tcfinetuned":
            case "tc_finetune":
            case "tc_finetuned":
            case "finetunedpertestcase":
                checkTransformerParameter();
                LOGGER.info("Mode: TC_FINETUNE");
                fineTunedPerTestCase(gpu, tracks, getFractions(cmd), transformerModels, transformersCache,
                        targetDirForModels, isMultipleTextsToMultipleExamples, textExtractor, isAutoThresholding);
                break;
            case "track_finetune":
            case "track_finetuned":
            case "tracks_finetune":
            case "tracks_finetuned":
            case "finetunedpertrack":
                checkTransformerParameter();
                LOGGER.info("Mode: TRACK_FINETUNE");
                fineTunedPerTrack(gpu, tracks, getFractions(cmd), transformerModels,
                        transformersCache, targetDirForModels, isMultipleTextsToMultipleExamples,
                        textExtractor, isAutoThresholding);
                break;
            case "track_finetune_hp":
            case "track_finetuned_hp":
            case "tracks_finetune_hp":
            case "tracks_finetuned_hp":
            case "finetunedpertrackhp":
                checkTransformerParameter();
                LOGGER.info("Mode: TRACK_FINETUNE_HP");
                fineTunedPerTrackWithHyperparameter(gpu, tracks, getFractions(cmd), transformerModels,
                        transformersCache, targetDirForModels, isMultipleTextsToMultipleExamples,
                        textExtractor, isAutoThresholding);
                break;
            case "global_finetune":
            case "global_finetuned":
            case "finetune_global":
            case "finetuned_global":
                checkTransformerParameter();
                LOGGER.info("Mode: GLOBAL_FINETUNE");
                globalFineTuning(gpu, tracks, getFractions(cmd), transformerModels, transformersCache,
                        targetDirForModels, isMultipleTextsToMultipleExamples, textExtractor, isAutoThresholding);
                break;
            case "baseline":
            case "baselines":
                LOGGER.info("Mode: BASELINE");
                baselineMatchers(tracks, false);
                break;
            case "baseline_light":
            case "light_baseline":
                LOGGER.info("Mode: BASELINE_LIGHT");
                baselineMatchers(tracks, true);
                break;
            default:
                LOGGER.warn("Mode '{}' not found.", mode);
                System.exit(1);
        }

        LOGGER.info("DONE");
    }


    private static void checkTransformerParameter() {
        if (transformerModels == null || transformerModels.length == 0) {
            LOGGER.warn("No transformer model specified. ABORTING program.");
            System.exit(1);
        }
    }

    /**
     * This helper method simply parses the provided training fractions as float array.
     *
     * @param cmd The command line.
     * @return Float array. Will default to a value if no fractions parameter is provided.
     */
    static Float[] getFractions(CommandLine cmd) {
        if (!cmd.hasOption("f")) {
            LOGGER.error("No fractions provided. Please provide them space-separated via the -f option, e.g.:\n" +
                    "-f 0.2 0.4\n" +
                    "Using default: 0.2.");
            return new Float[]{0.2f};
        }
        String[] fractions = cmd.getOptionValues("f");
        return Arrays.stream(fractions).map(Float::parseFloat).collect(Collectors.toList()).toArray(new Float[1]);
    }

    // external access required for used for unit-testing (therefore, not in main)
    static List<Track> tracks = new ArrayList<>();
    static String[] transformerModels;


    static void fineTunedPerTrack(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                  File transformersCache, File targetDir, boolean isMultipleTextsToMultipleExamples,
                                  TextExtractor textExtractor, boolean isAutoThresholding) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        for (float fraction : fractions) {
            for (String model : transformerModels) {
                for (Track track : tracks) {

                    List<TestCase> trainingTestCases = TrackRepository.generateTrackWithSampledReferenceAlignment(track,
                            fraction, 41, false);

                    // Step 1 Training
                    String configurationName =
                            "ftTrack_" + track.getName() + "_" + model + "_" + fraction + "_" +
                                    textExtractor.getClass().getSimpleName() +
                                    "_isMulti_" + isMultipleTextsToMultipleExamples +
                                    "_isAutoThreshold_" + isAutoThresholding;
                    configurationName = configurationName.replaceAll(" ", "_");
                    File finetunedModelFile = new File(targetDir, configurationName);

                    MatcherYAAAJena recallMatcher;
                    if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                        recallMatcher = new RecallMatcherKgTrack();
                    } else {
                        recallMatcher = (new RecallMatcherAnatomy());
                    }
                    TrainingPipeline trainingPipeline = new TrainingPipeline(gpu, model, finetunedModelFile,
                            transformersCache, recallMatcher, isMultipleTextsToMultipleExamples,
                            textExtractor);

                    Executor.run(trainingTestCases, trainingPipeline, configurationName);
                    try {
                        trainingPipeline.getFineTuner().finetuneModel();
                    } catch (Exception e) {
                        LOGGER.warn("Exception during training:", e);
                    }

                    // Step 2: Apply Model
                    ers.addAll(Executor.run(trainingTestCases, new ApplyModelPipeline(gpu, finetunedModelFile.getAbsolutePath(),
                            transformersCache, recallMatcher, isMultipleTextsToMultipleExamples,
                            textExtractor, isAutoThresholding), configurationName));
                }
            }
        } // end of fractions loop

        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }
    
    static void fineTunedPerTrackWithHyperparameter(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                  File transformersCache, File targetDir, boolean isMultipleTextsToMultipleExamples,
                                  TextExtractor textExtractor, boolean isAutoThresholding) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        for (float fraction : fractions) {
            for (String model : transformerModels) {
                for (Track track : tracks) {

                    List<TestCase> trainingTestCases = TrackRepository.generateTrackWithSampledReferenceAlignment(track,
                            fraction, 41, false);

                    // Step 1 Training
                    String configurationName =
                            "fthpTrack_" + track.getName() + model + "_" + fraction + "_" +
                                    textExtractor.getClass().getSimpleName() +
                                    "_isMulti_" + isMultipleTextsToMultipleExamples +
                                    "_isAutoThreshold_" + isAutoThresholding;
                    configurationName = configurationName.replaceAll(" ", "_");
                    File finetunedModelFile = new File(targetDir, configurationName);

                    MatcherYAAAJena recallMatcher;
                    if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                        recallMatcher = new RecallMatcherKgTrack();
                    } else {
                        recallMatcher = (new RecallMatcherAnatomy());
                    }
                    TrainingPipelineHyperparameterTuning trainingPipeline = new TrainingPipelineHyperparameterTuning(
                            gpu, model, finetunedModelFile,
                            transformersCache, recallMatcher, isMultipleTextsToMultipleExamples,
                            textExtractor);

                    Executor.run(trainingTestCases, trainingPipeline, configurationName);
                    try {
                        trainingPipeline.getFineTuner().finetuneModel();
                    } catch (Exception e) {
                        LOGGER.warn("Exception during training:", e);
                    }

                    // Step 2: Apply Model
                    ers.addAll(Executor.run(trainingTestCases, new ApplyModelPipeline(gpu, finetunedModelFile.getAbsolutePath(),
                            transformersCache, recallMatcher, isMultipleTextsToMultipleExamples,
                            textExtractor, isAutoThresholding), configurationName));
                }
            }
        } // end of fractions loop

        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }

    /**
     * One fine-tuned model for all data (all tracks).
     *
     * @param gpu               The GPU to be used.
     * @param tracks            Tracks to be evaluated.
     * @param fractions         Fractions (training share in train-test split) that shall be evaluated as float array.
     * @param transformerModels Models (Strings) to be used.
     * @param transformersCache Cache for transformers.
     * @param targetDir         Where the models shall be written to.
     */
    static void globalFineTuning(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                 File transformersCache, File targetDir, boolean isMultipleTextsToMultipleExamples,
                                 TextExtractor textExtractor, boolean isAutoThresholding) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        for (float fraction : fractions) {
            for (String model : transformerModels) {

                String configurationName =
                        "ftGlobal_" + model + "_" + fraction + "_" + textExtractor.getClass().getSimpleName() +
                                "_ismulti" + isMultipleTextsToMultipleExamples +
                                "_isAutoThreshold_" + isAutoThresholding;
                File finetunedModelFile = new File(targetDir, configurationName);

                TrainingPipeline trainingPipeline = new TrainingPipeline(gpu, model, finetunedModelFile,
                        transformersCache, new RecallMatcherAnatomy(), isMultipleTextsToMultipleExamples,
                        textExtractor);

                for (Track track : tracks) {
                    List<TestCase> trainingTestCases = TrackRepository.generateTrackWithSampledReferenceAlignment(track,
                            fraction, 41, false);
                    MatcherYAAAJena recallMatcher;
                    if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                        recallMatcher = new RecallMatcherKgTrack();
                    } else {
                        recallMatcher = (new RecallMatcherAnatomy());
                    }
                    trainingPipeline.setRecallMatcher(recallMatcher);
                    Executor.run(trainingTestCases, trainingPipeline, configurationName);
                }

                // train
                try {
                    trainingPipeline.getFineTuner().finetuneModel();
                } catch (Exception e) {
                    LOGGER.warn("Exception during training:", e);
                }

                // evaluate
                for (Track track : tracks) {
                    MatcherYAAAJena recallMatcher;
                    if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                        recallMatcher = new RecallMatcherKgTrack();
                    } else {
                        recallMatcher = (new RecallMatcherAnatomy());
                    }
                    ers.addAll(Executor.run(track, new ApplyModelPipeline(gpu, finetunedModelFile.getAbsolutePath(),
                                    transformersCache, recallMatcher, isMultipleTextsToMultipleExamples, textExtractor,
                                    isAutoThresholding),
                            configurationName));
                }
            }
        } // end of fractions loop

        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
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
    static void fineTunedPerTestCase(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                     File transformersCache, File targetDir,
                                     boolean isMultipleTextsToMultipleExamples,
                                     TextExtractor textExtractor, boolean isAutoThresholding) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        outerLoop:
        for (float fraction : fractions) {
            for (Track track : tracks) {
                for (TestCase testCase : track.getTestCases()) {

                    TestCase trainingCase = TrackRepository.generateTestCaseWithSampledReferenceAlignment(testCase,
                            fraction,
                            41, false);

                    for (String model : transformerModels) {
                        // Step 1: Training
                        // ----------------

                        String configurationName = "ftTestCase_" + model + "_" + fraction + "_" + textExtractor.getClass().getSimpleName() +
                                "_isMulti_" + isMultipleTextsToMultipleExamples + "_" + testCase.getName() +
                                "_isAutoThreshold_" + isAutoThresholding;
                        configurationName = configurationName.replaceAll(" ", "_");
                        File finetunedModelFile = new File(targetDir, configurationName);


                        // Step 1.1.: Running the test case
                        MatcherYAAAJena recallMatcher;
                        if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                            recallMatcher = new RecallMatcherKgTrack();
                        } else {
                            recallMatcher = (new RecallMatcherAnatomy());
                        }
                        TrainingPipeline trainingPipeline = new TrainingPipeline(gpu, model, finetunedModelFile,
                                transformersCache, recallMatcher, isMultipleTextsToMultipleExamples,
                                textExtractor);

                        Executor.run(trainingCase, trainingPipeline, configurationName);

                        // Step 1.2: Fine-Tuning the Model
                        try {
                            trainingPipeline.getFineTuner().finetuneModel();
                        } catch (Exception e) {
                            LOGGER.warn("Exception during training:", e);
                        }

                        // Step 2: Apply Model
                        // -------------------
                        ers.addAll(Executor.run(trainingCase, new ApplyModelPipeline(gpu, finetunedModelFile.getAbsolutePath(),
                                        transformersCache, recallMatcher, isMultipleTextsToMultipleExamples,
                                        textExtractor, isAutoThresholding),
                                configurationName));
                        //break outerLoop;

                        /*
                        MatcherYAAAJena trainAndFineTune = new MatcherYAAAJena() {
                            @Override
                            public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
                                MatcherYAAAJena recallMatcher;
                                if (trainingCase.getTrack().equals(TrackRepository.Knowledgegraph.V4)) {
                                    recallMatcher = new RecallMatcherKgTrack();
                                } else {
                                    recallMatcher = new RecallMatcherAnatomy();
                                }
                                
                                Alignment recallAlignment = recallMatcher.match(source, target, new Alignment(), properties);
                                Alignment trainingAlignment = AddNegativesViaMatcher.addNegatives(recallAlignment, inputAlignment);
                                
                                //training
                                File finetunedModelFile = new File(targetDir, model + "_" + fraction + "_" + testCase.getName());
                                
                                TransformersFineTuner fineTuner = new TransformersFineTuner(extractorFallback, model, finetunedModelFile);
                                fineTuner.match(source, target, trainingAlignment, properties);
                                fineTuner.finetuneModel();
                                
                                //apply
                                LOGGER.info("Recall alignment with {} correspondences", recallAlignment.size());

                                TransformersFilter finetunedModelFilter = new TransformersFilter(extractorFallback, FileUtil.getCanonicalPathIfPossible(finetunedModelFile));
                                finetunedModelFilter.setCudaVisibleDevices(gpu);
                                finetunedModelFilter.setTransformersCache(transformersCache);
                                finetunedModelFilter.setTmpDir(new File("./mytmpDir_filter"));

                                Alignment alignmentWithConfidence = finetunedModelFilter.match(source, target, recallAlignment, properties);

                                MaxWeightBipartiteExtractor extractorMatcher = new MaxWeightBipartiteExtractor();

                                return extractorMatcher.match(source, target, alignmentWithConfidence, properties);
                            }
                        };
                        ers.addAll(Executor.run(trainingCase, trainAndFineTune, model + " (fine-tuned with fraction " + fraction + ")"));
                        */
                    }
                }
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }

    /**
     * Simply evaluates the baseline and recall matchers on the provided tracks.
     *
     * @param tracks  The tracks to be evaluated.
     * @param isLight False if {@link EvaluatorCSV} shall be used.
     */
    static void baselineMatchers(List<Track> tracks, boolean isLight) {
        SimpleStringMatcher ssm = new SimpleStringMatcher();
        ssm.setVerboseLoggingOutput(false);

        List<TestCase> testCases = new ArrayList<>();
        for (Track track : tracks) {
            testCases.addAll(track.getTestCases());
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        // just adding some baseline matchers below:
        SimpleStringMatcher smatch = new SimpleStringMatcher();
        smatch.setVerboseLoggingOutput(false);
        ers.addAll(Executor.run(testCases, new RecallMatcherKgTrack()));
        ers.addAll(Executor.run(testCases, new RecallMatcherAnatomy()));
        //ers.addAll(Executor.run(testCases, new RecallMatcherGeneric(20, true)));
        ers.addAll(Executor.run(testCases, smatch));

        LOGGER.info("All matchers run. Starting evaluation...");

        Evaluator evaluator;
        if (isLight) {
            LOGGER.info("Light evaluation mode...");
            evaluator = new EvaluatorBasic(ers);
        } else {
            LOGGER.info("Full evaluation mode...");
            evaluator = new EvaluatorCSV(ers);
        }
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
    static void zeroShotEvaluation(String gpu, String[] transformerModels, File transformersCache,
                                   List<Track> tracks, boolean isMultipleTextsToMultipleExamples,
                                   TextExtractor textExtractor, boolean isAutoThresholding) {
        List<TestCase> testCasesNoKG = new ArrayList<>();
        List<TestCase> testCasesKG = new ArrayList<>();
        for (Track track : tracks) {
            if (track != TrackRepository.Knowledgegraph.V4) {
                testCasesNoKG.addAll(track.getTestCases());
            } else {
                testCasesKG.addAll(track.getTestCases());
            }
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        for (String transformerModel : transformerModels) {
            LOGGER.info("Processing transformer model: " + transformerModel);
            String configurationName =
                    "zero_" + transformerModel + "_isMulti_" + isMultipleTextsToMultipleExamples +
                            "_isAutoThreshold_" + isAutoThresholding +
                            "_" + textExtractor.getClass().getSimpleName();
            try {
                if (testCasesNoKG.size() > 0) {
                    ers.addAll(Executor.run(testCasesNoKG, new ApplyModelPipeline(gpu,
                            transformerModel, transformersCache, new RecallMatcherAnatomy(),
                            isMultipleTextsToMultipleExamples, textExtractor, isAutoThresholding), configurationName));
                }
                if (testCasesKG.size() > 0) {
                    ers.addAll(Executor.run(testCasesKG, new ApplyModelPipeline(gpu,
                            transformerModel, transformersCache, new RecallMatcherKgTrack(),
                            isMultipleTextsToMultipleExamples, textExtractor, isAutoThresholding), configurationName));
                }
            } catch (Exception e) {
                LOGGER.warn("A problem occurred with transformer: '{}'.\n" +
                        "Continuing process...", transformerModel, e);
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }

}
