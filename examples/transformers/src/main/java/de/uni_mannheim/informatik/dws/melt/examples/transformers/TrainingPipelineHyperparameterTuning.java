package de.uni_mannheim.informatik.dws.melt.examples.transformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.addnegatives.AddNegativesViaMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTunerHpSearch;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.io.File;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;

/**
 * This training pipeline writes the training file.
 */
public class TrainingPipelineHyperparameterTuning extends MatcherYAAAJena {


    private MatcherYAAAJena recallMatcher;
    private final TransformersFineTunerHpSearch fineTunerHpSearch;

    public TrainingPipelineHyperparameterTuning(String gpu, String transformerModel, File finetunedModelFile, File transformersCache,
                            MatcherYAAAJena recallMatcher,
                            boolean isMultipleTextsToMultipleExamples,
                            TextExtractor textExtractor) {
        textExtractor = TextExtractor.appendStringPostProcessing(textExtractor, StringProcessing::normalizeOnlyCamelCaseAndUnderscore);
        
        this.fineTunerHpSearch = new TransformersFineTunerHpSearch(textExtractor, transformerModel, finetunedModelFile);
        this.fineTunerHpSearch.setCudaVisibleDevices(gpu);
        this.fineTunerHpSearch.setTransformersCache(transformersCache);
        this.fineTunerHpSearch.setMultipleTextsToMultipleExamples(isMultipleTextsToMultipleExamples);
        this.fineTunerHpSearch.setNumberOfTrials(12);
        this.fineTunerHpSearch.setAdjustMaxBatchSize(true);
        this.recallMatcher = recallMatcher;
    }
    
    public TrainingPipelineHyperparameterTuning(MatcherYAAAJena recallMatcher, TransformersFineTunerHpSearch fineTuner) {
        this.recallMatcher = recallMatcher;
        this.fineTunerHpSearch = fineTuner;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment recallAlignment = this.recallMatcher.match(source, target, new Alignment(), properties);
        Alignment trainingAlignment = AddNegativesViaMatcher.addNegatives(recallAlignment, inputAlignment);

        //append to training file
        fineTunerHpSearch.match(source, target, trainingAlignment, properties);

        return inputAlignment;
    }

    public TransformersFineTunerHpSearch getFineTuner() {
        return fineTunerHpSearch;
    }

    public void setRecallMatcher(MatcherYAAAJena recallMatcher) {
        this.recallMatcher = recallMatcher;
    }
}
