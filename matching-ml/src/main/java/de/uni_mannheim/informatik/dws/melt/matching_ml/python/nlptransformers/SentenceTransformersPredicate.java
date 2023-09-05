package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

public interface SentenceTransformersPredicate {
    
    public void init(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters);
    
    public boolean keepSourceEntity(OntResource r);
    
    public boolean keepTargetEntity(OntResource r);
}
