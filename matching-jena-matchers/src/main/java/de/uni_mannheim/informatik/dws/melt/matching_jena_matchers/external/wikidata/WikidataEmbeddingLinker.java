package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class WikidataEmbeddingLinker implements LabelToConceptLinker {


    private static final Logger LOGGER = LoggerFactory.getLogger(WikidataEmbeddingLinker.class);

    private static final WikidataLinker wikidataLinker = new WikidataLinker();

    Set<String> uris;

    private String linkerName = "Wikidata Embedding Linker";

    /**
     * Constructor
     * @param filePathToEntityFile A file holding the URIs for which an embedding exists.
     */
    public WikidataEmbeddingLinker(String filePathToEntityFile) {
        uris = StringOperations.readSetFromFile(filePathToEntityFile);
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        String link = wikidataLinker.linkToSingleConcept(labelToBeLinked);
        if (link == null) {
            return null;
        }
        if (wikidataLinker.isMultiConceptLink(link)) {
            for (String linkPart : wikidataLinker.getUris(link)) {
                if (uris.contains(linkPart)) {
                    // confirm the link as soon as we find a vector for it
                    return labelToBeLinked;
                }
            }
            // even though a link was found, there is no vector for it...
            LOGGER.error("No vectors found for link: '" + link + "'");
            return null;
        } else if (uris.contains(link)) {
            return link;
        } else {
            LOGGER.error("No vector found for link: '" + link + "'");
            return null;
        }
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        Set<String> links = wikidataLinker.linkToPotentiallyMultipleConcepts(labelToBeLinked);
        if (links == null) {
            return null;
        }

        checkLinks:
        for (String link : links) {
            if (wikidataLinker.isMultiConceptLink(link)) {
                for (String linkPart : wikidataLinker.getUris(link)) {
                    if (uris.contains(linkPart)) {
                        // confirm the link as soon as we find a vector for it
                        continue checkLinks;
                    }
                }
                // even though a link was found, there is no vector for it...
                LOGGER.error("No vectors found for link: '" + labelToBeLinked + "'");
                return null;
            } else if (!uris.contains(link)) {
                LOGGER.error("No vector found for link: '" + link + "'");
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
}
