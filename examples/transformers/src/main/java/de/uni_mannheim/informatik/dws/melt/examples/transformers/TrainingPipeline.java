package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives.AddNegativesViaMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorForTransformers;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * This training pipeline writes the training file.
 */
public class TrainingPipeline extends MatcherYAAAJena {


    private MatcherYAAAJena recallMatcher;
    private final TransformersFineTuner fineTuner;

    public TrainingPipeline(String gpu, String transformerModel, File finetunedModelFile, File transformersCache, MatcherYAAAJena recallMatcher) {
        TextExtractor textExtractor = new TextExtractorForTransformers();
        textExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);
        
        this.fineTuner = new TransformersFineTuner(textExtractor, transformerModel, finetunedModelFile);
        this.fineTuner.setCudaVisibleDevices(gpu);
        this.fineTuner.setTransformersCache(transformersCache);
        
        this.recallMatcher = recallMatcher;
    }
    
    public TrainingPipeline(MatcherYAAAJena recallMatcher, TransformersFineTuner fineTuner) {
        this.recallMatcher = recallMatcher;
        this.fineTuner = fineTuner;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        Alignment recallAlignment = this.recallMatcher.match(source, target, new Alignment(), properties);
        Alignment trainingAlignment = AddNegativesViaMatcher.addNegatives(recallAlignment, inputAlignment);

        //append to training file
        fineTuner.match(source, target, trainingAlignment, properties);

        return inputAlignment;
    }

    public TransformersFineTuner getFineTuner() {
        return fineTuner;
    }

    public void setRecallMatcher(MatcherYAAAJena recallMatcher) {
        this.recallMatcher = recallMatcher;
    }
}
