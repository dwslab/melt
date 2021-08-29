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

    @Test
    void getSetDirectory(){
        final String newDir = "./new_persistence_dir";

        PersistenceService service = PersistenceService.getService();
        assertNotNull(service);

        // calling it again:
        service = PersistenceService.getService();
        assertNotNull(service);

        assertEquals(service.getPersistenceDirectory(), PersistenceService.DEFAULT_PERSISTENCE_DIRECTORY);
        PersistenceService service2 = PersistenceService.getService(newDir);
        assertEquals(newDir, service2.getPersistenceDirectory() );
        assertEquals(newDir, service.getPersistenceDirectory() );
        service.closePersistenceService();
    }

}