package de.uni_mannheim.informatik.dws.melt.multisourceexperiment;

import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.multisourceexperiment.analysis.AnalyzeModelOrderDifference;
import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.external.seals.MatcherSealsBuilder;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutorMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrix;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.EvaluatorMultiSourceBasic;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutionResultSetMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutorMultiSourceParallel;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.ClusterLinkage;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.ModelAndIndex;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherAllPairs;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherIncrementalMergeByClusterText;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherIncrementalMergeByOrder;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherTransitivePairsOrderBased;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.MultiSourceDispatcherTransitivePairsTextBased;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.wrapper.ParisMatcher;
import static de.uni_mannheim.informatik.dws.melt.multisourceexperiment.Main.BASE_MATCHER;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class which allows to run multi source matcher by providing one to one matchers.
 */
public class Main {
    static{ System.setProperty("log4j.skipJansi", "false");}
    
    public static final String BASE_MATCHER = DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + "BaseMatcher";
    public static final String METHOD = DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + "Method";
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args){
        
        Options options = new Options();

        options.addOption(Option.builder("t")
                .longOpt("track")
                .hasArg()
                .desc("track to execute. possible values: conference, conferenceall, largebio, kg")
                .build());
        
        options.addOption(Option.builder("m")
                .longOpt("matcher")
                .hasArg()
                .argName("paths")
                //.required()
                .desc("path to seals matchers to use. can be repeated multiple times.")
                .build());
        
        options.addOption(Option.builder("c")
                .longOpt("cache")
                .hasArg()
                .argName("path")
                .desc("The path to the cache folder for ontologies")
                .build());
        
        options.addOption(Option.builder("g")
                .longOpt("graph")
                .hasArgs()
                .argName("paths")
                .desc("Paths which point to additional KGs in some RDF format.")
                .build());
        
        options.addOption(Option.builder("r")
                .longOpt("results")
                .hasArgs()
                .argName("folderName")
                .desc("Provide the folder name of the results directory")
                .build());
        
        options.addOption(Option.builder("j")
                .longOpt("jobs")
                .hasArgs()
                .argName("number of parallel jobs")
                .desc("Provide the number of parallel jobs which should be started.")
                .build());
        
        options.addOption("p", "paris", false, "If used, the PARIS matcher is added to the base matchers.");
        
        options.addOption(Option.builder("w")
                .longOpt("write")
                .hasArgs()
                .argName("resultsFolder")
                .desc("Write the results in a nicer format given the results folder")
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
        
        MeltUtil.logWelcomeMessage();
        
        if(cmd.hasOption("cache")){
            File cacheFile = new File(cmd.getOptionValue("cache"));
            Track.setCacheFolder(cacheFile);
        }
        
        String writeResultsPath = cmd.getOptionValue("write");
        if(writeResultsPath == null){
            runOAEIMatchersInMultiSourceSetup(cmd);
        }else{
            writeResults(new File(writeResultsPath));
        }        
    }
    
    
    public static void runOAEIMatchersInMultiSourceSetup(CommandLine cmd){
        Track track;
        switch(cmd.getOptionValue("track").toLowerCase()){
            case "conference":{
                track = TrackRepository.Conference.V1;
                break;
            }
            case "conferenceall":{
                track = TrackRepository.Conference.V1_ALL_TESTCASES;
                break;
            }
            case "largebio":{
                track = TrackRepository.Largebio.V2016.ONLY_WHOLE;
                break;
            }
            case "kg":{
                track = TrackRepository.Knowledgegraph.V3;
                break;
            }
            default:{
                LOGGER.warn("Track is not recognized. Do nothing.");
                return;
            }
        }
        
        MatcherSealsBuilder matcherSealsBuilder = new MatcherSealsBuilder();
        File sealsTmp = new File("sealsTmp");
        sealsTmp.mkdir();
        matcherSealsBuilder.setTmpFolder(sealsTmp);
        matcherSealsBuilder.addJavaRuntimeParameters("-Xmx200g"); //8g 200g
        matcherSealsBuilder.addJavaRuntimeParameters(TrackRepository.Largebio.getArgumentForUnlimitEntityExpansion());
        matcherSealsBuilder.setDoNotUseInputAlignment(true);
        
        Map<String, Object> oneToOneMatchers = new HashMap<>();
        String[] matcherPaths = cmd.getOptionValues("matcher");
        if(matcherPaths != null){
            for(String matcherPath : matcherPaths){
                File matcherFile = new File(matcherPath);
                if(matcherFile.exists() == false){
                    LOGGER.warn("Matcher path does not exists. Skip: {}", matcherPath);
                    continue;
                }
                oneToOneMatchers.put(FilenameUtils.removeExtension(matcherFile.getName()), matcherSealsBuilder.build(matcherFile));
            }
        }
        
        if(cmd.hasOption("p")){
            oneToOneMatchers.put("Paris", new UriInterfaceWrapper(new ParisMatcher(new File("oaeimatcher", "paris.jar"), new File("parisTmp"), "java", Arrays.asList("-Xmx200g"))));
        }
                
        Map<String, Comparator<ModelAndIndex>> orders;
        Set<ClusterLinkage> clusterLinkages;
        if(cmd.getOptionValues("g") == null){
            orders = AnalyzeModelOrderDifference.getOrders(track);
            clusterLinkages = AnalyzeModelOrderDifference.getClusterLinkages(track);
        }else{
            orders = AnalyzeModelOrderDifference.getAllOrders();
            clusterLinkages = AnalyzeModelOrderDifference.getAllClusterLinkages();
        }
        
        Map<String, Object> matchers = new HashMap<>();
                
        
        for(Entry<String, Object> ontToOne : oneToOneMatchers.entrySet()){
            matchers.put(ontToOne.getKey() + "_AllPairs", new MultiSourceDispatcherAllPairs(ontToOne.getValue()));
            for(Entry<String, Comparator<ModelAndIndex>> order : orders.entrySet()){
                matchers.put(ontToOne.getKey() + "_IncrementalMerge" + order.getKey(), new MultiSourceDispatcherIncrementalMergeByOrder(ontToOne.getValue(), order.getValue()));
                
                matchers.put(ontToOne.getKey() + "_TransitivePairsWindow" + order.getKey(), new MultiSourceDispatcherTransitivePairsOrderBased(ontToOne.getValue(), order.getValue(), false));
                matchers.put(ontToOne.getKey() + "_TransitivePairsFirstVsRest" + order.getKey(), new MultiSourceDispatcherTransitivePairsOrderBased(ontToOne.getValue(), order.getValue(), true));
            }
            matchers.put(ontToOne.getKey() + "_TransitivePairsTextBased", new MultiSourceDispatcherTransitivePairsTextBased(ontToOne.getValue()));

            for(ClusterLinkage clusterLinkage : clusterLinkages){
                matchers.put(ontToOne.getKey() + "_IncrementalMergeText" + WordUtils.capitalizeFully(clusterLinkage.toString()) , new MultiSourceDispatcherIncrementalMergeByClusterText(ontToOne.getValue(), clusterLinkage));
            }
        }
        
        Properties addParams = new Properties();
        
        File serializationFolder = new File("serializationFolder");
        serializationFolder.mkdirs();
        addParams.put(ParameterConfigKeys.SERIALIZATION_FOLDER, serializationFolder.getAbsolutePath());
        if(TrackRepository.retrieveDefinedTracks(TrackRepository.Knowledgegraph.class).contains(track))
            addParams.put(ParameterConfigKeys.DEFAULT_ONTOLOGY_SERIALIZATION_FORMAT, "N-Triples"); // for KG track
        
        
        ExecutionResultSetMultiSource results; 
        String jobs = cmd.getOptionValue("jobs");
        if(jobs != null){
            int numberJobs;
            try{
                numberJobs = Integer.parseInt(jobs);
            }catch(NumberFormatException ex){
                LOGGER.warn("Could nto parse number of jobs: {}. defaulting to 2", jobs, ex);
                numberJobs = 2;
            }
            LOGGER.info("Run matchers parallel with {} threads.", numberJobs);
            ExecutorMultiSourceParallel executor = new ExecutorMultiSourceParallel(numberJobs);
            results = executor.runMultipleMatchersMultipleTracks(
                    Arrays.asList(track), matchers, addParams, getAdditonalGraphs(cmd));
        }else{
            LOGGER.info("Run matchers one after the other.");
            results = ExecutorMultiSource.runMultipleMatchersWithAdditionalGraphs(
                track.getTestCases(), matchers, addParams, getAdditonalGraphs(cmd));
        }
        
        String resultsName = cmd.getOptionValue("results");
        File resultDir;
        if(resultsName == null){
            resultDir = Evaluator.getDirectoryWithCurrentTime();
        }else{
            resultDir = new File(Evaluator.getDefaultResultsDirectory(), resultsName);
        }
        
        new EvaluatorMultiSourceBasic(results).writeToDirectory(resultDir);
        
        ExecutionResultSet oneToOneResults = results.toExecutionResultSet();
        
        updateExecutionResultSet(oneToOneResults);

        EvaluatorCSV eval = new EvaluatorCSV(oneToOneResults);
        eval.writeToDirectory(resultDir);
    }
    
    private static List<URL> getAdditonalGraphs(CommandLine cmd){
        List<URL> additionalGraphs = new ArrayList<>();
        String[] graphs = cmd.getOptionValues("g");
        if(graphs == null)
            return additionalGraphs;
        
        for(String graph : graphs){
            File graphFile = new File(graph);
            if(graphFile.exists() == false){
                LOGGER.warn("provided graph file does not exist. Skipping this file: {}", graph);
                continue;
            }
            try {
                additionalGraphs.add(graphFile.toURI().toURL());
            } catch (MalformedURLException ex) {
                LOGGER.warn("File URI of graph file could not be converted to a URL: {}", graph, ex);
            }
        }
        return additionalGraphs;
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
    
    
    public static void writeResults(File resultsFolder){
        ExecutionResultSetMultiSource results = EvaluatorMultiSourceBasic.load(resultsFolder);
        ExecutionResultSet ers = results.toExecutionResultSet();
        new WriteLatex(ers).writeToDirectory();
        
        updateExecutionResultSet(ers);
        EvaluatorCSV eval = new EvaluatorCSV(ers);
        eval.writeToDirectory();
    }
}


class WriteLatex extends Evaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteLatex.class);
    private static final DecimalFormat DF = new DecimalFormat("#.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
    
    public WriteLatex(ExecutionResultSet results) {
        super(results);
    }
    
    @Override
    protected void writeResultsToDirectory(File baseDirectory) {
        List<String> matchers = getMatcherNamePart(0);
        List<String> approaches = getMatcherNamePart(1);
        ConfusionMatrixMetric confusionMatrixMetric = new ConfusionMatrixMetric();
        try(BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(baseDirectory, "multiSourceResults.tex")), StandardCharsets.UTF_8))){
            
            out.write("\\documentclass[12pt]{article}");out.newLine();
            out.write("\\usepackage{lscape}");out.newLine();
            out.write("\\begin{document}");out.newLine();
            out.write("\\begin{landscape}");out.newLine();            
            out.write("\\begin{tabular}{|l|" + StringUtils.repeat("l|l|", matchers.size()) + "}");out.newLine();
            out.write("\\hline");out.newLine();
            for(String matcher : matchers){
                out.write("& \\multicolumn{2} {c|} {\\bfseries " + matcher + "}");
            }
            out.write("\\\\");out.newLine();
            out.write("Approach" + StringUtils.repeat("& F-M. & Time", matchers.size()) + "\\\\");out.newLine();
            
            
            int multiColumn = 1 + (matchers.size() * 2);
            for(Track track : this.results.getDistinctTracksSorted()){
                out.write("\\hline");out.newLine();
                out.write("\\multicolumn{" + multiColumn + "}{|c|}{" + track.getName() + "}\\\\");out.newLine();
                out.write("\\hline");out.newLine();
                for(String approach : approaches){
                    out.write(approach);
                    for(String baseMatcher : matchers){
                        Set<ExecutionResult> all = this.results.getGroup(track, baseMatcher + "_" + approach);
                        ConfusionMatrix microAllCm = confusionMatrixMetric.getMicroAveragesForResults(all);
                        String formattedRuntime = EvaluatorCSV.getFormattedRuntime(EvaluatorCSV.getSummedRuntimeOfResults(all));
                        
                        out.write(" & " + DF.format(microAllCm.getF1measure()) + " & " + formattedRuntime);
                    }
                    out.write("\\\\");
                    out.newLine();
                }
            }
            out.write("\\hline");out.newLine();
            out.write("\\end{tabular}");out.newLine();
            out.write("\\end{landscape}");out.newLine();            
            out.write("\\end{document}");out.newLine();
        } catch (IOException ex) {
            LOGGER.error("Could not write results to file", ex);
        }
    }
    
    private List<String> getMatcherNamePart(int position){
        Set<String> baseMatchers = new HashSet<>();
        for(ExecutionResult result : this.results){
            String[] m = result.getMatcherName().split("_");
            if(m.length >= position){
                baseMatchers.add(m[position]);
            }else{
                LOGGER.warn("Could not extract the part of the matcher name:{} with position {}", result.getMatcherName(), position);
            }
        }
        List<String> list = new ArrayList<>(baseMatchers);
        Collections.sort(list);
        return list;
    }
}
