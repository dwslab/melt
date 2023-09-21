package de.uni_mannheim.informatik.dws.melt.examples.llm_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackNameLookup;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCopyResults;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunOLaLaForOAEI {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunOLaLaForOAEI.class);
    
    public static void main(String[] args){
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(150);
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
            LOGGER.info("Setting OAEI cache to {}", cacheFile);
            Track.setCacheFolder(cacheFile);
        }
        
        
        List<TestCase> testCases = getTestCases(cmd);
        OLaLaForOAEI matcher = new OLaLaForOAEI();
        if(cmd.hasOption("g"))
            matcher.setGpus(cmd.getOptionValue("g"));
        if(cmd.hasOption("tc"))
            matcher.setTransformersCache(new File(cmd.getOptionValue("tc")));
        
        ExecutionResultSet ers = Executor.run(testCases, matcher, "OLaLa");
        
        File resultsDir = Evaluator.getDirectoryWithCurrentTime();
        LOGGER.info("EvaluatorCopyResults");
        new EvaluatorCopyResults(ers).writeResultsToDirectory(resultsDir);
        LOGGER.info("EvaluatorCSV");
        new EvaluatorCSV(ers).writeToDirectory(resultsDir);
        LOGGER.info("Finish evaluating");
    }
    
    private static Options createOptions(){
        Options options = new Options();
        
        options.addOption(Option.builder("c")
                .longOpt("cache")
                .hasArg()
                .argName("path")
                .desc("The path to the cache folder for ontologies.")
                .build());
        
        options.addOption(Option.builder("g")
                .longOpt("gpu")
                .hasArg()
                .desc("Which GPUs to use. This can be comma separated. Eg. 0,1 which uses GPU zero and one.")
                .build());
        
        options.addOption(Option.builder("h")
                .longOpt("help")
                .desc("Print this help message.")
                .build());
        
        options.addOption(Option.builder("p")
                .longOpt("python")
                .hasArg()
                .desc("The python command to use.")
                .build());
        
        options.addOption(Option.builder("sp")
                .longOpt("serverport")
                .hasArg()
                .desc("The port of the python server.")
                .build()
        );
        
        options.addOption(Option.builder("tc")
                .longOpt("transformerscache")
                .hasArg()
                .desc("The file path to the transformers cache.")
                .build());

        options.addOption(Option.builder("tracks")
                .longOpt("tracks")
                .required()
                .hasArgs()
                .valueSeparator(' ')
                .desc("The tracks to be used, separated by spaces.")
                .build()
        );
        
        options.addOption(Option.builder("testcases")
                .longOpt("testcases")
                .hasArgs()
                .valueSeparator(' ')
                .desc("The testcases to use (only those which are mentioned in this option are used"
                        + " even if other tracks are provided.")
                .build()
        );
        return options;
    }
    
    private static int getFreePortOnHost() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException ex) {
            return 8080;
        }
    }
    
    private static List<TestCase> getTestCases(CommandLine cmd){
        List<TestCase> testcases = new ArrayList<>();
        for(Track track : getTracks(cmd)){
            testcases.addAll(track.getTestCases());
        }
        if (testcases.isEmpty()) {
            LOGGER.warn("No testcase can be retrived for all specified tracks. ABORTING program.");
            System.exit(1);
        }
        if(cmd.hasOption("testcases")){
            List<TestCase> selectedTestcases = new ArrayList<>();
            for(String tc : cmd.getOptionValues("testcases")){
                TestCase testcase = getTestCaseFromList(testcases, tc);
                if(testcase == null){
                    LOGGER.warn("Did not find test case specified by \"{}\". Skipping this testcase.", tc);
                }else{
                    selectedTestcases.add(testcase);
                }                
            }
            if (selectedTestcases.isEmpty()) {
                LOGGER.warn("No testcase can be retrived for all specified tracks and testcase. ABORTING program.");
                System.exit(1);
            }
            return selectedTestcases;
        }else{
            return testcases;
        }
    }
    
    private static List<Track> getTracks(CommandLine cmd){
        List<Track> tracks = new ArrayList<>();        
        if (cmd.hasOption("tracks")) {
            for (String trackString : cmd.getOptionValues("tracks")) {
                trackString = trackString.toLowerCase(Locale.ROOT).trim();
                Track track = TrackNameLookup.getTrackByString(trackString);
                if(track == null){
                    LOGGER.warn("Could not map track: " + trackString);
                    System.exit(1);
                }else{
                    tracks.add(track);
                }
            }
        }
        if (tracks.isEmpty()) {
            LOGGER.warn("No tracks specified. ABORTING program.");
            System.exit(1);
        }
        return tracks;
    }
    
    private static TestCase getTestCaseFromList(List<TestCase> testcases, String name){
        for(TestCase tc : testcases){
            if(tc.getName().equals(name)){
                return tc;
            }
        }
        return null;
    }
}
