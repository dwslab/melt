package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.HashSet;
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
public class TextExtractorSet implements TextExtractor {

    private TextExtractorAllAnnotationProperties annotationExtractor = new TextExtractorAllAnnotationProperties();
    
    @Override
    public Set<String> extract(Resource r) {
        Set<NormalizedLiteral> texts = new HashSet<>();
        
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
                        
                        if(PropertyVocabulary.DESCRIPTIVE_PROPERTIES.contains(p) ||
                                PropertyVocabulary.hasPropertyLabelFragment(p) || 
                                PropertyVocabulary.hasPropertyCommentFragment(p)){
                            texts.add(new NormalizedLiteral(text));
                        }
                        
                        if(text.length() > longestLiteral.length()){
                            longestLiteral = text;
                        }
                    }
                }
            }
        }
        if(longestLiteral.isEmpty() == false)
            texts.add(new NormalizedLiteral(longestLiteral));
        
        //add literal
        String uri = r.getURI();
        if(uri != null){
            String fragment = URIUtil.getUriFragment(uri).trim();
            if(StringProcessing.containsMostlyNumbers(fragment) == false){
                texts.add(new NormalizedLiteral(fragment));
            }
        }
        
        //add annotation properties
        for(String s : annotationExtractor.extract(r)){
            texts.add(new NormalizedLiteral(s));
        }
                
        Set<String> extractedLiterals = new HashSet<>();
        for(NormalizedLiteral l : texts){
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