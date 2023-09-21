
package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.textExtractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.TextExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class TextExtractorResourceDescriptionInRDF extends TextExtractorRDFBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextExtractorResourceDescriptionInRDF.class);
    private static final Pattern MULTIPLE_WHITESPACE = Pattern.compile("[ \t]+");
    
    protected boolean removeNewlines;
    protected boolean removeMultipleSpaces;
    protected boolean useLabelInsteadOfResource;
    
    protected boolean includePrefixesInModel;
    protected boolean removePrefixDefition;
    /**
     * The serialization format to use - more info at <a href="https://jena.apache.org/documentation/io/rdf-output.html">the corresponding jena page</a>.
     */
    protected RDFFormat serializationFormat;

    public TextExtractorResourceDescriptionInRDF(boolean useLabelInsteadOfResource, RDFFormat serializationFormat) {
        super();
        this.removeNewlines = true;
        this.removeMultipleSpaces = true;
        this.useLabelInsteadOfResource = useLabelInsteadOfResource;
        this.includePrefixesInModel = true;
        this.removePrefixDefition = true;
        this.serializationFormat = serializationFormat;
    }
    
    public TextExtractorResourceDescriptionInRDF(boolean useLabelInsteadOfResource) {
        this(useLabelInsteadOfResource, RDFFormat.TURTLE_FLAT);
    }

    public TextExtractorResourceDescriptionInRDF() {
        this(true);
    }
    
    @Override
    public Set<String> extract(Resource r) {
        Model m = this.useLabelInsteadOfResource ? getModelWithLabel(r) : getModelWithResource(r);
        m = removeUnusedPrefixes(m);
        try(StringWriter writer = new StringWriter()){
            RDFDataMgr.write(writer, m, this.serializationFormat);
            /*
            System.out.println("================NTRIPLES_UTF8================");
            RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES_UTF8);
            System.out.println("================NT================");
            RDFDataMgr.write(System.out, m, RDFFormat.NT);
            System.out.println("================NTRIPLES================");
            RDFDataMgr.write(System.out, m, RDFFormat.NTRIPLES);
            System.out.println("================TURTLE================");
            RDFDataMgr.write(System.out, m, RDFFormat.TURTLE);
            System.out.println("================TURTLE_BLOCKS================");
            RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_BLOCKS);
            System.out.println("================TURTLE_FLAT================");
            RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_FLAT);
            System.out.println("================TURTLE_PRETTY================");
            RDFDataMgr.write(System.out, m, RDFFormat.TURTLE_PRETTY);
            */
            String[] lines = writer.toString().split("\\r?\\n");
            List<String> finalLines = new ArrayList<>();
            if(this.removePrefixDefition){
                for(String line : lines){
                    if(line.startsWith("@prefix")==false)
                        finalLines.add(line);
                }
            }else{
                finalLines = Arrays.asList(lines);
            }
            
            String result = this.removeNewlines ? String.join(" ", finalLines) : String.join("\n", finalLines);
            if(this.removeMultipleSpaces){
                result = MULTIPLE_WHITESPACE.matcher(result).replaceAll(" ");
            }
            return new HashSet<>(Arrays.asList(result));
        } catch (IOException ex) {
            LOGGER.info("Exception for StringWriter.", ex);
            return new HashSet<>();
        }
    }
    
    private static final Set<Property> LABEL_PROP = new HashSet<>(Arrays.asList(
            SKOS.prefLabel, RDFS.label, SKOS.altLabel
    ));
    
    protected Model getModelWithResource(Resource r){
        List<Statement> statements = new ArrayList<>();
        StmtIterator i = r.listProperties();
        while(i.hasNext()){
            Statement s = i.next();
            statements.add(s);
            if(s.getObject().isResource()){
                Resource neighbour = s.getObject().asResource();
                for(Property p : LABEL_PROP){
                    StmtIterator neighbourLabelIterator = neighbour.listProperties(p);
                    while(neighbourLabelIterator.hasNext()){
                        Statement neighbourLabel = neighbourLabelIterator.next();
                        String processed = StringProcessing.normalizeOnlyCamelCaseAndUnderscore(neighbourLabel.getObject().asLiteral().getLexicalForm());
                        statements.add(ResourceFactory.createStatement(
                            neighbourLabel.getSubject(), neighbourLabel.getPredicate(), ResourceFactory.createStringLiteral(processed)));
                    }
                }
            }
        }
        Model m = createEmptyModel(r);
        m.add(this.statementProcessor.apply(statements));
        return m;
    }
    
    private static final TextExtractorOnlyLabel labelExtractor = new TextExtractorOnlyLabel();
    protected Model getModelWithLabel(Resource r){
        List<Statement> statements = new ArrayList<>();
        StmtIterator i = r.listProperties();
        while(i.hasNext()){
            Statement s = i.next();
            if(s.getObject().isResource()){
                String neighbourLabel = labelExtractor.extractOne(s.getObject().asResource());
                if(neighbourLabel.isEmpty()){
                    if(s.getObject().isURIResource())
                        statements.add(s); // only add if it is a URIResource - otherwise it is a blank node which has no information (also no labels etc).
                }else{
                    statements.add(ResourceFactory.createStatement(
                            s.getSubject(), s.getPredicate(), ResourceFactory.createStringLiteral(neighbourLabel)));
                }
            }else{
                if(LABEL_PROP.contains(s.getPredicate())){
                    //process labels
                    String processed = StringProcessing.normalizeOnlyCamelCaseAndUnderscore(s.getObject().asLiteral().getLexicalForm());
                    statements.add(ResourceFactory.createStatement(
                            s.getSubject(), s.getPredicate(), ResourceFactory.createStringLiteral(processed)));
                }else{
                    statements.add(s);
                }
            }
        }
        Model m = createEmptyModel(r);
        m.add(this.statementProcessor.apply(statements));
        return m;
    }
    
    
    private static Set<Lang> noPrefixLang = new HashSet<>(Arrays.asList(
            Lang.NTRIPLES, Lang.NT, Lang.NQUADS, Lang.NQ));
    private Model removeUnusedPrefixes(Model m){
        if(m.hasNoMappings())
            return m;
        if(noPrefixLang.contains(this.serializationFormat.getLang()))
            return m;
        
        StmtIterator i = m.listStatements();
        Set<String> urisUsed = new HashSet<>();
        while(i.hasNext()){
            Statement s = i.next();
            String uri = s.getSubject().getURI();
            if(uri != null){
                urisUsed.add(uri);
            }
            uri = s.getPredicate().getURI();
            if(uri != null){
                urisUsed.add(uri);
            }
            if(s.getObject().isURIResource()){
                urisUsed.add(s.getObject().asResource().getURI());
            }
        }

        Set<String> usedPrefixes = new HashSet<>();
        for(String uri : urisUsed){
            String prefix = getPrefix(m.getNsPrefixMap(), uri);
            if(prefix != null)
                usedPrefixes.add(prefix);
        }
        Set<String> keys = m.getNsPrefixMap().keySet();
        keys.removeAll(usedPrefixes);
        for(String k : keys){
            m.removeNsPrefix(k);
        }
        return m;
    }
    
    private String getPrefix(Map<String, String> map, String uri){
        for (Map.Entry<String, String> e: map.entrySet()){
            if(uri.startsWith(e.getValue()))
                return e.getKey();
        }
        return null;
    }
    
    private Model createEmptyModel(Resource r){
        Model m = ModelFactory.createDefaultModel();
        if(this.includePrefixesInModel){            
            Map<String, String> map = r.getModel().getNsPrefixMap();
            map.putAll(PrefixMapping.Standard.getNsPrefixMap());
            map.putAll(PrefixMapping.Extended.getNsPrefixMap());
            m.setNsPrefixes(map);
        }
        return m;
    }

    public boolean isRemoveNewlines() {
        return removeNewlines;
    }

    public TextExtractorResourceDescriptionInRDF setRemoveNewlines(boolean removeNewlines) {
        this.removeNewlines = removeNewlines;
        return this;
    }

    public boolean isUseLabelInsteadOfResource() {
        return useLabelInsteadOfResource;
    }

    public TextExtractorResourceDescriptionInRDF setUseLabelInsteadOfResource(boolean useLabelInsteadOfResource) {
        this.useLabelInsteadOfResource = useLabelInsteadOfResource;
        return this;
    }

    public boolean isIncludePrefixesInModel() {
        return includePrefixesInModel;
    }

    public TextExtractorResourceDescriptionInRDF setIncludePrefixesInModel(boolean includePrefixesInModel) {
        this.includePrefixesInModel = includePrefixesInModel;
        return this;
    }

    public boolean isRemovePrefixDefition() {
        return removePrefixDefition;
    }

    public TextExtractorResourceDescriptionInRDF setRemovePrefixDefition(boolean removePrefixDefition) {
        this.removePrefixDefition = removePrefixDefition;
        return this;
    }

    public RDFFormat getSerializationFormat() {
        return serializationFormat;
    }

    public TextExtractorResourceDescriptionInRDF setSerializationFormat(RDFFormat serializationFormat) {
        this.serializationFormat = serializationFormat;
        return this;
    }

    public boolean isRemoveMultipleSpaces() {
        return removeMultipleSpaces;
    }

    public TextExtractorResourceDescriptionInRDF setRemoveMultipleSpaces(boolean removeMultipleSpaces) {
        this.removeMultipleSpaces = removeMultipleSpaces;
        return this;
    }
}
