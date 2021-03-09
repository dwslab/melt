package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;

import java.util.Properties;

/**
 * Matcher which applies String matching and matches then with the provided background knowledge source and strategy.
 */
public class BackgroundMatcherStandAlone extends MatcherYAAAJena {


    MatcherPipelineYAAAJenaConstructor pipelineYAAAJena;

    public BackgroundMatcherStandAlone(SemanticWordRelationDictionary backgroundKnowledgeSource,
                                       ImplementedStrategies strategy){

        SimpleStringMatcher simpleStringMatcher = new SimpleStringMatcher();
        BackgroundMatcher backgroundMatcher = new BackgroundMatcher(backgroundKnowledgeSource, strategy);

        pipelineYAAAJena = new MatcherPipelineYAAAJenaConstructor(simpleStringMatcher, backgroundMatcher);
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return pipelineYAAAJena.match(source, target, inputAlignment, properties);
    }
}
