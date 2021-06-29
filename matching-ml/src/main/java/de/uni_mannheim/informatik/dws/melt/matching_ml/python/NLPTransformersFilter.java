package de.uni_mannheim.informatik.dws.melt.matching_ml.python;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
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
import java.io.IOException;

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
    private boolean changeClass;
    private TransformerConfiguration config;

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
     * @param cudaVisibleDevices a string which is set to the environment variable CUDA_VISIBLE_DEVICES to select on
     *                           which GPU the process should run. If null or empty, the default is used (all available GPUs).
     * @param transformersCache the cache of the transformers models when using a pretrained one. If null, the default is used.
     * @param changeClass if false the confidences of class 1 are used, if true the confidences of class 2 are used.
     * @param config configuration of the huggingface trainer via the training arguments. Any of the training arguments can be used. See {@link TransformerConfiguration}
     */
    public NLPTransformersFilter(TextExtractor extractor, String modelName, File tmpDir, boolean usingTF, String cudaVisibleDevices, File transformersCache, boolean changeClass, TransformerConfiguration config) {
        this.extractor = extractor;
        this.modelName = modelName;
        this.baseDir = tmpDir;
        this.usingTF = usingTF;
        this.cudaVisibleDevices = cudaVisibleDevices;
        this.transformersCache = transformersCache;
        this.changeClass = changeClass;
        this.config = config;
    }
    
    /**
     * Constructor with default values. Uses the systems default tmp dir, pytorch.
     * @param extractor the extractor to select which text for each resource should be used.
     * @param modelName the name of the pretrained model which
     *   is downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>).
     * @param cudaVisibleDevices a string wich is set to the environment variable CUDA_VISIBLE_DEVICES to select on
     *                           which GPU the process should run. If null or empty, the default is used (all available GPUs).
     */
    public NLPTransformersFilter(TextExtractor extractor, String modelName, String cudaVisibleDevices) {
        this(extractor, modelName, FileUtil.SYSTEM_TMP_FOLDER, false, cudaVisibleDevices, null, false, new TransformerConfiguration());
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
        this(extractor, modelName, "");
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception{
        
        File inputFile = FileUtil.createFileWithRandomNumber(this.baseDir, "alignment_transformers_predict", ".txt");
        List<Correspondence> orderedCorrespondences = new ArrayList<>();
        LOGGER.info("Write text to prediction file {}", inputFile);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFile), StandardCharsets.UTF_8))){
            for(Correspondence c : inputAlignment){
                String left = getTextFromResource(source.getResource(c.getEntityOne()));
                String right = getTextFromResource(target.getResource(c.getEntityTwo()));
                if(StringUtils.isBlank(left) || StringUtils.isBlank(right)){
                    //setting to 0 if not existing
                    c.addAdditionalConfidence(this.getClass(), 0.0);
                    continue;
                }
                orderedCorrespondences.add(c);
                writer.write(StringEscapeUtils.escapeCsv(left) + "," + StringEscapeUtils.escapeCsv(right) + NEWLINE);
            }
        } catch (IOException ex) {
            LOGGER.warn("Could not write text to prediction file. Return unodified input alignment.", ex);
            return inputAlignment;
        }

        try{
            LOGGER.info("Run prediction for {} examples ({} correspondences do not have enough text to be processed).", orderedCorrespondences.size(), inputAlignment.size() - orderedCorrespondences.size());
            List<Double> confidenceList = predictConfidences(inputFile);
            LOGGER.info("Finished prediction");

            if(orderedCorrespondences.size() != confidenceList.size()){
                LOGGER.warn("Size of correspondences and predictions do not have the same size. Return input alignment.");
                return inputAlignment;
            }
            for(int i=0; i < orderedCorrespondences.size(); i++){
                orderedCorrespondences.get(i).addAdditionalConfidence(this.getClass(), confidenceList.get(i));
            }
        } finally {
            inputFile.delete();
        }
        return inputAlignment;
    }
    
    /**
     * Create the prediction file which is a CSV file with two columns.
     * The first column is the text from the left resource and the second column is the text from the right resource.
     * @param source The source model
     * @param target The target model
     * @param trainingAlignment the alignment to process. All correspondences are used.
     * @return the prediction file (CSV formatted)
     * @throws IOException in case the writing fails.
     */
    public File createPredictionFile(OntModel source, OntModel target, Alignment trainingAlignment) throws IOException {
        File file = FileUtil.createFileWithRandomNumber(this.baseDir, "alignment_transformers_predict", ".txt");
        createPredictionFile(source, target, trainingAlignment, file, false);
        return file;
    }

    /**
     * Create the prediction file which is a CSV file with two columns.
     * The first column is the text from the left resource and the second column is the text from the right resource.
     * @param source The source model
     * @param target The target model
     * @param trainingAlignment the alignment to process. All correspondences are used.
     * @param outputFile the csv file to which the output should be written to.
     * @param append if true, then the training alignment is append to the given file.
     * @throws IOException in case the writing fails.
     */
    public void createPredictionFile(OntModel source, OntModel target, Alignment trainingAlignment, File outputFile, boolean append) throws IOException {
        LOGGER.info("Write text to prediction file {}", outputFile);
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile, append), StandardCharsets.UTF_8))){
            for(Correspondence c : trainingAlignment){
                String left = getTextFromResource(source.getResource(c.getEntityOne()));
                String right = getTextFromResource(target.getResource(c.getEntityTwo()));
                if(StringUtils.isBlank(left) || StringUtils.isBlank(right)){
                    continue;
                }
                writer.write(StringEscapeUtils.escapeCsv(left) + "," + StringEscapeUtils.escapeCsv(right) + NEWLINE);
            }
        }
    }
    
    private String getTextFromResource(Resource r){
        StringBuilder sb = new StringBuilder();
        for(String text : this.extractor.extract(r)){
            sb.append(text.trim()).append(" ");
        }
        return sb.toString().trim();
    }
    
    /**
     * Run huggingface transformers library.
     * @param predictionFilePath path to csv file with two columns (text left and text right).
     * @throws Exception in case something goes wrong.
     * @return a list of confidences
     */
    public List<Double> predictConfidences(File predictionFilePath) throws Exception{
        return PythonServer.getInstance().transformersPrediction(this.modelName, predictionFilePath, this.usingTF, this.cudaVisibleDevices, this.transformersCache, this.changeClass, this.config);
    }

    //setter and getter

    public TextExtractor getExtractor() {
        return extractor;
    }

    public void setExtractor(TextExtractor extractor) {
        this.extractor = extractor;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public boolean isUsingTF() {
        return usingTF;
    }

    public void setUsingTF(boolean usingTF) {
        this.usingTF = usingTF;
    }

    public String getCudaVisibleDevices() {
        return cudaVisibleDevices;
    }

    public void setCudaVisibleDevices(String cudaVisibleDevices) {
        this.cudaVisibleDevices = cudaVisibleDevices;
    }

    public File getTransformersCache() {
        return transformersCache;
    }

    public void setTransformersCache(File transformersCache) {
        this.transformersCache = transformersCache;
    }

    public boolean isChangeClass() {
        return changeClass;
    }

    public void setChangeClass(boolean changeClass) {
        this.changeClass = changeClass;
    }
}
