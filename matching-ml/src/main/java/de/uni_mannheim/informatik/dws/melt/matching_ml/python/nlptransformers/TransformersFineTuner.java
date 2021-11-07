package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.FileUtil;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.PythonServer;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to fine-tune a transformer model based on a generated dataset.
 * In every call to the match method, the training data will be generated and appended to a temporary file.
 * When you call the {@link TransformersFineTuner#finetuneModel() } method, then a model is fine-tuned and the
 * training file is deleted.
 */
public class TransformersFineTuner extends TransformersBaseFineTuner implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersFineTuner.class);
    
    //memory issue with transformers library
    //https://github.com/huggingface/transformers/issues/6753
    //https://github.com/huggingface/transformers/issues/1742

    /**
     * Run the training of a NLP transformer.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     * @param tmpDir Sets the tmp directory used by the matcher. In this folder the file with all texts from the knowledge graph are stored.
     */
    public TransformersFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation, File tmpDir) {
        super(extractor, initialModelName, resultingModelLocation, tmpDir);
    }
    
    /**
     * Run the training of a NLP transformer.
     * @param extractor used to extract text from a given resource. This is the text which represents a resource.
     * @param initialModelName the initial model name for fine tuning which can be downloaded or a path to a directory containing model weights
     *   (<a href="https://huggingface.co/transformers/main_classes/model.html#transformers.PreTrainedModel.from_pretrained">
     *   see first parameter pretrained_model_name_or_path of the from_pretrained
     *   function in huggingface library</a>). This value can be also changed by {@link #setModelName(java.lang.String) }.
     * @param resultingModelLocation the final location where the fine-tuned model should be stored.
     */
    public TransformersFineTuner(TextExtractor extractor, String initialModelName, File resultingModelLocation) {
        this(extractor, initialModelName, resultingModelLocation, FileUtil.SYSTEM_TMP_FOLDER);
    }    
    
    @Override
    public File finetuneModel(File trainingFile) throws Exception{
        PythonServer.getInstance().transformersFineTuning(this, trainingFile);
        return this.resultingModelLocation;
    }
    
    /**
     * This will add (potencially multiple) training parameters to the current {@link TransformersBase#trainingArguments trainingArguments}
     * to make the training faster. Thus do not change the trainingArguments object afterwards 
     * (with {@link TransformersBase#setTrainingArguments(de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersTrainerArguments) setTrainingArguments} ).
     * What you can do is to add more training arguments via getTrainingArguments.addParameter (as long as you do not modify any parameters added by this method).
     * The following parameters are set:
     * <ul>
     *  <li>fp16</li>   
     * </ul>
     * @see <a href="https://huggingface.co/transformers/performance.html">Transformers performance page on huggingface</a>
     */
    public void addTrainingParameterToMakeTrainingFaster(){
        //https://huggingface.co/transformers/performance.html
        //https://huggingface.co/transformers/performance.html
        this.trainingArguments.addParameter("fp16", true);
    }
}
