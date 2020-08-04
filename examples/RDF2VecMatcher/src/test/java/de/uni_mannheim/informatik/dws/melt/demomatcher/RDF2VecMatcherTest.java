package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.ExactStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BadHostsFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.NoOpMatcher;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;


class RDF2VecMatcherTest {

    @Test
    public void evalSimpleMatcher(){
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        matchers.put("ExactStringMatch", new ExactStringMatcher());
        
        matchers.put("RDF2Vec", new MatcherPipelineYAAAJenaConstructor(new ExactStringMatcher(), new RDF2VecMatcher()));
        
        matchers.put("RDF2VecPostProcessed", new MatcherPipelineYAAAJenaConstructor(new ExactStringMatcher(), new RDF2VecMatcher(), 
                new BadHostsFilter(true), new ConfidenceFilter(0.96), new NaiveDescendingExtractor()));
        
        ExecutionResultSet results = Executor.run(TrackRepository.Anatomy.Default, matchers);
        
        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setBaselineMatcher(new NoOpMatcher());  
        e.writeToDirectory();
    }

}