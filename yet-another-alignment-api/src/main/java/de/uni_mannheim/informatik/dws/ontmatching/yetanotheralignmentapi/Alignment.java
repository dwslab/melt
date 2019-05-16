package de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi;

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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Data structure to represent an Alignment.
 * An alignment is a collection of multiple {@link Correspondence} instances.
 * An alignment is also known as "mapping" or "mappings".
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
    protected HashMap<String, String> extensions;

    public Alignment() {
        init(true, true, true, true);
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
    
    public Alignment(Alignment alignment) {
        init(alignment.indexSource != null, alignment.indexTarget != null, alignment.indexRelation != null, alignment.indexConfidence != null);
        addAll(alignment);
    }
    
    private void init(boolean indexSource, boolean indexTarget, boolean indexRelation, boolean indexConfidence){
        this.method = "";
        this.type = "11";
        this.onto1 = new OntoInfo();
        this.onto2 = new OntoInfo();

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
     * @param extensions extenions
     */
    public void add(String entityOne, String entityTwo, double confidence, CorrespondenceRelation relation, Map<String,String> extensions) {
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
            LOGGER.error("Alignment contains more than one correspondence with source, target, relation. Maybe equals and/or hashcode of Correspondence are overridden. " +
                    "A mapping correspondence is equal when source, target and relation are equal.");
        }
        return result;
    }
    
    public Iterable<Correspondence> getCorrespondencesSourceTarget(String source, String target) {
        return this.retrieve(QueryFactory.and(QueryFactory.equal(Correspondence.SOURCE, source), QueryFactory.equal(Correspondence.TARGET, target)));
    }

    /**
     * Obtain an iterator for all mappins where the given source is involved.
     * @param source The source that shall be looked up.
     * @return Iterable over {@link Correspondence}. Note: If there is no match, the iterable is not null.
     */
    public Iterable<Correspondence> getCorrespondencesSource(String source) {
        return this.retrieve(QueryFactory.equal(Correspondence.SOURCE, source));
    }
    
    public boolean isSourceContained(String source) {
        return getCorrespondencesSource(source).iterator().hasNext();
    }

    /**
     *
     * @param source
     * @param relation
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
    
    public boolean isSourceRelationContained(String source, CorrespondenceRelation relation) {
        return getCorrespondencesSourceRelation(source, relation).iterator().hasNext();
    }
    
    public Iterable<Correspondence> getCorrespondencesTarget(String target) {
        return this.retrieve(QueryFactory.equal(Correspondence.TARGET, target));
    }
    
    public boolean isTargetContained(String target) {
        return getCorrespondencesTarget(target).iterator().hasNext();
    }
    
    public Iterable<Correspondence> getCorrespondencesTargetRelation(String target, CorrespondenceRelation relation) {
        return this.retrieve(
                QueryFactory.and(
                    QueryFactory.equal(Correspondence.TARGET, target),
                    QueryFactory.equal(Correspondence.RELATION, relation)
                )
        );
    }
    
     public boolean isTargetRelationContained(String target, CorrespondenceRelation relation) {
        return getCorrespondencesTargetRelation(target, relation).iterator().hasNext();
    }
    
    
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
     * Serialize this mapping directly to a given file.
     * This also works if the alignment is huge.
     * @param f The file for writing the mapping.
     * @throws IOException 
     */
    public void serialize(File f) throws IOException{
        AlignmentSerializer.serialize(this, f);
    }
    
    /**
     * Returns a new alignment which contains only correspondences above or equal the given threshold (it will not modify the current object).
     * @param threshold threshold for cutting
     * @return a new alignment with filtered correspondences
     */
    public Alignment cut(double threshold ){
        assertIndexOnConfidence();
        Alignment m = new Alignment(this.indexSource != null, this.indexTarget != null, this.indexRelation != null, this.indexConfidence != null);
        ResultSet<Correspondence> result = this.retrieve(QueryFactory.greaterThanOrEqualTo(Correspondence.CONFIDENCE, threshold));
        //m.addAll(result.stream().collect(Collectors.toList()));  //makes an arraylist 
        //List<Correspondence> list = new ArrayList<>(result.size());
        //for(Correspondence c : result){
        //    list.add(c);
        //}
        //m.addAll(list);        
        for(Correspondence c : result){
            m.add(c);
        }
        return m;
    }
    
    
    
    
    /**
     * Create the intersection between the two given alignments.
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
     * Create the union between the two given alignments.
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
     * Serialize this mapping to a string.
     * Better use the function serialize(File f) if the mapping is huge.
     * @return the mapping in the alignment xml format
     * @throws IOException 
     */
    public String serialize() throws IOException{
        return AlignmentSerializer.serialize(this);
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
        ArrayList<Correspondence> list = new ArrayList<>(this);
        list.sort(new CorrespondenceConfidenceComparator());
        return list;
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
    
    public Iterable<String> getDistinctTargets(){
        if(this.indexTarget == null){
            return this.stream().map(c -> c.entityTwo).collect(Collectors.toSet());
        }else{
            return this.indexTarget.getDistinctKeys(noQueryOptions());
        }
    }
    
    public Iterable<CorrespondenceRelation> getDistinctRelations(){
        if(this.indexRelation == null){
            return this.stream().map(c -> c.relation).collect(Collectors.toSet());
        }else{
            return this.indexRelation.getDistinctKeys(noQueryOptions());
        }
    }
    
    
    public Iterable<Double> getDistinctConfidences(){
        if(this.indexConfidence == null){
            return this.stream().map(c -> c.confidence).collect(Collectors.toSet());
        }else{
            return this.indexConfidence.getDistinctKeys(noQueryOptions());
        }
    }
    

    /**
     * Obtain the value of an extension.
     * @param extensionUri The URI identifying the extension.
     * @return The value of the extension as String, null if there is no value.
     */
    public String getExtensionValue(String extensionUri){
        if(extensions == null) return null;
        return extensions.get(extensionUri);
    }


    /**
     * Set the value for an extension.
     * @param extensionUri The URI identifying the extension.
     * @param extensionValue The value of the extension to be set.
     */
    public void addExtensionValue(String extensionUri, String extensionValue){
        if(extensions == null) extensions = new HashMap<>();
        extensions.put(extensionUri, extensionValue);
    }

    public HashMap<String, String> getExtensions() { return this.extensions; }
}
