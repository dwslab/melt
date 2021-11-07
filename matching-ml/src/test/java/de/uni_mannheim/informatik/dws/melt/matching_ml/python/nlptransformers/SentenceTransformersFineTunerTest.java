package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import static de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcherTest.correlation;
import de.uni_mannheim.informatik.dws.melt.matching_ml.util.TrainTestSplitAlignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentenceTransformersFineTunerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceTransformersFineTunerTest.class);
    
    //@Test
    public void testValidationFile() throws Exception {
        
        TestCaseOntModel testcase = getStsTestCase();
        
        Alignment referenceSample = testcase.reference.sampleByFraction(0.2, 1234);
        
        TrainTestSplitAlignment split = new TrainTestSplitAlignment(referenceSample, 0.2, 1324); // always same split
        Alignment train = split.getTrain();
        Alignment validation = split.getTest();
        
        train = makeNegatives(train, 2, 1234);
        validation = makeNegatives(validation, 2, 1234);
        
        File model = new File("./sentenceTransformersModel");
        File tmp = new File("./tmp");
        tmp.mkdirs();
        SentenceTransformersFineTuner fineTuner = new SentenceTransformersFineTuner(new LabelExtractor(), "sshleifer/tiny-distilroberta-base", model, tmp);
        
        File trainingFile = fineTuner.createTrainingFile(testcase.source, testcase.target, train);
        File validationFile = fineTuner.createTrainingFile(testcase.source, testcase.target, validation);
        
        fineTuner.setNumberOfEpochs(1);
        
        float score = fineTuner.finetuneModel(trainingFile, validationFile);
        
        //LOGGER.info("Score: {}", score);
        
        double notFineTuned = runMatching(testcase, "sshleifer/tiny-distilroberta-base");
        double finetinued = runMatching(testcase, model.getAbsolutePath());
        
        assertTrue(finetinued > notFineTuned, "Fine tuned is not better than not finetuned.");
        
        FileUtils.deleteDirectory(model);
        FileUtils.deleteDirectory(tmp);
    }
    
    private double runMatching(TestCaseOntModel testcase, String model) throws Exception{
        SentenceTransformersMatcher matcher = new SentenceTransformersMatcher(new LabelExtractor(), model);
        matcher.setTopK(10);
        matcher.setBothDirections(true);
        matcher.setResourcesExtractor(Arrays.asList((ontmodel, parameters) -> ontmodel.listIndividuals()));
        //matcher.setTmpDir(new File("./mytmp"));
        Alignment systemAlignment = matcher.match(testcase.source, testcase.target, new Alignment(), new Properties());
        double corr = correlation(testcase.reference, systemAlignment);
        return corr;
    }
    
   
    private static Alignment makeNegatives(Alignment referenceAlignment, int amountOfNegatives, int seed){
        Random rnd = new Random(seed);
        Alignment alignmentWithNegatives = new Alignment(referenceAlignment);
        for(Correspondence c : referenceAlignment){
            Set<String> sourceSet = referenceAlignment.getDistinctSourcesAsSet();
            sourceSet.remove(c.getEntityOne());
            Set<String> targetSet = referenceAlignment.getDistinctTargetsAsSet();
            targetSet.remove(c.getEntityTwo());
            
            List<String> sources = new ArrayList<>(sourceSet);
            List<String> targets = new ArrayList<>(targetSet);
            
            for(int i = 0; i < amountOfNegatives; i++){
                int randomIndex = rnd.nextInt(targets.size());
                alignmentWithNegatives.add(c.getEntityOne(), targets.get(randomIndex), CorrespondenceRelation.INCOMPAT);
                targets.remove(randomIndex);
            }
            for(int i = 0; i < amountOfNegatives; i++){
                int randomIndex = rnd.nextInt(sources.size());
                alignmentWithNegatives.add(sources.get(randomIndex), c.getEntityTwo(), CorrespondenceRelation.INCOMPAT);
                sources.remove(randomIndex);
            }            
        }
        return alignmentWithNegatives;
    }
    
    
    class TestCaseOntModel{
        public OntModel source;
        public OntModel target;
        public Alignment reference;        
    }
    
    public TestCaseOntModel getStsTestCase() throws IOException{
        File stsTestFile = new File(getClass().getClassLoader().getResource("sts-test.csv").getFile());
        
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntModel target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Alignment referenceAlignment = new Alignment();
        try(Reader in = new InputStreamReader(new FileInputStream(stsTestFile), "UTF-8")){
            int i = 0;
            for (CSVRecord row : CSVFormat.TDF.withQuote('\\').parse(in)) {
                Double confidence = Double.parseDouble(row.get(4));
                String sentenceSource = row.get(5);
                String sentenceTarget = row.get(6);
                
                String urlSource = "http://source.com/" + i;
                String urlTarget = "http://target.com/" + i;
                i++;
                source.createIndividual(urlSource, OWL.Thing).addProperty(RDFS.label, sentenceSource);
                target.createIndividual(urlTarget, OWL.Thing).addProperty(RDFS.label, sentenceTarget);
                
                referenceAlignment.add(urlSource, urlTarget, confidence);
            }
        }
        TestCaseOntModel testcase = new TestCaseOntModel();
        testcase.source = source;
        testcase.target = target;
        testcase.reference = referenceAlignment;
        return testcase;
    }
}
