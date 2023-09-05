package de.uni_mannheim.informatik.dws.melt.examples.llm_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersArguments;
import java.util.HashMap;
import java.util.Map;

public class LLMConfiguration {
    private String systemPromtTemplate;
    private TransformersArguments loadingArguments;
    
    public LLMConfiguration(){
        this.systemPromtTemplate = "{userpromt}";
        this.loadingArguments = new TransformersArguments();
    }
    
    public LLMConfiguration setPromtTemplate(String systemPromtTemplate){
        this.systemPromtTemplate = systemPromtTemplate;
        return this;
    }
    
    public LLMConfiguration addLoadingArguments(String key, Object value){
        this.loadingArguments.addParameter(key, value);
        return this;
    }
    
    /**
     * Returns the processed promt given the userPromt.
     * This means the user promt may be embedded by a system promt.
     * @param userPromt the user promt to use.
     * @return the final processed promt
     */
    public String processPromt(String userPromt){
        return this.systemPromtTemplate.replace("{userpromt}", userPromt);
    }

    public String getSystemPromtTemplate() {
        return systemPromtTemplate;
    }

    public TransformersArguments getLoadingArguments() {
        return loadingArguments;
    }
    
    
    
    
    
    
    public static LLMConfiguration getConfiguration(String modelName){
        return DEFAULTS.getOrDefault(modelName, new LLMConfiguration());
    }
    
    private static Map<String, LLMConfiguration> DEFAULTS = generateDefaults();
    private static Map<String, LLMConfiguration> generateDefaults(){
        
        LLMConfiguration commonConfig = new LLMConfiguration()
                .setPromtTemplate("### User:\n{userpromt}\n\n### Assistant:\n")
                .addLoadingArguments("device_map", "auto")
                .addLoadingArguments("torch_dtype", "torch.float16")
                .addLoadingArguments("load_in_8bit", true);
        
        
        
        Map<String, LLMConfiguration> map = new HashMap<>();
        map.put("upstage/Llama-2-70b-instruct-v2", commonConfig);
        map.put("upstage/Llama-2-70b-instruct", commonConfig);
        map.put("upstage/llama-65b-instruct", commonConfig);
        
        LLMConfiguration llama = new LLMConfiguration()
                .addLoadingArguments("device_map", "auto")
                .addLoadingArguments("torch_dtype", "torch.float16")
                .addLoadingArguments("load_in_8bit", true);
        map.put("meta-llama/Llama-2-7b-hf", llama);
        map.put("meta-llama/Llama-2-13b-hf", llama);
        map.put("meta-llama/Llama-2-70b-hf", llama);
        
        map.put("meta-llama/Llama-2-7b-chat-hf", commonConfig);
        map.put("meta-llama/Llama-2-13b-chat-hf", commonConfig);
        map.put("meta-llama/Llama-2-70b-chat-hf", commonConfig);
        
        String belugaSystemPromt = "### System:\nYou are Stable Beluga, an AI that follows instructions extremely well. Help as much as you can. Remember, be safe, and don't do anything illegal.\n\n";
        map.put("stabilityai/StableBeluga2", new LLMConfiguration()
                .setPromtTemplate(belugaSystemPromt + "### User:{userpromt}\n\n### Assistant:\n")
                .addLoadingArguments("device_map", "auto")
                //.addLoadingArguments("low_cpu_mem_usage", true) //When passing a device_map, low_cpu_mem_usage is automatically set to True
                //https://huggingface.co/docs/transformers/main_classes/model#large-model-loading
                .addLoadingArguments("torch_dtype", "torch.float16"));
        
        map.put("jondurbin/airoboros-l2-70b-2.1", new LLMConfiguration()
                .setPromtTemplate("You are an ontology matching tool which performs quite well.\nUSER: {userpromt}\nASSISTANT: ")
                .addLoadingArguments("device_map", "auto")
                .addLoadingArguments("load_in_8bit", true)
                .addLoadingArguments("torch_dtype", "torch.float16"));
        
        //tiiuae/falcon-40b    //https://huggingface.co/tiiuae/falcon-40b
        
        //https://huggingface.co/openchat/openchat_v3.1
        return map;
    }
}
