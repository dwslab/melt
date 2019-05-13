package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

import static org.junit.jupiter.api.Assertions.*;

class CorrespondenceRelationTest {

    @org.junit.jupiter.api.Test
    void parse() {
        assertEquals(CorrespondenceRelation.EQUIVALENCE, CorrespondenceRelation.parse("="));
    }
}