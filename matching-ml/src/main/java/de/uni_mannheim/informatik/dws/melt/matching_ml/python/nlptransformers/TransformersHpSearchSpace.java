package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the search space for hyper parameters.
 * Each method represents the type of sampling and it need a parameter which appears as a <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training argument</a>.
 * <pre>
 * {@code
 * NLPTransformersHpSearchSpace psace = new NLPTransformersHpSearchSpace()
 *      .
 * System.out.println(s);
 * }
 * </pre>
 */
public class TransformersHpSearchSpace {


    private static final Logger LOGGER = LoggerFactory.getLogger(TransformersHpSearchSpace.class);
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    static{
        JSON_MAPPER.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }
    
    private final Map<String, Object> config;
    
    public TransformersHpSearchSpace(){
        this.config = new HashMap<>();
    }
    
    /**
     * Sample a float value uniformly between lower and upper.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower the lower value of the distribution
     * @param upper the upper value of the distribution
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace uniform(String parameter, float lower, float upper){
        SearchSpace sp = new SearchSpace("uniform");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Sample a quantized float value uniformly between lower and upper.
     * The value will be quantized, i.e. rounded to an integer increment of q. Quantization makes the upper bound inclusive.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower the lower value of the distribution
     * @param upper the upper value of the distribution
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace quniform(String parameter, float lower, float upper, float q){
        SearchSpace sp = new SearchSpace("quniform");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        sp.addParam("q", q);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Sugar for sampling in different orders of magnitude. Base is 10.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 1e-4)
     * @param upper upper boundary of the output interval (e.g. 1e-2)
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace loguniform(String parameter, float lower, float upper){
        return loguniform(parameter, lower, upper, 10.0f);
    }
    
    /**
     * Sugar for sampling in different orders of magnitude.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 1e-4)
     * @param upper upper boundary of the output interval (e.g. 1e-2)
     * @param base Base of the log
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace loguniform(String parameter, float lower, float upper, float base){
        SearchSpace sp = new SearchSpace("loguniform");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        sp.addParam("base", base);
        this.config.put(parameter, sp);
        return this;
    }
    
    
    /**
     * Sugar for sampling in different orders of magnitude.
     * Quantization makes the upper bound inclusive.
     * The base will default to 10.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 1e-4)
     * @param upper upper boundary of the output interval (e.g. 1e-2)
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qloguniform(String parameter, float lower, float upper, float q){
        return qloguniform(parameter, lower, upper, q, 10);
    }
    
    /**
     * Sugar for sampling in different orders of magnitude.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 1e-4)
     * @param upper upper boundary of the output interval (e.g. 1e-2)
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @param base base of the log
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qloguniform(String parameter, float lower, float upper, float q, float base){
        SearchSpace sp = new SearchSpace("qloguniform");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        sp.addParam("q", q);
        sp.addParam("base", base);
        this.config.put(parameter, sp);
        return this;
    }
    
    
    
    /**
     * Sample a float value normally with mean 0 and sd 1.
     * Quantization makes the upper bound inclusive.
     * The base will default to 10.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace randn(String parameter){
        return randn(parameter, 0.0f, 1.0f);
    }
        
    /**
     * Sample a float value normally with mean and sd.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param mean mean of the normal distribution
     * @param sd standard deviation of the normal distribution
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace randn(String parameter, float mean, float sd){
        SearchSpace sp = new SearchSpace("randn");
        sp.addParam("mean", mean);
        sp.addParam("sd", sd);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Sample a float value normally with mean and sd.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param mean mean of the normal distribution
     * @param sd standard deviation of the normal distribution
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qrandn(String parameter, float mean, float sd, float q){
        SearchSpace sp = new SearchSpace("qrandn");
        sp.addParam("mean", mean);
        sp.addParam("sd", sd);
        sp.addParam("q", q);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Sample an integer value uniformly between lower and upper. lower is inclusive, upper is exclusive.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace randint(String parameter, int lower, int upper){
        SearchSpace sp = new SearchSpace("randint");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Sample an integer value uniformly between lower and upper. lower is inclusive, upper is exclusive. Q set to 1.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qrandint(String parameter, int lower, int upper){
        return qrandint(parameter, lower, upper, 1);
    }
    
    /**
     * Sample an integer value uniformly between lower and upper. lower is inclusive, upper is exclusive.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qrandint(String parameter, int lower, int upper, int q){
        SearchSpace sp = new SearchSpace("qrandint");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        sp.addParam("q", q);
        this.config.put(parameter, sp);
        return this;
    }
    
    
    /**
     * Sample an integer value log-uniformly between lower and upper, with base 10.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace lograndint(String parameter, int lower, int upper){
        return lograndint(parameter, lower, upper, 10.0f);
    }
    
    /**
     * Sample an integer value log-uniformly between lower and upper, with base being the base of logarithm.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @param base base of the log
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace lograndint(String parameter, int lower, int upper, float base){
        SearchSpace sp = new SearchSpace("lograndint");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        sp.addParam("base", base);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Sample an integer value log-uniformly between lower and upper, with base 10.
     * lower is inclusive, upper is also inclusive (!).
     * The value will be quantized, i.e. rounded to an integer increment of q. Quantization makes the upper bound inclusive.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qlograndint(String parameter, int lower, int upper, int q){
        return qlograndint(parameter, lower, upper, q, 10.0f);
    }
    
    /**
     * Sample an integer value log-uniformly between lower and upper, with base being the base of logarithm.
     * lower is inclusive, upper is also inclusive (!).
     * The value will be quantized, i.e. rounded to an integer increment of q. Quantization makes the upper bound inclusive.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param lower lower boundary of the output interval (e.g. 5)
     * @param upper upper boundary of the output interval (e.g. 10)
     * @param q quantization number. The result will be rounded to an integer increment of this value.
     * @param base base of the log
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace qlograndint(String parameter, int lower, int upper, int q, float base){
        SearchSpace sp = new SearchSpace("qlograndint");
        sp.addParam("lower", lower);
        sp.addParam("upper", upper);
        sp.addParam("q", q);
        sp.addParam("base", base);
        this.config.put(parameter, sp);
        return this;
    }
    
    
    /**
     * Sample a categorical value.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param categories sample a categorical value.
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace choice(String parameter, List<Object> categories){
        SearchSpace sp = new SearchSpace("choice");
        sp.addParam("categories", categories);
        this.config.put(parameter, sp);
        return this;
    }
    
    /**
     * Grid search over a value. The final number of trials is: the number of values per gridsearch multiplied with number of trials.
     * @param parameter parameter name (which should appear in the 
     * <a href="https://huggingface.co/transformers/main_classes/trainer.html#transformers.TrainingArguments">training arguments</a>
     * like learning_rate.
     * @param values a list whose parameters will be gridded
     * @return the search space (this) itself for next call
     */
    public TransformersHpSearchSpace gridSearch(String parameter, List<Object> values){
        SearchSpace sp = new SearchSpace("grid_search");
        sp.addParam("values", values);
        this.config.put(parameter, sp);
        return this;
    }    
    
    public String toJsonString(){
        try {
            return JSON_MAPPER.writeValueAsString(this.config);
        } catch (JsonProcessingException ex) {
            LOGGER.warn("Could not construct JSON string. Sending an empty string. Be warned.", ex);
            return "";
        }
    }
    
    
    
    /**
     * Initializes a default search space for hyper parameter search. 
     * @return a default search space for hyper parameter search
     */
    public static TransformersHpSearchSpace getDefaultHpSpace(){
        //from https://github.com/huggingface/transformers/blob/546dc24e0883e5e9f5eb06ec8060e3e6ccc5f6d7/src/transformers/trainer_utils.py#L187
        //https://huggingface.co/blog/ray-tune   --> 
        //https://medium.com/distributed-computing-with-ray/hyperparameter-optimization-for-transformers-a-guide-c4e32c6c989b --> 
        //                https://colab.research.google.com/drive/1tQgAKgcKQzheoh503OzhS4N9NtfFgmjF?usp=sharing
        //https://docs.ray.io/en/master/tune/examples/pbt_transformers.html
        //https://github.com/huggingface/transformers/blob/9160d81c98854df44b1d543ce5d65a6aa28444a2/src/transformers/trainer_utils.py#L186
        
        //"learning_rate", "num_train_epochs","per_device_train_batch_size","weight_decay","warmup_steps"
        //
        
        return new TransformersHpSearchSpace()
                .loguniform("learning_rate", 1e-6f, 1e-4f)
                .choice("num_train_epochs", Arrays.asList(1,2,3,4,5))
                .uniform("seed", 1, 40) //TODO: maybe use randint because seed is int and not float
                .choice("per_device_train_batch_size", Arrays.asList(4, 8, 16, 32, 64));
    }
    
    public static TransformersHpSearchSpace getDefaultHpSpaceMutations(){
        ////https://docs.ray.io/en/master/tune/examples/pbt_transformers.html
        return new TransformersHpSearchSpace()
                .uniform("weight_decay", 0.0f, 0.3f)
                .uniform("learning_rate", 1e-5f, 5e-5f)
                .choice("per_device_train_batch_size", Arrays.asList(4, 8, 16, 32, 64));
    }
}

class SearchSpace{
    private String name;
    private Map<String,Object> params;
    
    public SearchSpace(String name){
        this.name = name;
        this.params = new HashMap<>();
    }
    
    public void addParam(String key, Object value){
        this.params.put(key, value);
    }

    public String getName() {
        return name;
    }

    public Map<String, Object> getParams() {
        return params;
    }
}
