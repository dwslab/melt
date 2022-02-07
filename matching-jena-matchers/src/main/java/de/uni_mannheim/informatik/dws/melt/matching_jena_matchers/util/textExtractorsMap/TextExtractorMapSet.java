package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractorsMap;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors.*;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractorMap;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;


/**
 * A {@link TextExtractor} which extracts texts from a resource which can be used by transformer
 * based matchers like {@link de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter}
 * or {@link de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner}.
 */
public class TextExtractorMapSet implements TextExtractorMap {
    private TextExtractorAllAnnotationProperties annotationExtractor = new TextExtractorAllAnnotationProperties();
    
    @Override
    public Map<String, Set<String>> extract(Resource r) {
        Set<NormalizedLiteral> shortTexts = new HashSet<>();
        Set<NormalizedLiteral> longTexts = new HashSet<>();
        
        String longestLiteral = "";
        StmtIterator i = r.listProperties();
        while(i.hasNext()){
            Statement stmt = i.next();
            RDFNode object = stmt.getObject();
            if(object.isLiteral()){
                Literal literal = object.asLiteral();
                if(TextExtractorAllStringLiterals.isLiteralAString(literal)){
                    String text = literal.getLexicalForm().trim();
                    if(!text.isEmpty()){
                        Property p = stmt.getPredicate();
                        
                        if(PropertyVocabulary.hasPropertyLabelFragment(p)){
                            shortTexts.add(new NormalizedLiteral(text));
                        }else if(PropertyVocabulary.hasPropertyCommentFragment(p)){
                            longTexts.add(new NormalizedLiteral(text));
                        }                        
                        if(text.length() > longestLiteral.length()){
                            longestLiteral = text;
                        }
                    }
                }
            }
        }
        
        if(longestLiteral.isEmpty() == false){
            NormalizedLiteral longest = new NormalizedLiteral(longestLiteral);
            if(shortTexts.contains(longest) == false && longTexts.contains(longest) == false){
                longTexts.add(longest);
            }
        }
            
        
        //add literal
        String uri = r.getURI();
        if(uri != null){
            String fragment = URIUtil.getUriFragment(uri).trim();
            if(StringProcessing.containsMostlyNumbers(fragment) == false){
                shortTexts.add(new NormalizedLiteral(fragment));
            }
        }
        
        //add annotation properties
        for(String s : annotationExtractor.extract(r)){
            NormalizedLiteral candidate = new NormalizedLiteral(s);
            if(shortTexts.contains(candidate) == false && longTexts.contains(candidate) == false){
                shortTexts.add(candidate);
            }
        }
                
        Map<String, Set<String>> extractedLiterals = new HashMap<>();
        extractedLiterals.put("shortTexts", getTexts(shortTexts));
        extractedLiterals.put("longTexts", getTexts(longTexts));        
        return extractedLiterals;
    }
    
    private Set<String> getTexts(Set<NormalizedLiteral> literals){
        Set<String> extractedLiterals = new HashSet<>();
        for(NormalizedLiteral l : literals){
            extractedLiterals.add(l.getLexical());
        }
        return extractedLiterals;
    }
}
class NormalizedLiteral{
    private final String lexical;
    private final String normalized;

    public NormalizedLiteral(String lexical) {
        this.lexical = lexical;
        this.normalized = String.join(" ", StringProcessing.normalize(lexical));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.normalized);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NormalizedLiteral other = (NormalizedLiteral) obj;
        if (!Objects.equals(this.normalized, other.normalized)) {
            return false;
        }
        return true;
    }
    
    public String getLexical() {
        return lexical;
    }
}