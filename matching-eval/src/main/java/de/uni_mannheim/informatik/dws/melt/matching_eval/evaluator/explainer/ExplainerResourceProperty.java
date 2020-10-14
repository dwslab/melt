package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer;

import de.uni_mannheim.informatik.dws.melt.matching_base.IExplainerResource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util.PrefixLookup;

import java.util.*;
import java.util.Map.Entry;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple {@link IExplainerResource} which is capable of retrieving properties for given resources.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class ExplainerResourceProperty implements IExplainerResourceWithJenaOntology {

    /**
     * Data structure that holds the mapping of names to RDF properties.
     */
    private Map<String, Property> properties;

    /**
     * Names of the resource features in a list. Not that there is an order.
     */
    private ArrayList<String> resourceFeatureNames;
    
    private PrefixLookup uriPrefixLookup;

    /**
     * default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(ExplainerResourceProperty.class);

    
    public ExplainerResourceProperty() {
        this.resourceFeatureNames = new ArrayList<>();
        this.properties = new HashMap<>();
        this.uriPrefixLookup = PrefixLookup.DEFAULT;
    }
    
    /**
     * Constructor
     * @param properties Desired RDF properties together with their "feature names" in the order you desire.
     */
    public ExplainerResourceProperty(List<NamePropertyTuple> properties) {
        this();
        for (NamePropertyTuple npt : properties) {
            add(npt.name, npt.property);
        }
    }
        
    
    /**
     * Use the given properties and choose the local name of the proeprty as the name.
     * @param properties Desired properties
     */
    public ExplainerResourceProperty(Property... properties) {
        this();
        for (Property p : properties) {
            String propertyName = p.getLocalName();
            if(propertyName.length() > 1){
                //make first letter uppercase:
                propertyName = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            }
            add(propertyName, p);
        }
    }
    

    /**
     * Add an RDF feature.
     * @param name Name of the RDF feature.
     * @param property RDF property that shall be used to describe a resource.
     */
    public void add(String name, Property property){
        this.resourceFeatureNames.add(name);
        this.properties.put(name, property);
    }


    /**
     * Retrieves the features for the given URI.
     *
     * @param uri the resource uri
     * @return A map with key = featureName and value = featureValue.
     */
    @Override
    public Map<String, String> getResourceFeatures(String uri) {
        Map<String, String> result = new HashMap<>();

        if(uri == null){
            LOGGER.debug("Given URI is null. Returning feature set with empty values.");
            for (Entry<String, Property> property : properties.entrySet()) {
                result.put(property.getKey(), "[]");
            }
            return result;
        }

        OntResource resource = ontModel.getOntResource(uri);

        if (resource == null) {
            LOGGER.debug("Could not find resource with uri: " + uri + ". Returning feature set with empty values.");
            for (Entry<String, Property> property : properties.entrySet()) {
                result.put(property.getKey(), "[]");
            }
            return result;
        }

        for (Entry<String, Property> property : properties.entrySet()) {
            StmtIterator iter = resource.listProperties(property.getValue());
            List<String> propertyValueList = new ArrayList<>();
            while (iter.hasNext()) {
                RDFNode n = iter.nextStatement().getObject();
                if(n.isLiteral()){
                    propertyValueList.add("\"" + n.asLiteral().getLexicalForm() + "\"");
                }else if(n.isURIResource()){
                    propertyValueList.add("\"" + this.uriPrefixLookup.getPrefix(n.asResource().getURI()) + "\"");
                }else{
                    propertyValueList.add("\"" + n.asResource().toString() + "\"");
                }
            }
            Collections.sort(propertyValueList);
            String jsonArray = "[" + String.join(",", propertyValueList) + "]";
            result.put(property.getKey(), jsonArray);
        }
        return result;
    }

    @Override
    public ArrayList<String> getResourceFeatureNames() {
        return this.resourceFeatureNames;
    }

    /**
     * The ontModel that is used to explain resources.
     */
    private OntModel ontModel;

    @Override
    public void setOntModel(OntModel ontModel) {
        this.ontModel = ontModel;
    }

    public PrefixLookup getUriPrefixLookup() {
        return uriPrefixLookup;
    }

    public void setUriPrefixLookup(PrefixLookup uriPrefixLookup) {
        this.uriPrefixLookup = uriPrefixLookup;
    }
    
    
}
