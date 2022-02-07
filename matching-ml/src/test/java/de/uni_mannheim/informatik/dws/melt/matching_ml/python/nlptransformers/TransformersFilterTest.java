package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformersFilterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersFilterTest.class);
    
    @Test
    public void testFilterWithBert() throws Exception {
        //https://huggingface.co/sgugger/finetuned-bert-mrpc
        //https://huggingface.co/datasets/glue
        File mrpcTestFile = new File(getClass().getClassLoader().getResource("mrpc-test-subset.csv").getFile());
        TransformersFilter zeroShot = new TransformersFilter((TextExtractor)null, "bert-base-cased-finetuned-mrpc");
        //zeroShot.setTransformersCache(new File("N:\\tmp\\transformers_cache"));
        
        List<Double> confidences = zeroShot.predictConfidences(mrpcTestFile);
        List<Integer> gold = getGoldStandard(mrpcTestFile);
        double acc = accuracy(gold, confidences);
        assertTrue(acc > 0.8, "accuracy should be higher than 0.8 but was " + acc);
        
        zeroShot.setChangeClass(true);
        confidences = zeroShot.predictConfidences(mrpcTestFile);
        acc = accuracy(gold, confidences);
        assertTrue(acc < 0.2, "accuracy should be smaller than 0.2 but was " + acc);
    }
    
    
    @Test
    public void testWthModels() throws Exception {
        File mrpcTestFile = new File(getClass().getClassLoader().getResource("mrpc-test-subset.csv").getFile());
        String namespace = "http://example.com/";
        OntModel source = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        OntModel target = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        Map<Correspondence, Integer> goldMap = new HashMap<>();
        Alignment alignment = new Alignment();
        try(Reader in = new InputStreamReader(new FileInputStream(mrpcTestFile), "UTF-8")){
            int i = 0;
            Random rnd = new Random();
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                String sourceURI = namespace + "source/" +  i + "/" + rnd.nextInt();
                String targetURI = namespace + "target/" +  i + "/" + rnd.nextInt();
                source.createResource(sourceURI).addProperty(RDFS.comment, row.get(0)).addProperty(RDFS.comment, "a");//.addProperty(RDFS.comment, Integer.toString(rnd.nextInt()));
                target.createResource(targetURI).addProperty(RDFS.comment, row.get(1)).addProperty(RDFS.comment, "Lorem ipsum");//.addProperty(RDFS.comment, Integer.toString(rnd.nextInt()));
                
                Correspondence c = new Correspondence(sourceURI, targetURI);
                alignment.add(c);
                goldMap.put(c, Integer.parseInt(row.get(2)));
                i++;
            }
        }
        
        TransformersFilter zeroShot = new TransformersFilter(new CommentExtractor(), "bert-base-cased-finetuned-mrpc");
        //zeroShot.setTransformersCache(new File("N:\\tmp\\transformers_cache"));
        Alignment systemAlignment = zeroShot.match(source, target, alignment, new Properties());
        
        List<Double> confidences = new ArrayList<>();
        List<Integer> gold = new ArrayList<>();
        for(Correspondence c : systemAlignment){
            confidences.add(c.getAdditionalConfidence(TransformersFilter.class));
            Integer goldValue = goldMap.get(c);
            assertNotNull(goldValue);
            gold.add(goldValue);
        }
        double acc = accuracy(gold, confidences);
        assertTrue(acc > 0.8, "accuracy should be higher than 0.8 but was " + acc);
        
        //second time with setMultipleTextsToMultipleExamples set to true
        zeroShot.setMultipleTextsToMultipleExamples(true);
        systemAlignment = zeroShot.match(source, target, alignment, new Properties());
        
        confidences = new ArrayList<>();
        gold = new ArrayList<>();
        for(Correspondence c : systemAlignment){
            confidences.add(c.getAdditionalConfidence(TransformersFilter.class));
            Integer goldValue = goldMap.get(c);
            assertNotNull(goldValue);
            gold.add(goldValue);
        }
        acc = accuracy(gold, confidences);
        assertTrue(acc > 0.8, "accuracy should be higher than 0.8 but was " + acc);
    }
    
    private static double accuracy(List<Integer> gold, List<Double> confidences){
        int tp = 0;
        for(int i = 0; i < confidences.size(); i++){
            if((gold.get(i) == 0 && confidences.get(i) <= 0.5d) ||
                gold.get(i) == 1 && confidences.get(i) >= 0.5d){
                tp++;
            }
        }
        double acc = (double)tp / (double)confidences.size();
        return acc;
    }
    
    private static List<Integer> getGoldStandard(File file) throws IOException{
        List<Integer> list = new ArrayList<>();
        try(Reader in = new InputStreamReader(new FileInputStream(file), "UTF-8")){
            for (CSVRecord row : CSVFormat.DEFAULT.parse(in)) {
                list.add(Integer.parseInt(row.get(2)));
            }
        }
        return list;
    }
}

class CommentExtractor implements TextExtractor {

    @Override
    public Set<String> extract(Resource r) {
        Set<String> values = new HashSet<>();
        StmtIterator i = r.listProperties(RDFS.comment);
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