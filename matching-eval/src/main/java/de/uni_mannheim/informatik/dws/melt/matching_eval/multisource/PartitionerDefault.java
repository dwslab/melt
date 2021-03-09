package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * 
 */
public class PartitionerDefault implements Partitioner{
    
    private Track track;

    public PartitionerDefault(Track track) {
        this.track = track;
    }
    
    /**
     * This method will load each ontology to check if a URI is contained.
     * @param uris the URIs to check
     * @return a map of testcase to source and target URIs
     */
    @Override
    public Map<TestCase, SourceTargetURIs> partition(Collection<String> uris) {
        Map<TestCase, SourceTargetURIs> map = new HashMap<>();
        
        //make list of resources
        List<Resource> uriResources = new ArrayList<>(uris.size());
        for(String uri : uris){
            uriResources.add(ResourceFactory.createResource(uri));
        }

        for(TestCase tc : track.getTestCases()){
            SourceTargetURIs sourceTarget = map.computeIfAbsent(tc, __-> new SourceTargetURIs());
            
            OntModel source = tc.getSourceOntology(OntModel.class);
            for(Resource r : uriResources){
                if(source.containsResource(r)){
                    sourceTarget.addSourceURI(r.getURI());
                }
            }
            
            OntModel target = tc.getTargetOntology(OntModel.class);
            for(Resource r : uriResources){
                if(target.containsResource(r)){
                    sourceTarget.addTargetURI(r.getURI());
                }
            }
        }
        return map;
    }
}
