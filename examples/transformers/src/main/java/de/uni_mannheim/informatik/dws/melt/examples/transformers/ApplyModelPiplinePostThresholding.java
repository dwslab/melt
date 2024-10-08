package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.matching_data.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_eval.paramtuning.ConfidenceFinder;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ConfidenceCombiner;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class ApplyModelPiplinePostThresholding extends MatcherYAAAJena {


    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyModelPipeline.class);

    private final MatcherYAAAJena recallMatcher;
    private final TransformersFilter transformersFilter;
    private boolean isAutoThresholding;

    public ApplyModelPiplinePostThresholding(String gpu, String transformerModel, File transformersCache,
                              MatcherYAAAJena recallMatcher, boolean isMultipleTextsToMultipleExamples,
                              TextExtractor te, boolean isAutoThresholding) {
        TextExtractor textExtractor = te;
        textExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);
        this.transformersFilter = new TransformersFilter(textExtractor, transformerModel);
        this.transformersFilter.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
        this.transformersFilter.setCudaVisibleDevices(gpu);
        this.transformersFilter.setTransformersCache(transformersCache);
        this.recallMatcher = recallMatcher;
        this.isAutoThresholding = isAutoThresholding;
    }

    public ApplyModelPiplinePostThresholding( MatcherYAAAJena recallMatcher, TransformersFilter transformersFilter) {
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
        if(recallAlignment.size() > 50_000) {
            // set to false for next time
            this.transformersFilter.setOptimizeAll(false);
        }
        // now we need to set the transformer confidence as main confidence for the MWB extractor
        ConfidenceCombiner confidenceCombiner = new ConfidenceCombiner(TransformersFilter.class);
        Alignment alignmentWithOneConfidence = confidenceCombiner.combine(alignmentWithConfidence);

        // run the extractor
        MaxWeightBipartiteExtractor extractorMatcher = new MaxWeightBipartiteExtractor();
        Alignment extracted = extractorMatcher.match(source, target, alignmentWithOneConfidence, properties);

        // just for logging
        double bestConfidenceF1 = ConfidenceFinder.getBestConfidenceForFmeasure(inputAlignment,
                extracted,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);

        // just for logging
        double bestConfidencePrecision = ConfidenceFinder.getBestConfidenceForPrecision(inputAlignment,
                extracted,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE);

        // just for logging
        double bestConfidenceF05 = ConfidenceFinder.getBestConfidenceForFmeasureBeta(inputAlignment,
                extracted,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE, 0.5);

        // just for logging
        double bestConfidenceF15 = ConfidenceFinder.getBestConfidenceForFmeasureBeta(inputAlignment,
                extracted,
                GoldStandardCompleteness.PARTIAL_SOURCE_COMPLETE_TARGET_COMPLETE, 1.5);

        LOGGER.info("Best confidence F1: {}\nBest confidence precision: {}\nBest confidence F05: {}\nBest confidence " +
                        "F15: {}", bestConfidenceF1,
                bestConfidencePrecision, bestConfidenceF05, bestConfidenceF15);

        if(isAutoThresholding) {
            LOGGER.info("Apply auto thresholding with threshold " + bestConfidenceF1);
            ConfidenceFilter confidenceFilter = new ConfidenceFilter(bestConfidenceF1);
            extracted = confidenceFilter.filter(alignmentWithOneConfidence, source, target);
        }

        return extracted;
    }
}

