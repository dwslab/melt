package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 * The textExtractor is a base class for all extractors which lists all statements about a resource.
 * It allows to filter the statements which are included in the final representation.
 */
public abstract class TextExtractorRDFBase implements TextExtractor {
    
    protected Function<List<Statement>, List<Statement>> statementProcessor;
    
    public TextExtractorRDFBase(){
        this.statementProcessor = (s) -> s;
    }
    
    public TextExtractorRDFBase(Function<List<Statement>, List<Statement>> statementProcessor){
        this.statementProcessor = statementProcessor;
    }

    public Function<List<Statement>, List<Statement>> getStatementProcessor() {
        return statementProcessor;
    }

    public TextExtractorRDFBase setStatementProcessor(Function<List<Statement>, List<Statement>> statementProcessor) {
        if(statementProcessor == null)
            throw new IllegalArgumentException("statementProcessor should not be null.");
        this.statementProcessor = statementProcessor;
        return this;
    }
    
    
    
    private static final Set<Resource> SKIP_RESOURCES = new HashSet<>(Arrays.asList(
        OWL.Thing, OWL.Class, RDFS.Class,
        OWL.DatatypeProperty, OWL.ObjectProperty,
        RDF.Property
    ));
    
    private static Set<String> SKIP_URIS = SKIP_RESOURCES.stream().map(r->r.getURI()).collect(Collectors.toSet());
    private static Set<String> SKIP_LITERALS = SKIP_RESOURCES.stream().map(r->URIUtil.getUriFragment(r.getURI()).trim()).collect(Collectors.toSet());
    //the labels of OWL.Thing, OWL.DatatypeProperty etc are always exactly the same as the URI fragment
     
    
    
    /**
     * This predicate should be used as keep predicate and filters out long (more than 150 characters) literals by completely ignoring them.
     * In addition all definitions such as x a owl:class or rdfs:class etc are removed.
     */
    public static Function<List<Statement>, List<Statement>> SKIP_DEFINITIONS_AND_LONG_LITERALS = new Function<List<Statement>, List<Statement>>() {
        @Override
        public List<Statement> apply(List<Statement> statements) {
            List<Statement> finalSatements = new ArrayList<>();
            for(Statement s : statements){
                if(s.getObject().isLiteral()){
                    String lexicalForm = s.getObject().asLiteral().getLexicalForm();
                    if(SKIP_LITERALS.contains(lexicalForm) == false && lexicalForm.length() < 150){
                        finalSatements.add(s);
                    }
                }else{
                    if(SKIP_URIS.contains(s.getObject().asResource().getURI()) == false){
                        finalSatements.add(s);
                    }
                }
            }
            if(finalSatements.isEmpty())//backup
                return statements;
            return finalSatements;
        }
    };
    
    public static Function<List<Statement>, List<Statement>> SKIP_DEFINITIONS_AND_SHORTEN_LONG_LITERALS = new Function<List<Statement>, List<Statement>>() {
        @Override
        public List<Statement> apply(List<Statement> statements) {
            List<Statement> finalSatements = new ArrayList<>();
            for(Statement s : statements){
                if(s.getObject().isLiteral()){
                    String lexicalForm = s.getObject().asLiteral().getLexicalForm();
                    if(SKIP_LITERALS.contains(lexicalForm))
                        continue;
                    if(lexicalForm.length() < 150){
                        finalSatements.add(s);
                    }else{
                        finalSatements.add(ResourceFactory.createStatement(
                            s.getSubject(), s.getPredicate(), ResourceFactory.createStringLiteral(shortenText(lexicalForm, 150))));
                    }
                }else{
                    if(SKIP_URIS.contains(s.getObject().asResource().getURI()) == false){
                        finalSatements.add(s);
                    }
                }
            }
            if(finalSatements.isEmpty())//backup
                return statements;
            return finalSatements;
        }
        
        private String shortenText(String text, int maxCharacters){
            if(text.length() < maxCharacters){
                return text;
            }
            text = text.substring(0, maxCharacters);
            //find good end

            int lastDotIndex = text.lastIndexOf(".");
            if(lastDotIndex >= 0){
                return text.substring(0, lastDotIndex + 1);
            }
            int lastWhitespaceIndex = text.lastIndexOf(" ");
            if(lastWhitespaceIndex >=0){
                return text.substring(0, lastWhitespaceIndex + 1) + "...";
            }else{
                return text;
            }
        }
    };
    
    public static Function<List<Statement>, List<Statement>> SKIP_DEFINITIONS = new Function<List<Statement>, List<Statement>>() {
        @Override
        public List<Statement> apply(List<Statement> statements) {
            List<Statement> finalSatements = new ArrayList<>();
            for(Statement s : statements){
                if(s.getObject().isLiteral()){
                    String lexicalForm = s.getObject().asLiteral().getLexicalForm();
                    if(SKIP_LITERALS.contains(lexicalForm) == false)
                        finalSatements.add(s);
                }else{
                    if(SKIP_URIS.contains(s.getObject().asResource().getURI()) == false){
                        finalSatements.add(s);
                    }
                }
            }
            if(finalSatements.isEmpty()) //backup
                return statements;
            return finalSatements;
        }
    };
}
