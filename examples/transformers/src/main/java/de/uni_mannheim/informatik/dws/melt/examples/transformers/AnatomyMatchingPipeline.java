package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher.RecallMatcherAnatomy;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorFallback;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorUrlFragment;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

public class AnatomyMatchingPipeline extends MatcherYAAAJena {


    public AnatomyMatchingPipeline(String gpu, String transformerModel, File transformersCache){
        this.gpu = gpu;
        this.transformerModel = transformerModel;
        this.transformersCache = transformersCache;
    }

    private String gpu;
    private String transformerModel;
    private File transformersCache;

    private static Logger LOGGER = LoggerFactory.getLogger(AnatomyMatchingPipeline.class);

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment recallAlignment = new RecallMatcherAnatomy().match(source, target, new Alignment(), properties);
        LOGGER.info("Recall alignment with {} correspondences", recallAlignment.size());


        TextExtractorFallback extractorFallback = new TextExtractorFallback(
                new TextExtractorAllAnnotationProperties(),
                new TextExtractorUrlFragment()
        );

        TransformersFilter zeroShot = new TransformersFilter(extractorFallback, transformerModel);
        zeroShot.setCudaVisibleDevices(gpu);
        zeroShot.setTransformersCache(transformersCache);
        zeroShot.setTmpDir(new File("./mytmpDir_filter"));

        Alignment alignmentWithConfidence = zeroShot.match(source, target, recallAlignment, properties);

        MaxWeightBipartiteExtractor extractorMatcher = new MaxWeightBipartiteExtractor();

        return extractorMatcher.match(source, target, alignmentWithConfidence, null);
    }
}
