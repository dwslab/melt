package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.IExplainerResource;

import java.util.*;
import java.util.Map.Entry;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util.PrefixLookup;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
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

    /**
     * default logger
     */
    private static Logger LOGGER = LoggerFactory.getLogger(ExplainerResourceProperty.class);

    /**
     * Constructor
     *
     * @param properties Desired RDF properties together with their "feature names" in the order you desire.
     */
    public ExplainerResourceProperty(ArrayList<NamePropertyTuple> properties) {
        this.resourceFeatureNames = new ArrayList<>();
        this.properties = new HashMap<>();
        for (NamePropertyTuple npt : properties) {
            resourceFeatureNames.add(npt.name);
            this.properties.put(npt.name, npt.property);
        }
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
            StringJoiner joiner = new StringJoiner(",");
            StmtIterator iter = resource.listProperties(property.getValue());
            List<String> propertyValueList = new ArrayList<>();
            while (iter.hasNext()) {
                propertyValueList.add(iter.nextStatement().getObject().toString());
            }
            Collections.sort(propertyValueList);

            for(String string : propertyValueList){
                if(property.getValue().equals(RDF.type)) {
                    joiner.add(PrefixLookup.getPrefix(string));
                } else joiner.add(string);
            }

            String jsonArray = "[" + joiner.toString() + "]";
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
}
