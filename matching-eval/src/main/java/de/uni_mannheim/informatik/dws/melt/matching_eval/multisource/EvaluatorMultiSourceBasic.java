package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class EvaluatorMultiSourceBasic extends EvaluatorMultiSource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatorMultiSourceBasic.class);
    
    public EvaluatorMultiSourceBasic(ExecutionResultSetMultiSource results){
        super(results);
    }

    @Override
    protected void writeResultsToDirectory(File baseDirectory) {        
        for(ExecutionResultMultiSource result : this.results){
            if(result.getTestCases().isEmpty())
                continue;
            //assumes that all testcases belongs to the same track (a warning is generated in evaluator if this is not the case
            Track track = result.getTestCases().iterator().next().getTrack();
            File trackMatcherFolder = getResultsDirectoryTrackMatcher(baseDirectory, track, result.getMatcherName());
            File alignmentFile = new File(trackMatcherFolder, "alignment.rdf");
            Alignment a = result.getAlignment(Alignment.class);
            if(a != null){
                try {
                    a.serialize(alignmentFile);
                } catch (IOException ex) {
                    LOGGER.error("Could not write alignment", ex);
                }
            }
            
            Properties p = result.getParameters(Properties.class);
            if(p != null){
                try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(new File(trackMatcherFolder, "parameters.json")), StandardCharsets.UTF_8))){
                    bw.write(new JSONObject(p).toString());
                } catch (IOException ex) {
                    LOGGER.error("Could not write parameters.json file to results folder.", ex);
                }
            }
            
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new File(trackMatcherFolder, "infos.json")), StandardCharsets.UTF_8))){
                Map<String,Object> map = new HashMap<>();
                map.put("allGraphs", result.getAllGraphs());
                map.put("testcases", result.getTestCases().stream().map(tc->tc.getName()).collect(Collectors.toList()));
                map.put("runtime", result.getTotalRuntime());
                map.put("computeTransitiveClosure", result.isComputeTransitiveClosure());
                bw.write(new JSONObject(map).toString());
            } catch (IOException ex) {
                LOGGER.error("Could not write parameters.json file to results folder.", ex);
            }
        }
    }
    
    
    
    public static ExecutionResultSetMultiSource load(File folder){        
        ExecutionResultSetMultiSource results = new ExecutionResultSetMultiSource(); 
        
        for (File trackFolder : folder.listFiles()) {
            if(trackFolder.isDirectory()){
                Track track = TrackRepository.getMapFromTrackNameAndVersionToTrack().get(trackFolder.getName());
                if(track == null){
                    LOGGER.error("cannot read from folder {} because track doesn't exist.", trackFolder.getName());
                    continue;
                }
                File multisourceResults = new File(trackFolder, "multisource_results");
                if(multisourceResults.isDirectory() == false){
                    LOGGER.info("Folder multisource_results does not exist. Use next track folder.");
                    continue;
                }
                for (File matcherFolder : multisourceResults.listFiles()) {
                    //load alignment
                    Alignment alignment = loadAlignment(matcherFolder);
                    if(alignment == null){
                        LOGGER.error("alignment file (alignment.rdf) is missing in folder {}. Continue with next folder.", matcherFolder.getAbsolutePath());
                        continue;
                    }
                    
                    Properties parameters = new Properties();
                    parameters.putAll(loadJSONObject(matcherFolder, "parameters.json").toMap());
                    
                    JSONObject infos = loadJSONObject(matcherFolder, "infos.json");
                    
                    List<URL> allGraphs = getAllGraphs(infos);                    
                    List<TestCase> testcases = getTestCases(infos, track);
                    
                    long runtime = 0;
                    try{
                        runtime = infos.getLong("runtime");
                    }catch(JSONException ex){
                        LOGGER.warn("Runtime not in json. Defaulting to zero");
                    }
                    
                    boolean computeTransitiveClosure = false;
                    try{
                        computeTransitiveClosure = infos.getBoolean("computeTransitiveClosure");
                    }catch(JSONException ex){
                        LOGGER.warn("ComputeTransitiveClosure not in json. Defaulting to false.");
                    }
                    
                    Partitioner partitioner = ExecutorMultiSource.getMostSpecificPartitioner(track);
                    
                    String matcherName = "";
                    try {
                        matcherName = URLDecoder.decode(matcherFolder.getName(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {LOGGER.warn("Unsupported Encoding", ex);}

                    results.add(new ExecutionResultMultiSource(alignment, parameters, null, matcherName, allGraphs, testcases, runtime, computeTransitiveClosure, partitioner));                    
                }
            }
        }
        if(results.isEmpty())
            LOGGER.warn("Did not read any results during the load funtion. Check folder {}", folder);
        return results;
    }
    
    private static Alignment loadAlignment(File matcherFolder){        
        File alignmentFile = new File(matcherFolder, "alignment.rdf");
        if(alignmentFile.exists() == false){
            return null;
        }      
        try {
            return AlignmentParser.parse(alignmentFile);
        }catch(FileNotFoundException ex){
            LOGGER.error("The system alignment file {} does not exist.", matcherFolder, ex);
        }
        catch (SAXException | IOException | NullPointerException ex) {
            LOGGER.error("The system alignment could not be parsed: {}", matcherFolder, ex);
        }
        return null;
    }
        
    
    private static JSONObject loadJSONObject(File matcherFolder, String filename){
        File parametersFile = new File(matcherFolder, filename);    
        
        String content;
        try {
            content = new Scanner(parametersFile).useDelimiter("\\Z").next();
        } catch (FileNotFoundException ex) {
            LOGGER.warn("{} is not found. Returning empty map.", filename);
            return new JSONObject();
        }
        
        if(content == null || content.trim().isEmpty()){
            LOGGER.warn("The content of {} is empty. Returning empty map.", filename);
            return new JSONObject();
        }
        
        try{
            return new JSONObject(content);
        }catch(JSONException ex){
            LOGGER.debug("Could not parse JSON in file {}. Returning empty map.", filename, ex);
            return new JSONObject();
        }
    }
    
    
    private static List<URL> getAllGraphs(JSONObject infos){
        List<URL> allGraphs = new ArrayList<>();
        
        JSONArray arr = null;
        try{
            arr = infos.getJSONArray("allGraphs");
        }catch(JSONException ex){
            LOGGER.warn("allGraphs not in json. Defaulting to empty list.");
            return allGraphs;
        }
        
        for (int i = 0; i < arr.length(); i++) {
            String s = "";
            try{
                s = arr.getString(i);
            }catch(JSONException ex){
                LOGGER.warn("Some element in allGraphs in infos.json is not a string and it is skipped");
                continue;
            }
            try {
                allGraphs.add(new URL(s));
            } catch (MalformedURLException ex) {
                LOGGER.warn("Some string in allGraphs in infos.json is not a URL and it is skipped: {}", s, ex);
            }
        }
        return allGraphs;
    }
    
    private static List<TestCase> getTestCases(JSONObject infos, Track track) {
        List<TestCase> testcases = new ArrayList<>();
        
        JSONArray arr = null;
        try{
            arr = infos.getJSONArray("testcases");
        }catch(JSONException ex){
            LOGGER.warn("No testcases in infos.json or not a list. Default to empty list.");
            return testcases;
        }
        
        for (int i = 0; i < arr.length(); i++) {
            String s = "";
            try{
                s = arr.getString(i);
            }catch(JSONException ex){
                LOGGER.warn("Some element in testcases in infos.json is not a string and it is skipped");
                continue;
            }
            
            TestCase c = track.getTestCase(s);
            if(c == null){
                LOGGER.warn("Testcase with name {} is not available in track {}. It is skipped.", s, track);
                continue;
            }
            testcases.add(c);
        }
        return testcases;
    }
    
    protected File getResultsDirectoryTrackMatcher(File baseDirectory, Track track, String matcher){
        try {
            return Paths.get(baseDirectory.getAbsolutePath(),
                    URLEncoder.encode(track.getName(), "UTF-8") + "_" + URLEncoder.encode(track.getVersion(), "UTF-8"),
                    "multisource_results", URLEncoder.encode(matcher, "UTF-8"))
                    .toFile();
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Could not crreate results folder", ex);
            return Paths.get(baseDirectory.getAbsolutePath()).toFile();
        }
    }
}
