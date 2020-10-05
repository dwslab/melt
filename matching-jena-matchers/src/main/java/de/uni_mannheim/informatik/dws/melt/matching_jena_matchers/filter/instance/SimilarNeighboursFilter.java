package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BaseFilterWithSetComparison;
import com.googlecode.cqengine.query.QueryFactory;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.SetSimilarity;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Checks for each instance mapping, how many already matched neighbours it has.
 * 
 * Source_Subject ------Source_Property------Source_Object
 *      |                                         |
 * subjectCorrespondence                      objecCorrespondence
 *      |                                         |
 * Target_Subject ------Target_Property------Target_Object
 * 
 */
public class SimilarNeighboursFilter extends BaseFilterWithSetComparison{

    /**
     * The minmum confidence for which a resource(neighbour) mapping is counted. Compared with greater or equal.
     */
    private double minResourceConfidence;    
    /**
     * Predicate to decide which properties should be counted.
     */
    private Predicate<Property> shouldPropertyBeCounted;
    
    /**
     * A function which processes a literal and returns some comparable (equals/hashCode) representation (usually a normalized text).
     */
    private Function<Literal, Object> literalProcessingFunction;
    
    /**
     * A function which returns a set of elements (uri as string and/or text filtered with literalprocessing function) which should be ignored.
     */
    private Function<Resource, Set<Object>> excludeNeighbours;
    
    private boolean useIngoing;
    private boolean useOutgoing;
    private boolean useResource;
    private boolean useLiteral;
    
    private boolean addNeighboursToCorrespondence = false;

    /**
     * Constructor
     * @param minResourceConfidence the confidence for which a neighour is counted as a mapping (greater or equal).
     * @param shouldPropertyBeCounted Predicate to decide which properties should be counted.
     * @param literalProcessingFunction A function which processes a literal and returns some comparable (equals/hashCode) representation (usually a normalized text) .
     * @param excludeNeighbours A function which returns a set of elements (uri as string and/or text filtered with literalprocessing function) which should be ignored.
     * @param useIngoing use ingoing edges
     * @param useOutgoing use outgoing edges
     * @param useResource use resources
     * @param useLiteral use literals
     * @param threshold The filtering threshold which should be larger or equal to be a valid match. Computation is based on set similarity.
     * @param setSimilatity The set similarity to choose when computing similarity value between the two distinct property sets.
     */
    public SimilarNeighboursFilter(double minResourceConfidence, Predicate<Property> shouldPropertyBeCounted, Function<Literal, Object> literalProcessingFunction,
            Function<Resource, Set<Object>> excludeNeighbours, boolean useIngoing, boolean useOutgoing, boolean useResource, boolean useLiteral, 
            double threshold, SetSimilarity setSimilatity) {
        super(threshold, setSimilatity);
        this.minResourceConfidence = minResourceConfidence;
        this.shouldPropertyBeCounted = shouldPropertyBeCounted;
        this.literalProcessingFunction = literalProcessingFunction;
        this.excludeNeighbours = excludeNeighbours;
        this.useIngoing = useIngoing;
        this.useOutgoing = useOutgoing;
        this.useResource = useResource;
        this.useLiteral = useLiteral;
    }
    
    /**
     * Constructor which uses ingoing and outgoing edges as well as resources and literals.
     * @param minResourceConfidence the confidence for which a neighour is counted as a mapping (greater or equal).
     * @param shouldPropertyBeCounted Predicate to decide which properties should be counted.
     * @param literalProcessingFunction A function which processes a literal and returns some comparable (equals/hashCode) representation (usually a normalized text) .
     * @param excludeNeighbours A function which returns a set of elements (uri and text) which should be ignored.
     * @param threshold The filtering threshold which should be larger or equal to be a valid match. Computation is based on set similarity.
     * @param setSimilatity The set similarity to choose when computing similarity value between the two distinct property sets.
     */
    public SimilarNeighboursFilter(double minResourceConfidence, Predicate<Property> shouldPropertyBeCounted, Function<Literal, Object> literalProcessingFunction,
            Function<Resource, Set<Object>> excludeNeighbours, double threshold, SetSimilarity setSimilatity) {
        this(minResourceConfidence, shouldPropertyBeCounted, literalProcessingFunction,
                excludeNeighbours, true, true, true, true, threshold, setSimilatity);
    }
    
    /**
     * Constructor
     * @param minResourceConfidence the confidence for which a neighour is counted as a mapping (greater or equal).
     * @param shouldPropertyBeCounted Predicate to decide which properties should be counted.
     * @param threshold The filtering threshold which should be larger or equal to be a valid match. Computation is based on set similarity.
     * @param setSimilatity The set similarity to choose when computing similarity value between the two distinct property sets.
     */
    public SimilarNeighboursFilter(double minResourceConfidence, Predicate<Property> shouldPropertyBeCounted, double threshold, SetSimilarity setSimilatity) {
        this(minResourceConfidence, shouldPropertyBeCounted, l->l.getLexicalForm(), r->new HashSet(), threshold, setSimilatity);
    }
    
    
    public SimilarNeighboursFilter(Function<Literal, Object> literalProcessingFunction, double threshold, SetSimilarity setSimilatity) {
        this(0.0, p -> true, literalProcessingFunction, r->new HashSet(), threshold, setSimilatity);
    }
    
    public SimilarNeighboursFilter(double threshold, SetSimilarity setSimilatity){
        this(0.0, p -> true, l->l.getLexicalForm(), r->new HashSet(), threshold, setSimilatity);
    }
    
    public SimilarNeighboursFilter() {
        this(0.0, SetSimilarity.BOOLEAN);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment filteredAlignment = new Alignment(inputAlignment, false);
        for(Correspondence correspondence : inputAlignment){
            Individual sourceIndividual = source.getIndividual(correspondence.getEntityOne());
            Individual targetIndividual = target.getIndividual(correspondence.getEntityTwo());
            if(sourceIndividual == null || targetIndividual == null){
                filteredAlignment.add(correspondence);
                continue;
            }
            
            Neighbours sourceNeighbours = getNeighbours(source, sourceIndividual);
            Neighbours targetNeighbours = getNeighbours(target, targetIndividual);
            
            Set<Object> literalIntersection = new HashSet(sourceNeighbours.getLiterals());
            literalIntersection.retainAll(targetNeighbours.getLiterals());
            
            
            Iterable<Correspondence> i = inputAlignment.retrieve(
                QueryFactory.and(
                    QueryFactory.in(Correspondence.SOURCE, sourceNeighbours.getUriResources()),
                    QueryFactory.in(Correspondence.TARGET, targetNeighbours.getUriResources()),
                    QueryFactory.greaterThanOrEqualTo(Correspondence.CONFIDENCE, minResourceConfidence)
                ));
            Set<String> mappedSources = new HashSet<>();
            Set<String> mappedTargets = new HashSet<>();            
            for(Correspondence c : i){
                mappedSources.add(c.getEntityOne());
                mappedTargets.add(c.getEntityTwo());
            }
            //in case of n:m mappings only the minimum amount of resource is the number of the intersection.
            int resourceIntersection = Math.min(mappedSources.size(), mappedTargets.size());
            
            if(this.addNeighboursToCorrespondence){
                Set<String> neighboursPrint = new HashSet();
                if(mappedSources.size() < mappedTargets.size()){
                    neighboursPrint.addAll(mappedSources);
                }else{
                    neighboursPrint.addAll(mappedTargets);
                }
                for(Object o : literalIntersection){
                    neighboursPrint.add(o.toString());
                }
                //reduce to max 20
                if(neighboursPrint.size() > 20){
                    StringJoiner sj = new StringJoiner(",");
                    Iterator<String> iter = neighboursPrint.iterator();
                    for (int j = 0; j < 20; j++) {
                        sj.add(iter.next());
                    }
                    correspondence.addAdditionalExplanation(this.getClass(), "[" + sj.toString() + "] (reduced to max 20 neighbours)");
                }else{
                    correspondence.addAdditionalExplanation(this.getClass(), "[" + String.join(",", neighboursPrint) + "]");
                }
                
            }
            
            //sum up resource mappings and literal mappings
            int countSourceNeighbours = sourceNeighbours.getUriResources().size() + sourceNeighbours.getLiterals().size();
            int countTargetNeighbours = targetNeighbours.getUriResources().size() + targetNeighbours.getLiterals().size();
            int countIntersection = resourceIntersection + literalIntersection.size();
            
            double value = setSimilarity.compute(countIntersection, countSourceNeighbours, countTargetNeighbours);
            if(value >= this.threshold){
                correspondence.addAdditionalConfidence(this.getClass(), value);
                filteredAlignment.add(correspondence);
            }
        }
        return filteredAlignment;
    }
    
    
    private Neighbours getNeighbours(OntModel model, Individual individual){
        Neighbours neighbours = new Neighbours();
        Set<Object> ignoreNeighbours = this.excludeNeighbours.apply(individual);
        if(useOutgoing){
            StmtIterator outgoingStmts = model.listStatements(individual, null, (RDFNode) null );
            while(outgoingStmts.hasNext()){
                Statement s = outgoingStmts.next();
                if(shouldPropertyBeCounted.test(s.getPredicate()) == false)
                    continue;
                RDFNode object = s.getObject();
                if(object.isURIResource() && useResource){
                    if(object.asResource().equals(individual) == false){ // check for reflexive edges
                        if(ignoreNeighbours.contains(object.asResource().getURI()) == false)
                            neighbours.addResource(object.asResource().getURI());
                    }
                }else if(object.isLiteral() && useLiteral){
                    Object processedLiteral = literalProcessingFunction.apply(object.asLiteral());
                    if(ignoreNeighbours.contains(processedLiteral) == false)
                        neighbours.addLiteral(processedLiteral);
                }
            }
        }
        
        if(useIngoing){
            StmtIterator ingoingStmts = model.listStatements(null, null, individual);
            while(ingoingStmts.hasNext()){
                Statement s = ingoingStmts.next();
                if(shouldPropertyBeCounted.test(s.getPredicate()) == false)
                    continue;
                Resource subject = s.getSubject();
                if(subject.isURIResource()){
                    if(subject.equals(individual) == false){ // check for reflexive edges
                        if(ignoreNeighbours.contains(subject.getURI()) == false)
                            neighbours.addResource(subject.getURI());
                    }
                }//can not be a literal (no outgoing edges)
            }
        }
        return neighbours;
    }

    //getter and setter
    
    public double getMinResourceConfidence() {
        return minResourceConfidence;
    }

    public void setMinResourceConfidence(double minResourceConfidence) {
        this.minResourceConfidence = minResourceConfidence;
    }

    public Predicate<Property> getShouldPropertyBeCounted() {
        return shouldPropertyBeCounted;
    }

    public void setShouldPropertyBeCounted(Predicate<Property> shouldPropertyBeCounted) {
        this.shouldPropertyBeCounted = shouldPropertyBeCounted;
    }

    public Function<Literal, Object> getLiteralProcessingFunction() {
        return literalProcessingFunction;
    }

    public void setLiteralProcessingFunction(Function<Literal, Object> literalProcessingFunction) {
        this.literalProcessingFunction = literalProcessingFunction;
    }

    public boolean isUseIngoing() {
        return useIngoing;
    }

    public void setUseIngoing(boolean useIngoing) {
        this.useIngoing = useIngoing;
    }

    public boolean isUseOutgoing() {
        return useOutgoing;
    }

    public void setUseOutgoing(boolean useOutgoing) {
        this.useOutgoing = useOutgoing;
    }

    public boolean isUseResource() {
        return useResource;
    }

    public void setUseResource(boolean useResource) {
        this.useResource = useResource;
    }

    public boolean isUseLiteral() {
        return useLiteral;
    }

    public void setUseLiteral(boolean useLiteral) {
        this.useLiteral = useLiteral;
    }

    public boolean isAddNeighboursToCorrespondence() {
        return addNeighboursToCorrespondence;
    }

    public void setAddNeighboursToCorrespondence(boolean addNeighboursToCorrespondence) {
        this.addNeighboursToCorrespondence = addNeighboursToCorrespondence;
    }
    
    public static Function<Resource, Set<Object>> createExcludeNeighboursFunction(Set<Property> excludeLiteralProeprties, Function<Literal, Object> literalProcessingFunction){
        return r->{
            Set<Object> exclude = new HashSet();
            for(Property p : excludeLiteralProeprties){
                StmtIterator i = r.listProperties(p);
                while(i.hasNext()){
                    RDFNode n = i.next().getObject();
                    if(n.isLiteral()){
                        exclude.add(literalProcessingFunction.apply(n.asLiteral()));
                    }
                }
            }
            return exclude;            
        };        
    }

    @Override
    public String toString() {
        return "SimilarNeighboursFilter";
    }
}

class Neighbours{
    private Set<String> uriResources;
    private Set<Object> literals;
    
    public Neighbours(){
        this.uriResources = new HashSet<>();
        this.literals = new HashSet<>();
    }
    
    public void addResource(String resourceURI){
        uriResources.add(resourceURI);
    }
    
    public void addLiteral(Object literal){
        literals.add(literal);
    }

    public Set<String> getUriResources() {
        return uriResources;
    }

    public Set<Object> getLiterals() {
        return literals;
    }
}