package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer;

import java.util.LinkedList;
import java.util.List;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;


public class ExplainerResourceUtil {
    public static String getSeparatedLiterals(ExtendedIterator<RDFNode> iter, String separator){
        List<String> list = new LinkedList<>();
        while(iter.hasNext()){
            RDFNode n = iter.next();
            if(n.isLiteral()){
                list.add(n.asLiteral().getLexicalForm());
            }
        }
        return String.join(separator, list);
    }    
}
