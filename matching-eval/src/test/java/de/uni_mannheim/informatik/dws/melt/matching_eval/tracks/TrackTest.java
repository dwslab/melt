package de.uni_mannheim.informatik.dws.melt.matching_eval.tracks;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


class TrackTest {

    
    @Test
    public void testDistinctTracks(){
        List<URL> distinctOntologies = TrackRepository.Conference.V1.getDistinctOntologies();
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-conference" + File.separator + "source.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-conference" + File.separator + "target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-confof" + File.separator + "target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-edas" + File.separator + "target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-ekaw" + File.separator + "target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-iasted" + File.separator + "target.rdf"));
        assertTrue(isLastPartSomeWhereInList(distinctOntologies, "cmt-sigkdd" + File.separator + "target.rdf"));
    }
    
    private boolean isLastPartSomeWhereInList(List<URL> list, String lastPart){
        return list.stream().anyMatch(uri->uri.toString().endsWith(lastPart));
    }
}