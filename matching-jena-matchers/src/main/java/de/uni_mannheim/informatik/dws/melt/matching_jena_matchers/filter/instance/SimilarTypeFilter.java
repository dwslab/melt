package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BaseFilterWithSetComparison;
import com.googlecode.cqengine.query.QueryFactory;
import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.SetSimilarity;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Checks for each instance mapping, how many already matched types it has in common.
 * For comparing a type hierarchy, choose SimilarHierarchyFilter.
 */
public class SimilarTypeFilter extends BaseFilterWithSetComparison implements Filter {

    /**
     * The minimum confidence for which a class mapping is counted. Compared with greater or equal.
     */
    private double minClassConfidence;    
    
    /**
     * Type property: usually rdf:type.
     */
    private Property typeProperty;

    public SimilarTypeFilter(double minClassConfidence, Property typeProperty, double threshold, SetSimilarity setSimilatity) {
        super(threshold, setSimilatity);
        this.minClassConfidence = minClassConfidence;
        this.typeProperty = typeProperty;
    }
    
    public SimilarTypeFilter(double threshold, SetSimilarity setSimilarity) {
        this(0.0d, RDF.type, threshold, setSimilarity);
    }
    
    public SimilarTypeFilter() {
        this(0.0d, SetSimilarity.ABSOLUTE);
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
            
            Set<String> sourceTypes = getTypes(sourceIndividual);
            Set<String> targetTypes = getTypes(targetIndividual);
            
            Iterable<Correspondence> i = inputAlignment.retrieve(
                QueryFactory.and(
                    QueryFactory.in(Correspondence.SOURCE, sourceTypes),
                    QueryFactory.in(Correspondence.TARGET, targetTypes),
                    QueryFactory.greaterThanOrEqualTo(Correspondence.CONFIDENCE, minClassConfidence)
                ));
            Set<String> mappedSources = new HashSet<>();
            Set<String> mappedTargets = new HashSet<>();            
            for(Correspondence c : i){
                mappedSources.add(c.getEntityOne());
                mappedTargets.add(c.getEntityTwo());
            }
            //in case of n:m mappings only the minimum amount of resource is the number of the intersection.
            int resourceIntersection = Math.min(mappedSources.size(), mappedTargets.size());
            
            double value = setSimilarity.compute(resourceIntersection, sourceTypes.size(), targetTypes.size());
            if(value >= this.threshold){
                correspondence.addAdditionalConfidence(this.getClass(), value);
                filteredAlignment.add(correspondence);
            }
        }
        return filteredAlignment;
    }
    
    
    private Set<String> getTypes(Individual individual){
        Set<String> types = new HashSet();
        StmtIterator stmts = individual.listProperties(this.typeProperty);
        while(stmts.hasNext()){
            Statement s = stmts.next();
            if(s.getObject().isURIResource()){
                types.add(s.getObject().asResource().getURI());
            }
        }
        return types;
    }

    @Override
    public String toString() {
        return "SimilarTypeFilter";
    }
}
