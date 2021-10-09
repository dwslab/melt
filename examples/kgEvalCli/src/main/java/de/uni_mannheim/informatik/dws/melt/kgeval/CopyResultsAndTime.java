package de.uni_mannheim.informatik.dws.melt.kgeval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import static de.uni_mannheim.informatik.dws.melt.matching_eval.Executor.loadFromFolder;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.Evaluator;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util.EvaluatorUtil;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CopyResultsAndTime extends Evaluator {
    private static final Logger LOGGER = LoggerFactory.getLogger(CopyResultsAndTime.class);

    private static ObjectMapper MAPPER = new ObjectMapper();
    private static final String RUNTIME_KEY = "runtime";
    private static final String INFO_FILE_NAME = "info.json";
    private static final String ALIGNMENT_FILE_NAME = "systemAlignment.rdf";
    
    public CopyResultsAndTime(ExecutionResultSet results) {
        super(results);
    }

    @Override
    public void writeResultsToDirectory(File baseDirectory) {
        for (ExecutionResult r : this.results) {
            EvaluatorUtil.copySystemAlignment(r, new File(getResultsFolderTrackTestcaseMatcher(baseDirectory, r), ALIGNMENT_FILE_NAME));
            
            File infoFile = new File(getResultsFolderTrackTestcaseMatcher(baseDirectory, r), INFO_FILE_NAME);
            
            Map<String, Object> map = new HashMap<>();
            map.put(RUNTIME_KEY, r.getRuntime());
            
            try {
                MAPPER.writeValue(infoFile, map);
            } catch (IOException ex) {
                LOGGER.error("Couldn't write the inof.json file to the results directory.", ex);
            }
        }
    }
    
    public static ExecutionResultSet loadFromFolder(File folder){
        
        Map<String, Track> txtToTrack = TrackRepository.getMapFromTrackNameAndVersionToTrack();
        if (!folder.isDirectory()) {
            LOGGER.error("The specified folder is not a directory. Returning empty resultSet.");
            return new ExecutionResultSet();
        }
        ExecutionResultSet results = new ExecutionResultSet();
        for (File trackFolder : folder.listFiles()) {
            if(trackFolder.isDirectory() == false){
                continue;
            }
            Track track = txtToTrack.get(trackFolder.getName());
            if(track == null){
                LOGGER.error("cannot read from folder {} because track doesn't exist.", trackFolder.getName());
                continue;
            }
            for (File testCaseFolder : trackFolder.listFiles()) {
                if(testCaseFolder.isDirectory() == false){
                    continue;
                }
                TestCase testcase = track.getTestCase(testCaseFolder.getName());
                if(testcase == null){
                    LOGGER.error("cannot read from folder {} because testcase doesn't exist in track {} .", testCaseFolder.getName(), track.getName());
                    continue;
                }

                for (File matcherFolder : testCaseFolder.listFiles()) {
                    if(matcherFolder.isDirectory() == false){
                        continue;
                    }
                    File infoFile = new File(matcherFolder, INFO_FILE_NAME);
                    if(infoFile.exists() == false){
                        LOGGER.warn("Info.json file is missing in folder {} Skipping it.", matcherFolder);
                        continue;
                    }
                    long runtime = 0;
                    try {
                        Map<String, Object> map = MAPPER.readValue(infoFile, new TypeReference<Map<String,Object>>(){});
                        runtime = (Long) map.get(RUNTIME_KEY);
                    } catch (IOException ex) {
                        LOGGER.error("Could not parse info.json file. Skipping this folder: {}", ex.getMessage());
                    }
                    
                    File systemAlignmentFile = new File(matcherFolder, ALIGNMENT_FILE_NAME);
                    if(systemAlignmentFile.exists() == false){
                        LOGGER.warn("{} file is missong in folder {} Skipping it.", ALIGNMENT_FILE_NAME, matcherFolder);
                        continue;
                    }
                    
                    //(TestCase testCase, String matcherName, URL originalSystemAlignment, long runtime, Object matcher) {
                    //matcherFolder
                    String matcherName;
                    try {
                        matcherName = URLDecoder.decode(matcherFolder.getName(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        matcherName = matcherFolder.getName();
                    }
                    try {    
                        results.add(new ExecutionResult(testcase, matcherName, systemAlignmentFile.toURI().toURL(), runtime, null));
                    } catch (MalformedURLException ex) {
                        LOGGER.warn("Could not transform file path to url. Skipping this folder: {}", matcherFolder);
                    }            
                }    
            }
        }
        return results;
    }
}

