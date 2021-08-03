package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.PropertyVocabulary;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 * A text textractor which extracts texts from a resource which can be used by transformer 
 * based matchers like TransformersFilter or TransformersFilterFineTuner.
 */
public class TextExtractorForTransformers implements TextExtractor {

    private int minToken;

    public TextExtractorForTransformers(int minToken) {
        this.minToken = minToken;
    }
    
    public TextExtractorForTransformers() {
        this(Integer.MAX_VALUE);
    }
    
    @Override
    public Set<String> extract(Resource r) {
        
        List<ProcessedLiteral> literals = new ArrayList<>(getPossibleLiterals(r));
        updateContained(literals);
        
        literals.sort(Comparator.comparing(ProcessedLiteral::getPropertyTypeCourseGrained)
                .thenComparing(ProcessedLiteral::getContainedLiteralsSize, Comparator.reverseOrder())
                .thenComparing(ProcessedLiteral::getPropertyTypeFineGrained)
                .thenComparing(ProcessedLiteral::getLexical)
        );
        
        Set<String> extractedLiterals = new HashSet<>();
        int countTokens = 0;
        while(countTokens < this.minToken){            
            if(literals.isEmpty())
                break;
            ProcessedLiteral literal = literals.get(0);
            extractedLiterals.add(literal.getLexical());
            countTokens += literal.getTokenCount();
            
            literals.remove(0);
            literals.removeAll(literal.getContainedLiterals());
        }
        return extractedLiterals;
    }
    
    /*
    private static final Set<ProperyType> TYPES = new HashSet<>(Arrays.asList(ProperyType.COMMENT, ProperyType.COMMENT_LIKE,
            ProperyType.COMMENT_NAME,ProperyType.LONGEST_LITERAL));
    private List<ProcessedLiteral> getCommentOrLongestLiteralSortedByMostContained(List<ProcessedLiteral> literals){
        List<ProcessedLiteral> filtered = new ArrayList<>();
        for(ProcessedLiteral l : literals){
            if(TYPES.contains(l.getPropertyType())){
                filtered.add(l);
            }
        }
        filtered.sort(Comparator.comparing(ProcessedLiteral::getContainedLiteralsSize).reversed());
        return filtered;
    }
    */
    
    
    private TextExtractorAllAnnotationProperties annotationExtractor = new TextExtractorAllAnnotationProperties();
    
    private Set<ProcessedLiteral> getPossibleLiterals(Resource r){
        Set<ProcessedLiteral> literals = new HashSet<>();
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
                        /*
                        if(PropertyVocabulary.COMMENT_LIKE_PROPERTIES.contains(p) ||
                           PropertyVocabulary.hasPropertyCommentFragment(p)){
                            literals.add(new ProcessedLiteral(ProperyType.COMMENT_AND_LONGEST_LITERAL, text));
                        } else if(PropertyVocabulary.LABEL_LIKE_PROPERTIES.contains(p) ||
                                  PropertyVocabulary.hasPropertyLabelFragment(p)){
                            literals.add(new ProcessedLiteral(ProperyType.LABELS, text));
                        }*/
                        
                        
                        if(p.equals(RDFS.label)){
                            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.LABEL, text));
                        }else if(p.equals(RDFS.comment)){
                            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.COMMENT, text));
                        }else if(PropertyVocabulary.LABEL_LIKE_PROPERTIES.contains(p)){
                            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.LABEL_LIKE, text));
                        }else if(PropertyVocabulary.COMMENT_LIKE_PROPERTIES.contains(p)){
                            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.COMMENT_LIKE, text));
                        }else if(PropertyVocabulary.hasPropertyLabelFragment(p)){
                            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.LABEL_NAME, text));
                        }else if(PropertyVocabulary.hasPropertyCommentFragment(p)){
                            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.COMMENT_NAME, text));
                        }
                        
                        if(text.length() > longestLiteral.length()){
                            longestLiteral = text;
                        }
                    }
                }
            }
        }
        if(longestLiteral.isEmpty() == false)
            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.LONGEST_LITERAL, longestLiteral));
        
        //add literal
        String uri = r.getURI();
        if(uri != null){
            String fragment = URIUtil.getUriFragment(uri).trim();
            if(StringProcessing.containsMostlyNumbers(fragment) == false){
                literals.add(new ProcessedLiteral(ProperyTypeFineGrained.FRAGMENT, fragment));
            }
        }
        
        //add annotation properties
        for(String s : annotationExtractor.extract(r)){
            literals.add(new ProcessedLiteral(ProperyTypeFineGrained.ANNOTATION_PROP, s));
        }
        
        return literals;
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
    
    /*
    private List<String> removeIfAlreadyContained(List<String> list){
        //preprocess
        
        List<String> preprocessed = new ArrayList<>();
        
        // save only indices which are later removed.
        list.sort(String::length);
        for(int i=0; i < list.size(); i++){
            String s = list.get(i);
            
            boolean isContained = false;
            for(int j=i; j < list.size(); j++){
                if(list.get(j).contains(s))
            }
            
        }
        
    }
*/

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + this.minToken;
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
        final TextExtractorForTransformers other = (TextExtractorForTransformers) obj;
        if (this.minToken != other.minToken) {
            return false;
        }
        return true;
    }

    
    
}

class ProcessedLiteral{
    private ProperyTypeCourseGrained propertyTypeCourseGrained;
    private ProperyTypeFineGrained propertyTypeFineGrained;
    private String lexical;
    private List<String> tokens;
    private String normalized;
    private Set<ProcessedLiteral> containedLiterals;

    public ProcessedLiteral(ProperyTypeFineGrained propertyTypeFineGrained, String lexical) {
        this.propertyTypeFineGrained = propertyTypeFineGrained;
        this.lexical = lexical;
        this.tokens = StringProcessing.normalize(lexical);
        this.normalized = String.join(" ", this.tokens);
        this.containedLiterals = new HashSet<>();
        
        switch(this.propertyTypeFineGrained){
            case COMMENT:
            case COMMENT_LIKE:
            case COMMENT_NAME:
            case LONGEST_LITERAL:
                this.propertyTypeCourseGrained = ProperyTypeCourseGrained.COMMENT_AND_LONGEST_LITERAL;
                break;
                
            case LABEL:
            case LABEL_LIKE:
            case LABEL_NAME:
                this.propertyTypeCourseGrained = ProperyTypeCourseGrained.LABELS;
                break;
                
            case ANNOTATION_PROP:
                this.propertyTypeCourseGrained = ProperyTypeCourseGrained.ANNOTATION_PROP;
                break;
                
            case FRAGMENT:
                this.propertyTypeCourseGrained = ProperyTypeCourseGrained.FRAGMENT;
                break;
                
            default:
                throw new IllegalArgumentException("ProperyTypeFineGrained is not valid.");
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 13 * hash + Objects.hashCode(this.normalized);
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
        final ProcessedLiteral other = (ProcessedLiteral) obj;
        if (!Objects.equals(this.normalized, other.normalized)) {
            return false;
        }
        return true;
    }
    
    public int getNormalizedLength(){
        return this.normalized.length();
    }

    public String getNormalized() {
        return normalized;
    }
    
    public void addLiteralContained(ProcessedLiteral literal){
        this.containedLiterals.add(literal);
    }

    public ProperyTypeCourseGrained getPropertyTypeCourseGrained() {
        return propertyTypeCourseGrained;
    }

    public ProperyTypeFineGrained getPropertyTypeFineGrained() {
        return propertyTypeFineGrained;
    }

    public Set<ProcessedLiteral> getContainedLiterals() {
        return containedLiterals;
    }
    
    public int getContainedLiteralsSize() {
        return containedLiterals.size();
    }

    public String getLexical() {
        return lexical;
    }
    
    public int getTokenCount(){
        return this.tokens.size();
    }
    
    
}


enum ProperyTypeCourseGrained{
    COMMENT_AND_LONGEST_LITERAL,
    LABELS,
    ANNOTATION_PROP,
    FRAGMENT;
}

enum ProperyTypeFineGrained{
    COMMENT,
    COMMENT_LIKE,
    COMMENT_NAME,
    
    LONGEST_LITERAL,
    
    LABEL,
    LABEL_LIKE,
    LABEL_NAME,
    
    ANNOTATION_PROP,
    
    FRAGMENT;
}