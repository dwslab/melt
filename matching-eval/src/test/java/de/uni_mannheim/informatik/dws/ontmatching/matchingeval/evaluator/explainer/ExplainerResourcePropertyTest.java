package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer;

import org.apache.jena.vocabulary.RDFS;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ExplainerResourcePropertyTest {

    @Test
    void getResourceFeatureNames() {
        ArrayList<NamePropertyTuple> properties = new ArrayList<>();
        properties.add(new NamePropertyTuple("label", RDFS.label));
        properties.add(new NamePropertyTuple("comment", RDFS.comment));
        ExplainerResourceProperty explainer = new ExplainerResourceProperty(properties);
        ArrayList<String> featureNames = explainer.getResourceFeatureNames();

        // make sure the components are correct
        assertTrue(featureNames.size() == 2);
        assertTrue(featureNames.contains("label"));
        assertTrue(featureNames.contains("comment"));

        // make sure the order is correct
        assertTrue(featureNames.get(0).equals("label"));
        assertTrue(featureNames.get(1).equals("comment"));

    }
}