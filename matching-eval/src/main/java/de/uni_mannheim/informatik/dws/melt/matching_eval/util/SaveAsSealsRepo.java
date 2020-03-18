
package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Track.class);
    
    
    public static void main(String[] args){
        save(TrackRepository.Largebio.V2016.FMA_NCI_SMALL, "./z_fma_nci_small/");
        save(TrackRepository.Largebio.V2016.FMA_NCI_WHOLE, "./z_fma_nci_whole/");
        save(TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL, "./z_fma_snomed_small/");
        save(TrackRepository.Largebio.V2016.FMA_SNOMED_WHOLE, "./z_fma_snomed_whole/");
        save(TrackRepository.Largebio.V2016.SNOMED_NCI_SMALL, "./z_snomed_nci_small/");
        save(TrackRepository.Largebio.V2016.SNOMED_NCI_WHOLE, "./z_snomed_nci_whole/");
    }

    
    public static void save(Track track, String folder){
        try {
            for(TestCase testCase : track.getTestCases()){
                FileUtils.copyURLToFile(testCase.getSource().toURL(), Paths.get(
                        folder,track.getName(), track.getVersion(), "suite",
                        testCase.getName(),"component", "source.xml").toFile());

                FileUtils.copyURLToFile(testCase.getTarget().toURL(), Paths.get(
                        folder,track.getName(), track.getVersion(), "suite",
                        testCase.getName(),"component", "target.xml").toFile());
                
                FileUtils.copyURLToFile(testCase.getReference().toURL(), Paths.get(
                        folder,track.getName(), track.getVersion(), "suite",
                        testCase.getName(),"component", "reference.xml").toFile());
            }
            
            saveSuiteFile(track, 
                    Paths.get(folder,track.getName(), track.getVersion(), "suite.xml").toFile()
            );
                    
            
        } catch (IOException ex) {
            LOGGER.error("Could not copy file.", ex);
        }
    }
    
    
    
    private static void saveSuiteFile(Track track, File suitefile){   
        Velocity.setProperty("resource.loader", "classpath");
        Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());        
        Velocity.init();
        
        VelocityContext context = new VelocityContext();
        context.put("track", track);
        
        Template suiteFileTemplate = Velocity.getTemplate("templates/seals/seals_suite_file.vm");
        
        try(Writer writer = new FileWriter(suitefile)){
            suiteFileTemplate.merge( context, writer );
        } catch (IOException ex) {
            LOGGER.error("Could not write to file.", ex);
        }
    }
    
   
}
