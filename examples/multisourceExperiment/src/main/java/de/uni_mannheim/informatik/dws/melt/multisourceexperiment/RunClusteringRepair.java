package de.uni_mannheim.informatik.dws.melt.multisourceexperiment;

import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.seals.MatcherSeals;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.seals.MatcherSealsBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.EvaluatorMultiSourceBasic;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutionResultMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutionResultSetMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutorMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering.FamerClustering;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.clustering.FilterByErrorDegree;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherIncrementalMergeByOrder;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherTransitivePairsOrderBased;
import static de.uni_mannheim.informatik.dws.melt.multisourceexperiment.Main.BASE_MATCHER;
import static de.uni_mannheim.informatik.dws.melt.multisourceexperiment.Main.METHOD;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.text.WordUtils;
import org.gradoop.famer.clustering.parallelClustering.center.Center;
import org.gradoop.famer.clustering.parallelClustering.clip.CLIP;
import org.gradoop.famer.clustering.parallelClustering.clip.dataStructures.CLIPConfig;
import org.gradoop.famer.clustering.parallelClustering.common.connectedComponents.ConnectedComponents;
import org.gradoop.famer.clustering.parallelClustering.common.dataStructures.ClusteringOutputType;
import org.gradoop.famer.clustering.parallelClustering.common.dataStructures.PrioritySelection;
import org.gradoop.famer.clustering.parallelClustering.mergeCenter.MergeCenter;
import org.gradoop.famer.clustering.parallelClustering.star.Star;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will run the clustering approaches to improve the all pairs approach.
 */
public class RunClusteringRepair {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunClusteringRepair.class);
    
    public static void main(String[] args){
        MeltUtil.logWelcomeMessage();
        Options options = new Options();

        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .required()
                .desc("path to a results folder which should be used as input")
                .build());
        
        options.addOption(Option.builder("c")
                .longOpt("cache")
                .hasArg()
                .argName("path")
                .desc("The path to the cache folder for ontologies")
                .build());
        
        options.addOption(Option.builder("r")
                .longOpt("results")
                .hasArgs()
                .argName("folderName")
                .desc("Provide the folder name of the results directory")
                .build());
        
        options.addOption("help", "print this message");
                
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
        
        if(cmd.hasOption("help")){
            formatter.printHelp("java -jar ", options);
            System.exit(1);
        }
        
        if(cmd.hasOption("cache")){
            File cacheFile = new File(cmd.getOptionValue("cache"));
            Track.setCacheFolder(cacheFile);
        }
             
        runClusteringOnTop(cmd);//
        //runClusteringOnTop(new File("all_pairs"));
    }
    
    
    
    public static void runClusteringOnTop(CommandLine cmd){
        File inputFolder = new File(cmd.getOptionValue("input"));
        if(inputFolder.isDirectory() == false){
            LOGGER.error("Folder {} is not a directory. Return.", cmd.getOptionValue("input"));
            return;
        }
        ExecutionResultSetMultiSource results = EvaluatorMultiSourceBasic.load(inputFolder);
        //get all all_pairs execution results
        ExecutionResultSetMultiSource filteredResults = new ExecutionResultSetMultiSource();
        Set<String> matcherNames = new HashSet<>();
        Set<Track> tracks = new HashSet<>();
        for(ExecutionResultMultiSource result : results){
            if(result.getMatcherName().endsWith("AllPairs")){
                filteredResults.add(result);
                matcherNames.add(result.getMatcherName());
                tracks.add(result.getTestCases().get(0).getTrack());
                //basicMatcher.add(result.getMatcherName().split("_")[0]);
            }
        }
        if(tracks.size() != 1){
            LOGGER.error("Not only one track in execution result");
            return;
        }
        DatasetIDExtractor idextractor = ExecutorMultiSource.getMostSpecificDatasetIdExtractor(tracks.iterator().next());
        
        for(String matcherName : matcherNames){
            
            for(boolean addCorrespondences : Arrays.asList(true, false)){
                filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_ConnectedComponents" + addCorrespondences, 
                    new FamerClustering(idextractor, new ConnectedComponents(Integer.MAX_VALUE, "", null, ClusteringOutputType.GRAPH), addCorrespondences, true));

                CLIPConfig clipconfig = new CLIPConfig();//0, 0, true, 0, 0, 0);
                filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_CLIP" + addCorrespondences, 
                        new FamerClustering(idextractor, new CLIP(clipconfig, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), addCorrespondences, true));

                filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_ConnectedComponents" + addCorrespondences, 
                        new FamerClustering(idextractor, new ConnectedComponents(Integer.MAX_VALUE, "", null, ClusteringOutputType.GRAPH), addCorrespondences, true));

                for(PrioritySelection prio : Arrays.asList(PrioritySelection.MIN, PrioritySelection.MAX)){
                    String prioName = WordUtils.capitalizeFully(prio.toString());
                    filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_Center" + prioName + addCorrespondences, 
                        new FamerClustering(idextractor, new Center(prio, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), addCorrespondences, true));

                    filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_MergeCenter" + prioName + addCorrespondences, 
                        new FamerClustering(idextractor, new MergeCenter(prio, 1.0, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), addCorrespondences, true));

                    filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_StarOne" + prioName + addCorrespondences, 
                        new FamerClustering(idextractor, new Star(prio, Star.StarType.ONE, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), addCorrespondences, true));

                    filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_StarTwo" + prioName +addCorrespondences, 
                        new FamerClustering(idextractor, new Star(prio, Star.StarType.TWO, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE), addCorrespondences, true));
                }
            }
            
            filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_Err99", new FilterByErrorDegree(0.99));
            filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_Err95", new FilterByErrorDegree(0.95));
            filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_Err90", new FilterByErrorDegree(0.90));
            filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_Err80", new FilterByErrorDegree(0.8));
            filteredResults = ExecutorMultiSource.runMatcherOnTop(filteredResults, matcherName, matcherName + "_Err60", new FilterByErrorDegree(0.6));
        }
        
        
        
        String resultsName = cmd.getOptionValue("results");
        File resultDir;
        if(resultsName == null){
            resultDir = Evaluator.getDirectoryWithCurrentTime();
        }else{
            resultDir = new File(Evaluator.getDefaultResultsDirectory(), resultsName);
        }
        
        new EvaluatorMultiSourceBasic(filteredResults).writeToDirectory(resultDir);
        
        ExecutionResultSet oneToOneResults = filteredResults.toExecutionResultSet();
        updateExecutionResultSet(oneToOneResults);
        EvaluatorCSV eval = new EvaluatorCSV(oneToOneResults);
        eval.writeToDirectory(resultDir);
    }
    
    
    public static void runClustering(Track track){
        MatcherSealsBuilder matcherSealsBuilder = new MatcherSealsBuilder();
        File sealsTmp = new File("sealsTmp");
        sealsTmp.mkdir();
        matcherSealsBuilder.setTmpFolder(sealsTmp);
        matcherSealsBuilder.addJavaRuntimeParameters("-Xmx8g"); //8g
        //matcherSealsBuilder.addJavaRuntimeParameters(TrackRepository.Largebio.getArgumentForUnlimitEntityExpansion());
        matcherSealsBuilder.setDoNotUseInputAlignment(true);
        
        MatcherSeals logMap = matcherSealsBuilder.build(Paths.get("oaeimatcher", "LogMap.zip").toFile());
        //MatcherSeals aml = matcherSealsBuilder.build(Paths.get("oaeimatcher", "AML.zip").toFile());
        //MatcherSeals atbox = matcherSealsBuilder.build(Paths.get("oaeimatcher", "ATBox.zip").toFile());
        Map<String, Object> oneToOneMatchers = new HashMap<>();
        oneToOneMatchers.put("LogMap", logMap);
        //oneToOneMatchers.put("AML", aml);
        //oneToOneMatchers.put("ATBox", atbox);

        
        Map<String, Object> matchers = new HashMap<>();
        for(Map.Entry<String, Object> ontToOne : oneToOneMatchers.entrySet()){
            //matchers.put(ontToOne.getKey(), new MultiSourceDispatcherAllPairs(ontToOne.getValue()));
            matchers.put(ontToOne.getKey(), new MultiSourceDispatcherTransitivePairsOrderBased(ontToOne.getValue(), MultiSourceDispatcherIncrementalMergeByOrder.AMOUNT_OF_CLASSES_ASCENDING, true));
        }
        
        //ExecutorMultiSourceParallel executor = new ExecutorMultiSourceParallel(5);
        //ExecutionResultSet results = executor.runMultipleMatchersMultipleTracks(
        ExecutionResultSetMultiSource results = ExecutorMultiSource.runMultipleMatchers(track.getTestCases(), matchers);
        
        DatasetIDExtractor idextractor = ExecutorMultiSource.getMostSpecificDatasetIdExtractor(track);
                
        for(Map.Entry<String, Object> ontToOne : oneToOneMatchers.entrySet()){
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_ConnectedComponents", 
                    new FamerClustering(idextractor, new ConnectedComponents(Integer.MAX_VALUE, "", null, ClusteringOutputType.GRAPH)));

            /*
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Center", 
                    new FamerClustering(idextractor, new Center(PrioritySelection.MIN, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE)));
            
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Center", 
                    new FamerClustering(idextractor, new Center(PrioritySelection.MIN, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE)));

            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_MergeCenter", 
                    new FamerClustering(idextractor, new MergeCenter(PrioritySelection.MIN, 1.0, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE)));
            
            CLIPConfig clipconfig = new CLIPConfig(0, 0, true, 0, 0, 0);
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_CLIP", 
                    new FamerClustering(idextractor, new CLIP(clipconfig, ClusteringOutputType.GRAPH, Integer.MAX_VALUE)));
            
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_StarOne", 
                    new FamerClustering(idextractor, new Star(PrioritySelection.MIN, Star.StarType.ONE, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE)));
            
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_StarTwo", 
                    new FamerClustering(idextractor, new Star(PrioritySelection.MIN, Star.StarType.TWO, false, ClusteringOutputType.GRAPH, Integer.MAX_VALUE)));
            
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_ConnectedComponents", 
                    new FamerClustering(idextractor, new ConnectedComponents(Integer.MAX_VALUE, "", null, ClusteringOutputType.GRAPH)));
            
            
            
            new FilterByErrorDegree().analyzeErrDistribution(results.iterator().next().getAlignment(Alignment.class), new File("logmap_analysis.txt"));
            
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Err99", new FilterByErrorDegree(0.99));
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Err95", new FilterByErrorDegree(0.95));
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Err90", new FilterByErrorDegree(0.90));
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Err80", new FilterByErrorDegree(0.8));
            results = ExecutorMultiSource.runMatcherOnTop(results, ontToOne.getKey(), ontToOne.getKey() + "_Err60", new FilterByErrorDegree(0.6));
            */
        }
        
        //new EvaluatorMultiSourceBasic(results).writeToDirectory();
        
                
        ExecutionResultSet oneToOneResults = results.toExecutionResultSet();
                
        updateExecutionResultSet(oneToOneResults);

        EvaluatorCSV eval = new EvaluatorCSV(oneToOneResults);
        eval.writeToDirectory();
    }
    
    private static void updateExecutionResultSet(ExecutionResultSet results){
        for(ExecutionResult result : results){
            Alignment a = result.getSystemAlignment();
            if(a == null)
                continue;
            String[] m = result.getMatcherName().split("_");
            if(m.length >= 2){
                a.addExtensionValue(BASE_MATCHER, m[0]);
                a.addExtensionValue(METHOD, m[1]);
            }
        }
    }
    
}
