package de.uni_mannheim.informatik.dws.melt.matching_ml;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.io.*;
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

/**
 * Updates the confidence of already matched resources.
 * It writes a textual representation of each resource to a csv file (text generation can be modified by subclassing and overriding getResourceText method).
 */
public class VectorSpaceModelMatcher extends MatcherYAAAJena {
    private final static Logger LOGGER = LoggerFactory.getLogger(VectorSpaceModelMatcher.class);
    private static final String newline = System.getProperty("line.separator");
    
    private Set<String> textAvailable;
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
        this.textAvailable = new HashSet<>();
        File coporaFile = new File("./corpora.txt");
        coporaFile.deleteOnExit();
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(coporaFile), "UTF-8"))){
            writeResourceText(source.listClasses(), writer);
            writeResourceText(source.listOntProperties(), writer);
            writeResourceText(source.listIndividuals(), writer);
            
            writeResourceText(target.listClasses(), writer);
            writeResourceText(target.listOntProperties(), writer);
            writeResourceText(target.listIndividuals(), writer);
        }
        String modelName = "corpora";
        Gensim.getInstance().trainVectorSpaceModel(modelName, coporaFile.getCanonicalPath());        
        for(Correspondence c : inputAlignment){
            if(this.textAvailable.contains(c.getEntityOne()) && this.textAvailable.contains(c.getEntityTwo())){
                try{
                    double conf = Gensim.getInstance().queryVectorSpaceModel(modelName, c.getEntityOne(), c.getEntityTwo());
                    c.setConfidence(conf);
                }catch(Exception e){
                    LOGGER.warn("Could not get confidence from python server", e);
                }
            }
        }

        if(coporaFile.exists()) {
            coporaFile.delete();
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
            this.textAvailable.add(r.getURI());
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
