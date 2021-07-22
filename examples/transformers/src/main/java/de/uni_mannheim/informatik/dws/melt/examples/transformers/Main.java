package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherAnatomy;
import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherKgTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
            System.out.println("No tracks specified. Using anatomy...");
            tracks.add(TrackRepository.Anatomy.Default);
        }

        zeroShotEvaluation(gpu, transformerModels, transformersCache, tracks);
    }

    // just used for testing!
    static List<Track> tracks = new ArrayList<>();
    static String[] transformerModels;

    static void zeroShotEvaluation(String gpu, String[] transformerModels, File transformersCache,
                                   List<Track> tracks) throws Exception {
        if (tracks == null || tracks.size() == 0) {
            System.out.println("No tracks specified. ABORTING program.");
            return;
        }
        if (transformerModels == null || transformerModels.length == 0) {
            System.out.println("No transformer model specified. ABORTING program.");
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
}
