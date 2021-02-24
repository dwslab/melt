package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.wordNet;


import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.SemanticWordRelationDictionary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * API for WordNet requests.
 */
public class WordNetKnowledgeSource extends SemanticWordRelationDictionary {


	private IDictionary dict;

	/**
	 * Directory where required wordnet files reside.
	 */
	private String wordnetDirectoryPath;

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
	 * @param wordnetDirectoryPath The path to the WordNet directory.
	 */
	public WordNetKnowledgeSource(String wordnetDirectoryPath){
		try {
			this.wordnetDirectoryPath = wordnetDirectoryPath;
			File wordnetDirectory = new File(this.wordnetDirectoryPath);
			URL wordnetLocalURL = wordnetDirectory.toURI().toURL();
			dict = new Dictionary(wordnetLocalURL);
			dict.open();
			buffer = new HashMap<>();
			linker = new WordNetLinker(this);
		} catch(MalformedURLException mue) {
			mue.printStackTrace();
		} catch (IOException e) {
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
		
		IIndexWord indexWordNoun = dict.getIndexWord(word, POS.NOUN);
		if(indexWordNoun != null) {
			return true;
		}

		IIndexWord indexWordVerb = dict.getIndexWord(word, POS.VERB);
		if(indexWordVerb != null) {
			return true;
		}

		IIndexWord indexWordAdjective = dict.getIndexWord(word, POS.ADJECTIVE);
		if(indexWordAdjective != null) {
			return true;
		}

		IIndexWord indexWordAdVerb = dict.getIndexWord(word, POS.ADVERB);
		if(indexWordAdVerb != null) {
			return true;
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
		IIndexWord indexWordNoun = dict.getIndexWord(word, partOfSpeech);
		if(indexWordNoun != null) {
			// loop over ids
			for(IWordID wordId : indexWordNoun.getWordIDs()) {
				IWord myword = dict.getWord(wordId); 
				ISynset synset = myword.getSynset();
				// loop over synset components
				//System.out.println();
				for(IWord w : synset.getWords()) {
					result.add(w.getLemma());
					//System.out.println(w.getLemma());
				}
			}
		}
		return result;
	}

	@Override
	public HashSet<String> getHypernyms(String linkedConcept) {
		HashSet<String> result = new HashSet<>();
		IIndexWord indexWordNoun = dict.getIndexWord(linkedConcept, POS.NOUN);
		if(indexWordNoun != null) {


			// loop over ids
			for(IWordID wordId : indexWordNoun.getWordIDs()) {
				IWord myword = dict.getWord(wordId);
				ISynset synset = myword.getSynset();

				for(ISynsetID hypernymySynsetId : synset.getRelatedSynsets(Pointer.HYPERNYM)){
					for(IWord w : dict.getSynset(hypernymySynsetId).getWords()) {
						result.add(w.getLemma());
					}
				}

			}

			//for(IWordID wordId : indexWordNoun.getWordIDs()) {
			//	IWord myword = dict.gets(wordId);
			//
			//	for(IWordID hypernymId : myword.getRelatedWords(Pointer.HYPERNYM)) {
			//		IWord hypernymGroup = dict.getWord(hypernymId);
			//		for(IWord hypernym: hypernymGroup.getSynset().getWords())
			//		result.add(hypernym.getLemma());
			//		//System.out.println(w.getLemma());
			//	}
			//}
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
