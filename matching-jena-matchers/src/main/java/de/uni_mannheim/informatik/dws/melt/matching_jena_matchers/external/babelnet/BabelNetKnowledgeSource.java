package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.babelnet;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetQuery;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetRelation;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.kb.Sense;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.persistence.PersistenceService.PreconfiguredPersistences.*;

/**
 * A dictionary that will use BabelNet offline indices.
 */
public class BabelNetKnowledgeSource extends SemanticWordRelationDictionary {


    private static BabelNet babelNet;

    /**
     * Buffer for synonyms.
     */
    private ConcurrentMap<String, HashSet<String>> synonymBuffer;

    /**
     * Buffer for hypernyms.
     */
    private ConcurrentMap<String, HashSet<String>> hypernymyBuffer;

    /**
     * Local persistence service.
     */
    private PersistenceService persistenceService;

    /**
     * Local linker.
     */
    private BabelNetLinker linker;

    /**
     * Constructor.
     * Make sure that the indices are available offline and that the project is configured correctly.
     */
    public BabelNetKnowledgeSource() {
        babelNet = BabelNet.getInstance();
        initializeBuffers();
        this.linker = new BabelNetLinker(this);
    }

    /**
     * Initializes local database
     */
    private void initializeBuffers(){
        persistenceService = PersistenceService.getService();
        synonymBuffer = persistenceService.getMapDatabase(BABELNET_SYNONYM_BUFFER);
        hypernymyBuffer = persistenceService.getMapDatabase(BABELNET_HYPERNYMY_BUFFER);
    }

    /**
     * The word will be only looked up in the English BabelNet.
     *
     * @param word The word to be looked for.
     * @return true if word found, else false.
     */
    public boolean isInDictionary(String word) {
        BabelNetQuery query = new BabelNetQuery.Builder(word)
                .from(Language.EN)
                .build();
        List<BabelSynset> synsets = babelNet.getSynsets(query);
        if (synsets.size() == 0) {
            return false;
        } else return true;
    }

    /**
     * The synonyms will be only looked up in the English BabelNet.
     *
     * @param linkedConcept The word for which synonyms shall be retrieved.
     * @return Set with Synonyms.
     */
    @Override
    public Set<String> getSynonymsLexical(String linkedConcept) {
        String key = linkedConcept + "_EN";
        if(synonymBuffer.containsKey(key)){
            return synonymBuffer.get(key);
        }
        HashSet<String> result = new HashSet<>();
        BabelNetQuery query = new BabelNetQuery.Builder(linkedConcept)
                .from(Language.EN)
                .build();
        List<BabelSynset> synsets = babelNet.getSynsets(query);
        for (BabelSynset synset : synsets) {
            for (Sense s : synset.getSenses(Language.EN)) {
                result.add(normalize(s.getSimpleLemma()));
            }
        }
        synonymBuffer.put(key, result);
        commit(BABELNET_SYNONYM_BUFFER);
        return result;
    }

    @Override
    public HashSet<String> getHypernyms(String linkedConcept) {
        String key = linkedConcept + "_EN";
        if(hypernymyBuffer.containsKey(key)){
            return hypernymyBuffer.get(key);
        }
        HashSet<String> result = new HashSet<>();
        BabelNetQuery query = new BabelNetQuery.Builder(linkedConcept)
                .from(Language.EN)
                .build();
        List<BabelSynset> synsets = babelNet.getSynsets(query);
        for (BabelSynset synset : synsets) {
            for(BabelSynsetRelation edge : synset.getOutgoingEdges(BabelPointer.HYPERNYM)){
                for (Sense s : babelNet.getSynset(edge.getBabelSynsetIDTarget()).getSenses(Language.EN)){
                    result.add(normalize(s.getSimpleLemma()));
                }
            }
        }
        hypernymyBuffer.put(key, result);
        commit(BABELNET_HYPERNYMY_BUFFER);
        return result;
    }

    @Override
    public void close() {
        persistenceService.closeDatabase(BABELNET_SYNONYM_BUFFER);
        persistenceService.closeDatabase(BABELNET_HYPERNYMY_BUFFER);
    }

    private void commit(PersistenceService.PreconfiguredPersistences persistence){
        switch (persistence){
            case BABELNET_SYNONYM_BUFFER:
                persistenceService.commit(BABELNET_SYNONYM_BUFFER);
                return;
            case BABELNET_HYPERNYMY_BUFFER:
                persistenceService.commit(BABELNET_HYPERNYMY_BUFFER);
                return;
        }
    }

    /**
     * Normalizes string for this particular dictionary.
     *
     * @param stringToBeNormalized String that shall be normalized.
     * @return Normalized String.
     */
    public String normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        stringToBeNormalized = stringToBeNormalized.replace(" ", "_");
        stringToBeNormalized = stringToBeNormalized.toLowerCase();
        // delete non alpha-numeric characters:
        stringToBeNormalized = stringToBeNormalized.replaceAll("[^a-zA-Z\\d\\s:_]", ""); // regex: [^a-zA-Z\d\s:]
        return stringToBeNormalized;
    }

    @Override
    public LabelToConceptLinker getLinker() {
        return this.linker;
    }

    @Override
    public String getName(){
        return "BabelNet Symbolic";
    }
}
