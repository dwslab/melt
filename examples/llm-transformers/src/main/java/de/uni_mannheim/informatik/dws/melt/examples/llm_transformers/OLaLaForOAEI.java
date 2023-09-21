package de.uni_mannheim.informatik.dws.melt.examples.llm_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.HighPrecisionMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BadHostsFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.AddAlignmentMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ConfidenceCombiner;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorOnlyLabel;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorSet;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.LLMBinaryFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class OLaLaForOAEI implements IMatcher<OntModel,Alignment,Properties> {
    
    private File transformersCache = null;
    private String gpus = null;
    
    
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters) throws Exception {
        
        SentenceTransformersMatcher biEncoder = new SentenceTransformersMatcher(
            TextExtractor.appendStringPostProcessing(new TextExtractorSet(), StringProcessing::normalizeOnlyCamelCaseAndUnderscore),
            "multi-qa-mpnet-base-dot-v1"//"all-MiniLM-L6-v2"
        );
        biEncoder.setMultipleTextsToMultipleExamples(true);
        biEncoder.setTopK(5); 
        if(this.transformersCache != null)
            biEncoder.setTransformersCache(this.transformersCache);
        if(this.gpus != null)
            biEncoder.setCudaVisibleDevices(this.gpus);
        biEncoder.addResourceFilter(SentenceTransformersPredicateBadHosts.class);
        
        
        //String model = "TaylorAI/Flash-Llama-7B";
        String model = "upstage/Llama-2-70b-instruct-v2";
                
        LLMBinaryFilter llmTransformersFilter = new LLMBinaryFilter(
                new TextExtractorOnlyLabel(), 
                model,
                CLIOptions.PREDEFINED_PROMPTS.get(7));
        llmTransformersFilter.setMultipleTextsToMultipleExamples(true);
        if(this.transformersCache != null)
            llmTransformersFilter.setTransformersCache(this.transformersCache);
        if(this.gpus != null)
            llmTransformersFilter.setCudaVisibleDevices(this.gpus);
        
        llmTransformersFilter
                .addGenerationArgument("max_new_tokens", 10)
                .addGenerationArgument("temperature", 0.0);
        llmTransformersFilter.addLoadingArguments(LLMConfiguration.getConfiguration(model).getLoadingArguments());
        
        MatcherPipelineYAAAJenaConstructor highPrecision = new MatcherPipelineYAAAJenaConstructor(
            new HighPrecisionMatcher(),
            new BadHostsFilter()
        );
        Alignment highPrecisionAlignment = highPrecision.match(source, target, inputAlignment, parameters);
        
        MatcherPipelineYAAAJenaConstructor matcher = new MatcherPipelineYAAAJenaConstructor(
            biEncoder, 
            llmTransformersFilter,
            new ConfidenceCombiner(LLMBinaryFilter.class),
            new AddAlignmentMatcher(highPrecisionAlignment),
            new NaiveDescendingExtractor(),
            new ConfidenceFilter(0.5)
        );
        
        return matcher.match(source, target, inputAlignment, parameters);
    }

    public File getTransformersCache() {
        return transformersCache;
    }

    public void setTransformersCache(File transformersCache) {
        this.transformersCache = transformersCache;
    }

    public String getGpus() {
        return gpus;
    }

    public void setGpus(String gpus) {
        this.gpus = gpus;
    }
}
