package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * Configuration objetc for {@link BoundedPathMatching } class.
 * There are static methods which creates preconfigured elements such as class or property hierarchy.
 */
public abstract class BoundedPathMatchingConfiguration {
    
    private int maxIntermediateNodes;
    
    public abstract boolean isOfInterest(OntResource source, OntResource target);
    
    public abstract Iterable<Resource> getSuccesors(Resource r);

    public BoundedPathMatchingConfiguration(int maxIntermediateNodes) {
        this.maxIntermediateNodes = maxIntermediateNodes;
    }
    
    public int getMaxIntermediateNodes() {
        return maxIntermediateNodes;
    }

    public void setMaxIntermediateNodes(int maxIntermediateNodes) {
        this.maxIntermediateNodes = maxIntermediateNodes;
    }
    
    
    public static BoundedPathMatchingConfiguration createClassHierarchyConfiguration(){
        return createClassHierarchyConfiguration(1);
    }
    
    public static BoundedPathMatchingConfiguration createClassHierarchyConfiguration(int maxIntermediateNodes){
        return new BoundedPathMatchingConfiguration(maxIntermediateNodes) {
            @Override
            public boolean isOfInterest(OntResource source, OntResource target) {
                return source.isClass() && target.isClass();
            }

            @Override
            public Iterable<Resource> getSuccesors(Resource r) {
                Set<Resource> hierarchy = new HashSet<>();
                StmtIterator i = r.listProperties(RDFS.subClassOf);
                while(i.hasNext()){
                    RDFNode next = i.next().getObject();
                    if(next.isResource()){
                        hierarchy.add(next.asResource());
                    }
                }
                return hierarchy;
            }
        };    
    }
    
    public static BoundedPathMatchingConfiguration createPropertyHierarchyConfiguration(){
        return createPropertyHierarchyConfiguration(1);
    }
    
    public static BoundedPathMatchingConfiguration createPropertyHierarchyConfiguration(int maxIntermediateNodes){
       return new BoundedPathMatchingConfiguration(maxIntermediateNodes) {
            @Override
            public boolean isOfInterest(OntResource source, OntResource target) {
                return source.isProperty() && target.isProperty();
            }

            @Override
            public Iterable<Resource> getSuccesors(Resource r) {
                Set<Resource> hierarchy = new HashSet<>();
                StmtIterator i = r.listProperties(RDFS.subPropertyOf);
                while(i.hasNext()){
                    RDFNode next = i.next().getObject();
                    if(next.isResource()){
                        hierarchy.add(next.asResource());
                    }
                }
                return hierarchy;
            }
        };    
    }
}
