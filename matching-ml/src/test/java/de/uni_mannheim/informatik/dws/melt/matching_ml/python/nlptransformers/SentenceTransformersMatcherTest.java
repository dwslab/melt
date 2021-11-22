package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SentenceTransformersMatcherTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SentenceTransformersMatcherTest.class);
    
    @Test
    public void matchStsTest() throws Exception {
        File stsTestFile = new File(getClass().getClassLoader().getResource("sts-test.csv").getFile());
        
        //create source and target ontology from stsTestFile as well as reference alignment
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
        
        //run the matcher
        SentenceTransformersMatcher matcher = new SentenceTransformersMatcher(new LabelExtractor(), "paraphrase-MiniLM-L6-v2");
        matcher.setTopK(10);
        matcher.setBothDirections(true);
        matcher.setResourcesExtractor(Arrays.asList((model, parameters) -> model.listIndividuals()));
        //matcher.setTmpDir(new File("./mytmp"));
        Alignment systemAlignment = matcher.match(source, target, new Alignment(), new Properties());
        
        double corr = correlation(referenceAlignment, systemAlignment);
        
        //System.out.println(corr);
        assertTrue(corr > 0.8, "Correlation should be greater than 0.8 but was: " + corr);
        
        //check the topk parameter        
        for(String entityOne : systemAlignment.getDistinctSources()){
            int size = Alignment.makeSet(systemAlignment.getCorrespondencesSourceRelation(entityOne, CorrespondenceRelation.EQUIVALENCE)).size();
            assertTrue(size >= 10, "An entity has not enough corresponding entities. Thus topk is not fullfilled. "
                    + "t should be greater than 10 but was " + size + " for source " + entityOne);
        }
        
        for(String entityTwo : systemAlignment.getDistinctTargets()){
            int size = Alignment.makeSet(systemAlignment.getCorrespondencesTargetRelation(entityTwo, CorrespondenceRelation.EQUIVALENCE)).size();
            assertTrue(size >= 10, "An entity has not enough corresponding entities. Thus topk is not fullfilled. "
                    + "t should be greater than 10 but was " + size + " for target " + entityTwo);
        }
    }
    
    //test can be enabled but take very long
    //@Test
    public void checkTopKPerResource() throws Exception {
        File tmp = new File("./tmp");
        tmp.mkdir();
        FileUtil.setUserTmpFolder(tmp);
        
        File stsTestFile = new File(getClass().getClassLoader().getResource("sts-test.csv").getFile());

        //create source and target ontology from stsTestFile as well as reference alignment
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntModel target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Random random = new Random(1325);
        int i = 0;
        try(Reader in = new InputStreamReader(new FileInputStream(stsTestFile), "UTF-8")){            
            for (CSVRecord row : CSVFormat.TDF.withQuote('\\').parse(in)) {
                Double confidence = Double.parseDouble(row.get(4));
                String sentenceSource = row.get(5);
                String sentenceTarget = row.get(6);
                
                String urlSource = "http://source.com/" + i;
                String urlTarget = "http://target.com/" + i;
                i++;
                source.createIndividual(urlSource, OWL.Thing)
                        .addProperty(RDFS.label, sentenceSource)
                        .addProperty(RDFS.comment, getRandomString(20, random))
                        .addProperty(RDFS.comment, getRandomString(20, random));
                target.createIndividual(urlTarget, OWL.Thing)
                        .addProperty(RDFS.label, sentenceTarget)
                        .addProperty(RDFS.comment, getRandomString(20, random))
                        .addProperty(RDFS.comment, getRandomString(20, random));
            }
        }
        int topk = 10;
        //run the matcher
        SentenceTransformersMatcher matcher = new SentenceTransformersMatcher(new LabelExtractor(), "paraphrase-MiniLM-L6-v2");
        matcher.setResourcesExtractor(Arrays.asList((model, parameters) -> model.listIndividuals()));
        matcher.setTopK(topk);
        matcher.setBothDirections(true);
        matcher.setMultipleTextsToMultipleExamples(true);
        matcher.setTopkPerResource(true);
        
        Alignment systemAlignment = matcher.match(source, target, new Alignment(), new Properties());
        
        assertTrue(systemAlignment.size() >= i * topk && systemAlignment.size() <= (i*2) * topk);
        
        matcher.setTopkPerResource(false);
        systemAlignment = matcher.match(source, target, new Alignment(), new Properties());
        assertTrue(systemAlignment.size() >= i * topk * 3 && systemAlignment.size() <= (i*2) * topk * 3);
    }
    
    
    private static String getRandomString(int length, Random random){
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'

        return random.ints(leftLimit, rightLimit + 1)
          .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
          .limit(length)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();
    }
    
    public static double correlation(Alignment referenceAlignment, Alignment systemAlignment){
        List<Double> expected = new ArrayList<>(referenceAlignment.size());
        List<Double> actual = new ArrayList<>(referenceAlignment.size());
        
        for(Correspondence expectedCorrespondence : referenceAlignment){
            
            Correspondence systemCorrespondence = systemAlignment.getCorrespondence(
                    expectedCorrespondence.getEntityOne(), 
                    expectedCorrespondence.getEntityTwo(), 
                    expectedCorrespondence.getRelation()
            );
            
            expected.add(expectedCorrespondence.getConfidence());
            if(systemCorrespondence == null){
                actual.add(0.0);
                //LOGGER.info("Could not find confidence for correspondence {} ", expectedCorrespondence);
            }else{
                actual.add(systemCorrespondence.getConfidence());
            }
                
        }
        assertEquals(expected.size(), actual.size());
        
        double[] arrayExpected = new double[expected.size()];
        double[] arrayActual = new double[actual.size()];
        
        for(int i = 0; i < expected.size(); i++) arrayExpected[i] = expected.get(i);        
        for(int i = 0; i < actual.size(); i++) arrayActual[i] = actual.get(i);
        
        return new PearsonsCorrelation().correlation(arrayExpected, arrayActual);
    }
}

class LabelExtractor implements TextExtractor{
    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet<>();
        StmtIterator i = r.listProperties(RDFS.label);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                String text = n.asLiteral().getLexicalForm().trim();
                if(!text.isEmpty())
                    values.add(text);
            }
        }
        i = r.listProperties(RDFS.comment);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                String text = n.asLiteral().getLexicalForm().trim();
                if(!text.isEmpty())
                    values.add(text);
            }
        }
        return values;
    }
}
