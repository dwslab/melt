package de.uni_mannheim.informatik.dws.melt.kgeval;

import de.uni_mannheim.informatik.dws.melt.kgeval.baseline.BaselineLabel;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.http.MatcherHTTPCall;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.seals.MatcherSealsBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorAlignmentAnalyzer;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.dashboard.DashboardBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.resultspage.ResultsPageHTML;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.resultspage.ResultsPageLatex;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentXmlRepair;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.io.Closeable;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvaluateKGTrack {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluateKGTrack.class);

    private static final String SYSTEMS_OPTION = "systems";
    private static final String JAVA8_OPTION = "java8";
    private static final String TIMEOUT_OPTION = "timeoutHours";
    private static final String TMP_FOLDER_OPTION = "tmp";
    private static final String TESTCASES_OPTION = "testcases";
    private static final String TRACK_OPTION = "track";
    private static final String METHOD_OPTION = "method";
    private static final String INFOLDER_OPTION = "inFolder";
    private static final String OUTFOLDER_OPTION = "outFolder";
    
    public static void main(String[] args){
        Options options = new Options();
        
        options.addOption(Option.builder("s")
                .longOpt(SYSTEMS_OPTION)
                .hasArg()
                .numberOfArgs(Option.UNLIMITED_VALUES)
                .desc("The matching systems to run. Runs autodetect for the type of system.")
                .build());
        
        options.addOption(Option.builder("j")
                .longOpt(JAVA8_OPTION)
                .hasArg()
                .desc("If your system Java is not Java 8, you can set the path using this variable.")
                .build());
        
        options.addOption(Option.builder("t")
                .longOpt(TMP_FOLDER_OPTION)
                .hasArg()
                .argName("path")
                .desc("The path to a folder where temporary files can be written. This also includes the cache of OAEI files.")
                .build());
        
        options.addOption(Option.builder("m")
                .longOpt(METHOD_OPTION)
                .hasArg()
                .required()
                .desc("method to call. one of: run,repair,check,resultspage")
                .build());
        
        options.addOption(TIMEOUT_OPTION, true, "the number of hours for the timeout (default is 24 hours).");
        options.addOption(TESTCASES_OPTION, true, "name of the testcases to execute(separated by comma) e.g. starwars-swg,memoryalpha-stexpanded");                
        options.addOption(TRACK_OPTION, true, "The name of the track. Possible values: small-test,v1,v2,v3,v4,v3-nonmatch-small,v3-nonmatch-large");
        options.addOption(INFOLDER_OPTION, true, "The path to a folder where the files should be processed.");
        options.addOption(OUTFOLDER_OPTION, true, "The path to a folder where the processed files should be stored in the end.");
        
        options.addOption("help", "print this message");
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("java -jar kgEvalCli-1.0-jar-with-dependencies.jar", options);
            System.exit(1);
        }
        
        if(cmd.hasOption("help")){
            formatter.printHelp("java -jar kgEvalCli-1.0-jar-with-dependencies.jar", options);
            System.exit(1);
        }
        
        //arguments verification
        String tmpFolderString = cmd.getOptionValue(TMP_FOLDER_OPTION);
        File tmp = null;
        if(tmpFolderString != null){
            try {
                tmp = Paths.get(tmpFolderString).toFile();
            } catch (InvalidPathException | NullPointerException ex) {
                LOGGER.error("Option {} does not point to a valid directory.", TMP_FOLDER_OPTION);
                System.exit(1);
            }
            if(tmp.exists()){
                if(tmp.isDirectory() == false){
                    LOGGER.error("Option {} does not point to a  directory.", TMP_FOLDER_OPTION);
                    System.exit(1);
                }
            }else{
                tmp.mkdirs();
                if(tmp.isDirectory() == false){ // also checks if it exists
                    LOGGER.error("Option {} does not point to a directory or does not exist.", TMP_FOLDER_OPTION);
                    System.exit(1);
                }
            }
        }
        
        if(tmp != null){
            File cacheFile = new File(tmp, "oaei_track_cache");
            cacheFile.mkdirs();
            LOGGER.info("Setting cache to {}", cacheFile);
            Track.setCacheFolder(cacheFile);
        }
        
        switch(cmd.getOptionValue(METHOD_OPTION).toLowerCase()){
            case "run":{
                runMatchers(cmd);
                break;
            }
            case "repair":{
                repairAlignmentFiles(cmd);
                break;
            }
            case "check":{
                checkAlignments(cmd);
                break;
            }
            case "resultspage":{
                generateResultsPage(cmd);
                break;
            }
            default:{
                LOGGER.warn("No valid option for method");
                break;
            }
        }
    }
    
    private static void runMatchers(CommandLine cmd){        
        Set<Supplier<IOntologyMatchingToolBridge>> matchers = getMatchers(cmd);
        if(matchers == null || matchers.isEmpty())
            return;        
        List<TestCase> testCases = getTestCases(cmd);
        
        //LOGGER.info("Run the following matchers: {}", matchers.keySet().stream().collect(Collectors.joining(",")));
        //LOGGER.info("On following testcases: {}", testCases.stream().map(tc -> tc.getName()).collect(Collectors.joining(",")));        
        ExecutionResultSet results = new ExecutionResultSet();
        for(Supplier<IOntologyMatchingToolBridge> matcherSupplier : matchers){
            for(TestCase tc : testCases){
                IOntologyMatchingToolBridge matcher = matcherSupplier.get();
            
                results.addAll(Executor.run(tc, matcher));

                try {
                    if(matcher instanceof AutoCloseable){
                        ((AutoCloseable)matcher).close();
                    }else if(matcher instanceof Closeable){
                        ((Closeable)matcher).close();
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Could not close the matcher.", ex);
                }
            }
        }
        
        CopyResultsAndTime copyResults = new CopyResultsAndTime(results);
        copyResults.writeToDirectory();
        
        //EvaluatorBasic basic = new EvaluatorBasic(results);
        //basic.writeToDirectory();
    }
    
    private static void repairAlignmentFiles(CommandLine cmd){
        File in = getFileOrThrowException(cmd, INFOLDER_OPTION);
        File out = getFileOrThrowException(cmd, OUTFOLDER_OPTION);
        
        LOGGER.info("Repair alignment xml from folder {} and copies the results to {}", in, out);
        AlignmentXmlRepair.repairAlignmentFolder(in, out);
    }
    
    private static void checkAlignments(CommandLine cmd){
        File in = getFileOrThrowException(cmd, INFOLDER_OPTION);
        File out = getFileOrUseDefault(cmd, OUTFOLDER_OPTION);
        
        LOGGER.info("Check the alignments in folder {} and writes results to {}", in, out);        
        ExecutionResultSet resultSet = CopyResultsAndTime.loadFromFolder(in);    
        new EvaluatorAlignmentAnalyzer(resultSet).writeToDirectory(out);
    }
    
    private static void generateResultsPage(CommandLine cmd){
        File in = getFileOrThrowException(cmd, INFOLDER_OPTION);
        File out = getFileOrUseDefault(cmd, OUTFOLDER_OPTION);
        
        LOGGER.info("Generates a results page from folder {} and writes the results to {}", in, out);    
        
        ExecutionResultSet results = CopyResultsAndTime.loadFromFolder(in);
        
        //results page
        new ResultsPageHTML(results, false, false).writeToDirectory(out);
        new ResultsPageLatex(results, false).writeToDirectory(out);
        
        
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(results);
        
        evaluatorCSV.setBaselineMatcher(new BaselineLabel());
        
        evaluatorCSV.setResourceExplainers(Arrays.asList(
                new ExplainerResourceProperty(RDFS.label),
                new ExplainerResourceType()));
        
                
        evaluatorCSV.writeToDirectory(out);
                
        DashboardBuilder db = new DashboardBuilder(evaluatorCSV);
        db.clearElements();
        db.addTrackTestcaseSunburst();
        db.addConfidenceBar();
        db.addPieChart("matcherChart", "Matcher");
        db.addPieChartEvaluation();
        db.newRow();
        db.addBoxPlotMatcherConfidence();
        db.newRow();
        //db.addSelectMenu("resourceTypeLeftChart", "ResourceType Left", "width:190px");
        //db.addSelectMenu("resourceTypeRightChart", "ResourceType Right", "width:190px");
        db.addPieChart("resourceTypeLeftChart", "ResourceType Left");
        db.addPieChart("resourceTypeRightChart", "ResourceType Right");
        db.addPieChart("residualChart", "Residual True Positive");
        db.addResultPerTestCase();
        db.addResultPerMatcher();
        db.newRow();
        db.addMetricTableSelectedAndMatcher();
        db.newRow();
        db.addDataCount();
        db.addDataChart();        
        db.setDataLoadingIndicator(true);
        
        db.writeToFile(new File(out, "kgDashboard.html"), new File(out, "kgDashboard_data.csv"));
        db.writeToCompressedFile(new File(out, "kgDashboard_compressed.html"), new File(out, "kgDashboard_compressed_data.csv.gz.base64"));
    }
    
    private static Map<String, Track> MAP_VERSION_TO_TRACK = generateMapVersionToTrack();
    private static Map<String, Track> generateMapVersionToTrack(){
        Map<String, Track> map = new HashMap<>();
        for(Track t : TrackRepository.retrieveDefinedTracks(TrackRepository.Knowledgegraph.class)){
            map.put(t.getVersion().toLowerCase(), t);
        }
        return map;
    }
    
    private static Track getTrack(CommandLine cmd){        
        Track track = MAP_VERSION_TO_TRACK.get(cmd.getOptionValue(TRACK_OPTION, "V4").toLowerCase());
        if(track == null){
            LOGGER.error("Could not find knowledge track track version. Abort.");
            throw new IllegalArgumentException("Could not find knowledge track track version.");
        }
        return track;
    }
    
    private static List<TestCase> getTestCases(CommandLine cmd){
        Track track = getTrack(cmd);
        String testCases = cmd.getOptionValue(TESTCASES_OPTION, "");
        testCases = testCases.trim();
        if(testCases.isEmpty())
            return track.getTestCases();
        List<String> testCaseNames = Arrays.stream(testCases.split(",")).map(String::trim).collect(Collectors.toList());
        return track.getTestCases(testCaseNames);
    }
    
    private static Set<Supplier<IOntologyMatchingToolBridge>> getMatchers(CommandLine cmd){
        Set<Supplier<IOntologyMatchingToolBridge>> matchers = new HashSet<>();
        
        String[] systems = cmd.getOptionValues(SYSTEMS_OPTION);
        if (systems == null || systems.length == 0) {
            LOGGER.warn("Please specify systems to evaluate using the --systems option.\n" +
                "You can call --help for documentation/help. ABORTING PROGRAM...");
            return null;
        }
        
        MatcherSealsBuilder sealsBuilder = getSealsBuilder(cmd);
        
        for (String system : systems) {
            if (system == null || system.trim().equals("")) {
                continue;
            }
            if (system.toLowerCase().endsWith(".tar.gz")) {
                // we assume it is in MELT WEB DOCKER format:
                LOGGER.info("Recognized MELT WEB DOCKER package: {}", system);
                File dockerFile = new File(system);
                if (dockerFile.isDirectory()) {
                    LOGGER.warn("The provided file is a directory: {} . Skipping this matcher.", system);
                    continue;
                }
                if (!dockerFile.exists()) {
                    LOGGER.warn("The provided docker file does not exist {} . Skipping this matcher.", system);
                    continue;
                }
                matchers.add(()-> new MatcherDockerFile(new File(system)));
            } else if (system.toLowerCase().endsWith(".zip")) {
                // we assume it is a SEALS package:
                LOGGER.info("Recognized SEALS package: {}", system);
                matchers.add(()-> sealsBuilder.build(new File(system)));
            } else {
                // we assume it is a URL...
                LOGGER.info("Recognized HTTP URL matcher endpoint:\n{}\n", system);
                try {
                    URI uri = new URI(system);
                    matchers.add(()-> new MatcherHTTPCall(uri));
                } catch (URISyntaxException e) {
                    LOGGER.warn("Failed to create URI with system: {}\nSkipping matcher...\n", system);
                }
            }
        }
        
        if (matchers.isEmpty()) {
            LOGGER.warn("No systems are detected. ABORTING PROGRAM...");
            return null;
        }
        return matchers;
    }
    
    private static MatcherSealsBuilder getSealsBuilder(CommandLine cmd){
        List<String> javaRuntimeParameters = Arrays.asList("-Xmx25g", "-Xms15g");
        long timeout = Long.parseLong(cmd.getOptionValue(TIMEOUT_OPTION, "24"));
        TimeUnit timeUnit = TimeUnit.HOURS;
        MatcherSealsBuilder builder = new MatcherSealsBuilder()
                .setJavaRuntimeParameters(javaRuntimeParameters)                
                // KG track has no input alignment and some of the matchers throws an error if an input alignment is provided
                .setDoNotUseInputAlignment(true)
                .setFreshMatcherInstance(false)
                .setTimeout(timeout)
                .setTimeoutTimeUnit(timeUnit);
        
        String java8 = cmd.getOptionValue(JAVA8_OPTION);
        if(java8 != null)
            builder.setJavaCommand(java8);
        
        String tmpFolder = cmd.getOptionValue(TMP_FOLDER_OPTION);
        if(tmpFolder != null){
            LOGGER.info("Set the tmp folder of sealsbuilder to {}", tmpFolder);
            builder.setTmpFolder(new File(tmpFolder));
        }
        return builder;
    }
    
    private static File getFileOrThrowException(CommandLine cmd, String option){
        String value = cmd.getOptionValue(option);
        if(value == null){
            throw new IllegalArgumentException("Option " + option + " is not specified but required.");
        }
        return new File(value);
    }
    
    private static File getFileOrUseDefault(CommandLine cmd, String option){
        String value = cmd.getOptionValue(option);
        if(value == null){
            return Evaluator.getDirectoryWithCurrentTime();
        }else{
            return new File(value);
        }
    }
}
