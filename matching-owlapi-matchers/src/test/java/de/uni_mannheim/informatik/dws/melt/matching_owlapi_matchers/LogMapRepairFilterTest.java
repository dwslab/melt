package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers;

import de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers.logmap.LogMapRepairFilter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;

class LogMapRepairFilterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogMapRepairFilterTest.class);    
    
    public static void main(String[] args) throws Exception {
        
        //This "test" does not work when executed in an test environment - don't ask me why....
        //furthermore you need to exclude a dependency from alcomo and choose the right owlapi version:
        /*
        <dependency>
            <groupId>de.uni-mannheim.informatik.dws</groupId>
            <artifactId>alcomo</artifactId>
            <version>1.1</version>
            <exclusions>
                <exclusion>
                    <groupId>com.github.ansell.pellet</groupId>
                    <artifactId>pellet-modularity</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        
        <dependency>
            <groupId>net.sourceforge.owlapi</groupId>
            <artifactId>owlapi-distribution</artifactId>
            <version>4.1.3</version>
        </dependency>
        */
        
        OWLOntologyManager onto_manager = OWLManager.createOWLOntologyManager();
        OWLOntology source = onto_manager.loadOntologyFromOntologyDocument(getInputStream("cmt.owl"));
        OWLOntology target = onto_manager.loadOntologyFromOntologyDocument(getInputStream("ekaw.owl"));
        Alignment systemAlignment = AlignmentParser.parse(getInputStream("csa-cmt-ekaw.rdf"));
        
        Correspondence wrongCorrespondence = new Correspondence("http://cmt#assignedByReviewer", "http://ekaw#writtenBy");
        assertTrue(systemAlignment.contains(wrongCorrespondence));
        
        LogMapRepairFilter filter = new LogMapRepairFilter();            
        Alignment filteredAlignment = filter.match(source, target, systemAlignment, new Properties());
        
        LOGGER.info("alignment before: {} alignment after filtering: {}", systemAlignment.size(), filteredAlignment.size());
        LOGGER.info("alignment contains wrong correspondence (expected false): {}", filteredAlignment.contains(wrongCorrespondence));
    }
    
    private static InputStream getInputStream(String fileName){
        return LogMapRepairFilterTest.class.getClassLoader().getResourceAsStream(fileName);
    }
}