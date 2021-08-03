package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.TextExtractorMultipleProperties;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Find matches based on simple token overlap.
 * All string valued literals and the fragment are taken into account.
 * They are normalized and tokenized.
 * Afterwards the matches are generated based on the highest token overlap.
 */
public class RecallMatcherGeneric extends MatcherYAAAJena {


    private static final Logger LOGGER = LoggerFactory.getLogger(RecallMatcherGeneric.class);

    private int max;
    private boolean useBothDirections;

    public RecallMatcherGeneric(int max, boolean useBothDirections) {
        this.max = max;
        this.useBothDirections = useBothDirections;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        LOGGER.debug("Match classes");
        matchResources(source.listClasses(), target.listClasses(), inputAlignment);
        LOGGER.debug("Match properties");
        matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);
        LOGGER.debug("Match instances");
        matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
        LOGGER.debug("Finished");
        return inputAlignment;
    }

    public void matchResources(Iterator<? extends Resource> sourceResources, Iterator<? extends Resource> targetResources, Alignment alignment) {
        matchResources(sourceResources, targetResources, alignment, true);
        if (this.useBothDirections)
            matchResources(targetResources, sourceResources, alignment, false);
    }

    public void matchResources(Iterator<? extends Resource> corpusResources, Iterator<? extends Resource> queryResources, Alignment alignment, boolean isCorpusSource) {
        Map<String, Set<String>> tokenToResourceURI = new HashMap<>();
        Counter<String> tokenCounter = new Counter<>();

        //corpus
        while (corpusResources.hasNext()) {
            Resource resource = corpusResources.next();
            if (resource.isURIResource() == false)
                continue;
            String resourceURI = resource.getURI();
            Set<String> bow = normalizedAndTokenize(resource);
            tokenCounter.addAll(bow);
            for (String token : bow) {
                tokenToResourceURI.computeIfAbsent(token, __ -> new HashSet<>()).add(resourceURI);
            }
        }

        //query
        while (queryResources.hasNext()) {
            Resource resource = queryResources.next();
            if (resource.isURIResource() == false)
                continue;
            String resourceURI = resource.getURI();
            Set<String> bow = normalizedAndTokenize(resource);
            Counter<String> urisCounter = new Counter<>();
            for (String token : bow) {
                Set<String> s = tokenToResourceURI.get(token);
                if (s != null)
                    urisCounter.addAll(s);
            }

            if (isCorpusSource) {
                for (Entry<String, Integer> res : urisCounter.mostCommon(max)) {
                    //for(Entry<String, Integer> res : urisCounter.mostCommonWithHighestCount()){
                    alignment.add(res.getKey(), resourceURI, res.getValue() / (double) urisCounter.getAmountOfDistinctElements());
                }
            } else {
                for (Entry<String, Integer> res : urisCounter.mostCommon(max)) {
                    //for(Entry<String, Integer> res : urisCounter.mostCommonWithHighestCount()){
                    alignment.add(resourceURI, res.getKey(), res.getValue() / (double) urisCounter.getAmountOfDistinctElements());
                }
            }

        }
    }


    private final TextExtractor extractor = new TextExtractorMultipleProperties(PropertyVocabulary.LABEL_LIKE_PROPERTIES);

    /**
     * This methods return the bag of words (bow) of a resource.
     *
     * @return
     */
    private Set<String> normalizedAndTokenize(Resource resource) {
        Set<String> set = new HashSet<>();
        for (String label : extractor.extract(resource)) {
            set.addAll(StringProcessing.normalize(label));
        }

        //check URI fragment
        //resource is always a uri resource
        String fragment = URIUtil.getUriFragment(resource.getURI()).trim();
        if (StringProcessing.containsMostlyNumbers(fragment) == false) {
            set.addAll(StringProcessing.normalize(fragment));
        }

        return set;
    }

}
