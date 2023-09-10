package de.uni_mannheim.informatik.dws.melt.examples.llm_transformers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BadHostsFilter;
import de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.SentenceTransformersPredicate;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

public class SentenceTransformersPredicateBadHosts implements SentenceTransformersPredicate {
    
    private String sourceHost;
    private String targetHost;
    
    public SentenceTransformersPredicateBadHosts(){
        this.sourceHost = "";
        this.targetHost = "";
    }
    public void init(OntModel source, OntModel target, Alignment inputAlignment, Properties parameters){
        this.sourceHost = BadHostsFilter.getHostURIOfModelBySampling(source);
        this.targetHost = BadHostsFilter.getHostURIOfModelBySampling(target);
    }
    
    @Override
    public boolean keepSourceEntity(OntResource r){
        return checkResource(r, this.sourceHost);
    }
    
    @Override
    public boolean keepTargetEntity(OntResource r){
        return checkResource(r, this.targetHost);
    }
    
    private boolean checkResource(OntResource r, String hostURI){
        String uri = r.getURI();
        if(uri == null)
            return false;
        String host = BadHostsFilter.getHostOfURI(uri);
        if(host == null || host.isEmpty())
            return true; // do not filter -> keep it
        if(hostURI.equals(host)){
            return true; // do not filter -> keep it
        }else{
            return false; // filter it out
        }
    }
}
