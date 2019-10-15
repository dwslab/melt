package de.uni_mannheim.informatik.dws.ontmatching.ml;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.io.BufferedWriter;
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


public class TextGenerator extends MatcherYAAAJena{
    private static final String newline = System.getProperty("line.separator");
    private Collection<Property> textProperties;

    public TextGenerator(){
        this.textProperties = new ArrayList<>();
    }
    
    public TextGenerator(Collection<Property> textProperties){
        this.textProperties = textProperties;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        //TODO: make temporary file and delete and the end
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("oaei-resources/corpora.txt"), "UTF-8"))){
            writeResourceText(source.listClasses(), textProperties, true, writer);
            writeResourceText(source.listOntProperties(), textProperties, true, writer);
            writeResourceText(source.listIndividuals(), textProperties, true, writer);
            
            writeResourceText(target.listClasses(), textProperties, true, writer);
            writeResourceText(target.listOntProperties(), textProperties, true, writer);
            writeResourceText(target.listIndividuals(), textProperties, true, writer);
        }        
        return inputAlignment;
        
    }
    
    protected void writeResourceText(ExtendedIterator<? extends OntResource> resources, Collection<Property> properties, boolean addFragment, Writer writer) throws IOException{
        while (resources.hasNext()) {
            OntResource r = resources.next();
            if(r.isURIResource() == false)
                continue;
            Set<String> resourceText = new HashSet<>();
            if(addFragment){
                String localName = r.getLocalName();
                if(localName != null){
                    String processed = StringUtil.getProcessedString(localName);
                    if(isBlank(processed) == false)
                        resourceText.add(processed);
                }
            }
            
            List<Statement> statements = new ArrayList<>();
            if(properties.isEmpty()){
                statements = r.listProperties().toList();
            }else{
                for(Property p : properties){
                    statements.addAll(r.listProperties(p).toList());
                }
            }

            for(Statement stmt : statements){
                RDFNode n = stmt.getObject();
                if(n.isLiteral()){
                    Literal lit = n.asLiteral();
                    if(isString(lit)){
                        String processed = StringUtil.getProcessedString(lit.getLexicalForm());
                        if(isBlank(processed) == false)
                            resourceText.add(processed);
                    }
                }
            }
            
            if(resourceText.isEmpty())
                continue;
            
                        
            writer.write(r.getURI() + "," + StringEscapeUtils.escapeCsv(String.join(" ", resourceText)) + newline);
        }
    }
        
    private static boolean isString(Literal lit){        
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
