package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.query.QueryFactory;
import static com.googlecode.cqengine.query.QueryFactory.noQueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Data structure to represent an Alignment.
 * An alignment is a set of multiple {@link Correspondence} instances.
 * An alignment is also known as "mapping" or "mappings".
 * Each {@link Correspondence} is uniquely identified by entityOne, entityTwo and relation.
 * This means, if you add a Correspondence which already exists, it will not be modified.
 * To modify an already existing correspondence you can use {@link Alignment#addOrModify(Correspondence)}.
 *
 * @author Sven Hertling
 * @author Jan Portisch
 */
public class Alignment extends ConcurrentIndexedCollection<Correspondence> {


    /**
     * Default logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Alignment.class);
    
    protected String method;
    protected String type;
    protected String level;

    protected OntoInfo onto1;
    protected OntoInfo onto2;
    
    protected HashIndex<String, Correspondence> indexSource = null;
    protected HashIndex<String, Correspondence> indexTarget = null;
    protected HashIndex<CorrespondenceRelation, Correspondence> indexRelation = null;
    protected NavigableIndex<Double, Correspondence> indexConfidence = null;

    /**
     * Extended attributes.
     */
    protected Map<String, String> extensions;

    public Alignment() {
        init(true, true, true, true);
    }
    
    public Alignment(Iterable<Correspondence> correspondences) {
        init(true, true, true, true);
        for(Correspondence c : correspondences)
            this.add(c);
    }
        
    public Alignment(boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence){
        init(indexSource, indexTarget, indexRelation, indexConfidence);
    }
    
    public Alignment(URL url) throws SAXException, IOException{
	this(AlignmentParser.getInputStreamFromURL(url), true, true, true, true);
    }
    
    public Alignment(URL url, boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence) throws SAXException, IOException{
	this(AlignmentParser.getInputStreamFromURL(url), indexSource, indexTarget, indexRelation, indexConfidence);
    }
    
    public Alignment(File f) throws SAXException, IOException{
	this(new FileInputStream(f), true, true, true, true);
    }
    
    public Alignment(File f, boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence) throws SAXException, IOException{
	this(new FileInputStream(f), indexSource, indexTarget, indexRelation, indexConfidence);
    }
    
    public Alignment(InputStream s) throws SAXException, IOException{
        init(true, true, true, true);
        AlignmentParser.parse(s, this);
    }
    
    public Alignment(InputStream s, boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence) throws SAXException, IOException{
        init(indexSource, indexTarget, indexRelation, indexConfidence);
        AlignmentParser.parse(s, this);
    }

    /**
     * Copy constructor which copies all information stores in alignment as well as all correspondences.
     * @param alignment The alignment which shall be copied (deep copy).
     */
    public Alignment(Alignment alignment) {
        this(alignment, true);
    }
    
    /**
     * Copy constructor which copies all information stores in alignment as well as all correspondences (depending on attribute copyCorrespondences).
     * @param alignment The alignment which shall be copied (deep copy).
     * @param copyCorrespondences if true copies all information, if false copies all but no correspondences
     */
    public Alignment(Alignment alignment, boolean copyCorrespondences) {
        init(alignment.indexSource != null, alignment.indexTarget != null, alignment.indexRelation != null, alignment.indexConfidence != null);
        this.method = alignment.method;
        this.type = alignment.type;
        this.level = alignment.level;
        this.onto1 = new OntoInfo(alignment.onto1);
        this.onto2 = new OntoInfo(alignment.onto2);        
        this.extensions = new HashMap<>(alignment.extensions);
        if(copyCorrespondences)
            addAll(alignment);
    }
    
    private void init(boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence){
        this.method = "";
        this.type = "11";
        this.onto1 = new OntoInfo();
        this.onto2 = new OntoInfo();
        this.extensions = new HashMap<>();   

        if(indexSource)
            this.assertIndexOnSource();
        if(indexTarget)
            this.assertIndexOnTarget();
        if(indexRelation)
            this.assertIndexOnRelation();
        if(indexConfidence)
            this.assertIndexOnConfidence();
    }
    
    
    /**
     * Creates a new {@link Correspondence} and adds it to this mapping.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     * @param extensions Extensions.
     */
    public void add(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation, Map<String,Object> extensions) {
        add(new Correspondence(entityOne, entityTwo, confidence, relation, extensions));
    }

    /**
     * Creates a new {@link Correspondence} and adds it to this mapping.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     */
    public void add(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation) {
        add(new Correspondence(entityOne, entityTwo, confidence, relation));
    }

    /**
     * Creates a new {@link Correspondence} and adds it to this mapping.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     */
    public void add(String entityOne, String entityTwo, double confidence) {
        add(new Correspondence(entityOne, entityTwo, confidence));
    }

    /**
     * Creates a new {@link Correspondence} and adds it to this mapping.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param relation The relation that holds between the two entities.
     */
    public void add(String entityOne, String entityTwo, CorrespondenceRelation relation) {
        add(new Correspondence(entityOne, entityTwo, relation));
    }

    /**
     * Creates a new {@link Correspondence} and adds it to this mapping.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     */
    public void add(String entityOne, String entityTwo) {
        add(new Correspondence(entityOne, entityTwo));
    }

    /**
     * Adds the correspondence if not existent or adds the extensions values and updates confidence value.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @param relation The relation that holds between the two entities.
     * @param extensions extensions
     * @return the updated correspondence
     */
    public Correspondence addOrModify(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation, Map<String,Object> extensions) {
        return addOrModify(new Correspondence(entityOne, entityTwo, confidence, relation, extensions));
    }
    
    /**
     * Adds the correspondence if not existent or adds the extensions values and updates confidence value.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param extensions extensions
     * @return the updated correspondence
     */
    public Correspondence addOrModify(String entityOne, String entityTwo, Map<String,Object> extensions) {
        return addOrModify(new Correspondence(entityOne, entityTwo, 1.0, CorrespondenceRelation.EQUIVALENCE, extensions));
    }
    
    /**
     * Adds the correspondence if not existent or adds the extensions values and updates confidence value.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param extensionKey The key of the extension.
     * @param extensionValue The value of the extension.
     * @return The updated correspondence.
     */
    public Correspondence addOrModify(String entityOne, String entityTwo, String extensionKey, Object extensionValue) {
        Correspondence c = new Correspondence(entityOne, entityTwo);
        c.addExtensionValue(extensionKey, extensionValue);
        return addOrModify(c);
    }
    
    /**
     * Adds the correspondence if not existing or adds the extensions values and updates confidence value.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param matcherClass The class of the matcher.
     * @param confidence The additional confidence.
     * @return the updated correspondence
     */
    public Correspondence addAdditionalConfidence(String entityOne, String entityTwo, Class<?> matcherClass, double confidence) {
        Correspondence c = new Correspondence(entityOne, entityTwo);
        c.addAdditionalConfidence(matcherClass, confidence);
        return addOrModify(c);
    }
    
    /**
     * Adds the correspondence if not existent or adds the extensions values and updates confidence value.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param matcherClass the class of the matcher
     * @param explanation the explanation for a correspondence
     * @return the updated correspondence
     */
    public Correspondence addAdditionalExplanation(String entityOne, String entityTwo, Class<?> matcherClass, String explanation) {
        Correspondence c = new Correspondence(entityOne, entityTwo);
        c.addAdditionalExplanation(matcherClass, explanation);
        return addOrModify(c);
    }
    
    /**
     * Adds the correspondence if not existent or adds the extensions values and updates confidence value.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param matcherClass the class of the matcher
     * @param confidence the additional confidence
     * @param explanation the explanation for a correspondence
     * @return the updated correspondence
     */
    public Correspondence addAdditionalConfidenceAndExplanation(String entityOne, String entityTwo, Class<?> matcherClass, double confidence, String explanation) {
        Correspondence c = new Correspondence(entityOne, entityTwo);
        c.addAdditionalConfidence(matcherClass, confidence);
        c.addAdditionalExplanation(matcherClass, explanation);
        return addOrModify(c);
    }
    
    /**
     * Adds the correspondence if not existent or adds the extensions values and updates confidence value.
     * @param correspondence Correspondence to be added.
     * @return the updated correspondence
     */
    public Correspondence addOrModify(Correspondence correspondence) {
        ResultSet<Correspondence> r = this.retrieve(
                QueryFactory.and(
                    QueryFactory.equal(Correspondence.SOURCE, correspondence.getEntityOne()),
                    QueryFactory.equal(Correspondence.TARGET, correspondence.getEntityTwo()),
                    QueryFactory.equal(Correspondence.RELATION, correspondence.getRelation())
                ));
        Iterator<Correspondence> iterator = r.iterator();
        if (!iterator.hasNext()) {
           this.add(correspondence);
           return correspondence;            
        }
        Correspondence result = iterator.next();
        this.remove(result);
        result.getExtensions().putAll(correspondence.getExtensions());
        result.setConfidence(correspondence.getConfidence());
        this.add(result);
        return result;
    }
    
    
    /**
     * Adds the correspondence if not existing.
     * In case it already exists, adds the extensions values and updates confidence value(but only if it increases the confidence value).
     * @param c Correspondence to be added
     * @return the updated Correspondence
     */
    public Correspondence addOrUseHighestConfidence(Correspondence c) {
        ResultSet<Correspondence> r = this.retrieve(
                QueryFactory.and(
                    QueryFactory.equal(Correspondence.SOURCE, c.getEntityOne()),
                    QueryFactory.equal(Correspondence.TARGET, c.getEntityTwo()),
                    QueryFactory.equal(Correspondence.RELATION, c.getRelation())
                ));
        Iterator<Correspondence> iterator = r.iterator();
        if (!iterator.hasNext()) {
           this.add(c);
           return c;
        }
        Correspondence result = iterator.next();
        result.getExtensions().putAll(c.getExtensions());
        if(c.getConfidence() > result.getConfidence()){ // set the confidence only if higher
            this.remove(result);
            result.setConfidence(c.getConfidence());
            this.add(result);
            //this.update(Arrays.asList(result), Arrays.asList(result));
        }
        return result;
    }
    
    /**
     * Adds the correspondence if not existent.
     * In case it already exists, updates confidence value only if it increases.
     * @param entityOne URI of the entity from the source ontology as String.
     * @param entityTwo URI of the entity from the target ontology as String.
     * @param confidence The confidence of the mapping.
     * @return the updated Correspondence
     */
    public Correspondence addOrUseHighestConfidence(String entityOne, String entityTwo, double confidence) {
        return addOrUseHighestConfidence(new Correspondence(entityOne, entityTwo, confidence));
    }

    /**
     * Returns the specified correspondence (source, target, relation). If not available it returns null.
     * @param source Source URI.
     * @param target Target URI.
     * @param relation Relation that holds between the URIs.
     * @return mapping correspondence or null
     */
    public Correspondence getCorrespondence(String source, String target, CorrespondenceRelation relation) {
        ResultSet<Correspondence> r = this.retrieve(
                QueryFactory.and(
                    QueryFactory.equal(Correspondence.SOURCE, source),
                    QueryFactory.equal(Correspondence.TARGET, target),
                    QueryFactory.equal(Correspondence.RELATION, relation)
                ));
        Iterator<Correspondence> iterator = r.iterator();
        if (!iterator.hasNext()) {
            return null;
        }
        Correspondence result = iterator.next();
        if (iterator.hasNext()) {
            LOGGER.error("Alignment contains more than one correspondence with source, target, relation. Maybe equals " +
                    "and/or hashcode of Correspondence are overridden. " +
                    "A mapping correspondence is equal when source, target and relation are equal.");
        }
        return result;
    }

    /**
     * Returns correspondences involving the specified source AND target.
     * @param source Source (URI)
     * @param target Target (URI)
     * @return Iterable of correspondences involving source AND target (at the same time).
     */
    public Iterable<Correspondence> getCorrespondencesSourceTarget(String source, String target) {
        return this.retrieve(QueryFactory.and(QueryFactory.equal(Correspondence.SOURCE, source), QueryFactory.equal(Correspondence.TARGET, target)));
    }

    /**
     * Obtain an iterator for all correspondences where the given source is involved.
     * @param source The source that shall be looked up.
     * @return Iterable over {@link Correspondence}. Note: If there is no match, the iterable is not null.
     */
    public Iterable<Correspondence> getCorrespondencesSource(String source) {
        return this.retrieve(QueryFactory.equal(Correspondence.SOURCE, source));
    }

    /**
     * Check whether the specified {@code source} is contained in this alignment instance.
     * @param source The source to be looked up.
     * @return True if source is contained, else false.
     */
    public boolean isSourceContained(String source) {
        return getCorrespondencesSource(source).iterator().hasNext();
    }

    /**
     * Obtain an iterator for all correspondences where the given source and the given relation are involved.
     * @param source The source that shall be looked up.
     * @param relation The relation that shall hold between the specified source and an arbitrary target.
     * @return Iterable over {@link Correspondence}.
     */
    public Iterable<Correspondence> getCorrespondencesSourceRelation(String source, CorrespondenceRelation relation) {
        return this.retrieve(
                QueryFactory.and(
                    QueryFactory.equal(Correspondence.SOURCE, source),
                    QueryFactory.equal(Correspondence.RELATION, relation)
                )
        );
    }

    /**
     * Check whether the given source and the given relation are contained in this alignment.
     * @param source The source that shall be looked up.
     * @param relation The relation that shall hold between the specified source and an arbitrary target.
     * @return True if correspondence with the specified criteria could be found, else false.
     */
    public boolean isSourceRelationContained(String source, CorrespondenceRelation relation) {
        return getCorrespondencesSourceRelation(source, relation).iterator().hasNext();
    }
    
    public Iterable<Correspondence> getCorrespondencesTarget(String target) {
        return this.retrieve(QueryFactory.equal(Correspondence.TARGET, target));
    }

    /**
     * Check whether the specified {@code target} is contained in this alignment instance.
     * @param target The target to be looked up.
     * @return True if target is contained, else false.
     */
    public boolean isTargetContained(String target) {
        return getCorrespondencesTarget(target).iterator().hasNext();
    }

    /**
     * Obtain an iterator for all correspondences where the given target and the given relation are involved.
     * @param target The target that shall be looked up.
     * @param relation The relation that shall hold between the specified target and an arbitrary source.
     * @return Iterable over {@link Correspondence}.
     */
    public Iterable<Correspondence> getCorrespondencesTargetRelation(String target, CorrespondenceRelation relation) {
        return this.retrieve(
                QueryFactory.and(
                    QueryFactory.equal(Correspondence.TARGET, target),
                    QueryFactory.equal(Correspondence.RELATION, relation)
                )
        );
    }

    /**
     * Check whether the given target and the given relation are contained in this alignment.
     * @param target The target that shall be looked up.
     * @param relation The relation that shall hold between the specified target and an arbitrary source.
     * @return True if correspondence with the specified criteria could be found, else false.
     */
     public boolean isTargetRelationContained(String target, CorrespondenceRelation relation) {
        return getCorrespondencesTargetRelation(target, relation).iterator().hasNext();
    }

    /**
     * Obtain an iterator for all correspondences where the given relation are involved.
     * @param relation The relation that shall hold between the specified target and an arbitrary source.
     * @return Iterable over {@link Correspondence}.
     */
    public Iterable<Correspondence> getCorrespondencesRelation(CorrespondenceRelation relation) {
        return this.retrieve(QueryFactory.equal(Correspondence.RELATION, relation));
    }

    /**
     * Check whether the given relation are contained in this alignment.
     * @param relation The relation that shall hold.
     * @return True if correspondence with the specified criteria could be found, else false.
     */
     public boolean isRelationContained(CorrespondenceRelation relation) {
        return getCorrespondencesRelation(relation).iterator().hasNext();
    }

    /**
     * Removes correspondences where source AND target appear (at the same time), i.e.
     * {@code <source, target, someConfidence>}.
     * @param source Source concept (typically URI)
     * @param target Target concept (typically URI)
     */
    public void removeCorrespondencesSourceTarget(String source, String target) {
        for(Correspondence c : getCorrespondencesSourceTarget(source, target)){
            this.remove(c);
        }
    }
    
    public void removeCorrespondencesSource(String source) {
        for(Correspondence c : getCorrespondencesSource(source)){
            this.remove(c);
        }
    }
    
    public void removeCorrespondencesTarget(String target) {
        for(Correspondence c : getCorrespondencesTarget(target)){
            this.remove(c);
        }
    }
    
    /**
     * Removes all correspondence extensions.
     */
    public void removeCorrespondenceExtensions() {
        for(Correspondence c : this){
            c.removeExtensions();
        }
    }
    
    /**
     * Remove all correspondence extensions which appear in blacklist.
     * @param blacklist the extension keys to be removed.
     */
    public void removeCorrespondenceExtensions(Iterable<String> blacklist){
        for(Correspondence c : this){
            c.removeExtensions(blacklist);
        }
    }
    
    /**
     * Removes all correspondence extensions, but keep the extensions with keys appearing in whitelist.
     * @param whitelist the extensions keys which should be kept
     */
    public void removeCorrespondenceExtensionsNotIn(Set<String> whitelist){
        for(Correspondence c : this){
            c.removeExtensionsNotIn(whitelist);
        }
    }
    
    /**
     * Returns a new alignment which contains only correspondences above or equal the given threshold (it will not modify the current object).
     * @param threshold Threshold for cutting (correspondences greater than or equal the threshold will be added).
     * @return A new alignment with filtered correspondences. This alignment stays untouched from the operation.
     */
    public Alignment cut(double threshold){
        assertIndexOnConfidence();
        Alignment m = new Alignment(this, false);
        ResultSet<Correspondence> result = this.retrieve(QueryFactory.greaterThanOrEqualTo(Correspondence.CONFIDENCE, threshold));
        for(Correspondence c : result){
            m.add(c);
        }
        return m;
    }

    /**
     * Returns a random sample of correspondences.
     * If the parameter n is greater than the alignment size, an IllegalArgumentException is thrown.
     * @param n the number of correspondences to be returned. Should be smaller than the size of this alignment.
     * @return a new Alignment which contains the sampled correspondences.
     */
    public Alignment sample(int n){
        return sample(n, new Random());
    }
    
    /**
     * Returns a random sample of correspondences.
     * If the parameter n is greater than the alignment size, an IllegalArgumentException is thrown.
     * If the random parameter is always in the same state e.g. always providing a fresh instance with the same seed like new Random(1234)
     * or setting the seed of one random instance always to the same value,  then a smaller subset (sample with 10 percent) will 
     * be contained in the larger subset (sample with 20 percent).
     * @param n the number of correspondences to be returned. Should be smaller than the size of this alignment.
     * @param rnd the source of randomness.
     * @return a new Alignment which contains the sampled correspondences.
     */
    public Alignment sample(int n, Random rnd){
        if(n > this.size() || n < 0) {
            throw new IllegalArgumentException("Parameter n is out of range (smaller zero or greater than the size of current alignment.");
        }
        ArrayList<Correspondence> correspondenceList = new ArrayList<>(this);
        Collections.shuffle(correspondenceList, rnd);
        Alignment samples = new Alignment(this, false);
        samples.addAll(correspondenceList.subList(0, n));
        return samples;
    }
    
    /**
     * Returns a random sample of correspondences.
     * If the parameter n is greater than the alignment size, an IllegalArgumentException is thrown.
     * If the random parameter is always in the same, then a smaller subset (sample with 10 percent) will 
     * be contained in the larger subset (sample with 20 percent).
     * @param n the number of correspondences to be returned. Should be smaller than the size of this alignment.
     * @param seed the seed for random.
     * @return a new Alignment which contains the sampled correspondences.
     */
    public Alignment sample(int n, long seed){
        return sample(n, new Random(seed));
    }
    
    /**
     * Returns a random sample of correspondences.
     * If the parameter n is greater than the alignment size, the full alignment (copy of this alignment) is returned.
     * @param fraction the number of correspondences to be returned. Should be smaller than the size of this alignment.
     * @return a new Alignment which contains the sampled correspondences.
     */
    public Alignment sampleByFraction(double fraction){
        return sampleByFraction(fraction, new Random());
    }
    
    /**
     * Returns a random sample of correspondences.
     * If the parameter n is greater than the alignment size, the full alignment (copy of this alignment) is returned.
     * If the random parameter is always in the same state e.g. always providing a fresh instance with the same seed like new Random(1234)
     * or setting the seed of one random instance always to the same value,  then a smaller subset (sample with 10 percent) will 
     * be contained in the larger subset (sample with 20 percent).
     * @param fraction the number of correspondences to be returned. Should be smaller than the size of this alignment.
     * @param rnd the source of randomness.
     * @return a new Alignment which contains the sampled correspondences.
     */
    public Alignment sampleByFraction(double fraction, Random rnd){
        if(fraction < 0.0 || fraction > 1.0) {
            throw new IllegalArgumentException("Fraction is out of range (smaller zero or greater one");
        }
        return sample((int)Math.round((double)this.size() * fraction), rnd);
    }
    
    /**
     * Returns a random sample of correspondences.
     * If the parameter n is greater than the alignment size, the full alignment (copy of this alignment) is returned.
     * If the random parameter is always the same, then a smaller subset (sample with 10 percent) will 
     * be contained in the larger subset (sample with 20 percent).
     * @param fraction the number of correspondences to be returned. Should be smaller than the size of this alignment.
     * @param seed the source of randomness.
     * @return a new Alignment which contains the sampled correspondences.
     */
    public Alignment sampleByFraction(double fraction, long seed){
        return sampleByFraction(fraction, new Random(seed));
    }
    
    /**
     * Returns all extension keys of correspondences in this alignment.
     * @return a set of all correspondence extension keys.
     */
    public Set<String> getDistinctCorrespondenceExtensionKeys(){
        Set<String> keys = new HashSet<>();
        for(Correspondence c : this){
            keys.addAll(c.getExtensions().keySet());
        }
        return keys;
    }
    
    /**
     * Returns all used (sub-) extension keys of correspondences which can be used with method getAdditionalConfidence.
     * @return a set of all correspondence (sub-) extension keys.
     */
    public Set<String> getDistinctCorrespondenceConfidenceKeys(){
        Set<String> keys = new HashSet<>();
        for(Correspondence c : this){
            keys.addAll(c.getAdditionalConfidences().keySet());
        }
        return keys;
    }
    
    /**
     * Reverse the alignment (switches sources with targets) and does not change the relation.
     * It creates a new Alignment.
     * If the relation should be changed use the {@link #reverse()} method.
     * @return NEW reversed alignment.
     */
    public Alignment reverseWithoutRelationChange() {
        Alignment result = new Alignment(this, false);//copy constructor but no copy of correspondences
        for(Correspondence c : this){
            result.add(c.reverseWithoutRelationChange());
        }
        return result;
    }
    
    /**
     * Reverse the alignment (switches sources with targets) and adjust(reverse) the relation.
     * It creates a new Alignment.
     * If the relation should not be changed use the {@link #reverseWithoutRelationChange()} method.
     * @return New reversed alignment.
     */
    public Alignment reverse() {
        Alignment result = new Alignment(this, false);//copy constructor but no copy of correspondences
        for(Correspondence c : this){
            result.add(c.reverse());
        }
        return result;
    }

    /**
     * Create the subtraction between the two given alignments. Only copies the alignment and not further infos like
     * onto or extensions.
     * @param alignment_1 Set 1.
     * @param alignment_2 Set 2.
     * @return Subtraction alignment.
     */
    public static Alignment subtraction(Alignment alignment_1, Alignment alignment_2) {
        Alignment result = new Alignment();
        result.addAll(alignment_1);
        result.removeAll(alignment_2);
        return result;
    }
    
    /**
     * Create the intersection between the two given alignments. Only copies the alignment and not further infos like
     * onto or extensions.
     * @param alignment_1 Set 1.
     * @param alignment_2 Set 2.
     * @return Intersection alignment.
     */
    public static Alignment intersection(Alignment alignment_1, Alignment alignment_2) {
        Alignment result = new Alignment();
        result.addAll(alignment_1);
        result.addAll(alignment_2);
        result.retainAll(alignment_1);
        result.retainAll(alignment_2);
        return result;
    }

    /**
     * Create the union between the two given alignments. Only copies the alignment and not further infos like onto or
     * extensions.
     * @param alignment_1 Set 1.
     * @param alignment_2 Set 2.
     * @return Union alignment.
     */
    public static Alignment union(Alignment alignment_1, Alignment alignment_2) {
        Alignment result = new Alignment();
        result.addAll(alignment_1);
        result.addAll(alignment_2);
        return result;
    }

    /**
     * Switches sources with targets. Does not change the relation.
     * This method is only for erroneous alignments where a matcher switched the source with the target ontology.
     * @deprecated use function reverse
     * @param alignment The alignment where the source shall be switched with the target.
     * @return Edited alignment.
     */
    public static Alignment switchSourceWithTarget(Alignment alignment){
        return alignment.reverseWithoutRelationChange();
    }

    /**
     * Serialize this mapping to a string.
     * Better use the function serialize(File f) if the mapping is huge.
     * @return the mapping in the alignment xml format
     * @throws IOException An IOException.
     */
    public String serialize() throws IOException{
        return AlignmentSerializer.serialize(this);
    }
    
    /**
     * Serialize this mapping directly to a given file.
     * This also works if the alignment is huge.
     * @param file The file for writing the mapping.
     * @throws IOException An IOException.
     */
    public void serialize(File file) throws IOException{
        AlignmentSerializer.serialize(this, file);
    }
    
    /**
     * Serialize this mapping directly to a given file in CSV format.
     * This also works if the alignment is huge.
     * @param file The file for writing the mapping.
     * @throws IOException An IOException.
     */
    public void serializeToCSV(File file) throws IOException{
        AlignmentSerializer.serializeToCSV(this, file);
    }
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public OntoInfo getOnto2() {
        return onto2;
    }

    public void setOnto2(OntoInfo onto2) {
        this.onto2 = onto2;
    }

    public OntoInfo getOnto1() {
        return onto1;
    }

    public void setOnto1(OntoInfo onto1) {
        this.onto1 = onto1;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
    
    public List<Correspondence> getConfidenceOrderedMapping(){
        //slower
        //ResultSet<Correspondence> rs = this.retrieve(QueryFactory.all(Correspondence.class), 
        //      QueryFactory.queryOptions(QueryFactory.orderBy(QueryFactory.descending(Correspondence.CONFIDENCE))));
        ArrayList<Correspondence> list = new ArrayList<>(this);
        list.sort(new CorrespondenceConfidenceComparator());
        return list;
    }

    public double getMinimalConfidence(){
        List<Correspondence> list = getConfidenceOrderedMapping();
        if(list == null || list.isEmpty()){
            LOGGER.error("There are no confidences. Returning 0.0 as minimum confidence.");
            return 0.0;
        }
        return list.get(0).getConfidence();
    }

    public double getMaximalConfidence() {
        List<Correspondence> list = getConfidenceOrderedMapping();
        if(list == null || list.isEmpty()){
            LOGGER.error("There are no confidences. Returning 1.0 as maximum confidence.");
            return 1.0;
        }
        return list.get(list.size() - 1).getConfidence();
    }
    
    public void assertIndexOnSource(){
        if(this.indexSource == null){
            this.indexSource = HashIndex.onAttribute(Correspondence.SOURCE);
            this.addIndex(this.indexSource);
        }
    }
    
    public void assertIndexOnTarget(){
        if(this.indexTarget == null){
            this.indexTarget = HashIndex.onAttribute(Correspondence.TARGET);
            this.addIndex(this.indexTarget);
        }
    }
    
    public void assertIndexOnRelation(){
        if(this.indexRelation == null){
            this.indexRelation = HashIndex.onAttribute(Correspondence.RELATION);
            this.addIndex(this.indexRelation);
        }
    }
    
    public void assertIndexOnConfidence(){
        if(this.indexConfidence == null){
            this.indexConfidence = NavigableIndex.onAttribute(Correspondence.CONFIDENCE);
            this.addIndex(this.indexConfidence);
        }
    }
    
    public Iterable<String> getDistinctSources(){
        if(this.indexSource == null){
            return this.stream().map(c -> c.entityOne).collect(Collectors.toSet());
        }else{
            return this.indexSource.getDistinctKeys(noQueryOptions());
        }
    }
    
    public Set<String> getDistinctSourcesAsSet(){
        return makeSet(this.getDistinctSources());
    }
    
    public Iterable<String> getDistinctTargets(){
        if(this.indexTarget == null){
            return this.stream().map(c -> c.entityTwo).collect(Collectors.toSet());
        }else{
            return this.indexTarget.getDistinctKeys(noQueryOptions());
        }
    }
    
    public Set<String> getDistinctTargetsAsSet(){
        return makeSet(this.getDistinctTargets());
    }
    
    public Iterable<CorrespondenceRelation> getDistinctRelations(){
        if(this.indexRelation == null){
            return this.stream().map(c -> c.relation).collect(Collectors.toSet());
        }else{
            return this.indexRelation.getDistinctKeys(noQueryOptions());
        }
    }
    
    public Set<CorrespondenceRelation> getDistinctRelationsAsSet(){
        return makeSet(this.getDistinctRelations());
    }
    
    
    public Iterable<Double> getDistinctConfidences(){
        if(this.indexConfidence == null){
            return this.stream().map(c -> c.confidence).collect(Collectors.toSet());
        }else{
            return this.indexConfidence.getDistinctKeys(noQueryOptions());
        }
    }
    
    public Set<Double> getDistinctConfidencesAsSet(){
        return makeSet(this.getDistinctConfidences());
    }
       
    /**
     * Creates a set from an iterable.
     * @param <T> the type of the iterable
     * @param iterable iterable which should be converted to set
     * @return set containing all elements of iterable
     */
    public static <T> Set<T> makeSet(Iterable<T> iterable) {
        Set<T> set = new HashSet<>();
        for(T element : iterable){
            set.add(element);
        }
        return set;
    }
    
    /**
     * Creates a list from an iterable.
     * @param <T> the type of the iterable
     * @param iterable iterable which should be converted to list
     * @return list containing all elements of iterable
     */
    public static <T> List<T> makeList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for(T element : iterable){
            list.add(element);
        }
        return list;
    }
    
    /**
     * Returns the size of the iterator.
     * @param i the iterator to use
     * @return the size of the iterator.
     */
    public static long iteratorSize(Iterator<?> i) {
        long count = 0;
        while(i.hasNext()){
            i.next();
            count++;
        }
        return count;
    }
    
    /**
     * Returns the size of the iterable.
     * @param i the iterable to use
     * @return the size of the iterable.
     */
    public static long iterableSize(Iterable<?> i) {
        return iteratorSize(i.iterator());
    }

    /**
     * Obtain the value of an extension.
     * @param extensionUri The URI identifying the extension.
     *                     Note that many default extension URIs are contained in {@link DefaultExtensions}.
     * @return The value of the extension as String, null if there is no value.
     */
    public String getExtensionValue(String extensionUri){
        if(extensions == null) return null;
        return extensions.get(extensionUri);
    }

    /**
     * Set the value for an extension.
     * @param extensionUri The URI identifying the extension.
     *                     Note that many default extension URIs are contained in {@link DefaultExtensions}.
     * @param extensionValue The value of the extension to be set.
     */
    public void addExtensionValue(String extensionUri, String extensionValue){
        if(extensions == null) extensions = new HashMap<>();
        extensions.put(extensionUri, extensionValue);
    }

    /**
     * Obtain the alignment extensions as Map.
     * @return The map is build as follows:
     * <ul>
     *  <li>key: extension URI</li>
     *  <li>value: extension value</li>
     * </ul>
     */
    public Map<String, String> getExtensions() { return this.extensions; }

    /**
     * Set the extensions of the alignment. Note that this method will overwrite existing extensions of the
     * alignment.
     * @param extensions The alignment extensions to be set.
     */
    public void setExtensions(Map<String, String> extensions) {
        this.extensions = extensions;
    }
    
    private static final String NEWLINE = System.getProperty("line.separator");
            
    /**
     * ToString method which returns the alignment in multiple lines (each correspondence in one line) to have a better overview.
     * @return a string which contains the alignment in multiple lines.
     */
    public String toStringMultiline(){
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(NEWLINE);
        for(Correspondence c : this){
            sb.append("    ").append(c.toString()).append(",").append(NEWLINE);
        }
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * ToString method which returns the alignment in multiple lines (each correspondence in one line) to have a better overview.
     * In comparison to toStringMultiline, this method prints also the alignment and correspondence extensions and onto infos.
     * @return a string which contains the alignment in multiple lines.
     */
    public String toStringMultilineInfo(){
        StringBuilder sb = new StringBuilder();
        sb.append("Alignment{").append(NEWLINE);
        sb.append("  indexSource=").append(indexSource!=null)
                .append(", indexTarget=").append(indexTarget!=null)
                .append(", indexRelation=").append(indexRelation!=null)
                .append(", indexConfidence=").append(indexConfidence!=null)
                .append(",").append(NEWLINE);
        
        if(this.onto1 != null)
            sb.append("  source=").append(this.onto1.toString()).append(",").append(NEWLINE);
        if(this.onto2 != null)
            sb.append("  target=").append(this.onto2.toString()).append(",").append(NEWLINE);
        if(this.method != null && !this.method.isEmpty())
            sb.append("  method=").append(this.method).append(",").append(NEWLINE);
        if(this.type != null && !this.type.isEmpty())
            sb.append("  type=").append(this.type).append(",").append(NEWLINE);
        if(this.level != null && !this.level.isEmpty())
            sb.append("  level=").append(this.level).append(",").append(NEWLINE);
        if(this.extensions != null){
            for(Entry<String, String> e : this.extensions.entrySet()){
                sb.append("  ").append(e.getKey()).append("=").append(e.getValue()).append(",").append(NEWLINE);
            }
        }
        
        sb.append("  [").append(NEWLINE);
        for(Correspondence c : this){
            sb.append("    ").append(c.toStringWithExtensions()).append(",").append(NEWLINE);
        }
        sb.append("  ]").append(NEWLINE);
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * ToString method which returns the alignment in one line.
     * @return a string which contains the alignment in one line.
     */
    public String toStringOneLine(){
        Iterator<Correspondence> it = iterator();
        if (! it.hasNext())
            return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            Correspondence e = it.next();
            sb.append(e.toString());
            if (! it.hasNext())
                return sb.append(']').toString();
            sb.append(',').append(' ');
        }
    }

    @Override
    public String toString() {
        return toStringMultiline();
    }
}
