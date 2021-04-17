package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.TopXFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.HungarianExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.MaxWeightBipartiteExtractor;
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

    private SimpleStringMatcher simpleStringMatcher;

    private BackgroundMatcher backgroundMatcher;

    private TopXFilter topXFilter;

    /**
     * The name of the matcher.
     */
    private String name;

    /**
     * Constructor
     * @param backgroundKnowledgeSource The background knowledge source to be used.
     * @param strategy The strategy to be applied.
     * @param threshold The minimal required threshold that is required for a match.
     */
    public BackgroundMatcherStandAlone(SemanticWordRelationDictionary backgroundKnowledgeSource,
                                       ImplementedBackgroundMatchingStrategies strategy,
                                       double threshold){
        this(backgroundKnowledgeSource, strategy, false, threshold);
    }

    /**
     * Constructor
     * @param backgroundKnowledgeSource The background knowledge source to be used.
     * @param strategy The strategy to be applied.
     * @param isUseOneToOneExtractor True if alignment shall be transformed to a 1-1 alignment.
     * @param threshold The minimal required threshold that is required for a match.
     */
    public BackgroundMatcherStandAlone(SemanticWordRelationDictionary backgroundKnowledgeSource,
                                       ImplementedBackgroundMatchingStrategies strategy,
                                       boolean isUseOneToOneExtractor,
                                       double threshold){
        this(backgroundKnowledgeSource, strategy, isUseOneToOneExtractor, null, threshold, 1);
    }

    /**
     * Constructor
     * @param backgroundKnowledgeSource The background knowledge source to be used.
     * @param strategy The strategy to be applied.
     * @param isUseOneToOneExtractor True if alignment shall be transformed to a 1-1 alignment.
     * @param extractor The desired extractor that shall be used.
     * @param threshold The minimal required threshold that is required for a match.
     * @param topX The top X correspondences that shall be kept.‚
     */
    public BackgroundMatcherStandAlone(SemanticWordRelationDictionary backgroundKnowledgeSource,
                                       ImplementedBackgroundMatchingStrategies strategy,
                                       boolean isUseOneToOneExtractor,
                                       MatcherYAAAJena extractor,
                                       double threshold,
                                       int topX){
        this.backgroundKnowledgeSource = backgroundKnowledgeSource;
        this.strategy = strategy;
        this.threshold = threshold;
        this.simpleStringMatcher = new SimpleStringMatcher();
        this.backgroundMatcher = new BackgroundMatcher(backgroundKnowledgeSource, strategy, threshold);
        this.topXFilter = new TopXFilter(topX, TopXFilter.TopFilterMode.SMALLEST, threshold);

        if(isUseOneToOneExtractor){
            if(extractor == null) {
                // default extractor: Use Hungarian at the moment due to infinity loop issues with MWBE.
                //MaxWeightBipartiteExtractor mwb = new MaxWeightBipartiteExtractor();
                HungarianExtractor he = new HungarianExtractor();
                pipelineYAAAJena = new MatcherPipelineYAAAJenaConstructor(simpleStringMatcher, backgroundMatcher,
                        topXFilter, he);
            } else {
                pipelineYAAAJena = new MatcherPipelineYAAAJenaConstructor(simpleStringMatcher, backgroundMatcher,
                        topXFilter, extractor);
            }
        } else {
            pipelineYAAAJena = new MatcherPipelineYAAAJenaConstructor(simpleStringMatcher, backgroundMatcher);
        }
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties)
            throws Exception {
        Alignment alignment =  pipelineYAAAJena.match(source, target, inputAlignment, properties);
        alignment.addExtensionValue("http://a.com/matcherThreshold", "" + threshold);
        alignment.addExtensionValue("http://a.com/matcherStrategy", this.strategy.toString());
        alignment.addExtensionValue("http://a.com/backgroundDataset", this.backgroundKnowledgeSource.getName());
        return alignment;
    }

    /**
     * Do not exclude String matches when matching in the second step with background knowledge.
     * @param allowForCumulativeMatches True if multi-matches shall be allowed.
     */
    public void setAllowForCumulativeMatches(boolean allowForCumulativeMatches) {
        this.backgroundMatcher.setAllowForCumulativeMatches(allowForCumulativeMatches);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LabelToConceptLinker getLinker(){
        return backgroundKnowledgeSource.getLinker();
    }

    public void setIsVerboseLoggingOutput(boolean isVerboseLoggingOutput){
        this.simpleStringMatcher.setVerboseLoggingOutput(isVerboseLoggingOutput);
        this.backgroundMatcher.setVerboseLoggingOutput(isVerboseLoggingOutput);
    }

    public void getIsVerboseLoggingOutput(){
        this.backgroundMatcher.isVerboseLoggingOutput();
    }
}
