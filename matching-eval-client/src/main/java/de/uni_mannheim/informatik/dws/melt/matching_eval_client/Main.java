package de.uni_mannheim.informatik.dws.melt.matching_eval_client;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.http.MatcherHTTPCall;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.seals.MatcherSeals;
import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.SealsTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import org.apache.commons.cli.*;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Missing arguments. You can use option -h for help.");
        }

        // initialize the command line
        CommandLineParser cliParser = new DefaultParser();
        CommandLine cmd = null;
        Options options = getOptions();
        try {
            cmd = cliParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("An exception occurred while trying to parse the arguments.\n" +
                    "You can use option -h for help.\n");
            e.printStackTrace();
        }
        if (cmd == null) return;

        // help option
        if (cmd.hasOption(HELP_OPTION_STRING)) {
            HelpFormatter helpFormatter = new HelpFormatter();
            helpFormatter.printHelp("ant", options);
            return;
        }

        // default evaluation option

        // get systems
        String[] systems;
        final String missingSystemString = "Please specify systems to evaluate using the --systems option.\n" +
                "You can call --help for documentation/help. ABORTING PROGRAM...";
        if (cmd.hasOption(SYSTEMS_OPTION)) {
            systems = cmd.getOptionValues(SYSTEMS_OPTION);
        } else {
            System.out.println(missingSystemString);
            return;
        }
        if (systems == null || systems.length == 0) {
            System.out.println(missingSystemString);
            return;
        }

        // get evaluation track
        Track track = null;
        TestCase testCase = null;
        if (cmd.hasOption(TRACK_OPTION)) {
            String[] trackData = cmd.getOptionValues(TRACK_OPTION);
            if (trackData.length != 3) {
                System.out.printf("Please state the track data as follows:\n" +
                        "--%s <location_uri> <collection_name> <version>\n", TRACK_OPTION);
                return;
            }
            track = new SealsTrack(trackData[0], trackData[1], trackData[2]);
        } else if (cmd.hasOption(LOCAL_TRACK_OPTION)) {
            String[] trackData = cmd.getOptionValues(LOCAL_TRACK_OPTION);
            if (trackData.length != 3) {
                System.out.printf("Please state the local track data as follows:\n" +
                        "--%s <location> <name> <version>\n", TRACK_OPTION);
                return;
            }
            // unfortunately, the local track constructor uses the parameters in a different order
            track = new LocalTrack(trackData[1], trackData[2], trackData[0]);
        } else if (cmd.hasOption(LOCAL_TEST_CASE_OPTION)) {
            String[] testCaseData = cmd.getOptionValues(LOCAL_TEST_CASE_OPTION);
            if (testCaseData.length != 3) {
                System.out.printf("Please state the local test case data as follows:\n" +
                        "--%s <onto1-path> <onto2-path> <reference-path>\n", LOCAL_TEST_CASE_OPTION);
                return;
            }
            File onto1 = new File(testCaseData[0]);
            File onto2 = new File(testCaseData[1]);
            File reference = new File(testCaseData[2]);

            testCase = new TestCase("Local TC",
                    onto1.toURI(),
                    onto2.toURI(),
                    reference.toURI(),
                    null);
        } else {
            System.out.printf("Please provide a track, local track, or local testcase.\n" +
                    "Call --%s for help.", HELP_OPTION_STRING);
            return;
        }

        // checking if java8 is defined:
        String java8 = null;
        if (cmd.hasOption(JAVA8_OPTION)) {
            java8 = cmd.getOptionValue(JAVA8_OPTION);
        }

        // let's evaluate
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        for (String system : systems) {
            if (system == null || system.trim().equals("")) {
                continue;
            }
            if (system.toLowerCase().endsWith(".tar.gz")) {
                // we assume it is in MELT WEB DOCKER format:
                System.out.printf("Recognized MELT WEB DOCKER package:\n%s\n", system);
                System.out.println("Please make sure that docker is running on your system.");
                File dockerFile = new File(system);
                if (dockerFile.isDirectory()) {
                    System.out.println("The provided file is a directory: " + system);
                    continue;
                }
                if (!dockerFile.exists()) {
                    System.out.println("The provided docker file does not exist: " + system);
                    continue;
                }
                MatcherDockerFile matcherDockerFile = new MatcherDockerFile(new File(system));
                matchers.put(system, matcherDockerFile);
            } else if (system.toLowerCase().endsWith(".zip")) {
                // we assume it is a SEALS package:
                System.out.printf("Recognized SEALS package:\n%s\n", system);
                MatcherSeals matcherSeals = new MatcherSeals(system);
                if (java8 != null) {
                    matcherSeals.setJavaCommand(java8);
                }
                matchers.put(system, matcherSeals);
            } else {
                // we assume it is a URL...
                System.out.printf("Recognized HTTP URL matcher endpoint:\n%s\n", system);
                try {
                    URI uri = new URI(system);
                    matchers.put(system, new MatcherHTTPCall(uri));
                } catch (URISyntaxException e) {
                    System.out.printf("Failed to create URI with system: %s\nSkipping matcher...\n", system);
                }
            }
        }

        if (matchers.size() == 0) {
            System.out.println("No runnable matchers given. ABORTING PROGRAM...");
            return;
        }

        // run track or test case
        ExecutionResultSet ers = null;
        System.out.println("Running matching systems...");
        if (track != null) {
            ers = Executor.run(track, matchers);
        } else {
            ers = Executor.run(testCase, matchers);
        }

        System.out.println("Evaluating matching system results...");
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);

        // decide on results directory
        File resultsDirectory = null;
        if (cmd.hasOption(RESULTS_DIRECTORY_OPTION)) {
            String resultsDirectoryPath = cmd.getOptionValue(RESULTS_DIRECTORY_OPTION);
            resultsDirectory = new File(resultsDirectoryPath);
            if (!resultsDirectory.exists()) {
                if (!resultsDirectory.mkdirs()) {
                    System.out.printf("Failed to make directory: %s\nCreating temporary directory.\n",
                            resultsDirectoryPath);
                    resultsDirectory = null;
                }
            }
        }

        if (resultsDirectory == null) {
            // let's make our own temporary directory...
            resultsDirectory = FileUtil.createFolderWithRandomNumberInDirectory(FileUtil.SYSTEM_TMP_FOLDER,
                    "melt-client-results");
        }

        // evaluate...
        evaluatorCSV.writeToDirectory(resultsDirectory);
        System.out.printf("Results written to: %s\n", resultsDirectory.getAbsolutePath());

        // now lets print the results in the console
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(new File(resultsDirectory, EvaluatorCSV.getTrackPerformanceCubeFileName())),
                StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, EvaluatorCSV.getCsvFormat().withFirstRecordAsHeader())
        ) {
            for (CSVRecord record : parser.getRecords()) {
                Map<String, String> recordMap = record.toMap();
                if (recordMap.get("Type").equalsIgnoreCase("ALL")) {
                    System.out.println("\n");
                    System.out.printf("Matcher: %s\n", recordMap.get("Matcher"));
                    System.out.printf("Macro Precision: %s\n", recordMap.get("Macro Precision (P)"));
                    System.out.printf("Macro Recall: %s\n", recordMap.get("Macro Recall (R)"));
                    System.out.printf("Macro F1: %s\n", recordMap.get("Macro F1"));
                    System.out.printf("Micro Precision: %s\n", recordMap.get("Micro Precision (P)"));
                    System.out.printf("Micro Recall: %s\n", recordMap.get("Micro Recall (R)"));
                    System.out.printf("Micro F1: %s\n", recordMap.get("Micro F1"));
                    System.out.printf("Total Runtime (HH:MM:SS): %s\n", recordMap.get("Total Runtime (HH:MM:SS)"));
                    try {
                        if (
                                recordMap.get("Matcher").endsWith(".tar.gz") &&
                                        ((Double) Double.parseDouble(recordMap.get("Macro F1"))).equals(0.0d)
                        ) {
                            System.out.printf("Since F1 is 0, you can find the full log of %s below:\n", recordMap.get(
                                    "Matcher"));
                            System.out.println(((MatcherDockerFile) matchers.get(recordMap.get("Matcher")))
                                    .getAllLogLinesFromContainer());
                        }
                    } catch (Exception e) {
                        System.out.println("Tried to print the log since the F1 is 0 but failed to do so.");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("An error occurred while trying to read the result file.");
            e.printStackTrace();
        }
    }

    private static final String SYSTEMS_OPTION = "systems";
    private static final String TRACK_OPTION = "track";
    private static final String LOCAL_TRACK_OPTION = "local-track";
    private static final String LOCAL_TEST_CASE_OPTION = "local-testcase";
    private static final String HELP_OPTION_STRING = "help";
    private static final String RESULTS_DIRECTORY_OPTION = "results";
    private static final String JAVA8_OPTION = "java8";

    /**
     * Generates the options available in the CLI.
     *
     * @return Options instance.
     */
    private static Options getOptions() {
        Options options = new Options();

        // Alignment system option
        Option systemOption = new Option("s", SYSTEMS_OPTION, true, "The alignment systems.");
        systemOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(systemOption);

        // Track option
        Option trackOption = new Option("t", TRACK_OPTION, true, "The track to execute.\n" +
                "Three arguments are required: -t <location_uri> <collection_name> <version>"
        );
        trackOption.setArgs(3);
        options.addOption(trackOption);

        // Local track option
        Option localTrackOption = new Option("lt", LOCAL_TRACK_OPTION, true, "The local track to execute.\n" +
                "Three arguments are required: -lt <folder-to-testcases> <name> <version>");
        trackOption.setArgs(3);
        options.addOption(localTrackOption);

        // local test case option
        Option localTestcaseOption = new Option("ltc", LOCAL_TEST_CASE_OPTION, true,
                "Alternative to a local track you can also just specify two ontologies and a\n" +
                        "reference alignment: -ltc <onto1-path> <onto2-pat> <reference-path>");
        localTestcaseOption.setArgs(3);
        options.addOption(localTestcaseOption);

        // Help option
        Option helpOption = new Option("h", HELP_OPTION_STRING, false,
                "Print the documentation / Show help.");
        helpOption.setRequired(false);
        options.addOption(helpOption);

        // Java 8 option
        Option java8option = new Option("j", JAVA8_OPTION, true,
                "If your system Java is not Java 8, you can set the path using this variable.");
        java8option.setArgs(1);
        java8option.setRequired(false);
        options.addOption(java8option);

        // Results directory option
        Option resultsDirectoryOption = new Option("r", RESULTS_DIRECTORY_OPTION, true,
                "Provide a directory where the results shall be written to.");
        resultsDirectoryOption.setArgs(1);
        resultsDirectoryOption.setRequired(false);
        options.addOption(resultsDirectoryOption);

        return options;
    }
}
