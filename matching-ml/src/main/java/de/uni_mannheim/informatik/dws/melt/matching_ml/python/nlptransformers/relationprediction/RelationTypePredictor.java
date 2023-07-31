package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.relationprediction;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.BatchSizeOptimization;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher predicts the relation type given a transformer model.
 * This component do not create new correspondences but refine the relation of given class correspondences.
 */
public class RelationTypePredictor extends TransformersFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(RelationTypePredictor.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    protected boolean allowFiltering;
    
    
    public RelationTypePredictor(TextExtractor extractor, String modelName) {
        super(extractor, modelName);
        this.allowFiltering = true;
    }
    
    public RelationTypePredictor(TextExtractorMap extractor, String modelName) {
        super(extractor, modelName);
        this.allowFiltering = true;
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        Alignment newAlignment = new Alignment(inputAlignment, false);
        
        File inputFile = FileUtil.createFileWithRandomNumber("alignment_transformers_predict", ".txt");
        Map<Correspondence, List<Integer>> map;
        try{
            map = createPredictionFile(source, target, inputAlignment, inputFile, false);
        }catch (IOException ex) {
            LOGGER.warn("Could not write text to prediction file. Return unmodified input alignment.", ex);
            inputFile.delete();
            return inputAlignment;
        }
        try {
            if(map.isEmpty()){
                LOGGER.warn("No correspondences have enough text to be processed (the input alignment has {} " +
                        "correspondences) - the input alignment is returned unchanged.", inputAlignment.size());
                return inputAlignment;
            }
            
            LOGGER.info("Run prediction");
            List<List<Double>> predictions = predictConfidencesMultiClass(inputFile);
            LOGGER.info("Finished prediction");
            
            Map<Correspondence, List<RelationTypePredictionResult>> results = getSortedResults(predictions, map);
            for(Entry<Correspondence, List<RelationTypePredictionResult>> correspondenceToResults : results.entrySet()){
                
                Correspondence newCorrespondence = new Correspondence(correspondenceToResults.getKey());
                List<RelationTypePredictionResult> resultsPerCorrespondence = correspondenceToResults.getValue();
                
                RelationTypePredictionResult r = getFinalBestPrediction(resultsPerCorrespondence, 6);
                if(r.getClazz() != 6){
                    switch(r.getClazz()){
                        case 0:{
                            newCorrespondence.setRelation(CorrespondenceRelation.EQUIVALENCE);
                            break;
                        }
                        case 1:{
                            newCorrespondence.setRelation(CorrespondenceRelation.SUBSUMED);
                            break;
                        }
                        case 2:{
                            newCorrespondence.setRelation(CorrespondenceRelation.SUBSUME);
                            break;
                        }
                        case 3:{
                            newCorrespondence.setRelation(CorrespondenceRelation.PART_OF);
                            break;
                        }
                        case 4:{
                            newCorrespondence.setRelation(CorrespondenceRelation.HAS_A);
                            break;
                        }
                        case 5:{
                            newCorrespondence.setRelation(CorrespondenceRelation.RELATED);
                            break;
                        }
                    }
                    newCorrespondence.addAdditionalConfidence(this.getClass(), r.getConfidence());
                    newAlignment.add(newCorrespondence);
                }
            }
        } finally {
            inputFile.delete();
        }
        return newAlignment;
    }
    
    @Override
    public Map<Correspondence, List<Integer>> createPredictionFile(OntModel source, OntModel target, Alignment predictionAlignment, File outputFile, boolean append) throws IOException {
        return createPredictionFile(source, target, predictionAlignment, outputFile, append, false);
    }
        
    public Map<Correspondence, List<Integer>> createPredictionFile(OntModel source, OntModel target, Alignment predictionAlignment, File outputFile, boolean append, boolean requiresSwitchExmaples) throws IOException {
        Map<Correspondence, List<Integer>> map = new HashMap<>();
        int i = 0;
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, append), StandardCharsets.UTF_8))){
            Map<Resource,Map<String, Set<String>>> cache = new HashMap<>();
            for(Correspondence c : predictionAlignment){
                c.addAdditionalConfidence(this.getClass(), 0.0d); // initialize it
                
                Map<String, Set<String>> sourceTexts = getTextualRepresentation(source.getResource(c.getEntityOne()), cache);
                Map<String, Set<String>> targetTexts = getTextualRepresentation(target.getResource(c.getEntityTwo()), cache);

                for(Map.Entry<String, Set<String>> textLeftGroup : sourceTexts.entrySet()){
                    for(String textRight : targetTexts.get(textLeftGroup.getKey())){
                        if(StringUtils.isBlank(textRight)){
                            continue;
                        }
                        for(String textLeft : textLeftGroup.getValue()){
                            if(StringUtils.isBlank(textLeft)){
                                continue;
                            }
                            writer.write(StringEscapeUtils.escapeCsv(textLeft) + "," + StringEscapeUtils.escapeCsv(textRight) + NEWLINE);
                            map.computeIfAbsent(c, __-> new ArrayList<>()).add(i);
                            i++;
                            
                            if(requiresSwitchExmaples){
                                writer.write(StringEscapeUtils.escapeCsv(textRight) + "," + StringEscapeUtils.escapeCsv(textLeft) + NEWLINE);
                                map.computeIfAbsent(c, __-> new ArrayList<>()).add(i);
                                i++;
                            }
                        }
                    }
                }
            }
        }
        LOGGER.info("Wrote {} examples to prediction file {}", i, outputFile);
        return map;
    }
    
    
    @Override
    public void setChangeClass(boolean changeClass) {
        throw new UnsupportedOperationException("RelationTypePredictor do not have a change class method");
    }
    @Override
    public List<Double> predictConfidences(File predictionFilePath) throws Exception{
        throw new UnsupportedOperationException("RelationTypePredictor do not have only one class and thus predictConfidences is not callable.");
    }
    
    public List<List<Double>> predictConfidencesMultiClass(File predictionFilePath) throws Exception{
        if(this.batchSizeOptimization != BatchSizeOptimization.NONE){
            this.trainingArguments.addParameter("per_device_eval_batch_size", getMaximumPerDeviceEvalBatchSize(predictionFilePath));
        }
        return PythonServer.getInstance().transformersMultiClassPrediction(this, predictionFilePath);
    }
    
    
    private Map<Correspondence, List<RelationTypePredictionResult>> getSortedResults(List<List<Double>> predictions, Map<Correspondence, List<Integer>> map){
        Map<Correspondence, List<RelationTypePredictionResult>> resultMap = new HashMap<>();
        for(Entry<Correspondence, List<Integer>> correspondenceToLineNumber : map.entrySet()){
            List<RelationTypePredictionResult> list = new ArrayList<>();
            for(Integer lineNumber : correspondenceToLineNumber.getValue()){
                List<Double> onePrediction = predictions.get(lineNumber);
                if(onePrediction == null){
                    throw new IllegalArgumentException("Could not find a confidence for a given correspondence.");
                }
                for(int i = 0; i < onePrediction.size(); i++){
                    list.add(new RelationTypePredictionResult(onePrediction.get(i), i));
                }
            }
            list.sort((RelationTypePredictionResult o1, RelationTypePredictionResult o2) -> Double.compare(o2.getConfidence(), o1.getConfidence()));
            resultMap.put(correspondenceToLineNumber.getKey(), list);
        }
        return resultMap;
    }
    
    private RelationTypePredictionResult getFinalBestPrediction(List<RelationTypePredictionResult> predictions, int negativeClass){
        if(this.allowFiltering){
            return predictions.get(0);
        }
        for(RelationTypePredictionResult pred : predictions){
            if(pred.getClazz() != negativeClass){
                return pred;
            }
        }
        return predictions.get(0);
    }
    
    /*
    public List<RelationTypePredictionResult> predictConfidencesMultiClassOnlyMaxConfidence(File predictionFilePath) throws Exception{
        List<List<Double>> predictions = predictConfidencesMultiClass(predictionFilePath);
        List<RelationTypePredictionResult> results = new ArrayList<>(predictions.size());
        for(List<Double> onePrediction : predictions){
            double max = 0.0;
            int pos = 0;
            for(int i = 0; i < onePrediction.size(); i++){
                double conf = onePrediction.get(i);
                if(conf > max){
                    max = conf;
                    pos = i;
                }
            }
            results.add(new RelationTypePredictionResult(max, pos));
        }
        return results;
    }
    
    
    public List<List<RelationTypePredictionResult>> predictConfidencesMultiClassSorted(File predictionFilePath) throws Exception{
        List<List<Double>> predictions = predictConfidencesMultiClass(predictionFilePath);
        List<List<RelationTypePredictionResult>> results = new ArrayList<>(predictions.size());
        for(List<Double> onePrediction : predictions){
            List<RelationTypePredictionResult> list = new ArrayList<>(onePrediction.size());
            for(int i = 0; i < onePrediction.size(); i++){
                list.add(new RelationTypePredictionResult(onePrediction.get(i), i));
            }
            list.sort((RelationTypePredictionResult o1, RelationTypePredictionResult o2) -> Double.compare(o1.getConfidence(), o2.getConfidence()));
            results.add(list);
        }
        return results;
    }
*/

    public boolean isAllowFiltering() {
        return allowFiltering;
    }

    public void setAllowFiltering(boolean allowFiltering) {
        this.allowFiltering = allowFiltering;
    }
}
