package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.dbpedia;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.MultiConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Link DBpedia embeddings using the "normal" DBpedia linker ({@link DBpediaLinker}.
 * This linker is suited for RDF2Vec Light embeddings.
 *
 * The linker is conceptually similar to
 * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wikidata.WikidataEmbeddingLinker}.
 */
public class DBpediaEmbeddingLinker implements LabelToConceptLinker, MultiConceptLinker {


    private static final Logger LOGGER = LoggerFactory.getLogger(DBpediaEmbeddingLinker.class);

    private static DBpediaLinker dbpediaLinker;

    private String linkerName = "DBpedia Embedding Linker";

    /**
     * Just for debugging and analysis: A data structure to store URIs that were not found.
     */
    private final Set<String> urisNotFound = new HashSet<>();

    /**
     * Set of URIs for which an embedding is available.
     */
    Set<String> uris;

    /**
     * Main Constructor
     * @param linker The desired DBpedia linker configuration.
     * @param filePathToUriFile UTF-8 encoded file holding the URIs for which an embedding exists.
     */
    public DBpediaEmbeddingLinker(DBpediaLinker linker, @NotNull String filePathToUriFile){
        // linker
        if(linker == null){
            DBpediaKnowledgeSource dks = new DBpediaKnowledgeSource(true);
            setDbpediaLinker(new DBpediaLinker(dks));
        } else {
            setDbpediaLinker(linker);
        }

        // URI file
        if(filePathToUriFile == null){
            LOGGER.error("URL file must not be null. The linker will not work.");
            return;
        }
        uris = StringOperations.readSetFromFile(filePathToUriFile);
    }

    /**
     * Constructor
     *
     * @param filePathToUriFile UTF-8 encoded file holding the URIs for which an embedding exists.
     */
    public DBpediaEmbeddingLinker(String filePathToUriFile){
        this(null, filePathToUriFile);
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        String link = dbpediaLinker.linkToSingleConcept(labelToBeLinked);
        if(link == null){
            return null;
        }
        if(dbpediaLinker.isMultiConceptLink(link)){
            for (String linkPart : dbpediaLinker.getUris(link)){
                if(uris.contains(linkPart)){
                    // confirm the link as soon as we find a vector for it
                    return link;
                }
                urisNotFound.add(linkPart);
                LOGGER.warn("Link part not found: '" + linkPart + "'");
            }
            // even though a link was found, there is no vector for it...
            LOGGER.error("No vectors found for link: '" + link + "'");
            return null;
        } else if(uris.contains(link)){
            return link;
        } else {
            LOGGER.error("No vector found for link: '" + link + "'");
            urisNotFound.add(link);
            return null;
        }
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        Set<String> links = dbpediaLinker.linkToPotentiallyMultipleConcepts(labelToBeLinked);
        if (links == null) {
            return null;
        }

        checkLinks:
        for (String link : links) {
            if (dbpediaLinker.isMultiConceptLink(link)) {
                for (String linkPart : dbpediaLinker.getUris(link)) {
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

    public static DBpediaLinker getDbpediaLinker() {
        return dbpediaLinker;
    }

    public static void setDbpediaLinker(DBpediaLinker dbpediaLinker) {
        DBpediaEmbeddingLinker.dbpediaLinker = dbpediaLinker;
    }

    public Set<String> getUrisNotFound() {
        return urisNotFound;
    }

    @Override
    public Set<String> getUris(String multiConceptLink) {
        return dbpediaLinker.getUris(multiConceptLink);
    }

    @Override
    public boolean isMultiConceptLink(String link) {
        return dbpediaLinker.isMultiConceptLink(link);
    }
}