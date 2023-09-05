
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;

/**
 * Extracts only one speaking label (language can be set in constructor) which can be (in decreasing importance):
 * skos:prefLabel, rdfs:label, fragment (only if more than 50 percent are not numbers), skos:altLabel, skos:hiddenLabel.
 */
public class TextExtractorOnlyLabel implements TextExtractor {

    protected String languageCode;
    
    public TextExtractorOnlyLabel(){
        this.languageCode = "en";
    }
    
    public TextExtractorOnlyLabel(String languageCode){
        if(this.languageCode == null)
            throw new IllegalArgumentException("language code should not be null");
        this.languageCode = languageCode;
    }
    
    @Override
    public Set<String> extract(Resource r) {
        String label = extractOne(r);
        if(label.isEmpty()){
            return new HashSet<>();
        }else{
            return new HashSet<>(Arrays.asList(label));
        }
    }
    
    public String extractOne(Resource r){
        String value = extractProperty(r, SKOS.prefLabel);
        if(value.isEmpty() == false)
            return StringProcessing.normalizeOnlyCamelCaseAndUnderscore(value);
        
        value = extractProperty(r, RDFS.label);
        if(value.isEmpty() == false)
            return StringProcessing.normalizeOnlyCamelCaseAndUnderscore(value);
        
        value = extractFragment(r);
        if(value.isEmpty() == false)
            return StringProcessing.normalizeOnlyCamelCaseAndUnderscore(value);        
        
        value = extractProperty(r, SKOS.altLabel);
        if(value.isEmpty() == false)
            return StringProcessing.normalizeOnlyCamelCaseAndUnderscore(value);
        
        return StringProcessing.normalizeOnlyCamelCaseAndUnderscore(extractProperty(r, SKOS.hiddenLabel));
        //TODO: maybe use any property which contains "label" or "name" in its label of the property or uri fragment
    }
    
    /**
     * Extract literal if language tag fits or (as fallback) literal with no language tag.
     * @param r the resource
     * @param p the property to analyze
     * @return the extracted lexical form of the literal or empty string (if no literal matches or is provided).
     */
    protected String extractProperty(Resource r, Property p){
        List<String> fallback = new ArrayList<>();
        StmtIterator i = r.listProperties(p);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                Literal l = n.asLiteral();
                String lexical = l.getLexicalForm().trim();
                if(lexical.isEmpty())
                    continue;
                if(langTagMatch(l.getLanguage())){
                    return lexical;
                }
                if(l.getLanguage().isEmpty()){
                    fallback.add(lexical);
                }
            }
        }
        if(fallback.isEmpty() == false){
            return fallback.get(0);
        }
        return "";
    }
    
    protected boolean langTagMatch(String target) {
        return (this.languageCode.equalsIgnoreCase( target )) ||
               (target.length() > this.languageCode.length() && this.languageCode.equalsIgnoreCase( target.substring( this.languageCode.length() ) ));
    }
    
    public static String extractFragment(Resource r){
        String uri = r.getURI();
        if(uri == null){
            return "";
        }
        String fragment = URIUtil.getUriFragment(uri).trim();
        if(fragment.isEmpty()){
            return "";
        }
        if(StringProcessing.containsMostlyNumbers(fragment)){
            return "";
        }
        return fragment;
    }
    
}
