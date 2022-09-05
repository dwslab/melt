package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers.logmap;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.HashSet;
import java.util.Set;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.krr.logmap2.mappings.objects.MappingObjectStr;

/**
 * Converts Alignment to Set of MappingObjectStr.
 */
public class Alignment2LogmapMapping { //extends AbstractTypeTransformer<Alignment, Set<MappingObjectStr>>{

    /*
    public Alignment2LogmapMapping() {
        super(Alignment.class, Set<MappingObjectStr>.class);
    }
    
    @Override
    public Set<MappingObjectStr> transform(Alignment value, Properties parameters) throws TypeTransformationException {
        
        return transformAlignment(value);
    }
    */
    
     
    public static Set<MappingObjectStr> transformAlignment(Alignment alignment){
        Set<MappingObjectStr> mappings = new HashSet<>();        
        for(Correspondence c : alignment){
            mappings.add(
                new MappingObjectStr(
                    c.getEntityOne(),//iri entity 1
                    c.getEntityTwo(),//iri entity 2
                    c.getConfidence(),//Confidence
                    transformRelation(c.getRelation()),//Direction  
                    MappingObjectStr.UNKNOWN//Type
                )
            );
        }
        return mappings;
    }
    
    
    
    public static Set<MappingObjectStr> transformAlignment(Alignment alignment, OWLOntology source, OWLOntology target){
        Set<MappingObjectStr> mappings = new HashSet<>();                
        for(Correspondence c : alignment){
            mappings.add(
                new MappingObjectStr(
                    c.getEntityOne(),//iri entity 1
                    c.getEntityTwo(),//iri entity 2
                    c.getConfidence(),//Confidence
                    transformRelation(c.getRelation()),//Direction  
                    determineType(c, source, target)//Type
                )
            );
        }
        return mappings;
    }
    
    private static int determineType(Correspondence c, OWLOntology source, OWLOntology target){        
        IRI sourceEntity = IRI.create(c.getEntityOne());
        IRI targetEntity = IRI.create(c.getEntityTwo());
        if(source.containsClassInSignature(sourceEntity) && target.containsClassInSignature(targetEntity))
            return MappingObjectStr.CLASSES;
        if(source.containsDataPropertyInSignature(sourceEntity) && target.containsDataPropertyInSignature(targetEntity))
            return MappingObjectStr.DATAPROPERTIES;
        if(source.containsObjectPropertyInSignature(sourceEntity) && target.containsObjectPropertyInSignature(targetEntity))
            return MappingObjectStr.OBJECTPROPERTIES;
        if(source.containsIndividualInSignature(sourceEntity) && target.containsIndividualInSignature(targetEntity))
            return MappingObjectStr.INSTANCES;
        return MappingObjectStr.UNKNOWN;
    }
    
    private static int transformRelation(CorrespondenceRelation rel){
        switch(rel){
            case EQUIVALENCE:
                return MappingObjectStr.EQ;
            case SUBSUMED:
                return MappingObjectStr.SUP; //not sure here...
            case SUBSUME:
                return MappingObjectStr.SUB; //not sure here...
            default:
                return MappingObjectStr.EQ; // do not have any other representations....
        }
    }
}
