package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.literalextractors;

import de.uni_mannheim.informatik.dws.melt.matching_jena.LiteralExtractor;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * This extractor uses all literals which are also strings e.g. all literal with datatype XSD string or dtLangString or have a language tag. 
 */
public class LiteralExtractorAllStringLiterals implements LiteralExtractor {

    @Override
    public Set<Literal> extract(Resource r) {
        Set<Literal> values = new HashSet<>();
        StmtIterator i = r.listProperties();
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                Literal lit = n.asLiteral();
                if(isLiteralAString(lit)){
                    values.add(lit);
                }
            }
        }
        return values;
    }
    
    public static boolean isLiteralAString(Literal lit){        
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
    
    @Override
    public int hashCode() {
        return 165724;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        return getClass() == obj.getClass();
    }
}
