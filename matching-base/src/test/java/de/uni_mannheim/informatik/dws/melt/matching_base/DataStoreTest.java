package de.uni_mannheim.informatik.dws.melt.matching_base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    @Test
    void dataStoreTest(){
        DataStore ds = DataStore.getGlobal();
        ds.put("key", new Integer(12345));
        assertEquals(new Integer(12345), ds.get("key", Integer.class));
        Integer i1 = ds.get("key");
        assertEquals(12345, i1);
    }

}