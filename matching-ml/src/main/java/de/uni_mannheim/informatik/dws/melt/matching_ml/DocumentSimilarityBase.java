package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for all matchers which write a csv file where every line 
 * represents a resource with with cell as identifier like URI and 
 * second cell the corresponding tokens (whitespace separated).
 */
public abstract class DocumentSimilarityBase extends MatcherYAAAJena{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DocumentSimilarityBase.class);
    private static final String newline = System.getProperty("line.separator");
    
    protected File corpusFile;
    protected Collection<Property> textProperties;
    protected boolean addFragment;
    protected boolean matchClasses;
    protected boolean matchProperties;
    protected boolean matchIndividuals;
    
    
    public DocumentSimilarityBase(){
        this.corpusFile = null;
        this.textProperties = new ArrayList<>();
        this.addFragment = true;
        this.matchClasses = true;
        this.matchProperties = true;
        this.matchIndividuals = true;
    }

    
    protected void createCorpusFileIfNecessary(OntModel source, OntModel target) throws IOException{
        if(this.corpusFile == null){
            this.corpusFile = new File("./corpora.txt");
            LOGGER.info("Write corpus file to {} which is later removed.", this.corpusFile.getCanonicalPath());
            this.corpusFile.deleteOnExit();
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.corpusFile), "UTF-8"))){
                if(matchClasses){
                    writeResourceText(source.listClasses(), writer);
                    writeResourceText(target.listClasses(), writer);
                }
                if(matchProperties){
                    writeResourceText(source.listOntProperties(), writer);
                    writeResourceText(target.listOntProperties(), writer);
                }
                if(matchIndividuals){
                    writeResourceText(source.listIndividuals(), writer);
                    writeResourceText(target.listIndividuals(), writer);
                }
            }
        }
    }
    
    protected void writeResourceText(ExtendedIterator<? extends OntResource> resources, Writer writer) throws IOException{
        while (resources.hasNext()) {
            OntResource r = resources.next();
            if(r.isURIResource() == false)
                continue;
            String textForResource = getResourceText(r).trim();
            if(textForResource.isEmpty())
                continue;
            writer.write(StringEscapeUtils.escapeCsv(r.getURI()) + "," + StringEscapeUtils.escapeCsv(textForResource) + newline);
        }
    }
    
    protected String getResourceText(OntResource r){
        Set<String> resourceText = new HashSet<>();
        if(this.addFragment){
            String localName = r.getLocalName();
            if(localName != null){
                String processed = processText(localName);
                if(isBlank(processed) == false)
                    resourceText.add(processed);
            }
        }

        List<Statement> statements = new ArrayList<>();
        if(this.textProperties.isEmpty()){
            statements = r.listProperties().toList();
        }else{
            for(Property p : this.textProperties){
                statements.addAll(r.listProperties(p).toList());
            }
        }

        for(Statement stmt : statements){
            RDFNode n = stmt.getObject();
            if(n.isLiteral()){
                Literal lit = n.asLiteral();
                if(isString(lit)){
                    String processed = processText(lit.getLexicalForm());
                    if(isBlank(processed) == false)
                        resourceText.add(processed);
                }
            }
        }
        return String.join(" ", resourceText);        
    }
    
    protected String processText(String text){
        return StringUtil.getProcessedString(text);
    }
    
        
    protected static boolean isString(Literal lit){        
        //check datatype
        String dtStr = lit.getDatatypeURI() ;
        if (dtStr != null){
            //have datatype -> check it
            if(dtStr.equals(XSDDatatype.XSDstring.getURI()))
                return true;
            if(dtStr.equals(RDF.dtLangString.getURI()))
                return true;
        }
        //datatype == null -> check for language tag
        String lang = lit.getLanguage();
        if ( lang != null  && ! lang.equals(""))
            return true;
        return false;
    }
    
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }
}
