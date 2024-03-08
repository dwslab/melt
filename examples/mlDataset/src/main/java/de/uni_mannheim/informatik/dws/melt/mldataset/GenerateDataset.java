package de.uni_mannheim.informatik.dws.melt.mldataset;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCaseType;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class will generate the splits and the new dataset.
 */
public class GenerateDataset {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateDataset.class);
    
    public static void main(String[] args) throws IOException, Exception{
        
        //in the order of the melt track repository:
                
        /*
        //anatomy
        MLDataGenerator gen = new MLDataGenerator(TrackRepository.Anatomy.Default.getFirstTestCase());
        gen.saveToFolder(new File("./datasets/anatomy"));
        
        
        //biodiv
        List<String> testCases = Arrays.asList(
            "envo-sweet", 
            "taxrefldAnimalia-ncbitaxonAnimalia",
            "taxrefldChromista-ncbitaxonChromista",
            "taxrefldFungi-ncbitaxonFungi",
            "taxrefldPlantae-ncbitaxonPlantae",
            "taxrefldProtozoa-ncbitaxonProtozoa"
        );
        for(String testCase : testCases){
            gen = new MLDataGenerator(TrackRepository.Biodiv.V2023.getTestCase(testCase));
            gen.saveToFolder(new File("./datasets/biodiv"));
        }
        
        //complex -> too few
        //food -> too few
        //conference -> handled extra
        
        //knowledge graph
        
        
        for(TestCase testCase : TrackRepository.Knowledgegraph.V4.getTestCases()){
            gen = new MLDataGenerator(testCase);
            gen.saveToFolder(new File("./datasets/knowledgegraph"));
            OntologyCacheJena.emptyCache();
        }
        */
        
        //commonkg -> too few
        //GeoLinkCruise -> not used
        //Laboratory -> not used and no reference
        //IIMDB -> not used
        //LargeBio -> old track
        //Link -> old track
        //multifarm -> too few
        //phenotype -> OLD
        //pm -> old
        //mse -> too few
        //pgx -> too few
        
        
        //bioML -> is already in the format
        for(TestCase testCase : TrackRepository.BioML.V2022.EQUIV_SUPERVISED.getTestCases()){
            saveToFolder(testCase, new File("./datasets/bioml"));
        }
    }

    
    private static void saveToFolder(TestCase tc, File folder) throws IOException{
        save(tc.getSource(), TestCaseType.SOURCE, folder, tc);
        save(tc.getTarget(), TestCaseType.TARGET, folder, tc);
        save(tc.getParameters(), TestCaseType.PARAMETERS, folder, tc);
        save(tc.getReference(), TestCaseType.REFERENCE, folder, tc);
        save(tc.getInputAlignment(), TestCaseType.INPUT, folder, tc);
        save(tc.getInputAlignment(), TestCaseType.EVALUATIONEXCLUSION, folder, tc);
    }
    
    private static void save(URI uri, TestCaseType type, File folder, TestCase tc) throws IOException{
        FileUtils.copyURLToFile(uri.toURL(), Paths.get(folder.getAbsolutePath(), tc.getName(), type.toFileName()).toFile());
    }
}
