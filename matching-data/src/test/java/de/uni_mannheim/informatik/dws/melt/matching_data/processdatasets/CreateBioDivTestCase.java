package de.uni_mannheim.informatik.dws.melt.matching_data.processdatasets;

import java.io.File;
import java.io.FilenameFilter;
import org.apache.commons.io.FileUtils;


public class CreateBioDivTestCase {
    
    public static void main(String[] args) throws Exception{
        
        File input = new File("./biodiv-owl");
        File output = new File("./out");
        
        File[] referenceFiles = input.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("reference-");
            }
        });
        
        for(File ref : referenceFiles){
            String testCaseName = ref.getName().replace("reference-", "").replace(".rdf", "");
            File testCaseDir = new File(output, testCaseName);
            String[] sourceTarget = testCaseName.split("-");
            
            
            File sourceOnt = new File(input, sourceTarget[0] + ".owl");
            if(sourceOnt.exists() == false){
                throw new Exception("Source not found: " + sourceTarget[0] + ".owl");
            }
            
            File targetOnt = new File(input, sourceTarget[1] + ".owl");
            if(targetOnt.exists() == false){
                throw new Exception("Target not found: " + sourceTarget[1] + ".owl");
            }
            
            FileUtils.copyFile(sourceOnt, new File(testCaseDir, "source.rdf"));
            FileUtils.copyFile(targetOnt, new File(testCaseDir, "target.rdf"));
            FileUtils.copyFile(ref, new File(testCaseDir, "reference.rdf"));            
        }
    }
    
}
