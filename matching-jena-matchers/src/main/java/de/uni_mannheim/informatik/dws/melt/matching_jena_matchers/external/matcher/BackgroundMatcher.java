package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.ExternalResourceWithSynonymCapability;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SynonymConfidenceCapability;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.embeddings.GensimEmbeddingModel;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.io.IOoperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorAllAnnotationProperties;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.BackgroundMatcherTools.getURIlabelMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;

/**
 * Template matcher where the background knowledge and the exploitation strategy (represented as {@link ImplementedBackgroundMatchingStrategies}) can be plugged-in.
 * This matcher can be used as matching component. It is sensible to use a simple string matcher before running this
 * matcher to increase the performance by filtering out simple matches. If you want a pre-packaged stand-alone
 * background-based matching system, you can try out {@link BackgroundMatcherStandAlone}.
 * <br>
 * This matcher relies on a similarity metric that is implemented within the background source and used in
 * {@link BackgroundMatcher#compare(String, String)}.
 */
public class BackgroundMatcher extends MatcherYAAAJena {


    /**
     * Linker used to link labels to concepts.
     */
    private final LabelToConceptLinker linker;

    /**
     * Alignment
     */
    private Alignment alignment = new Alignment();

    /**
     * Ontologies
     */
    private OntModel ontology1;
    private OntModel ontology2;

    /**
     * the knowledgeSource to be used
     */
    private ExternalResourceWithSynonymCapability knowledgeSource;

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundMatcher.class);

    /**
     * Matching strategy.
     */
    private ImplementedBackgroundMatchingStrategies strategy;

    /**
     * The minimal confidence threshold that is required for a match.
     */
    private double threshold;

    /**
     * If something has been matched in an earlier step, allow for it to be matched again.
     * Default: false.
     */
    private boolean isAllowForCumulativeMatches = false;

    /**
     * Log every match. Do not use in performance optimized settings.
     */
    private boolean isVerboseLoggingOutput = true;

    /**
     * The value extractor used to obtain labels for resources.
     */
    private final TextExtractor valueExtractor;

    /**
     * If a concept cannot be linked as full string, the longest substrings are matched.
     * This is expensive. If there are lengthy description texts etc., this should not be performed.
     * This variable represents the number of tokens within a label up to which the multi-linking will be performed.
     * The limit is inclusive, linking will not be performed if {@code |tokens in label| > multiConceptLinkerUpperLimit}
     */
    private int multiConceptLinkerUpperLimit = 7;

    /**
     * If true, there is a confidence score for each synonymy relation.
     */
    private final boolean isSynonymyConfidenceAvailable;

    /**
     * Main Constructor
     *
     * @param knowledgeSourceToBeUsed Specify the knowledgeSource to be used.
     * @param strategy                The knowledgeSource strategy that shall be applied.
     * @param threshold               The minimal required threshold that is required for a match.
     */
    public BackgroundMatcher(SemanticWordRelationDictionary knowledgeSourceToBeUsed, ImplementedBackgroundMatchingStrategies strategy, double threshold) {
        this.knowledgeSource = knowledgeSourceToBeUsed;
        this.isSynonymyConfidenceAvailable = this.knowledgeSource instanceof SynonymConfidenceCapability;
        this.linker = this.knowledgeSource.getLinker();
        this.strategy = strategy;
        this.threshold = threshold;
        this.valueExtractor = new TextExtractorAllAnnotationProperties();
    }

    /**
     * Convenience Default Constructor
     * Threshold: 0.0 and Strategy: Synonymy are assumed.
     *
     * @param knowledgeSourceToBeUsed The knowledge source that is to be used.
     */
    public BackgroundMatcher(SemanticWordRelationDictionary knowledgeSourceToBeUsed) {
        this(knowledgeSourceToBeUsed, ImplementedBackgroundMatchingStrategies.SYNONYMY, 0.0);
    }

    /**
     * Convenience Default Constructor
     * Threshold: 0.0 is assumed.
     *
     * @param knowledgeSourceToBeUsed The knowledge source that is to be used.
     * @param strategy                The strategy that shall be applied.
     */
    public BackgroundMatcher(SemanticWordRelationDictionary knowledgeSourceToBeUsed, ImplementedBackgroundMatchingStrategies strategy) {
        this(knowledgeSourceToBeUsed, strategy, 0.0);
    }

    @Override
    public Alignment match(OntModel sourceOntology, OntModel targetOntology, Alignment m, Properties p) throws Exception {
        LOGGER.info("Running BackgroundMatcher with the following configuration:\n" + getConfigurationListing());
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
        if (m != null) {
            this.alignment = m;
        } else {
            this.alignment = new Alignment();
        }

        match(ontology1.listClasses(), ontology2.listClasses());
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties());
        match(ontology1.listObjectProperties(), ontology2.listObjectProperties());
        LOGGER.info("Background Matcher Component: Mapping Completed");
        return this.alignment;
    }

    /**
     * Adds extension values.
     */
    private void addAlignmentExtensions() {
        this.alignment.addExtensionValue("http://a.com/matcherThreshold", "" + threshold);
        this.alignment.addExtensionValue("http://a.com/matcherStrategy", this.strategy.toString());
        if (this.knowledgeSource instanceof GensimEmbeddingModel) {
            this.alignment.addExtensionValue("http://a.com/strategyThreshold", "" + ((GensimEmbeddingModel) this.knowledgeSource).getThreshold());
        }
        this.alignment.addExtensionValue("http://a.com/backgroundDataset", this.knowledgeSource.getName());
    }

    /**
     * Get configuration of matcher as string output.
     *
     * @return The configuration as string.
     */
    private String getConfigurationListing() {
        String result = "- threshold: " + threshold + "\n" +
                "- matcherStrategy: " + this.strategy.toString() + "\n" +
                "- backgroundDataset: " + this.knowledgeSource.getName() + "\n";
        if (this.knowledgeSource instanceof GensimEmbeddingModel) {
            result += "- strategyThreshold: " + ((GensimEmbeddingModel) this.knowledgeSource).getThreshold() + "\n";
        }
        return result;
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
        Map<String, Set<String>> uri2labelMap_1 = getURIlabelMap(sourceOntologyIterator_1, valueExtractor);
        Map<String, Set<String>> uri2labelMap_2 = getURIlabelMap(targetOntologyIterator_2, valueExtractor);

        LOGGER.info("Beginning full string synonymy matching.");
        performFullStringSynonymyMatching(uri2labelMap_1, uri2labelMap_2);
        LOGGER.info("Full string synonymy matching performed.");

        // step 3: (more complex approach) look up long sub-parts in the label
        // issues: what to do with partial mappings, performance...
        LOGGER.info("Beginning longest string synonymy matching.");
        performLongestStringSynonymyMatching(uri2labelMap_1, uri2labelMap_2);
        LOGGER.info("Longest string synonymy matching performed.");

        // step 4:
        LOGGER.info("Beginning token based synonymy matching.");
        performTokenBasedSynonymyMatching(uri2labelMap_1, uri2labelMap_2);
        LOGGER.info("Token based synonymy matching performed.");
    }

    /**
     * Filter out token synonymy utilizing a synonymy strategy.
     * Note that the method accepts a HashMap of Uri -&gt; set(LINKS) rather than Uri -&gt; set(labels).
     *
     * @param uri2labelMap_1 URI2labels map of the source ontology.
     * @param uri2labelMap_2 URI2labels map of the target ontology.
     */
    private void performFullStringSynonymyMatching(Map<String, Set<String>> uri2labelMap_1,
                                                   Map<String, Set<String>> uri2labelMap_2) {
        LOGGER.info("BuildingMap:  Uri -> Link");
        Map<String, Set<String>> uris2linksSource_1 = convertToUriLinkMap(uri2labelMap_1, true);
        Map<String, Set<String>> uris2linksTarget_2 = convertToUriLinkMap(uri2labelMap_2, false);
        LOGGER.info("BuildingMap finished: Uri -> Link Map");

        for (Map.Entry<String, Set<String>> uri2linksSource_1 : uris2linksSource_1.entrySet()) {
            for (Map.Entry<String, Set<String>> uri2linksTarget_2 : uris2linksTarget_2.entrySet()) {
                Pair<Boolean, Double> isMatchConfidencePair =
                        fullMatchUsingDictionaryWithLinks(uri2linksSource_1.getValue(), uri2linksTarget_2.getValue());
                if (isMatchConfidencePair.getValue0()) {
                    Map<String, Object> extensions = new HashMap<>();
                    extensions.put("http://custom#addedInStep", "performFullStringSynonymyMatching()");
                    double confidence = isMatchConfidencePair.getValue1();
                    alignment.add(uri2linksSource_1.getKey(), uri2linksTarget_2.getKey(), confidence,
                            CorrespondenceRelation.EQUIVALENCE, extensions);
                    if (isVerboseLoggingOutput) {
                        LOGGER.info(uri2linksSource_1.getKey() + " " + uri2linksTarget_2.getKey() + " (full word synonymy match)");
                        LOGGER.info(uri2linksSource_1.getKey() + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_1.get(uri2linksSource_1.getKey())) + ")");
                        LOGGER.info(uri2linksTarget_2.getKey() + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_2.get(uri2linksTarget_2.getKey())) + ")");
                    }
                }
            } // end of target ontology loop
        } // end of source ontology loop
    }

    /**
     * Determines whether two sets of links match using the internal knowledgeSource.
     * Not that no linking is performed but links are expected in the sets.
     *
     * @param set1 Set 1 Set of links 1.
     * @param set2 Set 2 Set of links 2.
     * @return Pair where (1) boolean indicating whether there is a match, (2) providing the match confidence.
     */
    public Pair<Boolean, Double> fullMatchUsingDictionaryWithLinks(Set<String> set1, Set<String> set2) {
        for (String s1 : set1) {
            if (s1.length() < 100) {
                for (String s2 : set2) {
                    if (s2.length() < 100) {
                        if (compare(s1, s2)) {
                            if (knowledgeSource instanceof SynonymConfidenceCapability) {
                                return new Pair<>(true,
                                        ((SynonymConfidenceCapability) knowledgeSource).getSynonymyConfidence(s1,
                                                s2));
                            } else {
                                return new Pair<>(true, 1.0);
                            }
                        }
                    }
                }
            }
        }
        return new Pair<>(false, 0.0);
    }


    /**
     * Match based on token equality and synonymy.
     *
     * @param uri2labelMap_1 source uri2labels map
     * @param uri2labelMap_2 target uri2labels map
     */
    private void performTokenBasedSynonymyMatching(Map<String, Set<String>> uri2labelMap_1, Map<String, Set<String>> uri2labelMap_2) {
        LOGGER.info("Beginning to convert to URI -> Tokens map.");
        Map<String, List<Set<String>>> uri2tokensMap_1 = convertToUriTokenMap(uri2labelMap_1, true);
        Map<String, List<Set<String>>> uri2tokensMap_2 = convertToUriTokenMap(uri2labelMap_2, false);
        LOGGER.info("Conversion completed to URI -> Tokens map.");

        for (HashMap.Entry<String, List<Set<String>>> uri2tokenLists_1 : uri2tokensMap_1.entrySet()) {
            for (HashMap.Entry<String, List<Set<String>>> uri2tokenLists_2 : uri2tokensMap_2.entrySet()) {
                Pair<Boolean, Double> isSynonymousConfidencePair = isTokenSetSynonymous(uri2tokenLists_1.getValue(), uri2tokenLists_2.getValue());
                if (isSynonymousConfidencePair.getValue0()) {
                    String uri1 = uri2tokenLists_1.getKey();
                    String uri2 = uri2tokenLists_2.getKey();
                    HashMap<String, Object> extensions = new HashMap<>();
                    extensions.put("http://custom#addedInStep", "performTokenBasedSynonymyMatching()");
                    alignment.add(uri1, uri2, isSynonymousConfidencePair.getValue1(), CorrespondenceRelation.EQUIVALENCE,
                            extensions);
                    if (isVerboseLoggingOutput) {
                        LOGGER.info(uri1 + " " + uri2 + " (token based synonymy match)");
                        LOGGER.info(uri1 + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_1.get(uri1)) + ")");
                        LOGGER.info(uri2 + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_2.get(uri2)) + ")");
                    }
                }
            }
        }
    }

    /**
     * Checks whether the two lists are synonymous, this means that:
     * each component of one list can be found in the other list OR is synonymous to one component in the other list.
     *
     * @param tokenList1 List of words
     * @param tokenList2 List of words
     * @return true if synonymous, else false
     */
    Pair<Boolean, Double> isTokenSetSynonymous(List<Set<String>> tokenList1, List<Set<String>> tokenList2) {
        for (Set<String> set1 : tokenList1) {
            for (Set<String> set2 : tokenList2) {
                Pair<Boolean, Double> resultPair = isTokenSynonymous(set1, set2);
                if (resultPair.getValue0()) {
                    return resultPair;
                }
            }
        }
        return new Pair<>(false, 0.0);
    }

    /**
     * Compare the two maps for synonymous terms.
     *
     * @param set1 Set of tokens 1
     * @param set2 Set of tokens 2
     * @return true if the term of a set has a synonymous or equal counterpart in the other set. T
     * this is tested both ways (set1 -&gt; set2 and set2 -&gt; set1).
     */
    public Pair<Boolean, Double> isTokenSynonymous(Set<String> set1, Set<String> set2) {
        if (set1.size() != set2.size()) {
            return new Pair<>(false, 0.0);
        }

        // required to avoid modification to the passed sets
        HashSet<String> workingSet1 = new HashSet<>(set1);
        HashSet<String> workingSet2 = new HashSet<>(set2);

        // remove trivial matches
        workingSet1.removeAll(set2);
        workingSet2.removeAll(set1);

        HashSet<String> set2covered = new HashSet<>();
        List<Double> comparisonScores = new ArrayList<>();

        // set 1 check
        nextS1:
        for (String s1 : workingSet1) {
            s1 = linker.linkToSingleConcept(s1);
            if (s1 == null) return new Pair<>(false, 0.0); // synonymy cannot be determined
            nextS2:
            for (String s2 : workingSet2) {
                s2 = linker.linkToSingleConcept(s2);
                if (s2 == null) return new Pair<>(false, 0.0); // synonymy cannot be determined
                if (set2covered.contains(s2)) continue nextS2; // already mapped
                if (compare(s1, s2)) {
                    set2covered.add(s2);
                    comparisonScores.add(compareScore(s1, s2));
                    continue nextS1;
                } else {
                    // -> not strong form synonymous, no counterpart, return false
                    continue nextS2;
                }
            }
            return new Pair<>(false, 0.0);
        }
        double score = 0.0;
        for (double d : comparisonScores) {
            score += d;
        }
        score = score / set2covered.size();
        return new Pair<>(true, score);
    }

    /**
     * Match by determining multiple concepts for a label.
     *
     * @param uri2labelMap_1 URI2label map 1.
     * @param uri2labelMap_2 URI2label map 2.
     */
    private void performLongestStringSynonymyMatching(Map<String, Set<String>> uri2labelMap_1, Map<String, Set<String>> uri2labelMap_2) {
        LOGGER.info("Building URI 2 n-links map.");
        Map<String, List<Set<String>>> uri2linksMap_1 = convertToUriLinksMap(uri2labelMap_1, true);
        Map<String, List<Set<String>>> uri2linksMap_2 = convertToUriLinksMap(uri2labelMap_2, false);
        LOGGER.info("URI 2 n-links map built.");

        for (HashMap.Entry<String, List<Set<String>>> uri2links_1 : uri2linksMap_1.entrySet()) {
            for (HashMap.Entry<String, List<Set<String>>> uri2links_2 : uri2linksMap_2.entrySet()) {
                Pair<Boolean, Double> isSynonymousConfidencePair = isLinkListSynonymous(uri2links_1.getValue(),
                        uri2links_2.getValue());
                if (isSynonymousConfidencePair.getValue0()) {
                    HashMap<String, Object> extensions = new HashMap<>();
                    extensions.put("http://custom#addedInStep", "longsestStringMatch");
                    alignment.add(uri2links_1.getKey(), uri2links_2.getKey(), isSynonymousConfidencePair.getValue1(),
                            CorrespondenceRelation.EQUIVALENCE, extensions);
                    if (isVerboseLoggingOutput) {
                        LOGGER.info(uri2links_1.getKey() + " " + uri2links_2.getKey() + " (longest string synonymy match)");
                        LOGGER.info(uri2links_1.getKey() + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_1.get(uri2links_1.getKey())) + ")");
                        LOGGER.info(uri2links_2.getKey() + ": (" + IOoperations.convertSetToStringPipeSeparated(uri2labelMap_2.get(uri2links_2.getKey())) + ")");
                    }
                }
            }
        }
    }

    /**
     * Given two lists of links, this method checks whether those are synonymous.
     *
     * @param list_1 List of links 1.
     * @param list_2 List of links 2.
     * @return Returns true, if the links are synonymous.
     */
    private Pair<Boolean, Double> isLinkListSynonymous(List<Set<String>> list_1, List<Set<String>> list_2) {
        for (Set<String> set_1 : list_1) {
            for (Set<String> set_2 : list_2) {
                Pair<Boolean, Double> isSynonymousConfidencePair = isLinkSetSynonymous(set_1, set_2);
                if (isSynonymousConfidencePair.getValue0()) {
                    return new Pair<>(true, isSynonymousConfidencePair.getValue1());
                }
            }
        }
        return new Pair<>(false, 0.0);
    }

    /**
     * All components of set_1 have to be synonymous to components in set_2.
     *
     * @param set_1 Set 1.
     * @param set_2 Set 2.
     * @return True if synonymous, else false.
     */
    private Pair<Boolean, Double> isLinkSetSynonymous(Set<String> set_1, Set<String> set_2) {

        if (set_1.size() != set_2.size()) {
            return new Pair<>(false, 0.0);
        }

        // required to avoid modification to the passed sets
        HashSet<String> workingSet1 = new HashSet<>(set_1);
        HashSet<String> workingSet2 = new HashSet<>(set_2);

        // remove duplicate concepts
        workingSet1.removeAll(set_2);
        workingSet2.removeAll(set_1);

        HashSet<String> set2covered = new HashSet<>();
        List<Double> confidenceScores = new ArrayList<>();

        // set 1 check
        for (String s1 : workingSet1) {
            nextS2:
            for (String s2 : workingSet2) {

                if (set2covered.contains(s2)) continue nextS2; // already mapped

                if (compare(s1, s2)) {
                    set2covered.add(s2);
                    confidenceScores.add(compareScore(s1, s2));
                } else {
                    // -> not strong form synonymous, no counterpart, return false
                    return new Pair<>(false, 0.0);
                }
            }
        }

        double confidence = 0.0;
        for (double d : confidenceScores) {
            confidence += d;
        }
        confidence = confidence / set2covered.size();
        return new Pair<>(true, confidence);
    }

    /**
     * This method converts a URIs -&gt; labels HashMap to a URIs -&gt; {@code List<nlinks>}.
     * Mapped entries are ignored.
     *
     * @param uris2labels      URIs to labels map.
     * @param isSourceOntology True if the map refers to the source ontology.
     * @return Map {@code URI -> tokens}
     */
    private Map<String, List<Set<String>>> convertToUriLinksMap(Map<String, Set<String>> uris2labels, boolean isSourceOntology) {
        HashMap<String, List<Set<String>>> result = new HashMap<>();
        for (HashMap.Entry<String, Set<String>> uri2labels : uris2labels.entrySet()) {

            // filter out what has been mapped before
            if (!isAllowForCumulativeMatches) {
                if (isSourceOntology && mappingExistsForSourceURI(uri2labels.getKey())) {
                    continue;
                } else if (!isSourceOntology && mappingExistsForTargetURI(uri2labels.getKey())) {
                    continue;
                }
            }

            List<Set<String>> list = new LinkedList<>();
            for (String label : uri2labels.getValue()) {
                if (StringOperations.tokenizeBestGuess(label).length < multiConceptLinkerUpperLimit) {
                    Set<String> linkedConcepts = linker.linkToPotentiallyMultipleConcepts(label);
                    if (linkedConcepts != null) {
                        list.add(linkedConcepts);
                    }
                }
            }
            if (list.size() > 0) {
                result.put(uri2labels.getKey(), list);
            }
        }
        return result;
    }

    /**
     * Check whether the specified word is synonymous to a word in the given set.
     *
     * @param word Word to be checked.
     * @param set  Set containing the words.
     * @return true if synonymous.
     */
    private boolean setContainsSynonym(String word, HashSet<String> set) {
        String linkedWord = linker.linkToSingleConcept(word);
        for (String s : set) {
            if (compare(linkedWord, linker.linkToSingleConcept(s))) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method transforms the uri2labels into a uri2links HashMap.
     * Thereby, the linking function is called only once.
     * Furthermore, concepts that cannot be linked are not included in the resulting HashMap.
     * Mapped entries are not linked.
     *
     * @param uri2labels       Input HashMap URI -&gt; labels
     * @param isSourceOntology True if the map refers to the source ontology.
     * @return HashMap URI -&gt; links
     */
    private HashMap<String, Set<String>> convertToUriLinkMap(Map<String, Set<String>> uri2labels, boolean isSourceOntology) {
        HashMap<String, Set<String>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> uri2label : uri2labels.entrySet()) {

            // check whether already mapped (intuition: do not map something that has been mapped before by more accurate algorithm)
            if (!isAllowForCumulativeMatches) {
                if (isSourceOntology && mappingExistsForSourceURI(uri2label.getKey())) {
                    continue;
                } else if (!isSourceOntology && mappingExistsForTargetURI(uri2label.getKey())) {
                    continue;
                }
            }

            Set<String> links = new HashSet();
            for (String label : uri2label.getValue()) {
                if (label == null && label.trim().length() == 0) {
                    continue;
                }
                String linkedConcept = linker.linkToSingleConcept(label);
                if (linkedConcept != null) {
                    links.add(linkedConcept);
                }
            } // for loop over individual labels

            if (links.size() > 0) {
                result.put(uri2label.getKey(), links);
            }
        } // for loop over whole map
        return result;
    }

    /**
     * This method converts a URIs -&gt; labels HashMap to a URIs -&gt; tokens HashMap.
     * Mapped entries are ignored.
     *
     * @param uris2labels      URIs to labels map.
     * @param isSourceOntology True if the map refers to the source ontology.
     * @return Map: {@code URI -> tokens}
     */
    private Map<String, List<Set<String>>> convertToUriTokenMap(Map<String, Set<String>> uris2labels, boolean isSourceOntology) {
        HashMap<String, List<Set<String>>> result = new HashMap<>();
        for (Map.Entry<String, Set<String>> uri2label : uris2labels.entrySet()) {
            // check whether already mapped (intuition: do not map something that has been mapped before by a more accurate algorithm)
            if (!isAllowForCumulativeMatches) {
                if (isSourceOntology && mappingExistsForSourceURI(uri2label.getKey())) {
                    continue;
                } else if (!isSourceOntology && mappingExistsForTargetURI(uri2label.getKey())) {
                    continue;
                }
            }

            List<Set<String>> listOfTokenSequences = new LinkedList<>();
            for (String label : uri2label.getValue()) {
                listOfTokenSequences.add(tokenizeAndFilter(label));
            }
            result.put(uri2label.getKey(), listOfTokenSequences);
        } // end of for loop over whole map
        return result;
    }

    /**
     * Tokenizes a label and filters out stop words.
     *
     * @param label The label to be tokenized.
     * @return Tokenized label.
     */
    public static HashSet<String> tokenizeAndFilter(String label) {
        // camelcase resolution
        label = label.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_");

        // delete everything that is not a-z and A-Z
        label = label.replaceAll("[^a-zA-Z_]", "_");

        // after de-camelCasing: lowercase
        label = label.toLowerCase();

        // replace __ and ___ etc. with just one _
        label = label.replaceAll("(_)+", "_");

        // delete leading and trailing underscores
        if (label.startsWith("_")) {
            label = label.substring(1);
        }
        if (label.endsWith("_")) {
            label = label.substring(0, label.length() - 1);
        }

        // array conversion
        String[] tokens = label.split("_");

        // hashset conversion
        HashSet<String> result = new HashSet<>(Arrays.asList(tokens));

        // remove free floating genitive s
        result.remove("s");

        // stopword removal
        result = StringOperations.clearHashSetFromStopwords(result);
        return result;
    }

    /**
     * Checks whether there exists a mapping cell where the URI is used as source.
     *
     * @param uri URI for which the check shall be performed.
     * @return True if at least one mapping cell exists, else false.
     */
    private boolean mappingExistsForSourceURI(String uri) {
        return this.alignment.getCorrespondencesSource(uri).iterator().hasNext();
    }

    /**
     * Checks whether there exists a mapping cell where the URI is used as target.
     *
     * @param uri URI for which the check shall be performed.
     * @return True if at least one mapping cell exists, else false.
     */
    private boolean mappingExistsForTargetURI(String uri) {
        return this.alignment.getCorrespondencesTarget(uri).iterator().hasNext();
    }

    private double compareScore(String lookupTerm1, String lookupTerm2) {
        if (strategy == ImplementedBackgroundMatchingStrategies.SYNONYMY && isSynonymyConfidenceAvailable) {
            return ((SynonymConfidenceCapability) knowledgeSource).getStrongFormSynonymyConfidence(lookupTerm1, lookupTerm2);
        }
        return 1.0;
    }

    /**
     * The compare method compares two concepts that are available in a background knowledge source.
     * The concepts will be compared using the specified {@link BackgroundMatcher#strategy} and the method will
     * return true if the determined similarity is above the specified minimal {@link BackgroundMatcher#threshold}.
     *
     * @param lookupTerm1 Term 1.
     * @param lookupTerm2 Term 2.
     * @return True if similarity larger than minimal threshold, else false.
     */
    private boolean compare(String lookupTerm1, String lookupTerm2) {
        switch (strategy) {
            case SYNONYMY:
                return knowledgeSource.isStrongFormSynonymous(lookupTerm1, lookupTerm2);
            case SYNONYMY_OR_HYPERNYMY:
                return ((SemanticWordRelationDictionary) knowledgeSource).isSynonymousOrHypernymous(lookupTerm1, lookupTerm2);
            default:
                return false;
        }
    }

    /**
     * Get the name of the matcher.
     *
     * @return A textual representation of the matcher.
     */
    public String getMatcherName() {
        return this.strategy.toString() + "_" + this.knowledgeSource.getName() + "_" + this.threshold;
    }

    //--------------------------------------------------------------------------------------------
    // Getters and Setters
    //--------------------------------------------------------------------------------------------

    public ImplementedBackgroundMatchingStrategies getStrategy() {
        return strategy;
    }

    public void setStrategy(ImplementedBackgroundMatchingStrategies strategy) {
        this.strategy = strategy;
    }

    public ExternalResourceWithSynonymCapability getKnowledgeSource() {
        return knowledgeSource;
    }

    public void setKnowledgeSource(ExternalResourceWithSynonymCapability knowledgeSource) {
        this.knowledgeSource = knowledgeSource;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean isAllowForCumulativeMatches() {
        return isAllowForCumulativeMatches;
    }

    public void setAllowForCumulativeMatches(boolean allowForCumulativeMatches) {
        this.isAllowForCumulativeMatches = allowForCumulativeMatches;
    }

    public boolean isVerboseLoggingOutput() {
        return isVerboseLoggingOutput;
    }

    public void setVerboseLoggingOutput(boolean verboseLoggingOutput) {
        isVerboseLoggingOutput = verboseLoggingOutput;
    }

    public int getMultiConceptLinkerUpperLimit() {
        return multiConceptLinkerUpperLimit;
    }

    public void setMultiConceptLinkerUpperLimit(int multiConceptLinkerUpperLimit) {
        this.multiConceptLinkerUpperLimit = multiConceptLinkerUpperLimit;
    }

    public boolean isSynonymyConfidenceAvailable() {
        return isSynonymyConfidenceAvailable;
    }
}
