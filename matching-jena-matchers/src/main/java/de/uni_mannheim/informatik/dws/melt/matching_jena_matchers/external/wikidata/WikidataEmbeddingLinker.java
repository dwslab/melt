package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.MultiConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Link to Wikidata embeddings using the "normal" Wikidata linker ({@link WikidataLinker}.
 * This linker is suited for RDF2Vec Light embeddings.
 *
 * The linker is conceptually similar to
 * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia.DBpediaEmbeddingLinker}.
 */
public class WikidataEmbeddingLinker implements LabelToConceptLinker, MultiConceptLinker {


    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataEmbeddingLinker.class);

    private static final WikidataLinker WIKIDATA_LINKER = new WikidataLinker();

    /**
     * Set of URIs for which an embedding is available.
     */
    Set<String> uris = new HashSet<>();

    private String linkerName = "Wikidata Embedding Linker";

    /**
     * Just for debugging and analysis: A data structure to store URIs that were not found.
     */
    private final Set<String> urisNotFound = new HashSet<>();

    /**
     * Constructor
     * @param filePathToEntityFile A file holding the URIs for which an embedding exists.
     */
    public WikidataEmbeddingLinker(@NotNull String filePathToEntityFile) {
        if(filePathToEntityFile == null){
            LOGGER.error("File must not be null. The linker will not work.");
            return;
        }
        uris = StringOperations.readSetFromFile(filePathToEntityFile);
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        String link = WIKIDATA_LINKER.linkToSingleConcept(labelToBeLinked);
        if (link == null) {
            return null;
        }
        if (WIKIDATA_LINKER.isMultiConceptLink(link)) {
            for (String linkPart : WIKIDATA_LINKER.getUris(link)) {
                if (uris.contains(linkPart)) {
                    // confirm the link as soon as we find a vector for it
                    return link;
                }
                urisNotFound.add(linkPart);
                LOGGER.warn("Link part not found: '" + linkPart + "'");
            }
            // even though a link was found, there is no vector for it...
            LOGGER.error("No vectors found for link: '" + link + "'");
            return null;
        } else if (uris.contains(link)) {
            return link;
        } else {
            LOGGER.error("No vector found for link: '" + link + "'");
            urisNotFound.add(link);
            return null;
        }
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        Set<String> links = WIKIDATA_LINKER.linkToPotentiallyMultipleConcepts(labelToBeLinked);
        if (links == null) {
            return null;
        }

        checkLinks:
        for (String link : links) {
            if (WIKIDATA_LINKER.isMultiConceptLink(link)) {
                for (String linkPart : WIKIDATA_LINKER.getUris(link)) {
                    if (uris.contains(linkPart)) {
                        // confirm the link as soon as we find a vector for it
                        continue checkLinks;
                    }
                    LOGGER.warn("Link part not found: '" + linkPart + "'");
                    urisNotFound.add(linkPart);
                }
                // even though a link was found, there is no vector for it...
                LOGGER.error("No vectors found for link: '" + labelToBeLinked + "'");
                return null;
            } else if (!uris.contains(link)) {
                LOGGER.error("No vector found for link: '" + link + "'");
                urisNotFound.add(link);
                return null;
            }
        }
        return links;
    }

    @Override
    public String getNameOfLinker() {
        return this.linkerName;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        if (nameOfLinker == null) {
            LOGGER.error("Linker name cannot be null. Doing nothing.");
            return;
        }
        this.linkerName = nameOfLinker;
    }

    public Set<String> getUrisNotFound() {
        return urisNotFound;
    }

    @Override
    public Set<String> getUris(String multiConceptLink) {
        return WIKIDATA_LINKER.getUris(multiConceptLink);
    }

    @Override
    public boolean isMultiConceptLink(String link) {
        return WIKIDATA_LINKER.isMultiConceptLink(link);
    }
}
