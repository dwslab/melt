package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ConfidenceCombiner;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorForTransformers;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This pipeline matcher applies the given model.
 */
public class ApplyModelPipeline extends MatcherYAAAJena {


    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyModelPipeline.class);

    private final MatcherYAAAJena recallMatcher;
    private final TransformersFilter transformersFilter;


    public ApplyModelPipeline(String gpu, String transformerModel, File transformersCache,
                              MatcherYAAAJena recallMatcher, boolean isMultipleTextsToMultipleExamples,
                              TextExtractor te) {
        TextExtractor textExtractor = te;
        textExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);
        this.transformersFilter = new TransformersFilter(textExtractor, transformerModel);
        this.transformersFilter.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
        this.transformersFilter.setCudaVisibleDevices(gpu);
        this.transformersFilter.setTransformersCache(transformersCache);
        this.recallMatcher = recallMatcher;
    }
    
    public ApplyModelPipeline( MatcherYAAAJena recallMatcher, TransformersFilter transformersFilter) {
        this.recallMatcher = recallMatcher;
        this.transformersFilter = transformersFilter;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment recallAlignment = this.recallMatcher.match(source, target, new Alignment(), properties);
        LOGGER.info("Recall alignment with {} correspondences", recallAlignment.size());

        if(recallAlignment.size() > 50_000) {
            LOGGER.info("Optimizing the transformers filter, since the recall alignment is very large.");
            this.transformersFilter.setOptimizeAll(true);
        }

        Alignment alignmentWithConfidence = this.transformersFilter.match(source, target, recallAlignment, properties);

        // now we need to set the transformer confidence as main confidence for the MWB extractor
        ConfidenceCombiner confidenceCombiner = new ConfidenceCombiner(TransformersFilter.class);
        Alignment alignmentWithOneConfidence = confidenceCombiner.combine(alignmentWithConfidence);

        // run the extractor
        MaxWeightBipartiteExtractor extractorMatcher = new MaxWeightBipartiteExtractor();
        Alignment extractedAlignment =  extractorMatcher.match(source, target, alignmentWithOneConfidence, properties);

        double bestConfidence = ConfidenceFinder.getBestConfidenceForFmeasure(inputAlignment, extractedAlignment,
                GoldStandardCompleteness.PARTIAL_SOURCE_INCOMPLETE_TARGET_INCOMPLETE);

        LOGGER.info("Best confidence: " + bestConfidence);
        ConfidenceFilter confidenceFilter = new ConfidenceFilter(bestConfidence);
        return confidenceFilter.filter(extractedAlignment, source, target);
    }
}
