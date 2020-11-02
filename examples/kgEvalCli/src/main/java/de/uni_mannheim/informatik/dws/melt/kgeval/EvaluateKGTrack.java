package de.uni_mannheim.informatik.dws.melt.kgeval;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutorSeals;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorAlignmentAnalyzer;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceType;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.dashboard.DashboardBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.resultspage.ResultsPageHTML;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.visualization.resultspage.ResultsPageLatex;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentXmlRepair;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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

    public static void main(String[] args){
        Options options = new Options();
        
        //seals options
        options.addOption("sealsClient", true, "The path to seals client (defaults to ./seals-omt-client.jar)");
        options.addOption("sealsHome", true, "The path to seals home (defaults to ./SEALS_HOME/)");
        options.addOption("sealsResults", true, "The path to seals results (defaults to ./SEALS_RESULTS/)");
        options.addOption("sealsMatcher", true, "The path to seals matchers (defaults to ./SEALS_MATCHER/)");
        
        options.addOption("inFolder", true, "The path to a folder where the files should be processed.");
        options.addOption("outFolder", true, "The path to a folder where the processed files should be stored in the end.");
        
        options.addOption("timeoutHours", true, "the number of hours for the timeout (default is 24 hours).");      
        options.addOption("testcases", true, "name of the testcases to execute(separated by comma) e.g. starwars-swg,memoryalpha-stexpanded");                
        options.addOption("track", true, "The name of the track. Possible values: small-test,v1,v2,v3,v4,v3-nonmatch-small,v3-nonmatch-large");
        
        options.addOption(Option.builder("method")
                .hasArg()
                .required()
                .desc("method to call. one of: run,repair,check,resultspage")
                .build());
        options.addOption(Option.builder("cache")
                .hasArg()
                .argName("path")                
                .desc("The path to the cache folder for ontologies")
                .build());
        
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
        
        if(cmd.hasOption("cache")){
            File cacheFile = new File(cmd.getOptionValue("cache"));
            LOGGER.info("Setting cache to {}", cacheFile);
            Track.setCacheFolder(cacheFile);
        }
        
        switch(cmd.getOptionValue("method").toLowerCase()){
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
        File sealsClientJar = new File(cmd.getOptionValue("sealsClient", "./seals-omt-client.jar"));
        File sealsHome = new File(cmd.getOptionValue("sealsHome", "./SEALS_HOME/"));
        File sealsResults = new File(cmd.getOptionValue("sealsResults", "./SEALS_RESULTS/"));
        File sealsMatcher = new File(cmd.getOptionValue("sealsMatcher", "./SEALS_MATCHER/"));
        
        List<String> javaRuntimeParameters = Arrays.asList("-Xmx25g", "-Xms15g");
        long timeout = Long.parseLong(cmd.getOptionValue("timeoutHours", "24"));
        TimeUnit timeoutUnit = TimeUnit.HOURS;
        
        List<TestCase> testCases = getTestCases(cmd);
        
        LOGGER.info("Run seals with the following settings:");
        LOGGER.info("testCases: {}", testCases.stream().map(tc->tc.getName()).collect(Collectors.joining(",")));
        LOGGER.info("sealsClientJar: {}", sealsClientJar);
        LOGGER.info("sealsHome: {}", sealsHome);
        LOGGER.info("sealsResults: {}", sealsResults);
        LOGGER.info("sealsMatcher: {}", sealsMatcher);
        LOGGER.info("javaRuntimeParameters: {}", javaRuntimeParameters);
        LOGGER.info("for {} {}", timeout, timeoutUnit);
        
        ExecutorSeals e = new ExecutorSeals(sealsClientJar, sealsHome, sealsResults, javaRuntimeParameters, timeout, timeoutUnit, true);
        
        e.run(testCases, sealsMatcher);
    }
    
    private static void repairAlignmentFiles(CommandLine cmd){        
        File in = new File(cmd.getOptionValue("inFolder", "./SEALS_RESULTS/"));
        File out = new File(cmd.getOptionValue("outFolder", "./SEALS_RESULTS_REPAIRED/"));    
        
        LOGGER.info("Repair alignment xml from folder {} and copies the results to {}", in, out);
        AlignmentXmlRepair.repairAlignmentFolder(in, out);
    }
    
    private static void checkAlignments(CommandLine cmd){
        File in = new File(cmd.getOptionValue("inFolder", "./SEALS_RESULTS_REPAIRED/"));
        File out = new File(cmd.getOptionValue("outFolder", "./MELT_RESULTS/"));
        Track track = getTrack(cmd);
        
        LOGGER.info("Check the alignments in folder {} for track {} {} and writes the results to {}", in, track.getName(), track.getVersion(), out);        
        ExecutionResultSet resultSet = Executor.loadFromFolder(in, track);        
        new EvaluatorAlignmentAnalyzer(resultSet).writeToDirectory(out);
    }
    
    private static void generateResultsPage(CommandLine cmd){
        File in = new File(cmd.getOptionValue("inFolder", "./SEALS_RESULTS_REPAIRED/"));
        File out = new File(cmd.getOptionValue("outFolder", "./MELT_RESULTS/"));
        Track track = getTrack(cmd);
        
        LOGGER.info("Generates a results page from folder {} for track {} {} and writes the results to {}", in, track.getName(),track.getVersion(), out);    
        
        ExecutionResultSet results = Executor.loadFromFolder(in, track);
        
        //results page
        new ResultsPageHTML(results, false, false).writeToDirectory(out);
        new ResultsPageLatex(results, false).writeToDirectory(out);
        
        
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(results);

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
        Map<String, Track> map = new HashMap();
        for(Track t : TrackRepository.retrieveDefinedTracks(TrackRepository.Knowledgegraph.class)){
            map.put(t.getVersion().toLowerCase(), t);
        }
        return map;
    }
    
    private static Track getTrack(CommandLine cmd){        
        Track track = MAP_VERSION_TO_TRACK.get(cmd.getOptionValue("track", "V4").toLowerCase());
        if(track == null){
            LOGGER.error("Could not find knowledge track track version. Abort.");
            throw new IllegalArgumentException("Could not find knowledge track track version.");
        }
        return track;
    }
    
    private static List<TestCase> getTestCases(CommandLine cmd){
        Track track = getTrack(cmd);
        String testCases = cmd.getOptionValue("testcases", "");
        testCases = testCases.trim();
        if(testCases.isEmpty())
            return track.getTestCases();
        List<String> testCaseNames = Arrays.stream(testCases.split(",")).map(String::trim).collect(Collectors.toList());
        return track.getTestCases(testCaseNames);
    }
    
}
