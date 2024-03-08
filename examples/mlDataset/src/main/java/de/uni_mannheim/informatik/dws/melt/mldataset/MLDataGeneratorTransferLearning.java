package de.uni_mannheim.informatik.dws.melt.mldataset;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCaseType;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MLDataGeneratorTransferLearning {
    private static final Logger LOGGER = LoggerFactory.getLogger(MLDataGeneratorTransferLearning.class);
    
    private TestCase testCaseForTraining;
    private TestCase testCaseForTest;
    
    
    public MLDataGeneratorTransferLearning(TestCase testCaseForTraining, TestCase testCaseForTest){
        this.testCaseForTraining = testCaseForTraining;
        this.testCaseForTest = testCaseForTest;
    }
    
    public void saveToFolder(File folder) throws IOException{
        writeCombinedModel(
            this.testCaseForTraining.getSourceOntology(OntModel.class),
            this.testCaseForTest.getSourceOntology(OntModel.class),
            folder, 
            TestCaseType.SOURCE
        );
            
        writeCombinedModel(
            this.testCaseForTraining.getTargetOntology(OntModel.class),
            this.testCaseForTest.getTargetOntology(OntModel.class),
            folder, 
            TestCaseType.TARGET
        );
        
        save(this.testCaseForTraining.getReference().toURL(), folder, TestCaseType.INPUT);
        save(this.testCaseForTraining.getReference().toURL(), folder, TestCaseType.EVALUATIONEXCLUSION);
        save(this.testCaseForTest.getReference().toURL(), folder, TestCaseType.REFERENCE);
        
        if(this.testCaseForTest.getParameters() != null){
            save(this.testCaseForTest.getParameters().toURL(), folder, TestCaseType.PARAMETERS);
        }
    }
    
    private void writeCombinedModel(Model one, Model two, File folder, TestCaseType type) throws IOException{
        final Model union = ModelFactory.createDefaultModel();
        union.add(one);
        union.add(two);
        
        File file = Paths.get(folder.getAbsolutePath(), this.testCaseForTest.getName(), type.toFileName()).toFile();
        file.getParentFile().mkdirs();
        try(FileOutputStream fout = new FileOutputStream(file)){
            RDFDataMgr.write(fout, union, Lang.RDFXML);
        }
    }
    
    private void save(URL url, File folder, TestCaseType type) throws IOException{
        FileUtils.copyURLToFile(url, Paths.get(folder.getAbsolutePath(), this.testCaseForTest.getName(), type.toFileName()).toFile());
    }
}