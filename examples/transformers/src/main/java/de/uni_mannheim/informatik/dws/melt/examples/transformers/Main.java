package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherAnatomy;
import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherKgTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .required()
                .valueSeparator(' ')
                .desc("The transformer models to be used, separated by space.")
                .build());

        options.addOption(Option.builder("tracks")
                .longOpt("tracks")
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
                .desc("Available modes: ZEROSHOT, TC_FINETUNE, TRACK_FINETUNE, GLOBAL_FINETUNE")
                .required()
                .build()
        );

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

        if (cmd.hasOption("c")) {
            File cacheFile = new File(cmd.getOptionValue("c"));
            Track.setCacheFolder(cacheFile);
        }

        String gpu = cmd.getOptionValue("g", "");
        File transformersCache = null;
        if (cmd.hasOption("tc")) {
            transformersCache = new File(cmd.getOptionValue("tc"));
        }

        if (cmd.hasOption("tm")) {
            transformerModels = cmd.getOptionValues("tm");
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
                        System.out.println("Could not map track: " + trackString);
                }
            }
        }

        if (tracks.size() == 0) {
            System.out.println("No tracks specified. Using conference...");
            tracks.add(TrackRepository.Conference.V1_ALL_TESTCASES);
        }

        String mode = cmd.getOptionValue("m").toLowerCase(Locale.ROOT).trim();

        File targetDirForModels = new File("./models");

        switch (mode){
            case "zero":
            case "zeroshot":
            case "zeroShotEvaluation":
                LOGGER.info("Mode: zeroShotEvaluation");
                zeroShotEvaluation(gpu, transformerModels, transformersCache, tracks);
                break;
            case "tcfintune":
            case "tcfinetuned":
            case "tc_finetune":
            case "tc_finetuned":
            case "finetunedpertestcase":
                LOGGER.info("Mode: fineTunedPerTestCase");
                fineTunedPerTestCase(gpu, tracks, getFractions(cmd),transformerModels, transformersCache,
                        targetDirForModels);
                break;
            case "track_finetune":
            case "track_finetuned":
            case "tracks_finetune":
            case "tracks_finetuned":
            case "finetunedpertrack":
                LOGGER.info("Mode: fineTunedPerTrack");
                fineTunedPerTrack(gpu, tracks, getFractions(cmd), transformerModels,
                        transformersCache, targetDirForModels);
                break;
            case "global_finetune":
            case "global_finetuned":
            case "finetune_global":
            case "finetuned_global":
                globalFineTuning(gpu, tracks, getFractions(cmd), transformerModels, transformersCache, targetDirForModels);
                break;
            default:
                LOGGER.info("Mode '" + mode + "' not found.");
        }

    }

    /**
     * This helper method simply parses the provided training fractions as float array.
     * @param cmd The command line.
     * @return Float array. Will default to a value if no fractions parameter is provided.
     */
    static Float[] getFractions(CommandLine cmd) {
        if(!cmd.hasOption("f")){
            LOGGER.error("No fractions provided. Please provide them space-separated via the -f option, e.g.:\n" +
                    "-f 0.2 0.4\n" +
                    "Using default: 0.2.");
            return new Float[]{0.2f};
        }
        String fractions[] = cmd.getOptionValues("f");
        return Arrays.stream(fractions).map(Float::parseFloat).collect(Collectors.toList()).toArray(new Float[1]);
    }

    // external access required for used for unit-testing (therefore, not in main)
    static List<Track> tracks = new ArrayList<>();
    static String[] transformerModels;


    static void fineTunedPerTrack(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                  File transformersCache, File targetDir){
        if(!isOk(transformerModels, tracks)){
            return;
        }

        if(!targetDir.exists()){
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        for(float fraction : fractions) {
            for (String model : transformerModels) {
                for (Track track : tracks){

                    // Step 1 Training
                    String configurationName = model + "_" + fraction + "_" + track;
                    File finetunedModelFile = new File(targetDir,
                            configurationName);

                    TextExtractorFallback extractorFallback = new TextExtractorFallback(
                            new TextExtractorAllAnnotationProperties(),
                            new TextExtractorUrlFragment()
                    );
                    TransformersFineTuner fineTuner = new TransformersFineTuner(extractorFallback, model,
                            finetunedModelFile);
                    List<TestCase> trainingTestCases = TrackRepository.generateTrackWithSampledReferenceAlignment(track,
                            fraction, 41, false);

                    MatcherPipelineYAAAJena trainingPipelineMatcher = new MatcherPipelineYAAAJena() {
                        @Override
                        protected List<MatcherYAAAJena> initializeMatchers() {
                            List<MatcherYAAAJena> result = new ArrayList<>();
                            if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                                result.add(new RecallMatcherKgTrack());
                            } else {
                                result.add(new RecallMatcherAnatomy());
                            }
                            // TODO add AddNegatives
                            result.add(fineTuner);
                            return result;
                        }
                    };
                    Executor.run(trainingTestCases, trainingPipelineMatcher, configurationName);
                    try {
                        fineTuner.finetuneModel();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Step 2: Apply Model
                    if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                        ers.addAll(Executor.run(track,  new KnowledgeGraphMatchingPipeline(gpu,
                                finetunedModelFile.getAbsolutePath(), transformersCache), configurationName));
                    } else {
                        ers.addAll(Executor.run(track,  new AnatomyMatchingPipeline(gpu,
                                finetunedModelFile.getAbsolutePath(), transformersCache), configurationName));
                    }
                }
            }
        } // end of fractions loop

        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }

    /**
     * One fine-tuned model for all data (all tracks).
     * @param gpu The GPU to be used.
     * @param tracks Tracks to be evaluated.
     * @param fractions Fractions (training share in train-test split) that shall be evaluated as float array.
     * @param transformerModels Models (Strings) to be used.
     * @param transformersCache Cache for transformers.
     * @param targetDir Where the models shall be written to.
     */
    static void globalFineTuning(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                 File transformersCache, File targetDir){
        if(!isOk(transformerModels, tracks)){
            return;
        }

        if(!targetDir.exists()){
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        for(float fraction : fractions) {
            for (String model : transformerModels) {

                String configurationName = model + "_" + fraction + "_GLOBAL";
                File finetunedModelFile = new File(targetDir,
                        configurationName);
                TextExtractorFallback extractorFallback = new TextExtractorFallback(
                        new TextExtractorAllAnnotationProperties(),
                        new TextExtractorUrlFragment()
                );
                TransformersFineTuner fineTuner = new TransformersFineTuner(extractorFallback, model,
                        finetunedModelFile);

                for (Track track : tracks) {
                    List<TestCase> trainingTestCases = TrackRepository.generateTrackWithSampledReferenceAlignment(track,
                            fraction, 41, false);
                    MatcherPipelineYAAAJena trainingPipelineMatcher = new MatcherPipelineYAAAJena() {
                        @Override
                        protected List<MatcherYAAAJena> initializeMatchers() {
                            List<MatcherYAAAJena> result = new ArrayList<>();
                            if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                                result.add(new RecallMatcherKgTrack());
                            } else {
                                result.add(new RecallMatcherAnatomy());
                            }
                            // TODO add AddNegatives
                            result.add(fineTuner);
                            return result;
                        }
                    };
                    Executor.run(trainingTestCases, trainingPipelineMatcher, configurationName);
                }

                // train
                try {
                    fineTuner.finetuneModel();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // evaluate
                for (Track track : tracks) {
                    if (track.equals(TrackRepository.Knowledgegraph.V4)) {
                        ers.addAll(Executor.run(track,  new KnowledgeGraphMatchingPipeline(gpu,
                                finetunedModelFile.getAbsolutePath(), transformersCache)));
                    } else {
                        ers.addAll(Executor.run(track,  new AnatomyMatchingPipeline(gpu,
                                finetunedModelFile.getAbsolutePath(), transformersCache)));
                    }
                }
            }
        } // end of fractions loop

        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }

    /**
     * The model is fine-tuned per testcase.
     * @param gpu The GPU to be used.
     * @param tracks Tracks to be evaluated.
     * @param transformerModels Models (Strings) to be used.
     * @param transformersCache Cache for transformers.
     * @param targetDir Where the models shall be written to.
     */
    static void fineTunedPerTestCase(String gpu, List<Track> tracks, Float[] fractions, String[] transformerModels,
                                     File transformersCache, File targetDir) {
        if(!isOk(transformerModels, tracks)){
            return;
        }

        if(!targetDir.exists()){
            targetDir.mkdirs();
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        outerLoop:
        for(float fraction : fractions) {
            for (Track track : tracks) {
                for (TestCase testCase : track.getTestCases()) {

                    TestCase trainingCase = TrackRepository.generateTestCaseWithSampledReferenceAlignment(testCase,
                            fraction,
                            41, false);

                    TextExtractorFallback extractorFallback = new TextExtractorFallback(
                            new TextExtractorAllAnnotationProperties(),
                            new TextExtractorUrlFragment()
                    );

                    for (String model : transformerModels) {

                        // Step 1: Training
                        // ----------------

                        File finetunedModelFile = new File(targetDir,
                                model + "_" + fraction + "_" + testCase.getName());

                        // Step 1.1.: Running the test case
                        TransformersFineTuner fineTuner = new TransformersFineTuner(extractorFallback, model,
                                finetunedModelFile);
                        MatcherPipelineYAAAJena trainingPipelineMatcher = new MatcherPipelineYAAAJena() {
                            @Override
                            protected List<MatcherYAAAJena> initializeMatchers() {
                                List<MatcherYAAAJena> result = new ArrayList<>();
                                if (trainingCase.getTrack().equals(TrackRepository.Knowledgegraph.V4)) {
                                    result.add(new RecallMatcherKgTrack());
                                } else {
                                    result.add(new RecallMatcherAnatomy());
                                }
                                // TODO add AddNegatives
                                result.add(fineTuner);
                                return result;
                            }
                        };
                        Executor.run(trainingCase, trainingPipelineMatcher);

                        // Step 1.2: Fine-Tuning the Model
                        try {
                            fineTuner.finetuneModel();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Step 2: Apply Model
                        // -------------------
                        MatcherYAAAJena matcher;
                        if (testCase.getTrack().equals(TrackRepository.Knowledgegraph.V4)) {
                            matcher = new KnowledgeGraphMatchingPipeline(gpu,
                                    finetunedModelFile.getAbsolutePath(), transformersCache);
                        } else {
                            matcher = new AnatomyMatchingPipeline(gpu,
                                    finetunedModelFile.getAbsolutePath(), transformersCache);
                        }
                        ers.addAll(Executor.run(testCase, matcher,
                                model + " (fine-tuned with fraction " + fraction + ")"));
                        //break outerLoop;
                    }
                }
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }


    /**
     * Performs a zero shot evaluation on the given models using the provided tracks.
     * @param gpu The GPU to be used.
     * @param transformerModels Models (Strings) to be used.
     * @param transformersCache Cache for transformers.
     * @param tracks Tracks to be evaluated.
     * @throws Exception General exception.
     */
    static void zeroShotEvaluation(String gpu, String[] transformerModels, File transformersCache,
                                   List<Track> tracks) throws Exception {
        if(!isOk(transformerModels, tracks)){
            return;
        }

        List<TestCase> testCasesNoKG = new ArrayList<>();
        List<TestCase> testCasesKG = new ArrayList<>();
        for (Track track : tracks) {
            if(track != TrackRepository.Knowledgegraph.V4) {
                testCasesNoKG.addAll(track.getTestCases());
            } else {
                testCasesKG.addAll(track.getTestCases());
            }
        }

        ExecutionResultSet ers = new ExecutionResultSet();

        SimpleStringMatcher ssm = new SimpleStringMatcher();
        ssm.setVerboseLoggingOutput(false);

        // just adding some baseline matchers below:
        if(testCasesNoKG.size() > 0) {
            ers.addAll(Executor.run(testCasesNoKG, new RecallMatcherKgTrack()));
            ers.addAll(Executor.run(testCasesNoKG, new RecallMatcherAnatomy()));
            ers.addAll(Executor.run(testCasesNoKG, ssm));
        }
        if(testCasesKG.size() > 0){
            ers.addAll(Executor.run(testCasesKG, new RecallMatcherKgTrack()));
            ers.addAll(Executor.run(testCasesKG, new RecallMatcherAnatomy()));
            ers.addAll(Executor.run(testCasesKG, ssm));
        }

        for (String transformerModel : transformerModels) {
            System.out.println("Processing transformer model: " + transformerModel);
            try {
                if(testCasesNoKG.size() > 0) {
                    ers.addAll(Executor.run(testCasesNoKG, new AnatomyMatchingPipeline(gpu,
                            transformerModel, transformersCache), transformerModel));
                }
                if(testCasesKG.size() > 0){
                    ers.addAll(Executor.run(testCasesKG, new KnowledgeGraphMatchingPipeline(gpu,
                            transformerModel, transformersCache), transformerModel));
                }
            } catch (Exception e){
                System.out.println("A problem occurred with transformer: '" + transformerModel + "'.\n" +
                        "Continuing process...");
                e.printStackTrace();
            }
        }
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        evaluator.writeToDirectory();
    }


    /***
     * Very quick parameter check.
     * @param transformerModels Transformer models to be checked.
     * @param tracks Tracks to be checked.
     * @return True if OK, else false.
     */
    private static boolean isOk(String[] transformerModels, List<Track> tracks){
        if (tracks == null || tracks.size() == 0) {
            System.out.println("No tracks specified. ABORTING program.");
            return false;
        }
        if (transformerModels == null || transformerModels.length == 0) {
            System.out.println("No transformer model specified. ABORTING program.");
            return false;
        }
        return true;
    }
}
