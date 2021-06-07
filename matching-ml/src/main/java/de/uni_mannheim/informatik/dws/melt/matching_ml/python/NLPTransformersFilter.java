package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * This filter extracts the corresponding text for a resource (with the specified and customizable extractor) given all correspondences.
 * The texts of the two resources are fed into the specified transformer model and the prediction is added in form of a confidence to the correspondence.
 */
public class NLPTransformersFilter extends MatcherYAAAJena implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NLPTransformersFilter.class);
    private static final String NEWLINE = System.getProperty("line.separator");
    
    private TextExtractor extractor;
    private String modelName;
    
    private File baseDir;
    private boolean usingTF;
    private String cudaVisibleDevices;
    private File transformersCache;
    private boolean invertConfidences;

    /**
     * Init with all parameters.
     * @param extractor the extractor to select which text for each resource should be used.
     * @param modelName the name of the pretrained model which
     *   is downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>).
     * @param tmpDir the tmp dir to use. Some files are created in this directory.
     * @param usingTF if true, the models are run with tensorflow. If false, pytorch is used.
     * @param cudaVisibleDevices a string wich is set to the environemnt variable CUDA_VISIBLE_DEVICES to select on which GPU the process should run. If null or empty, the default is used (all available GPUs).
     * @param transformersCache the cache of the transformers models when using a pretrained one. If null, the default is used.
     * @param invertConfidences if true, the confidences are inverted in case the model predicts exactly the othere way around.
     */
    public NLPTransformersFilter(TextExtractor extractor, String modelName, File tmpDir, boolean usingTF, String cudaVisibleDevices, File transformersCache, boolean invertConfidences) {
        this.extractor = extractor;
        this.modelName = modelName;
        this.baseDir = tmpDir;
        this.usingTF = usingTF;
        this.cudaVisibleDevices = cudaVisibleDevices;
        this.transformersCache = transformersCache;
        this.invertConfidences = invertConfidences;
    }

    /**
     * Constructor with default values. Uses the systems default tmp dir, pytorch, and all visible GPUs.
     * @param extractor the extractor to select which text for each resource should be used.
     * @param modelName the name of the pretrained model which
     *   is downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>).
     */
    public NLPTransformersFilter(TextExtractor extractor, String modelName) {
        this(extractor, modelName, FileUtil.SYSTEM_TMP_FOLDER, false, "", null, false);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        List<Correspondence> orderedCorrespondences = new ArrayList<>(inputAlignment);
        LOGGER.info("Write text to prediction file");
        File inputFile = writePredictionFile(source, target, orderedCorrespondences);
        try{
            LOGGER.info("Run prediction");
            List<Double> confidenceList = predictConfidences(this.modelName, inputFile, this.usingTF, this.cudaVisibleDevices, this.transformersCache);
            LOGGER.info("Finished prediction");
            if(this.invertConfidences){
                confidenceList = invertList(confidenceList);
            }

            if(orderedCorrespondences.size() != confidenceList.size()){
                LOGGER.warn("Size of correspondences and predictions do not have the same size. Return input alignment.");
                return inputAlignment;
            }
            for(int i=0; i < orderedCorrespondences.size(); i++){
                orderedCorrespondences.get(i).addAdditionalConfidence(this.getClass(), confidenceList.get(i));
            }
        }finally{
            inputFile.delete();
        }
        return inputAlignment;
    }
    

    /**
     * Creates a file which contains two columns in a csv format.
     * The first column contains the text from the source entities and the second column contains the text from the target entities.
     * The returned file is not automatically removed. This has to be done outside.
     * @param source
     * @param target
     * @param inputAlignment
     * @return the csv file or null in case of an error
     */
    public File createPredictionFile(OntModel source, OntModel target, Alignment inputAlignment) {
        try {
            return writePredictionFile(source, target, new ArrayList<>(inputAlignment));
        } catch (IOException ex) {
            LOGGER.warn("Could not create prediction file.", ex);
            return null;
        }
    }
    
    private File writePredictionFile(OntModel source, OntModel target, List<Correspondence> orderedCorrespondences) throws IOException{
        File inputFile = FileUtil.createFileWithRandomNumber(this.baseDir, "alignment_transformers_predict", ".txt");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile), "UTF-8"))){
            for(Correspondence c : orderedCorrespondences){
                String left = getTextFromResource(source.getResource(c.getEntityOne()));
                String right = getTextFromResource(target.getResource(c.getEntityTwo()));
                if(StringUtils.isBlank(left) || StringUtils.isBlank(right)){
                    //setting to 0 if not extistent
                    c.addAdditionalConfidence(this.getClass(), 0.0);
                    continue;
                }
                writer.write(StringEscapeUtils.escapeCsv(left) + "," + StringEscapeUtils.escapeCsv(right) + NEWLINE);
            }
        }
        return inputFile;
    }
    
    private String getTextFromResource(Resource r){
        StringBuilder sb = new StringBuilder();
        for(String text : this.extractor.extract(r)){
            sb.append(text.trim()).append(" ");
        }
        return sb.toString().trim();
    }
        
    private List<Double> invertList(List<Double> list){
        List<Double> newList = new ArrayList<>(list.size());
        for(Double d : list)
            newList.add(1.0-d);
        return newList;
    }

    
    /**
     * Run huggingface transformers library.
     * @param predictionFilePath path to csv file with two columns (text left and text right).
     * @throws Exception in case something goes wrong.
     * @return a list of confidences
     */
    public List<Double> predictConfidences(File predictionFilePath) throws Exception{
        return PythonServer.getInstance().transformersPrediction(this.modelName, predictionFilePath, this.usingTF, this.cudaVisibleDevices, this.transformersCache);
    }

    /**
     * Run huggingface transformers library.
     * @param modelName the name of the pretrained model which
     *   is downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>).
     * @param predictionFilePath path to csv file with two columns (text left and text right).
     * @param usingTF if true, using tensorflow, if false use pytorch
     * @param cudaVisibleDevices the devices visible in cuda (can be null) examples are "0" to show only the first GPU or "1,2" to show only the second and thirs GPU.
     * @param transformersCache the directory where thre transformetrs library stores the models.
     * @throws Exception in case something goes wrong.
     * @return a list of confidences
     */
    public static List<Double> predictConfidences(String modelName, File predictionFilePath, boolean usingTF, String cudaVisibleDevices, File transformersCache) throws Exception{
        return PythonServer.getInstance().transformersPrediction(modelName, predictionFilePath, usingTF, cudaVisibleDevices, transformersCache);
    }
}
