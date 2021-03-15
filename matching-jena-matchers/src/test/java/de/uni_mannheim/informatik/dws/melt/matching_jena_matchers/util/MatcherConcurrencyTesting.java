package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.jena.ontology.OntModel;
import org.javatuples.Pair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Helper class for concurrent tests on Anatomy.
 */
public class MatcherConcurrencyTesting {


    static class MatcherRunnable implements Runnable{

        private MatcherYAAAJena matcher;

        private Alignment alignment;

        public MatcherRunnable(MatcherYAAAJena matcher){
            this.matcher = matcher;
        }

        @Override
        public void run() {
            // we need something larger here so that clashes are likely
            TestCase tc1 = TrackRepository.Anatomy.Default.getFirstTestCase();
            try {
                this.alignment = this.matcher.match(tc1.getSourceOntology(OntModel.class),
                        tc1.getTargetOntology(OntModel.class), null,
                        null);
            } catch (Exception e){return;}
        }

        public Alignment getAlignment() {
            return alignment;
        }
    }

    public static Pair<Alignment, Alignment> concurrencyMatching(MatcherYAAAJena matcher1, MatcherYAAAJena matcher2){
        try {
            MatcherRunnable r1 = new MatcherRunnable(matcher1);
            MatcherRunnable r2 = new MatcherRunnable(matcher2);
            Thread t1 = new Thread(r1);
            Thread t2 = new Thread(r2);
            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertTrue(r1.getAlignment().size() > 1);
            assertTrue(r2.getAlignment().size() > 1);
            return new Pair<>(r1.getAlignment(), r2.getAlignment());
        } catch (Exception e){
            return null;
        }
    }
}
