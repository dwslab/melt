package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.GoldStandardCompleteness;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
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

import java.io.File;
import java.util.*;

import org.apache.jena.ontology.OntModel;
import org.junit.jupiter.api.Test;


class RDF2VecMatcherTest {

    @Test
    public void evalSimpleMatcher() {
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        //matchers.put("ExactStringMatch", new ExactStringMatcher());
        //TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();

        List<TestCase> evalList = new ArrayList<>();
        for (Track track : TrackRepository.Multifarm.getMultifarmTrackForLanguage("de-en")) {
            for (TestCase tc : track.getTestCases()) {
                try {
                    Alignment sample = tc.getParsedReferenceAlignment().sampleByFraction(0.5);
                    File f = File.createTempFile("ref_sample", ".rdf");
                    sample.serialize(f);
                    evalList.add(new TestCase(tc.getName(), tc.getSource(), tc.getTarget(), tc.getReference(), track, f.toURI(), GoldStandardCompleteness.COMPLETE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        matchers.put("ReferenceSample", new ForwardMatcher());
        //matchers.put("RDF2Vec", new MatcherPipelineYAAAJenaConstructor(new ExactStringMatcher(), new ReferenceSampleMatcher(sample)));
        matchers.put("RDF2VecPostProcessed", new MatcherPipelineYAAAJenaConstructor(new RDF2VecMatcher(),
                new BadHostsFilter(true), new AnnonymousNodeFilter(), new ConfidenceFilter(0.85), new NaiveDescendingExtractor()));

        ExecutionResultSet results = Executor.run(evalList, matchers);

        EvaluatorCSV e = new EvaluatorCSV(results);
        e.writeToDirectory();
    }

}