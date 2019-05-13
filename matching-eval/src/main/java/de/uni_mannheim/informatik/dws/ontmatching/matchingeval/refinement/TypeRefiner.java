package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.refinement;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ResourceType;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;

import java.util.Objects;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type refiner is capable of refining an {@link ExecutionResult} according to types.
 * The implemented types are those of {@link ResourceType}.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class TypeRefiner implements Refiner {    
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeRefiner.class);

    /**
     * The ResourceType as defined for the refinement.
     */
    private ResourceType type;

    /**
     * Indicates whether all different property types like DATATYPE_PROPERTY, OBJECT_PROPERTY, ANNOTATION_PROPERTY,
     * and RDF_PROPERTY shall be subsumed.
     */
    private boolean subsumeProperties;

    /**
     * Constructor
     * @param type The type that shall be used for a refinement.
     * @param subsumeProperties Indicator whether property subsumption shall be enabled. Type subsumption is implemented
     *                          in {@link ResourceType} and  means that all different property types like
     *                          DATATYPE_PROPERTY, OBJECT_PROPERTY, ANNOTATION_PROPERTY, and RDF_PROPERTY are subsumed.
     */
    public TypeRefiner(ResourceType type, boolean subsumeProperties) {
        this.type = type;
        this.subsumeProperties = subsumeProperties;
    }

    /**
     * Constructor, subsumption of properties is assumed to be true by default.
     * @param type The type that shall be used for a refinement.
     */
    public TypeRefiner(ResourceType type) {
        this(type, true);
    }
    
    @Override
    public ExecutionResult refine(ExecutionResult toBeRefined) {
        OntModel sourceOntModel = toBeRefined.getSourceOntology(OntModel.class);
        OntModel targetOntModel = toBeRefined.getTargetOntology(OntModel.class);
        
        Alignment refinedSystem = refineMapping(sourceOntModel, targetOntModel, toBeRefined.getSystemAlignment());
        Alignment refinedReference = refineMapping(sourceOntModel, targetOntModel, toBeRefined.getReferenceAlignment());
       
        return new ExecutionResult(toBeRefined, refinedSystem, refinedReference, this);
    }


    /**
     * Perform the actual refinement.
     * @param source The source ontology model.
     * @param target The target ontology model.
     * @param originalAlignment The original mapping before the refinement action.
     * @return The refinde mapping.
     */
    protected Alignment refineMapping(OntModel source, OntModel target, Alignment originalAlignment){
        Alignment refinedAlignment = new Alignment();
        for(Correspondence correspondence : originalAlignment){
            ResourceType sourceType = getResourceType(source, correspondence.getEntityOne());
            if(sourceType == type){
                refinedAlignment.add(correspondence);
            } else if(sourceType == ResourceType.UNKNOWN){
                //try on entity two
                ResourceType targetType = getResourceType(target, correspondence.getEntityTwo());
                if(targetType == type){
                    refinedAlignment.add(correspondence);
                } else if(targetType == ResourceType.UNKNOWN){
                    LOGGER.warn("Could not determine resource typ from source nor target: " + correspondence.toString() +
                            " (Source Ontology: " + originalAlignment.getOnto1().getOntoID() +  "; Target Ontology: " + originalAlignment.getOnto2().getOntoID() + "). " +
                            "Ignore it silently. Have a look at the Alignment - some URIs might be wrong and do not appear in the ontology or the reasoner is not expressive enough.");
                }
            }
        }
        return refinedAlignment;
    }


    /**
     * Obtain the resource type for a particular URI.
     * @param model The model containing the resource.
     * @param resourceURI The URI of the resource for which the type shall be determined.
     * @return The resource type.
     */
    protected ResourceType getResourceType(Model model, String resourceURI){
        ResourceType t = ResourceType.analyze(model, resourceURI);
        if(this.subsumeProperties)
            return ResourceType.subsumeProperties(t);
        return t;
    }
    

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.type);
        hash = 71 * hash + (this.subsumeProperties ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TypeRefiner other = (TypeRefiner) obj;
        if (this.subsumeProperties != other.subsumeProperties) {
            return false;
        }
        if (this.type != other.type) {
            return false;
        }
        return true;
    }
    
}
