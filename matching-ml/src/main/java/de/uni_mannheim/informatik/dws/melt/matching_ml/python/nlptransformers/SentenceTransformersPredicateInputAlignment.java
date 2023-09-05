package de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

public class SentenceTransformersPredicateInputAlignment implements SentenceTransformersPredicate {
    
    private Set<String> sourceURLs;
    private Set<String> targetURLs;
    
    public SentenceTransformersPredicateInputAlignment(){
        this.sourceURLs = new HashSet<>();
        this.targetURLs = new HashSet<>();
    }
    public void init(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters){
        this.sourceURLs = inputAlignment.getDistinctSourcesAsSet();
        this.targetURLs = inputAlignment.getDistinctTargetsAsSet();
    }
    
    @Override
    public boolean keepSourceEntity(OntResource r){
        String uri = r.getURI();
        if(uri == null)
            return false;
        return !sourceURLs.contains(uri);
    }
    
    @Override
    public boolean keepTargetEntity(OntResource r){
        String uri = r.getURI();
        if(uri == null)
            return false;
        return !targetURLs.contains(uri);
    }
}
