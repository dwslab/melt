package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class AdditionalConfidenceByFunctionTest {
    
    @Test
    void testConfidenceCombiner() throws Exception {
        
        OntModel source = ModelFactory.createOntologyModel();
        source.createIndividual("http://source.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "This is a test")
                .addLiteral(RDFS.label, "Smaller");
        
        source.createIndividual("http://source.de/two", OWL.Thing)
                .addLiteral(RDFS.label, "Two");
        
        
        OntModel target = ModelFactory.createOntologyModel();
        target.createIndividual("http://target.de/one", OWL.Thing)
                .addLiteral(RDFS.label, "FooBar");
        
        Alignment alignment = new Alignment();
        alignment.add("http://source.de/one", "http://target.de/one");
        alignment.add("http://source.de/two", "http://target.de/one");
        
        AdditionalConfidenceByFunction matcher = new AdditionalConfidenceByFunction("LabelMaxLength", AdditionalConfidenceByFunction.MAX_LABEL_LENGTH);
        Alignment modified = matcher.match(source, target, alignment, new Properties());
        
        Correspondence first = modified.getCorrespondence("http://source.de/one", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE);
        Correspondence second = modified.getCorrespondence("http://source.de/two", "http://target.de/one", CorrespondenceRelation.EQUIVALENCE);
        
        assertEquals(14.0, first.getAdditionalConfidence("LabelMaxLength_Left"));
        assertEquals(6.0, first.getAdditionalConfidence("LabelMaxLength_Right"));
        
        assertEquals(3.0, second.getAdditionalConfidence("LabelMaxLength_Left"));
        assertEquals(6.0, second.getAdditionalConfidence("LabelMaxLength_Right"));
    }
}
