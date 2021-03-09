package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.ExternalResourceWithSynonymCapability;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.GensimEmbeddingModel;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;

import java.util.Properties;

/**
 * Matcher which applies String matching and matches then with the provided background knowledge source and strategy.
 */
public class BackgroundMatcherStandAlone extends MatcherYAAAJena {


    /**
     * The actual matcher that is executed.
     */
    MatcherPipelineYAAAJenaConstructor pipelineYAAAJena;

    private SemanticWordRelationDictionary backgroundKnowledgeSource;

    private ImplementedBackgroundMatchingStrategies strategy;

    private double threshold;

    /**
     * Constructor
     * @param backgroundKnowledgeSource The background knowledge source to be used.
     * @param strategy The strategy to be applied.
     * @param threshold The minimal required threshold that is required for a match.
     */
    public BackgroundMatcherStandAlone(SemanticWordRelationDictionary backgroundKnowledgeSource,
                                       ImplementedBackgroundMatchingStrategies strategy,
                                       double threshold){

        this.backgroundKnowledgeSource = backgroundKnowledgeSource;
        this.strategy = strategy;
        this.threshold = threshold;
        SimpleStringMatcher simpleStringMatcher = new SimpleStringMatcher();
        BackgroundMatcher backgroundMatcher = new BackgroundMatcher(backgroundKnowledgeSource, strategy, threshold);
        pipelineYAAAJena = new MatcherPipelineYAAAJenaConstructor(simpleStringMatcher, backgroundMatcher);
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties)
            throws Exception {
        Alignment alignment =  pipelineYAAAJena.match(source, target, inputAlignment, properties);
        alignment.addExtensionValue("http://a.com/matcherThreshold", "" + threshold);
        alignment.addExtensionValue("http://a.com/matcherStrategy", this.strategy.toString());

        /*
        if (this.backgroundKnowledgeSource instanceof GensimEmbeddingModel) {
            alignment.addExtensionValue("http://a.com/strategyThreshold",
                    "" + ((KnowledgeSourceEmbedding) this.backgroundKnowledgeSource).getThreshold());
        }
        */

        alignment.addExtensionValue("http://a.com/backgroundDataset", this.backgroundKnowledgeSource.getName());
        return alignment;
    }
}
