package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.instance;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.BaseFilterWithSetComparison;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.SetSimilarity;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * Filters individual/instance mappings by comparing literals.
 * The literals are selected by the corresponding properties (leave empty to select all).
 * The set of tokens created for each individual are compared with the {@link SetSimilarity}.
 */
public class BagOfWordsSetSimilarityFilter extends BaseFilterWithSetComparison {
    
    /**
     * The properties to choose when computing the tokens.
     * If empty, choose all.
     */
    private Set<Property> properties;
    
    /**
     * The tokenizer function which gets a literal and produce a set of tokens(strings).
     */
    private Function<Literal, Collection<String>> tokenizer;

    
    public BagOfWordsSetSimilarityFilter(Set<Property> properties, Function<Literal, Collection<String>> tokenizer, double threshold, SetSimilarity setSimilatity) {
        super(threshold, setSimilatity);
        this.properties = properties;
        this.tokenizer = tokenizer;
    }
    
    public BagOfWordsSetSimilarityFilter(Set<Property> properties, double threshold, SetSimilarity setSimilatity) {
        this(properties, 
            l->Arrays.asList(l.getLexicalForm().toLowerCase(Locale.ENGLISH).split(" ")),
            threshold, 
            setSimilatity);
    }
    
    public BagOfWordsSetSimilarityFilter(Property... properties) {
        this(new HashSet(Arrays.asList(properties)), 0.0, SetSimilarity.JACCARD);
    }
    
    public BagOfWordsSetSimilarityFilter() {
        this(new HashSet(), 0.0, SetSimilarity.JACCARD);
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        Alignment finalAlignment = new Alignment(inputAlignment, false);
        for(Correspondence corr : inputAlignment){            
            Individual sourceIndividual = source.getIndividual(corr.getEntityOne());
            Individual targetIndividual = target.getIndividual(corr.getEntityTwo());
            if(sourceIndividual == null || targetIndividual == null){
                finalAlignment.add(corr);
                continue;
            }
            
            Set<String> sourceTokens = getTokens(sourceIndividual);
            Set<String> targetTokens = getTokens(targetIndividual);
            
            double value = this.setSimilatity.compute(sourceTokens, targetTokens);
            if(value >= this.threshold){
                //DEBUG
                //corr.addExtensionValue(DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + "sourceTokens", getSortedTokens(sourceTokens));
                //corr.addExtensionValue(DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + "targetTokens", getSortedTokens(targetTokens));
                //Set<String> intersection = new HashSet<String>(sourceTokens);
                //intersection.retainAll(targetTokens);
                //corr.addExtensionValue(DefaultExtensions.MeltExtensions.CONFIGURATION_BASE + "intersectionTokens", getSortedTokens(intersection));
            
                corr.addAdditionalConfidence(this.getClass(), value);
                finalAlignment.add(corr);
            }
        }
        return finalAlignment;
    }
    
    
    public Set<String> getTokens(Individual individual){
        Set<String> tokens = new HashSet();
        if(properties.isEmpty()){
            StmtIterator stmts = individual.listProperties();
            while(stmts.hasNext()){
                Statement s = stmts.next();
                if(s.getObject().isLiteral()){
                    tokens.addAll(this.tokenizer.apply(s.getObject().asLiteral()));
                }
            }
        }else{
            for(Property p : properties){
                StmtIterator stmts = individual.listProperties(p);
                while(stmts.hasNext()){
                    Statement s = stmts.next();
                    if(s.getObject().isLiteral()){
                        tokens.addAll(this.tokenizer.apply(s.getObject().asLiteral()));
                    }
                }
            }
        }
        return tokens;
    }
    
    protected String getSortedTokens(Collection<String> tokens){
        List<String> list = new ArrayList<>(tokens);
        Collections.sort(list);
        return String.join(" ", list);
    }

    @Override
    public String toString() {
        return "BagOfWordsSetSimilarityFilter";
    }
}



