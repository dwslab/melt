package de.uni_mannheim.informatik.dws.ontmatching.ml;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VectorSpaceModelMatcher extends MatcherYAAAJena{
    private final static Logger LOGGER = LoggerFactory.getLogger(VectorSpaceModelMatcher.class);
    private static final String newline = System.getProperty("line.separator");
    
    private Collection<Property> textProperties;
    private boolean addFragment;

    public VectorSpaceModelMatcher(Collection<Property> textProperties, boolean addFragment){
        this.textProperties = textProperties;
        this.addFragment = addFragment;
    }
    
    public VectorSpaceModelMatcher(Collection<Property> textProperties){
        this(textProperties, true);
    }
    
    public VectorSpaceModelMatcher(){
        this(new ArrayList<>());
    }
    
    

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        //TODO: make temporary file and delete and the end
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("oaei-resources/corpora.txt"), "UTF-8"))){
            writeResourceText(source.listClasses(), writer);
            writeResourceText(source.listOntProperties(), writer);
            writeResourceText(source.listIndividuals(), writer);
            
            writeResourceText(target.listClasses(), writer);
            writeResourceText(target.listOntProperties(), writer);
            writeResourceText(target.listIndividuals(), writer);
        }
        
        Gensim.getInstance().trainVectorSpaceModel("corpora", "oaei-resources/corpora.txt");        
        for(Correspondence c : inputAlignment){
            try{
                double conf = Gensim.getInstance().queryVectorSpaceModel("corpora", c.getEntityOne(), c.getEntityTwo());
                c.setConfidence(conf);
            }catch(Exception e){
                LOGGER.warn("Could not get confidence from python server", e);
            }
        }
        
        return inputAlignment;
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
                String processed = StringUtil.getProcessedString(localName);
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
                    String processed = StringUtil.getProcessedString(lit.getLexicalForm());
                    if(isBlank(processed) == false)
                        resourceText.add(processed);
                }
            }
        }
        return String.join(" ", resourceText);        
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
