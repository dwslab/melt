package de.uni_mannheim.informatik.dws.melt.matching_eval;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enum represents different resource types that may occur in an ontology.
 * In addition, it offers services to determine the resource type given a model and a URI.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 * @deprecated better use {@link ConceptType} which has the same functionality.
 */
public enum ResourceType {


    CLASS,
    RDF_PROPERTY,
    DATATYPE_PROPERTY,
    OBJECT_PROPERTY,
    ANNOTATION_PROPERTY,
    INSTANCE,
    UNKNOWN;

    /**
     * Default Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceType.class);

    
    private static Node[] classTypes = new Node[] {
        OWL.Class.asNode(), OWL.Restriction.asNode(), RDFS.Class.asNode(), RDFS.Datatype.asNode()
    };    
    private static Node[] objectPropertyTypes = new Node[] {
        OWL.ObjectProperty.asNode(), 
        OWL.TransitiveProperty.asNode(), OWL.SymmetricProperty.asNode(), OWL.InverseFunctionalProperty.asNode(),
        OWL2.AsymmetricProperty.asNode(),OWL2.IrreflexiveProperty.asNode(), OWL2.ReflexiveProperty.asNode()
    };
    private static Node[] rdfPropertyTypes = new Node[] {
        RDF.Property.asNode(), 
        OWL.ObjectProperty.asNode(), OWL.DatatypeProperty.asNode(),OWL.AnnotationProperty.asNode(), 
        OWL.TransitiveProperty.asNode(),OWL.SymmetricProperty.asNode(), OWL.InverseFunctionalProperty.asNode(),
        OWL.FunctionalProperty.asNode()
    };
    
    //same as m.listAllOntProperties() vs m.listOntProperties()
    
    
    /**
     * Returns the most precise/specific resource type.
     * This method implements its own simplistic reasoning for determining the type of the resource.
     * For more sophisticated reasoning use {@link ResourceType#analyzeWithInference(InfModel, String)} } .
     * @param model any model
     * @param resourceURI The URI for which the resource type shall be determined.
     * @return The most specify resource type available (like ObjectProperty)
     */
    public static ResourceType analyze(Model model, String resourceURI){
        Node n = NodeFactory.createURI( resourceURI );
        Graph g = model.getGraph();
        Set<Node> types = allTypes(n, g);
        
        boolean clazz = intersect(types, classTypes) ||
                n.equals(OWL.Thing.asNode()) || n.equals(OWL.Nothing.asNode()) || n.equals(OWL.Class.asNode()) || n.equals(RDFS.Resource.asNode()) ||
                //from RDF namespace
                g.contains( Node.ANY, RDF.type.asNode(), n ) || //range of rdf:type
                //from RDFS namespace
                g.contains( Node.ANY , RDFS.subClassOf.asNode(), n ) || g.contains( n, RDFS.subClassOf.asNode(), Node.ANY ) || //subclassof - both directions
                g.contains( Node.ANY, RDFS.domain.asNode(), n ) || //domain
                g.contains( Node.ANY, RDFS.range.asNode(), n ) || //range
                //from OWL namespace
                //TODO: owl:AllDisjointClasses
                g.contains( Node.ANY, OWL.allValuesFrom.asNode(), n ) ||
                g.contains( Node.ANY, OWL.complementOf.asNode(), n ) || g.contains( n, OWL.complementOf.asNode(), Node.ANY ) ||                      
                g.contains( Node.ANY , OWL.disjointWith.asNode(), n ) || g.contains( n, OWL.disjointWith.asNode(), Node.ANY ) ||
                g.contains( n, OWL.equivalentClass.asNode(), Node.ANY ) || g.contains( Node.ANY , OWL.equivalentClass.asNode(), n ) ||
                g.contains( n, OWL.intersectionOf.asNode(), Node.ANY ) ||
                g.contains( Node.ANY, OWL.someValuesFrom.asNode(), n ) || 
                g.contains( n, OWL.oneOf.asNode(), Node.ANY ) ||                
                g.contains( n, OWL.unionOf.asNode(), Node.ANY ) ||
                //from OWL2 namespace
                g.contains( n, OWL2.disjointUnionOf.asNode(), Node.ANY ) ||
                g.contains( n, OWL2.hasKey.asNode(), Node.ANY ) ||
                g.contains( Node.ANY, OWL2.onClass.asNode(), n );

        if(clazz)
            return CLASS;
        
        
        boolean datatypeProperty = types.contains(OWL.DatatypeProperty.asNode());
        if(datatypeProperty)
            return DATATYPE_PROPERTY;
        
        boolean objectProperty = intersect(types, objectPropertyTypes) || 
                g.contains( n, OWL.inverseOf.asNode(), Node.ANY ) || g.contains( Node.ANY , OWL.inverseOf.asNode(), n );
        if(objectProperty)
            return OBJECT_PROPERTY;
                
        boolean annotationProperty = 
                //built-in annotation property
                n.equals(OWL.versionInfo.asNode()) || n.equals(RDFS.label.asNode()) || n.equals(RDFS.seeAlso.asNode()) ||
                n.equals(RDFS.comment.asNode()) || n.equals(RDFS.isDefinedBy.asNode()) ||
                types.contains(OWL.AnnotationProperty.asNode());
        if(annotationProperty)
            return ANNOTATION_PROPERTY;
        
        
        boolean rdfProperty = intersect(types, rdfPropertyTypes) || g.contains( Node.ANY, n, Node.ANY ) ||
                g.contains(Node.ANY , RDFS.subPropertyOf.asNode() , n ) || g.contains( n, RDFS.subPropertyOf.asNode() , Node.ANY ) ||
                g.contains( n, RDFS.domain.asNode(), Node.ANY ) || //domain
                g.contains( n, RDFS.range.asNode(), Node.ANY ) || //range                
                g.contains( Node.ANY , OWL.equivalentProperty.asNode(), n ) || g.contains( n , OWL.equivalentProperty.asNode(), Node.ANY ) ||
                g.contains( Node.ANY , OWL.onProperty.asNode(), n ) ||
                //OWL2 namepsace
                g.contains( Node.ANY , OWL2.assertionProperty.asNode(), n ) ||
                g.contains( Node.ANY , OWL2.propertyDisjointWith.asNode(), n ) || g.contains( n , OWL2.propertyDisjointWith.asNode(), Node.ANY );
        
        if(rdfProperty)
            return RDF_PROPERTY;
        
        if(g.contains( n, Node.ANY, Node.ANY ) == false && g.contains( Node.ANY, Node.ANY, n ) == false)
            return UNKNOWN;
        
        boolean individual = n.isURI() || n.isBlank();
        if(individual)
           return INSTANCE;
        return UNKNOWN;
    }
    
    private static Set<Node> allTypes( Node n, Graph g) {
        Set<Node> types = new HashSet<>();
        for (ExtendedIterator<Triple> i = g.find( n, RDF.type.asNode(), Node.ANY ); i.hasNext(); ) {
            types.add( i.next().getObject() );
        }
        return types;
    } 
    
    private static boolean intersect(Set<Node> prefetchedTypes, Node[] requestedTypes){
        for (Node typ: requestedTypes) {
            if (prefetchedTypes.contains(typ)) {
                return true;
            }
        }
        return false;
    }
    
    
    /**
     * Anlyze the type of the resource.
     * This heavily depends on the attached reasoner.
     * To have same good results, use at least an rdfs reasoner.
     * @param model any model
     * @param resourceURI The URI for which the resource type shall be determined.
     * @return The resource category of the URI in question.
     */
    public static ResourceType analyzeWithInference(InfModel model, String resourceURI){
        Node n = NodeFactory.createURI( resourceURI );
        Graph g = model.getGraph();
        
        Set<Node> types = allTypes(n, g);
        
        if(types.contains(RDFS.Class.asNode()))
            return ResourceType.CLASS;
        
        if(types.contains(OWL.DatatypeProperty.asNode()))
            return ResourceType.DATATYPE_PROPERTY;
        
        if(types.contains(OWL.ObjectProperty.asNode()))
            return ResourceType.OBJECT_PROPERTY;

        if(types.contains(OWL.AnnotationProperty.asNode()))
            return ResourceType.ANNOTATION_PROPERTY;
        
        if(types.contains(RDF.Property.asNode()))
            return ResourceType.RDF_PROPERTY;

        if(g.contains( n, Node.ANY, Node.ANY ) == false && g.contains( Node.ANY, Node.ANY, n ) == false)
            return UNKNOWN;
        
        if(n.isURI() || n.isBlank())
           return INSTANCE;  
        
        return ResourceType.UNKNOWN;
    }
    
    /**
     * This method returns only CLASS, RDF_PROPERTY, INSTANCE or UNKNOWN and, thus, subsumes all different property types like
     * DATATYPE_PROPERTY, OBJECT_PROPERTY, ANNOTATION_PROPERTY, and RDF_PROPERTY.
     * @param resourceType The resource type for which the parent shall be returned.
     * @return The parent type. If there is no parent, the resource type itself will be returned.
     */
    public static ResourceType subsumeProperties(ResourceType resourceType){
        if(resourceType == DATATYPE_PROPERTY) {
            return ResourceType.RDF_PROPERTY;
    	} else if(resourceType == OBJECT_PROPERTY) {
            return ResourceType.RDF_PROPERTY;
    	} else if(resourceType == ANNOTATION_PROPERTY) {
            return ResourceType.RDF_PROPERTY;
    	}else{
            return resourceType;
        }
    }
}
