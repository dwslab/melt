package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BackgroundMatcherTest {


    @Test
    void match(){
        assertNotNull(TrackRepository.Largebio.V2016.ALL);
    }

}