package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.elementlevel;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.util.HashMap;
import java.util.Properties;

/**
 * A very basic string matcher that can be used as baseline for matchers.
 */
public class BaselineStringMatcher extends MatcherYAAAJena {

    Alignment alignment;
    OntModel ontology1;
    OntModel ontology2;

    @Override
    public Alignment match(OntModel sourceOntology, OntModel targetOntology, Alignment alignment, Properties properties) throws Exception {
        this.alignment = new Alignment();
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
        match(ontology1.listClasses(), ontology2.listClasses());
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties());
        match(ontology1.listObjectProperties(), ontology2.listObjectProperties());
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties());
        match(ontology1.listIndividuals(), ontology2.listIndividuals());
        return this.alignment;
    }

    private void match(ExtendedIterator<? extends OntResource> resourceIterator1,
                       ExtendedIterator<? extends OntResource> resourceIterator2) {

        HashMap<BagOfWords, String> labelToURI_1 = new HashMap<>();

        while (resourceIterator1.hasNext()) {
            OntResource r1 = resourceIterator1.next();

            // String processing
            BagOfWords label = normalize(getLabelOrFragment(r1));
            if (label != null) {
                labelToURI_1.put(label, r1.getURI());
            }
        }

        while (resourceIterator2.hasNext()) {
            OntResource r2 = resourceIterator2.next();
            BagOfWords label2 = normalize(getLabelOrFragment(r2));
            if (label2 != null && labelToURI_1.containsKey(label2)) {
                if (labelToURI_1.get(label2) != null) {
                    alignment.add(labelToURI_1.get(label2), r2.getURI());
                }
            }
        }
    }


    /**
     * Normalizes a string and returns a bag of words.
     * @param stringToBeNormalized The string that shall be normalized.
     * @return bag of words.
     */
    public static BagOfWords normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        stringToBeNormalized = stringToBeNormalized.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace(" ", "_");
        stringToBeNormalized = stringToBeNormalized.toLowerCase();

        // delete non alpha-numeric characters:
        stringToBeNormalized = stringToBeNormalized.replaceAll("[^a-zA-Z\\d\\s:_]", ""); // regex: [^a-zA-Z\d\s:]

        return new BagOfWords(stringToBeNormalized.split("_"));
    }


    /**
     * Returns the label. If it does not exist: local name.
     *
     * @param resource The resource for which a string shall be retrieved.
     * @return Label or local name. Null if resource is anonymous.
     */
    public static String getLabelOrFragment(OntResource resource) {
        if (resource.isAnon()) {
            return null;
        }
        ExtendedIterator<RDFNode> iterator = resource.listLabels(null);
        while (iterator.hasNext()) {
            RDFNode node = iterator.next();
            return node.asLiteral().toString();
        }
        // no label found: return local name
        return resource.getLocalName();
    }
}
