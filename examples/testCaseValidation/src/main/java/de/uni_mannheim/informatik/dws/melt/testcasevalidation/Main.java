package de.uni_mannheim.informatik.dws.melt.testcasevalidation;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_data.LocalTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.SealsTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_validation.SemanticWebLibrary;
import de.uni_mannheim.informatik.dws.melt.matching_validation.TestCaseValidationService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String CACHE_OPTION = "cache";
    private static final String TRACK_OPTION = "track";
    private static final String LOCAL_TRACK_OPTION = "local-track";
    private static final String LOCAL_TEST_CASE_OPTION = "local-testcase";
    private static final String HELP_OPTION_STRING = "help";
    
    
    public static void main(String[] args){
        Options options = new Options();
        
        // Track option
        Option trackOption = new Option("t", TRACK_OPTION, true, "The track to execute.\n" +
                "Three arguments are required: -t <location_uri> <collection_name> <version>"
        );
        trackOption.setArgs(Option.UNLIMITED_VALUES); // allow for multiple arguments
        options.addOption(trackOption);

        // Local track option
        Option localTrackOption = new Option("lt", LOCAL_TRACK_OPTION, true, "The local track to execute.\n" +
                "Three arguments are required: -lt <folder-to-testcases> <name> <version>");
        localTrackOption.setArgs(3);
        options.addOption(localTrackOption);

        // local test case option
        Option localTestcaseOption = new Option("ltc", LOCAL_TEST_CASE_OPTION, true,
                "Alternative to a local track you can also just specify two ontologies and a\n" +
                        "reference alignment: -ltc <onto1-path> <onto2-pat> <reference-path>");
        localTestcaseOption.setArgs(3);
        options.addOption(localTestcaseOption);
        
        options.addOption("c", CACHE_OPTION, true, "The path to the cache folder for ontologies");

        // Help option
        Option helpOption = new Option("h", HELP_OPTION_STRING, false,
                "Print the documentation / Show help.");
        helpOption.setRequired(false);
        options.addOption(helpOption);        
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar testCaseValidation-1.0-jar-with-dependencies.jar", options);
            System.exit(1);
        }
        
        if(cmd.hasOption("help")){
            formatter.printHelp("java -jar testCaseValidation-1.0-jar-with-dependencies.jar", options);
            System.exit(1);
        }
        
        if(cmd.hasOption(CACHE_OPTION)){
            File cacheFile = new File(cmd.getOptionValue(CACHE_OPTION));
            LOGGER.info("Setting cache to {}", cacheFile);
            Track.setCacheFolder(cacheFile);
        }
        
        // get track
        List<TestCase> testCases = new ArrayList<>();
        if (cmd.hasOption(TRACK_OPTION)) {
            String[] trackData = cmd.getOptionValues(TRACK_OPTION);
            if (trackData.length == 2){
                testCases = new SealsTrack("http://oaei.webdatacommons.org/tdrs/", trackData[0], trackData[1]).getTestCases();
            } else if (trackData.length == 3){
                testCases = new SealsTrack(trackData[0], trackData[1], trackData[2]).getTestCases();
            } else {
                LOGGER.error("Please state the track data as follows:\n" +
                        "--{} <location_uri> <collection_name> <version>\n", TRACK_OPTION);
                return;
            }
        } else if (cmd.hasOption(LOCAL_TRACK_OPTION)) {
            String[] trackData = cmd.getOptionValues(LOCAL_TRACK_OPTION);
            if (trackData.length != 3) {
                LOGGER.error("Please state the local track data as follows:\n" +
                        "--{} <location> <name> <version>\n", TRACK_OPTION);
                return;
            }
            // unfortunately, the local track constructor uses the parameters in a different order
            testCases = new LocalTrack(trackData[1], trackData[2], trackData[0]).getTestCases();
        } else if (cmd.hasOption(LOCAL_TEST_CASE_OPTION)) {
            String[] testCaseData = cmd.getOptionValues(LOCAL_TEST_CASE_OPTION);
            if (testCaseData.length != 3) {
                LOGGER.error("Please state the local test case data as follows:\n" +
                        "--{} <onto1-path> <onto2-path> <reference-path>\n", LOCAL_TEST_CASE_OPTION);
                return;
            }
            File onto1 = new File(testCaseData[0]);
            File onto2 = new File(testCaseData[1]);
            File reference = new File(testCaseData[2]);
            
            testCases.add(new TestCase("Local TC",
                    onto1.toURI(),
                    onto2.toURI(),
                    reference.toURI(),
                    new LocalTrack("LocalTrack", "1.0", (File) null, GoldStandardCompleteness.COMPLETE)));
        } else {
            LOGGER.error("Please provide a track, local track, or local testcase.\n" +
                    "Call --{} for help.", HELP_OPTION_STRING);
            return;
        }
        
        LOGGER.info("RUN Jena");
        for(TestCase tc : testCases){
            TestCaseValidationService validationService = new TestCaseValidationService(tc, SemanticWebLibrary.JENA);
            LOGGER.info("Analysis for test case {}: {}", tc.getName(), validationService.toString());
        }
        
        LOGGER.info("RUN OWLAPI");
        for(TestCase tc : testCases){
            TestCaseValidationService validationService = new TestCaseValidationService(tc, SemanticWebLibrary.OWLAPI);
            LOGGER.info("Analysis for test case {}: {}", tc.getName(), validationService.toString());
        }
    }
}
