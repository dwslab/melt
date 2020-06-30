
package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    
    public static void main(String[] args) throws IOException{
        //save(TrackRepository.Largebio.V2016.FMA_NCI_SMALL, "./z_fma_nci_small/");
        //save(TrackRepository.Largebio.V2016.FMA_NCI_WHOLE, "./z_fma_nci_whole/");
        //save(TrackRepository.Largebio.V2016.FMA_SNOMED_SMALL, "./z_fma_snomed_small/");
        //save(TrackRepository.Largebio.V2016.FMA_SNOMED_WHOLE, "./z_fma_snomed_whole/");
        //save(TrackRepository.Largebio.V2016.SNOMED_NCI_SMALL, "./z_snomed_nci_small/");
        //save(TrackRepository.Largebio.V2016.SNOMED_NCI_WHOLE, "./z_snomed_nci_whole/");
        //save(TrackRepository.Complex.Popenslaved, "./Popenslaved_upload/");

        Track.setSkipTestCasesWithoutRefAlign(false);
        save(TrackRepository.Complex.Popconference0, "D:\\upload_tmp");
        save(TrackRepository.Complex.Popconference20, "D:\\upload_tmp");
        save(TrackRepository.Complex.Popconference40, "D:\\upload_tmp");
        save(TrackRepository.Complex.Popconference60, "D:\\upload_tmp");
        save(TrackRepository.Complex.Popconference80, "D:\\upload_tmp");
        save(TrackRepository.Complex.Popconference100, "D:\\upload_tmp");
        //transformPopulatedOntologies();
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
    
    
    /**
     * Transforms the zip file into a standard MELT format.
     * @throws IOException Exception occurred during transformation.
     */
    private static void transformPopulatedOntologies() throws IOException{
        
        String basedir = "conference_100\\ont";
        String targetDir = "C:\\Users\\shertlin\\oaei_track_cache\\oaei.webdatacommons.org\\popconference\\popconference-100-v1";
        Map<String, File> ontFiles = new HashMap();
        ontFiles.put("cmt", new File(basedir, "cmt.owl"));
        ontFiles.put("conference", new File(basedir, "conference.owl"));
        ontFiles.put("confOf", new File(basedir, "confOf.owl"));
        ontFiles.put("edas", new File(basedir, "edas.owl"));
        ontFiles.put("ekaw", new File(basedir, "ekaw.owl"));
        
        List<String> testcases = Arrays.asList(
            "cmt-conference", "cmt-confOf", "cmt-edas", "cmt-ekaw",
            "conference-cmt", "conference-confOf", "conference-edas", "conference-ekaw",
            "confOf-cmt", "confOf-conference", "confOf-edas", "confOf-ekaw",
            "edas-cmt", "edas-conference", "edas-confOf", "edas-ekaw",
            "ekaw-cmt", "ekaw-conference", "ekaw-confOf", "ekaw-edas"
        );
                
        for(String testcase : testcases){
            String[] names = testcase.split("-");
            if(names.length != 2){
                LOGGER.info("Wrong split");
                continue;
            }
            File source = ontFiles.get(names[0]);
            File target = ontFiles.get(names[1]);
            
            FileUtils.copyFile(source, Paths.get(targetDir, testcase, "source.rdf").toFile());
            FileUtils.copyFile(target, Paths.get(targetDir, testcase, "target.rdf").toFile());
        }
    }
   
}
