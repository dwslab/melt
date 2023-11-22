package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherPipelineYAAAJenaConstructor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.AnonymousNodeFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BadHostsFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConfidenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction.NaiveDescendingExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel.ForwardMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge;
import java.util.*;

import org.junit.jupiter.api.Test;


class RDF2VecMatcherTest {

    @Test
    public void evalSimpleMatcher() {
        Map<String, IOntologyMatchingToolBridge> matchers = new HashMap<>();
        //matchers.put("ExactStringMatch", new ExactStringMatcher());
        //TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();

        List<TestCase> evalList = new ArrayList<>();

        /*
        int i = 0;
        trackLoop:

            for (TestCase tc : TrackRepository.Multifarm.getSameOntologies()) {
            //for (TestCase tc : track.ge) {
                i++;
                try {
                    Alignment sample = tc.getParsedReferenceAlignment().sampleByFraction(0.5);
                    File f = File.createTempFile("ref_sample", ".rdf");
                    sample.serialize(f);
                    evalList.add(new TestCase(tc.getName(), tc.getSource(), tc.getTarget(), tc.getReference(), tc.getTrack(), f.toURI(), GoldStandardCompleteness.COMPLETE));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //if (i == 3){
                //    break trackLoop;
                //}
            //}
        }
        */

        TestCase tc = TrackRepository.Anatomy.Default.getFirstTestCase();
        Alignment sample = tc.getParsedReferenceAlignment().sampleByFraction(0.75);

        matchers.put("ReferenceSample", new ForwardMatcher());
        //matchers.put("RDF2Vec", new MatcherPipelineYAAAJenaConstructor(new ExactStringMatcher(), new ReferenceSampleMatcher(sample)));
        matchers.put("RDF2VecPostProcessed", new MatcherPipelineYAAAJenaConstructor(new RDF2VecMatcher(sample),
                new BadHostsFilter(true), new AnonymousNodeFilter(), new ConfidenceFilter(0.9), new NaiveDescendingExtractor()));

        ExecutionResultSet results = Executor.run(TrackRepository.Anatomy.Default, matchers);

        EvaluatorCSV e = new EvaluatorCSV(results);
        e.setBaselineMatcher(new ForwardMatcher(sample));
        e.writeToDirectory();
    }

}