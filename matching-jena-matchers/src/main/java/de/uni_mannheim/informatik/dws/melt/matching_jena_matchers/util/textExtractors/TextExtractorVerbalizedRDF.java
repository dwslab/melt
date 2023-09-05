
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TextExtractorVerbalizedRDF implements TextExtractor {
    
    private static final TextExtractorOnlyLabel labelExtractor = new TextExtractorOnlyLabel();
    
    protected boolean lineByLineTranslation;
    protected boolean includeQuotes;

    public TextExtractorVerbalizedRDF(boolean lineByLineTranslation, boolean includeQuotes) {
        this.lineByLineTranslation = lineByLineTranslation;
        this.includeQuotes = includeQuotes;
    }

    public TextExtractorVerbalizedRDF() {
        this(false, false);
    }
    
    
    private static Set<Property> LABEL_PROPERTIES = new HashSet<>(Arrays.asList(
            SKOS.prefLabel,
            SKOS.altLabel,
            RDFS.label
    ));
    
    @Override
    public Set<String> extract(Resource r) {
        
        String subjectLabel = labelExtractor.extractOne(r);
        if(subjectLabel.isEmpty())
            return new HashSet<>();
        subjectLabel = optionallyQuote(subjectLabel);
                
        StringBuilder sb = new StringBuilder();
        StmtIterator i = r.listProperties();
        List<String> lines = new ArrayList<>();
        while(i.hasNext()){
            Statement s = i.next();
            if(LABEL_PROPERTIES.contains(s.getPredicate()))
                continue;            
            String predicateLabel = s.getPredicate().equals(RDFS.subClassOf) ? "subclass of" : labelExtractor.extractOne(s.getPredicate());
            String objectLabel = "";
            if(s.getObject().isLiteral()){
                objectLabel = StringProcessing.normalizeOnlyCamelCaseAndUnderscore(s.getObject().asLiteral().getLexicalForm()).trim();
            }else{
                objectLabel = labelExtractor.extractOne(s.getObject().asResource());
            }
            
            if(predicateLabel.isEmpty() || objectLabel.isEmpty())
                continue;
            objectLabel = optionallyQuote(objectLabel);
            if(lineByLineTranslation){
                sb.append(subjectLabel).append(" ").append(predicateLabel).append(" ").append(objectLabel).append(". ");
            }else{
                lines.add(predicateLabel + " " + objectLabel);
            }
        }
        if(lineByLineTranslation){
            if(sb.isEmpty()){
                sb.append(subjectLabel);
            }
        }else{
            sb.append(subjectLabel).append(" ");
            int last = lines.size() - 1;
            String joined = String.join(" and ",
                                String.join(", ", lines.subList(0, last)),
                                lines.get(last));
            sb.append(joined).append(".");
        }
        return new HashSet<>(Arrays.asList(sb.toString().trim()));
    }
    
    protected String optionallyQuote(String text){
        if(this.includeQuotes){
            return "\"" + text.trim() + "\"";
        }else{
            return text;
        }
    }
}
