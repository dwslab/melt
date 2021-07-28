package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
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
        TransformersFilter zeroShot = new TransformersFilter(null, "bert-base-cased-finetuned-mrpc");

        List<Double> confidences = zeroShot.predictConfidences(mrpcTestFile);
        List<Integer> gold = getGoldStandard(mrpcTestFile);
        double acc = accuracy(gold, confidences);
        LOGGER.info("accuracy in testFilterWithBert (should be higher than 0.8): " + Double.toString(acc));
        assertTrue(acc > 0.8);
        
        zeroShot.setChangeClass(true);
        confidences = zeroShot.predictConfidences(mrpcTestFile);
        acc = accuracy(gold, confidences);      
        LOGGER.info("accuracy in testFilterWithBert (should be lower than 0.2: " + Double.toString(acc));
        assertTrue(acc < 0.2);
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
