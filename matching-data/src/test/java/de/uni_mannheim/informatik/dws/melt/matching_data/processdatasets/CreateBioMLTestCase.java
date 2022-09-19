package de.uni_mannheim.informatik.dws.melt.matching_data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shertlin
 */
public class CreateBioMLTestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateBioMLTestCase.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    
    public static void createTestCases(File rootFolder, Map<String, String> abbr, File outputFolder, CorrespondenceRelation relation) throws IOException{
        File refFolder = new File(rootFolder, "refs");
        File ontoFolder = new File(rootFolder, "ontos");
        
        for(File testCaseFile : refFolder.listFiles((File f) -> f.isDirectory())){
            String testCaseName = testCaseFile.getName().replace("2", "-");
            String[] ontoInfo = testCaseName.split("-");
            
            File ontoSource = new File(ontoFolder, ontoInfo[0] + ".owl");
            if(ontoSource.exists() == false){
                throw new IllegalArgumentException("Could not find ontology: " + ontoSource.getAbsolutePath());
            }
            File ontoTarget = new File(ontoFolder, ontoInfo[1] + ".owl");
            if(ontoTarget.exists() == false){
                throw new IllegalArgumentException("Could not find ontology: " + ontoTarget.getAbsolutePath());
            }
            
            //semisupervised            
            File semisupervised = new File(testCaseFile, "semi_supervised");
            Alignment inputAlignment = getTrainValAlignment(new File(semisupervised, "train.tsv"), new File(semisupervised, "val.tsv"), abbr, relation);
            Alignment testAlignment = getAlignment(new File(semisupervised, "test.tsv"), abbr, relation);            
            File testCase = new File(new File(outputFolder, "semisupervised"), testCaseName);            
            FileUtils.copyFile(ontoSource, new File(testCase, TestCaseType.SOURCE.toFileName()));
            FileUtils.copyFile(ontoTarget, new File(testCase, TestCaseType.TARGET.toFileName()));
            inputAlignment.serialize(new File(testCase, TestCaseType.INPUT.toFileName()));
            testAlignment.serialize(new File(testCase, TestCaseType.REFERENCE.toFileName()));
            writeParametersContent(new File(testCase, TestCaseType.PARAMETERS.toFileName()));
            
            //unsupervised
            File unsupervised = new File(testCaseFile, "unsupervised");
            inputAlignment = getAlignment(new File(unsupervised, "val.tsv"), abbr, relation);
            testAlignment = getAlignment(new File(unsupervised, "test.tsv"), abbr, relation);            
            testCase = new File(new File(outputFolder, "unsupervised"), testCaseName);            
            FileUtils.copyFile(ontoSource, new File(testCase, TestCaseType.SOURCE.toFileName()));
            FileUtils.copyFile(ontoTarget, new File(testCase, TestCaseType.TARGET.toFileName()));
            inputAlignment.serialize(new File(testCase, TestCaseType.INPUT.toFileName()));
            testAlignment.serialize(new File(testCase, TestCaseType.REFERENCE.toFileName()));
            writeParametersContent(new File(testCase, TestCaseType.PARAMETERS.toFileName()));
            
            //full
            //testAlignment = getAlignment(new File(testCaseFile, "full.tsv"), abbr, relation);            
            //testCase = new File(new File(outputFolder, "full"), testCaseName);            
            //FileUtils.copyFile(ontoSource, new File(testCase, TestCaseType.SOURCE.toFileName()));
            //FileUtils.copyFile(ontoTarget, new File(testCase, TestCaseType.TARGET.toFileName()));
            //testAlignment.serialize(new File(testCase, TestCaseType.REFERENCE.toFileName()));
            //writeParametersContent(new File(testCase, TestCaseType.PARAMETERS.toFileName()));
        }
    }
    
    private static void writeParametersContent(File f) throws IOException{
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParameterConfigKeys.MATCHING_CLASSES, true);
        parameters.put(ParameterConfigKeys.MATCHING_DATA_PROPERTIES, true);
        parameters.put(ParameterConfigKeys.MATCHING_OBJECT_PROPERTIES, true);
        parameters.put(ParameterConfigKeys.MATCHING_RDF_PROPERTIES, true);
        parameters.put(ParameterConfigKeys.MATCHING_INSTANCES, true);
        
        JSON_MAPPER.writeValue(f, parameters);        
    }
    
    
    
    private static Alignment getTrainValAlignment(File train, File val, Map<String, String> abbr, CorrespondenceRelation relation) throws IOException{
        Alignment trainAndVal = new Alignment();
        for(Correspondence c : getAlignment(train, abbr, relation)){
            c.addExtensionValue(DefaultExtensions.MeltExtensions.ML_SPLIT.toString(), "train");
            trainAndVal.add(c);
        }
        
        for(Correspondence c : getAlignment(val, abbr, relation)){
            c.addExtensionValue(DefaultExtensions.MeltExtensions.ML_SPLIT.toString(), "val");
            trainAndVal.add(c);
        }
        return trainAndVal;        
    }
    
    
    private static Alignment getAlignment(File file, Map<String, String> abbr, CorrespondenceRelation relation) throws IOException{
        Alignment a = new Alignment();
        try(CSVParser csvParser = CSVFormat.TDF.withFirstRecordAsHeader().parse(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))){
            for (CSVRecord record : csvParser) {
                Correspondence correspondence = new Correspondence(
                        getFullEntity(record.get("SrcEntity"), abbr), 
                        getFullEntity(record.get("TgtEntity"), abbr),
                        Double.parseDouble(record.get("Score").trim()),
                        relation
                );
                a.add(correspondence);
            }
        }
        return a;
    }
    
    private static String getFullEntity(String text, Map<String, String> abbreviations){
        String[] splitted = text.trim().split(":");
        return abbreviations.getOrDefault(splitted[0], splitted[0]) + splitted[1];
    }
    
    
    private static Map<String, String> getAbbreviations(File f){
        
        try { 
            HashMap<String,String> map = JSON_MAPPER.readValue(f, new TypeReference<HashMap<String,String>>() {});
            
            Map<String, String> finalMap = new HashMap<>();
            for(Entry<String, String> entry : map.entrySet()){
                String shortAbbr = entry.getValue();
                if(shortAbbr.endsWith(":"))
                    shortAbbr = shortAbbr.substring(0, shortAbbr.length()-1);
                finalMap.put(shortAbbr, entry.getKey());
            }
            return finalMap;            
        } catch (IOException ex) {
            LOGGER.error("Could not read abbreviations file", ex);
            return new HashMap<>();
        }
    }
    
    public static void main(String[] args) throws IOException{
        Map<String, String> abbr = getAbbreviations(new File("C:\\Users\\shertlin\\Desktop\\uri_abbreviations.json"));
        
        
        //IMPORTANT:
        //rename folders: 
        //UMLS\equiv_match\refs\snomed2fma.body  -> snomed.body2fma.body
        //UMLS\equiv_match\refs\snomed2ncit.neoplas  -> snomed.neoplas2ncit.neoplas
        //UMLS\equiv_match\refs\snomed2ncit.pharm  -> snomed.pharm2ncit.pharm
        
        //Mondo\subs_match\ontos\doid.subs.owl -> doid.owl
        //Mondo\subs_match\ontos\ordo.subs.owl -> ordo.owl
        
        //UMLS\subs_match\ontos\fma.body.subs.owl -> fma.body.owl
        //UMLS\subs_match\ontos\ncit.neoplas.subs.owl -> ncit.neoplas.owl
        //UMLS\subs_match\ontos\ncit.pharm.subs.owl -> ncit.pharm.owl
        
        //UMLS\subs_match\refs\snomed2fma.body  -> snomed.body2fma.body
        //UMLS\subs_match\refs\snomed2ncit.neoplas  -> snomed.neoplas2ncit.neoplas
        //UMLS\subs_match\refs\snomed2ncit.pharm  -> snomed.pharm2ncit.pharm
        
        createTestCases(new File("C:\\Users\\shertlin\\Desktop\\UMLS\\UMLS\\equiv_match"),
                abbr,
                new File("output"),
                CorrespondenceRelation.EQUIVALENCE);
        
        createTestCases(new File("C:\\Users\\shertlin\\Desktop\\Mondo\\Mondo\\equiv_match"),
                abbr,
                new File("output"),
                CorrespondenceRelation.EQUIVALENCE);
        
        //subsumption
        
        createTestCases(new File("C:\\Users\\shertlin\\Desktop\\Mondo\\Mondo\\subs_match"),
                abbr,
                new File("output_subsumption"),
                CorrespondenceRelation.SUBSUME);
        
        createTestCases(new File("C:\\Users\\shertlin\\Desktop\\UMLS\\UMLS\\subs_match"),
                abbr,
                new File("output_subsumption"),
                CorrespondenceRelation.SUBSUME);
        
    }
    
}
