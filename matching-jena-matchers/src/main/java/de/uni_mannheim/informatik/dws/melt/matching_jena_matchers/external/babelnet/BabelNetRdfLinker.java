package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Links concepts to BabelNet (using the RDF dataset - NOT the indices).
 * It is required that the RDF dataset is stored in TDB1.
 */
public class BabelNetRdfLinker implements LabelToConceptLinker {


    private static final Logger LOGGER = LoggerFactory.getLogger(BabelNetRdfLinker.class);

    private String nameOfLinker = "BabelNet RDF Linker";

    /**
     * The TDB dataset into which the DBpedia data set was loaded.
     */
    private Dataset tdbDataset;

    /**
     * TDB model
     */
    private Model tdbModel;

    /**
     * A set of string operations that are all performed.
     */
    Set<StringModifier> stringModificationSet = new HashSet<>();

    public BabelNetRdfLinker(String tdbDirectoryPath){
        stringModificationSet.add(new TokenizeConcatUnderscoreModifier());
        stringModificationSet.add(new TokenizeConcatUnderscoreLowercaseModifier());
        stringModificationSet.add(new TokenizeConcatUnderscoreCapitalizeModifier());
        this.tdbDataset = TDBFactory.createDataset(tdbDirectoryPath);
        this.tdbModel = this.tdbDataset.getDefaultModel();
    }

    @Override
    public String linkToSingleConcept(String labelToBeLinked) {
        if(labelToBeLinked == null) return null;
        for(StringModifier modifier : stringModificationSet) {
            String uriLabel = modifier.modifyString(labelToBeLinked);
            uriLabel = encode(uriLabel);

            // noun
            String uri = "http://babelnet.org/rdf/" + uriLabel +  "_n_EN";
            Resource r = tdbModel.createResource(uri);
            if (tdbModel.containsResource(r)) return uri;

            // verb
            uri = "http://babelnet.org/rdf/" + uriLabel +  "_v_EN";
            r = tdbModel.createResource(uri);
            if (tdbModel.containsResource(r)) return uri;

            // adjective
            uri = "http://babelnet.org/rdf/" + uriLabel +  "_r_EN";
            r = tdbModel.createResource(uri);
            if (tdbModel.containsResource(r)) return uri;

            // adverb
            uri = "http://babelnet.org/rdf/" + uriLabel +  "_a_EN";
            r = tdbModel.createResource(uri);
            if (tdbModel.containsResource(r)) return uri;
        }
        return null;
    }

    static String encode(String toBeEncoded){
        try {
            return URLEncoder.encode(toBeEncoded, StandardCharsets.UTF_8.toString());
        } catch (Exception e){
            LOGGER.error("Could not encode '" + toBeEncoded + "'.", e);
            return toBeEncoded;
        }
    }

    @Override
    public Set<String> linkToPotentiallyMultipleConcepts(String labelToBeLinked) {
        return null;
    }

    @Override
    public String getNameOfLinker() {
        return this.nameOfLinker;
    }

    @Override
    public void setNameOfLinker(String nameOfLinker) {
        this.nameOfLinker = nameOfLinker;
    }

    public void close(){
        tdbDataset.close();
    }
}
