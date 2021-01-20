package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceServiceTest {

    @Test
    void testPreconfiguredPersistences(){
        for(PersistenceService.PreconfiguredPersistences persistence :  PersistenceService.PreconfiguredPersistences.values() ){
            assertNotNull(persistence.getFilePath());
            assertNotNull(persistence.getKeySerializer());
            assertNotNull(persistence.getValueSerializer());
            assertNotNull(persistence.getKeyClass());
        }
    }

}