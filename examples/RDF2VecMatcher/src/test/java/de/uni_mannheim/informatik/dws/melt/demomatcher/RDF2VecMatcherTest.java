package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.ExactStringMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.AnnonymousNodeFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BadHostsFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.NoOpMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;


class RDF2VecMatcherTest {

    @Test
    public void evalSimpleMatcher(){
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        //matchers.put("ExactStringMatch", new ExactStringMatcher());
        //TestCase tc = TrackRepository.Multifarm.getSameOntologies().get(112);
        TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();

        Alignment sample = tc.getParsedReferenceAlignment().sampleByFraction(0.75);

        matchers.put("ReferenceSample", new ReferenceSampleMatcher(sample));
        //matchers.put("RDF2Vec", new MatcherPipelineYAAAJenaConstructor(new ExactStringMatcher(), new ReferenceSampleMatcher(sample)));
        matchers.put("RDF2VecPostProcessed", new MatcherPipelineYAAAJenaConstructor(new RDF2VecMatcher(sample),
                new BadHostsFilter(true), new AnnonymousNodeFilter(), new ConfidenceFilter(0.90), new NaiveDescendingExtractor()));
        
        ExecutionResultSet results = Executor.run(tc, matchers);
        
        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setBaselineMatcher(new ReferenceSampleMatcher(sample));
        e.writeToDirectory();
    }

}