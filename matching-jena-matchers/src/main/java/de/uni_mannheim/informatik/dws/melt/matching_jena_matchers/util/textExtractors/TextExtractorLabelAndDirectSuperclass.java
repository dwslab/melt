
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;

/**
 * Extracts a label for the given resource and also creates a text for the superclass such that more context is provided.
 */
public class TextExtractorLabelAndDirectSuperclass implements TextExtractor {

    private final TextExtractorOnlyLabel labelExtractor;    
    private final String subClassText;
    private final boolean includeQuotes;
    
    public TextExtractorLabelAndDirectSuperclass(String subClassText, boolean includeQuotes) {
        this.subClassText = subClassText;
        this.includeQuotes = includeQuotes;
        this.labelExtractor = new TextExtractorOnlyLabel();
    }

    public TextExtractorLabelAndDirectSuperclass() {
        this("which is subclass of", true);
    }
    
    @Override
    public Set<String> extract(Resource r) {
        String rLabel = labelExtractor.extractOne(r);
        if(rLabel.isEmpty())
            return new HashSet<>();
        
        Set<String> superClassLabels = getSuperclassLabels(r);
        if(superClassLabels.isEmpty()){
            return new HashSet<>(Arrays.asList(optionallyQuote(rLabel)));
        }
        if(superClassLabels.size() == 1){
            return new HashSet<>(Arrays.asList(optionallyQuote(rLabel) + " " + subClassText.trim() + " " + optionallyQuote(superClassLabels.iterator().next())));
        }else{
            if(this.includeQuotes){
                return new HashSet<>(Arrays.asList(optionallyQuote(rLabel) + " " + subClassText.trim() + " " + String.join(" and ", optionallyQuote(superClassLabels))));
            }else{
                return new HashSet<>(Arrays.asList(rLabel + " " + subClassText.trim() + " [" + String.join(",", superClassLabels) + "]"));
            }
            
        }
    }
    
    /**
     * Returns for each superclass at maximum one label. If a label for a super class cannot be extracted, it will also not be in the set.
     * @param r the resource to extract the superclass labels
     * @return the superclass labels
     */
    protected Set<String> getSuperclassLabels(Resource r){
        Set<String> superClassLabels = new HashSet<>();
        StmtIterator i = r.listProperties(RDFS.subClassOf);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isResource()){
                Resource superClass = n.asResource();
                if(superClass.equals(OWL.Thing)){
                    continue;
                }
                String superClassLabel = labelExtractor.extractOne(superClass);
                if(superClassLabel.isEmpty() == false){
                    superClassLabels.add(superClassLabel);
                }
            }
        }
        return superClassLabels;
    }
    
    protected String optionallyQuote(String text){
        if(this.includeQuotes){
            return "\"" + text.trim() + "\"";
        }else{
            return text;
        }
    }
    
    protected Set<String> optionallyQuote(Set<String> texts){
        if(this.includeQuotes){
            Set<String> newSet = new HashSet<>();
            for(String text : texts){
                newSet.add(optionallyQuote(text));
            }
            return newSet;
        }else{
            return texts;
        }
    }
}
