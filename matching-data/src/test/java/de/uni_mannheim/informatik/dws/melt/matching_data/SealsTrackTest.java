package de.uni_mannheim.informatik.dws.melt.matching_data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SealsTrackTest {

    @Test
    void constructor(){
        SealsTrack st = new SealsTrack("http://oaei.webdatacommons.org/tdrs/",
                "anatomy_track",
                "anatomy_track-default");
        assertEquals(1, st.getTestCases().size());
    }

}