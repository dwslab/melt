package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SSSOMParserTest {

    @Test
    public void SSSOMParserTest() throws IOException, SSSOMFormatException{
        InputStream i = this.getClass().getClassLoader().getResourceAsStream("mp_hp_mgi_all.sssom.tsv");
        Alignment a = SSSOMParser.parse(i);
        
        String objectSource = a.getExtensionValueCasted(DefaultExtensions.SSSOM.OBJECT_SOURCE.toString());
        assertEquals("http://purl.obolibrary.org/obo/hp", objectSource);
        
        List<String> creators = a.getExtensionValueCasted(DefaultExtensions.SSSOM.CREATOR_ID.toString());
        assertEquals(creators.size(), 5);
        assertTrue(creators.contains("https://orcid.org/0000-0003-4606-0597"));
        assertTrue(creators.contains("https://orcid.org/0000-0002-6490-7723"));
        assertTrue(creators.contains("https://orcid.org/0000-0003-2307-1226"));
        assertTrue(creators.contains("https://ror.org/021sy4w91"));
        assertTrue(creators.contains("https://www.wikidata.org/wiki/Q1951035"));
        
        assertEquals(770, a.size());
        Correspondence c = a.getCorrespondence("http://purl.obolibrary.org/obo/MP_0003598", "http://purl.obolibrary.org/obo/HP_0000039",
                CorrespondenceRelation.EQUIVALENCE);
        
        assertNotNull(c);
        
        String s = c.getExtensionValueCasted(DefaultExtensions.SSSOM.SUBJECT_LABEL);
        assertEquals("epispadia", s);
        
        List<String> authorIds = c.getExtensionValueCasted(DefaultExtensions.SSSOM.AUTHOR_ID);
        assertEquals(2, authorIds.size());
        assertTrue(creators.contains("https://orcid.org/0000-0003-4606-0597"));
        assertTrue(creators.contains("https://orcid.org/0000-0002-6490-7723"));
        
        LocalDate mappingDate = c.getExtensionValueCasted(DefaultExtensions.SSSOM.MAPPING_DATE);
        assertEquals(LocalDate.parse("2022-09-02"), mappingDate);
    }
    
    
    @Test
    public void SSSOMParserWithInvalidInput() throws IOException{
        InputStream i = this.getClass().getClassLoader().getResourceAsStream("LogMap-cmt-conference.rdf");
        assertThrows(SSSOMFormatException.class, ()->{
            Alignment a = SSSOMParser.parse(i);
        });
    }
    
}