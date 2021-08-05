package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * A {@link TextExtractor} which extracts texts from a resource which can be used by transformer
 * based matchers like {@link de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFilter}
 * or {@link de.uni_mannheim.informatik.dws.melt.matching_ml.python.nlptransformers.TransformersFineTuner}.
 */
public class TextExtractorShortAndLongTexts implements TextExtractor {

    private TextExtractorAllAnnotationProperties annotationExtractor = new TextExtractorAllAnnotationProperties();
    
    @Override
    public Set<String> extract(Resource r) {
        Set<ProcessedLiteral> shortTexts = new HashSet<>();
        Set<ProcessedLiteral> longTexts = new HashSet<>();
        
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
                        if(p.equals(RDFS.label)){
                            shortTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.LABEL, text));
                        } else if(p.equals(RDFS.comment)){
                            longTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.COMMENT, text));
                        } else if(PropertyVocabulary.LABEL_LIKE_PROPERTIES.contains(p)){
                            shortTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.LABEL_LIKE, text));
                        } else if(PropertyVocabulary.COMMENT_LIKE_PROPERTIES.contains(p)){
                            longTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.COMMENT_LIKE, text));
                        } else if(PropertyVocabulary.hasPropertyLabelFragment(p)){
                            shortTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.LABEL_NAME, text));
                        } else if(PropertyVocabulary.hasPropertyCommentFragment(p)){
                            longTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.COMMENT_NAME, text));
                        }
                        
                        if(text.length() > longestLiteral.length()){
                            longestLiteral = text;
                        }
                    }
                }
            }
        }
        if(longestLiteral.isEmpty() == false)
            longTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.LONGEST_LITERAL, longestLiteral));
        
        //add literal
        String uri = r.getURI();
        if(uri != null){
            String fragment = URIUtil.getUriFragment(uri).trim();
            if(StringProcessing.containsMostlyNumbers(fragment) == false){
                shortTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.FRAGMENT, fragment));
            }
        }
        
        //add annotation properties
        for(String s : annotationExtractor.extract(r)){
            longTexts.add(new ProcessedLiteral(ProperyTypeFineGrained.ANNOTATION_PROP, s)); //TODO: long texts????
        }
        
        
        //make list:
        List<ProcessedLiteral> shortTextList = new ArrayList<>(shortTexts);
        List<ProcessedLiteral> longTextList = new ArrayList<>(longTexts);
        updateContained(shortTextList);
        updateContained(longTextList);
        
        
        shortTextList.sort(Comparator.comparing(ProcessedLiteral::getContainedLiteralsSize, Comparator.reverseOrder())
                .thenComparing(ProcessedLiteral::getPropertyTypeFineGrained)
                .thenComparing(ProcessedLiteral::getLexical)
        );
        
        longTextList.sort(Comparator.comparing(ProcessedLiteral::getContainedLiteralsSize, Comparator.reverseOrder())
                .thenComparing(ProcessedLiteral::getPropertyTypeFineGrained)
                .thenComparing(ProcessedLiteral::getLexical)
        );
        
        Set<String> extractedLiterals = new HashSet<>();

        while(true){            
            if(shortTextList.isEmpty())
                break;
            ProcessedLiteral literal = shortTextList.get(0);
            extractedLiterals.add(literal.getLexical());
            shortTextList.remove(0);
            shortTextList.removeAll(literal.getContainedLiterals());
        }
        
        while(true){            
            if(longTextList.isEmpty())
                break;
            ProcessedLiteral literal = longTextList.get(0);
            extractedLiterals.add(literal.getLexical());
            longTextList.remove(0);
            longTextList.removeAll(literal.getContainedLiterals());
        }
        
        return extractedLiterals;
    }    
    
    private void updateContained(List<ProcessedLiteral> literals){
        literals.sort(Comparator.comparingInt(ProcessedLiteral::getNormalizedLength).thenComparing(ProcessedLiteral::getNormalized));
        for(int i=0; i<literals.size(); i++){
            ProcessedLiteral literal = literals.get(i);
            for(int j=i + 1; j<literals.size(); j++){
                ProcessedLiteral literalInner = literals.get(j);
                if(literalInner.getNormalized().contains(literal.getNormalized())){
                    literalInner.addLiteralContained(literal);
                }
            }
        }        
    }
}