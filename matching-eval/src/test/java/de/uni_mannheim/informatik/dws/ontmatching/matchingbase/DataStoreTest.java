package de.uni_mannheim.informatik.dws.ontmatching.matchingbase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataStoreTest {

    @Test
    void dataStoreTest(){
        DataStore ds = DataStore.getGlobal();
        ds.put("Hello", new Integer(12345));
        assertEquals(new Integer(12345), ds.get("Hello", Integer.class));

        Integer i1 = ds.get("Hello");
        assertEquals(12345, i1);
    }

}