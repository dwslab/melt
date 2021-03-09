package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.BagOfWords;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.io.IOoperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.BackgroundMatcherTools.getURIlabelMap;

/**
 * A relatively simple matcher that can be used before running {@link BackgroundMatcher} to filter out simple matches.
 */
public class SimpleStringMatcher extends MatcherYAAAJena {


    private static Logger LOGGER = LoggerFactory.getLogger(SimpleStringMatcher.class);

    private OntModel ontology1;
    private OntModel ontology2;

    /**
     * The value extractor used to obtain labels for resources.
     */
    private ValueExtractor valueExtractor = new ValueExtractorAllAnnotationProperties();

    /**
     * Alignment
     */
    private Alignment mapping = new Alignment();

    /**
     * Tracks clashed labels.
     */
    private HashMap<String, HashSet<String>> clashedLabels;

    @Override
    public Alignment match(OntModel sourceOntology, OntModel targetOntology, Alignment m, Properties p) throws Exception {
        initializeMappingProcess();
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
        match(ontology1.listClasses(), ontology2.listClasses());
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties());
        match(ontology1.listObjectProperties(), ontology2.listObjectProperties());
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties());
        LOGGER.info("Mapping Completed");
        return this.mapping;
    }

    /**
     * maps from URI -&gt; set&lt;label&gt;
     */
    private Map<String, Set<String>> uri2labelMap_1;

    /**
     * maps from URI -&gt; set&lt;label&gt;
     */
    private Map<String, Set<String>> uri2labelMap_2;

    /**
     * Log every match. Do not use in performance optimized settings.
     */
    private boolean isVerboseLoggingOutput = true;

    /**
     * (Re-)initialize data structures.
     */
    private void initializeMappingProcess() {
        LOGGER.info("Initialize Mapping");
        mapping = new Alignment();
        clashedLabels = new HashMap<>();
        uri2labelMap_1 = new HashMap<>();
        uri2labelMap_2 = new HashMap<>();
    }

    /**
     * Given two iterators, match the resources covered by them.
     *
     * @param sourceOntologyIterator_1 iterator 1 must be that of the source ontology
     * @param targetOntologyIterator_2 iterator 2 must be that of the target ontology
     */
    private void match(ExtendedIterator<? extends OntResource> sourceOntologyIterator_1,
                       ExtendedIterator<? extends OntResource> targetOntologyIterator_2) {

        if (sourceOntologyIterator_1 == null || targetOntologyIterator_2 == null) {
            LOGGER.info("One of the iterators is null. match() will not be executed.");
            return;
        }

        // step 0: get all the label data for the two Resource Iterators
        uri2labelMap_1 = getURIlabelMap(sourceOntologyIterator_1, valueExtractor);
        uri2labelMap_2 = getURIlabelMap(targetOntologyIterator_2, valueExtractor);

        // step 1: filter out simple string matches
        LOGGER.info("Beginning simple matching.");
        performSimpleMatching(uri2labelMap_1, uri2labelMap_2);
        LOGGER.info("Simple matching performed.");
    }

    /**
     * Filter out simple string matches utilizing an index strategy.
     *
     * @param uri2labelMap_1 URI 2 labels for the source ontology.
     * @param uri2labelMap_2 URI 2 labels for the target ontology.
     */
    private void performSimpleMatching(Map<String, Set<String>> uri2labelMap_1,
                                       Map<String, Set<String>> uri2labelMap_2) {
        HashMap<BagOfWords, String> labelToURI_1 = convertToBOW2URI(uri2labelMap_1);
        HashMap<BagOfWords, String> labelToURI_2 = convertToBOW2URI(uri2labelMap_2);
        for (BagOfWords currentBOW : labelToURI_1.keySet()) {
            if (labelToURI_2.containsKey(currentBOW)) {
                // a match has been found
                if (clashedLabels.containsKey(labelToURI_1.get(currentBOW)) || clashedLabels.containsKey(labelToURI_2.get(currentBOW))) {
                    // there is a HashCollision
                    mapToMany(labelToURI_1.get(currentBOW), labelToURI_2.get(currentBOW));
                } else {
                    // default case
                    String uri_1 = labelToURI_1.get(currentBOW);
                    String uri_2 = labelToURI_2.get(currentBOW);
                    HashMap<String, Object> extensions = new HashMap<>();
                    extensions.put("http://custom#addedInStep", "simple match");
                    mapping.add(uri_1, uri_2, 1.0, CorrespondenceRelation.EQUIVALENCE, extensions);
                    if (isVerboseLoggingOutput) {
                        LOGGER.info(uri_1 + " " + uri_2 + " (simple match)");
                        LOGGER.info(uri_1 + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_1.get(uri_1)) + ")");
                        LOGGER.info(uri_2 + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_2.get(uri_2)) + ")");
                    }
                }
            } // end check if label also exists in ontology 2
        } // end of loop over ontology 1 labels
    }

    /**
     * Adds a clashed label to the corresponding map.
     *
     * @param uri1 URI part 1.
     * @param uri2 URI part 2.
     */
    private void addClashedLabel(String uri1, String uri2) {
        if (clashedLabels.get(uri1) == null) {
            HashSet<String> clashedLabels1 = new HashSet<>();
            clashedLabels1.add(uri2);
            clashedLabels.put(uri1, clashedLabels1);
        } else {
            clashedLabels.get(uri1).add(uri2);
        }
        if (clashedLabels.get(uri2) == null) {
            HashSet<String> clashedLabels2 = new HashSet<>();
            clashedLabels2.add(uri1);
            clashedLabels.put(uri2, clashedLabels2);
        } else {
            clashedLabels.get(uri2).add(uri1);
        }
    }

    /**
     * If the applied comparison score cannot differentiate between multiple mapping options, add all.
     *
     * @param sourceURI Source URI
     * @param targetURI Target URI
     */
    private void mapToMany(String sourceURI, String targetURI) {

        HashSet<String> sourceURIs = new HashSet<>();
        HashSet<String> targetURIs = new HashSet<>();

        // add original URIs
        sourceURIs.add(sourceURI);
        targetURIs.add(targetURI);

        // add clashed URIs
        if (clashedLabels.get(sourceURI) != null) sourceURIs.addAll(clashedLabels.get(sourceURI));
        if (clashedLabels.get(targetURI) != null) targetURIs.addAll(clashedLabels.get(targetURI));

        for (String source : sourceURIs) {
            for (String target : targetURIs) {
                Map<String, Object> extensions = new HashMap<>();
                extensions.put("http://custom#addedInStep", "mapToMany()");
                mapping.add(source, target, 1.0, CorrespondenceRelation.EQUIVALENCE, extensions);
                if (isVerboseLoggingOutput) {
                    LOGGER.info(source + " " + target + " (simple match, clashed)");
                    LOGGER.info(source + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_1.get(source)) + ")");
                    LOGGER.info(target + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_2.get(target)) + ")");
                }
            }
        }
    }

    /**
     * Convert a {@code HashMap<String, HashSet<String>>} to a {@code HashMap<BagOfWords, String>}.
     * This is required for very simple String matchers.
     *
     * @param uri2labels Input HashMap.
     * @return Converted HashMap.
     */
    private HashMap<BagOfWords, String> convertToBOW2URI(Map<String, Set<String>> uri2labels) {
        HashMap<BagOfWords, String> result = new HashMap<>();

        for (HashMap.Entry<String, Set<String>> uri2labelEntry : uri2labels.entrySet()) {
            for (String label : uri2labelEntry.getValue()) {
                String previousValue = result.put(normalize(label), uri2labelEntry.getKey());
                // DEBUG: just a test whether there is a collapse (can be deleted in high-performance scenario)
                if (previousValue != null && !previousValue.equals(uri2labelEntry.getKey())) {
                    // -> there was already an entry for that particular BOW that was overwritten and was originally pointing to another entity
                    LOGGER.warn("Critical Name Collapse: " + previousValue + " / " + uri2labelEntry.getKey() + "   on label: " + label);
                    addClashedLabel(previousValue, uri2labelEntry.getKey());
                }
            }
        }
        return result;
    }

    /**
     * Normalizes a string. Recognizes camelCase.
     *
     * @param stringToBeNormalized The String that shall be normalized.
     * @return Bag of Words
     */
    static BagOfWords normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        if (stringToBeNormalized.length() > 3 && stringToBeNormalized.charAt(stringToBeNormalized.length() - 3) == '@') {
            stringToBeNormalized = stringToBeNormalized.substring(0, stringToBeNormalized.length() - 3);
        }
        stringToBeNormalized = stringToBeNormalized.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace(" ", "_");
        stringToBeNormalized = stringToBeNormalized.toLowerCase();

        // delete non alpha-numeric characters:
        stringToBeNormalized = stringToBeNormalized.replaceAll("[^a-zA-Z\\d\\s:_]", "_"); // regex: [^a-zA-Z\d\s:]

        stringToBeNormalized = StringOperations.removeEnglishGenitiveS(stringToBeNormalized);

        String[] tokenized = stringToBeNormalized.split("_");
        String[] tokenizedNoStopwords = StringOperations.clearArrayFromStopwords(tokenized);

        if (tokenizedNoStopwords == null || tokenizedNoStopwords.length == 0) {
            // token is made up of stopwords
            // return stopword string rather than nothing
            return new BagOfWords(tokenized);
        }
        return new BagOfWords(tokenizedNoStopwords);
    }

    public boolean isVerboseLoggingOutput() {
        return isVerboseLoggingOutput;
    }

    public void setVerboseLoggingOutput(boolean verboseLoggingOutput) {
        isVerboseLoggingOutput = verboseLoggingOutput;
    }
}
