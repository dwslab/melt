package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.net.URI;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class TrackTest {

    @Test
    public void testDistinctTracks(){
        List<URL> distinctOntologies = TrackRepository.Conference.V1.getDistinctOntologies();
        System.out.println(distinctOntologies);
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-conference/source.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-conference/target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-confof/target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-edas/target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-ekaw/target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-iasted/target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-sigkdd/target.rdf"));
    }
    
    private boolean isLastPartSomeWhereInList(List<URL> list, String lastPart){
        return list.stream().anyMatch(uri->uri.toString().endsWith(lastPart));
    }
}