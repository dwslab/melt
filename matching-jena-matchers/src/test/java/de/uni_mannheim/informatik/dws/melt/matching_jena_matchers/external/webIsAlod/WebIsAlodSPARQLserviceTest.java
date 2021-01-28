package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.sparql.SparqlServices;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.webIsAlod.classic.WebIsAlodClassicLinker;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test requires a working internet connection.
 */
class WebIsAlodSPARQLserviceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebIsAlodSPARQLserviceTest.class);

    @BeforeAll
    static void setup() {
        WebIsAlodSPARQLservice.closeAllServices();
        PersistenceService.getService().closePersistenceService();
        deletePersistenceDirectory();
        PersistenceService.getService();
    }

    @AfterAll
    static void tearDown() {
        WebIsAlodSPARQLservice.closeAllServices();
        PersistenceService.getService().closePersistenceService();
        deletePersistenceDirectory();
    }

    /**
     * Delete the persistence directory.
     */
    private static void deletePersistenceDirectory() {
        File result = new File(PersistenceService.PERSISTENCE_DIRECTORY);
        if (result.exists() && result.isDirectory()) {
            try {
                FileUtils.deleteDirectory(result);
            } catch (IOException e) {
                LOGGER.error("Failed to remove persistence directory.", e);
            }
        }
    }

    @Test
    void isSynonymousClassic() {
        WebIsAlodSPARQLservice service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT, true);
        assertTrue(service.isDiskBufferEnabled());

        //----------------------------------------------------------
        // Test 1: With Disk Buffer
        // option contract is a contract: 0.6998
        // contract is a option contract: 0.559519
        //----------------------------------------------------------

        assertFalse(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.8));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.5));

        // re test for buffer
        assertFalse(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.8));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.5));


        //----------------------------------------------------------
        // Test 2: Without Disk Buffer
        // option contract is a contract: 0.6998
        // contract is a option contract: 0.559519
        //----------------------------------------------------------

        // test without buffer (enforce query)
        service.close();
        service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT, false);

        assertFalse(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.8));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.5));

        // re test for buffer
        assertFalse(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.8));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/option_contract_>", "<http://webisa.webdatacommons.org/concept/_contract_>", 0.5));


        //----------------------------------------------------------
        // Test 3: Combined test with linker and disk buffer
        // option contract is a contract: 0.6998
        // contract is a option contract: 0.559519
        //----------------------------------------------------------

        service.close();
        service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT, true);

        WebIsAlodClassicLinker linker = new WebIsAlodClassicLinker();

        assertFalse(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.8));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.5));

        // re test for buffer
        assertFalse(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.8));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.5));


        //----------------------------------------------------------
        // Test 4: Combined test with linker and no disk buffer
        // option contract is a contract: 0.6998
        // contract is a option contract: 0.559519
        //----------------------------------------------------------

        service.close();
        service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT, false);

        linker = new WebIsAlodClassicLinker();

        assertFalse(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.8));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.5));

        // re test for buffer
        assertFalse(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.8));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("option contract"), linker.linkToSingleConcept("contract"), 0.5));

        // service MUST be closed to allow for reinitialization with another endpoint
        service.close();
    }

    @Test
    @Disabled
        // Unfortunately, the queries are to complex for the endpoint.
    void isSynonymousXL() {
        WebIsAlodSPARQLservice service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_XL_NO_PROXY);

        //----------------------------------------------------------
        // Test 1:
        // option contract is a contract: 0.6998
        // contract is a option contract:
        //----------------------------------------------------------

        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>", 0.0));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>"));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>", 0.1));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>"));
        assertFalse(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>", 0.8));

        // re test for buffer
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>", 0.0));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>"));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>", 0.1));
        assertTrue(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>"));
        assertFalse(service.isSynonymous("<http://webisa.webdatacommons.org/concept/man>", "<http://webisa.webdatacommons.org/concept/woman>", 0.8));

        //----------------------------------------------------------
        // Test 2: Combined test with linker
        // man is a woman:
        // woman is a man:
        //----------------------------------------------------------

        WebIsAlodClassicLinker linker = new WebIsAlodClassicLinker();
        assertFalse(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman"), 0.8));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman"), 0.1));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman"), 0.0));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman")));

        // re test for buffer
        assertFalse(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman"), 0.8));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman"), 0.1));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman"), 0.0));
        assertTrue(service.isSynonymous(linker.linkToSingleConcept("man"), linker.linkToSingleConcept("woman")));

        // service MUST be closed to allow for reinitialization with another endpoint
        service.close();
    }

    @Test
    void isURIinDictionaryClassic() {
        WebIsAlodSPARQLservice service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_CLASSIC_ENDPOINT);
        assertTrue(service.isURIinDictionary("http://webisa.webdatacommons.org/concept/option_contract_"));
        assertTrue(service.isURIinDictionary("<http://webisa.webdatacommons.org/concept/option_contract_>"));
        assertFalse(service.isURIinDictionary("<http://webisa.webdatacommons.org/concept/option%20contract>"));
        assertFalse(service.isURIinDictionary("http://webisa.webdatacommons.org/concept/option%20contract"));

        // service MUST be closed to allow for reinitialization with another endpoint
        service.close();
    }

    @Test
    void isURIinDictionaryXL() {
        WebIsAlodSPARQLservice service = WebIsAlodSPARQLservice.getInstance(WebIsAlodSPARQLservice.WebIsAlodEndpoint.ALOD_XL_NO_PROXY);
        assertFalse(service.isURIinDictionary("http://webisa.webdatacommons.org/concept/option_contract_"));
        assertFalse(service.isURIinDictionary("<http://webisa.webdatacommons.org/concept/option_contract_>"));
        assertTrue(service.isURIinDictionary("<http://webisa.webdatacommons.org/concept/option%20contract>"));
        assertTrue(service.isURIinDictionary("http://webisa.webdatacommons.org/concept/option%20contract"));

        // service MUST be closed to allow for reinitialization with another endpoint
        service.close();
    }

}