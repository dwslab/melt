package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import net.sf.extjwnl.JWNLException;
import net.sf.extjwnl.data.*;
import net.sf.extjwnl.data.IndexWord;
import net.sf.extjwnl.data.POS;
import net.sf.extjwnl.data.Pointer;
import net.sf.extjwnl.data.Synset;
import net.sf.extjwnl.data.Word;
import net.sf.extjwnl.dictionary.Dictionary;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * API for WordNet requests.
 */
public class WordNetKnowledgeSource extends SemanticWordRelationDictionary {


	private static Logger LOGGER = LoggerFactory.getLogger(WordNetKnowledgeSource.class);

	private Dictionary dictionary;

	/**
	 * Buffer for synonyms
	 */
	private Map<String, HashSet<String>> buffer;

	/**
	 * the linker that is used to link words to wordnet concepts
	 */
	WordNetLinker linker;

	/**
	 * Constructor
	 */
	public WordNetKnowledgeSource(){
		try {
			this.dictionary = Dictionary.getDefaultResourceInstance();
			buffer = new HashMap<>();
			linker = new WordNetLinker(this);
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks whether the given word can be found in the dictionary, regardless of POS.
	 * @param word to be found
	 * @return true if successful, else false.
	 */
	public boolean isInDictionary(String word) {
		if(word == null || word.length() == 0) {
			return false;
		}

		try {
			IndexWord indexWordNoun = dictionary.getIndexWord(POS.NOUN, word);
			if (indexWordNoun != null) {
				return true;
			}

			IndexWord indexWordVerb = dictionary.getIndexWord(POS.VERB, word);
			if (indexWordVerb != null) {
				return true;
			}

			IndexWord indexWordAdjective = dictionary.getIndexWord(POS.ADJECTIVE, word);
			if (indexWordAdjective != null) {
				return true;
			}

			IndexWord indexWordAdVerb = dictionary.getIndexWord(POS.ADVERB, word);
			if (indexWordAdVerb != null) {
				return true;
			}
		} catch (JWNLException e) {
			LOGGER.error("WordNet Error.", e);
		}
		return false;
	}

	/**
	 * Retrieve Synonyms for the given word.
	 * @param linkedConcept for which synonyms shall be retrieved.
	 * @return Synonyms
	 */
	public Set<String> getSynonymsLexical(String linkedConcept) {
		if(buffer.containsKey(linkedConcept)) {
			return buffer.get(linkedConcept);
		}
		HashSet<String> result = new HashSet<>();
		result.addAll(getSynonyms(linkedConcept, POS.NOUN));
		result.addAll(getSynonyms(linkedConcept, POS.VERB));
		result.addAll(getSynonyms(linkedConcept, POS.ADJECTIVE));
		result.addAll(getSynonyms(linkedConcept, POS.ADVERB));
		buffer.put(linkedConcept, result);
		return result;
	}

	/**
	 * Get synonyms for a particular part of speech.
	 * @param word The word for which the synonyms shall be retrieved.
	 * @param partOfSpeech The part of speech of the word.
	 * @return a set of synonyms.
	 */
	public HashSet<String> getSynonyms(String word, POS partOfSpeech){
		HashSet<String> result = new HashSet<>();
		IndexWord indexWordNoun = null;
		try {
			indexWordNoun = dictionary.getIndexWord(partOfSpeech, word);

		if(indexWordNoun != null) {
			// loop over ids
			for(Synset synset : indexWordNoun.getSenses()) {

				//System.out.println();
				for(Word w : synset.getWords()) {
					result.add(w.getLemma());
					//System.out.println(w.getLemma());
				}
			}
		}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	@NotNull
	public HashSet<String> getHypernyms(String linkedConcept) {
		HashSet<String> result = new HashSet<>();
		try {
			IndexWord indexWordNoun = dictionary.getIndexWord(POS.NOUN, linkedConcept);
			if (indexWordNoun != null) {
				for (Synset synset : indexWordNoun.getSenses()) {
					for (Pointer pointer : synset.getPointers(PointerType.HYPERNYM)) {
						for (Word w : pointer.getTargetSynset().getWords()) {
							result.add(w.getLemma());
						}
					}
				}
			}
		} catch (JWNLException e){
			LOGGER.error("JWNL Exception", e);
		}
		return result;
	}

	@Override
	public void close() {
		// no resources to close here
	}
	
	@Override
	public LabelToConceptLinker getLinker() {
		return this.linker;
	}

	@Override
	public String getName(){
		return "WordNet";
	}
}
