
package de.uni_mannheim.informatik.dws.melt.matching_eval.util;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A quick helper program for track organizers and MELT administrators.
 * Creates a track in folder test so that it can be used for the SEALS/MELT Track Repository.
 */
public class SaveAsSealsRepo {


    private static final Logger LOGGER = LoggerFactory.getLogger(SaveAsSealsRepo.class);

    /**
     * Simple utility function to save a track as SEALS repository track.
     * @param track Any track (can be a SealsTrack).
     * @param folder The folder containing the track data.
     */
    public static void save(Track track, String folder){
        List<TestCase> testCases = track.getTestCases();
        if(testCases.isEmpty()){
            LOGGER.error("The given track has no testcases.");
            return;
        }
        try {
            for(TestCase testCase : testCases){
                FileUtils.copyURLToFile(testCase.getSource().toURL(), Paths.get(
                        folder,track.getName(), track.getVersion(), "suite",
                        testCase.getName(),"component", "source.xml").toFile());

                FileUtils.copyURLToFile(testCase.getTarget().toURL(), Paths.get(
                        folder,track.getName(), track.getVersion(), "suite",
                        testCase.getName(),"component", "target.xml").toFile());
                
                if(testCase.getReference() != null){
                    FileUtils.copyURLToFile(testCase.getReference().toURL(), Paths.get(
                        folder,track.getName(), track.getVersion(), "suite",
                        testCase.getName(),"component", "reference.xml").toFile());
                }
            }
            saveSuiteFile(track, 
                    Paths.get(folder, track.getName(), track.getVersion(), "suite.xml").toFile()
            );
        } catch (IOException ex) {
            LOGGER.error("Could not copy file.", ex);
        }
    }

    private static void saveSuiteFile(Track track, File suiteFile){
        Velocity.setProperty("resource.loaders", "classpath");
        Velocity.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());        
        Velocity.init();
        
        VelocityContext context = new VelocityContext();
        context.put("track", track);
        
        Template suiteFileTemplate = Velocity.getTemplate("templates/seals/seals_suite_file.vm");
        
        try(Writer writer = new FileWriter(suiteFile)){
            suiteFileTemplate.merge( context, writer );
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
    }
    
    public static void main(String[] args){
        save(TrackRepository.Biodiv.V2021OWL, "./upload");
    }
}
