package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.instancelevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.DefaultHashMap;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Matches properties based on same subject and object and the distribution.
 */
public class MatchPropBasedOnInstances extends MatcherYAAAJena{

    private File debugFile;
    
    public MatchPropBasedOnInstances(){
        this.debugFile = null;
    }
    
    public MatchPropBasedOnInstances(File debugFile){
        this.debugFile = debugFile;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        inputAlignment.addAll(getPropertyAlignment(source, target, inputAlignment));
        return inputAlignment;
    }
    
    
    public Alignment getPropertyAlignment(OntModel source, OntModel target, Alignment inputAlignment) throws Exception {
        
        Map<String, Integer> sourceProperties = getPropertyURIs(source);
        Map<String, Integer> targetProperties = getPropertyURIs(target);
        
        
        Map<Correspondence, Integer> counter = new HashMap(); //map correspondence (prop-prop) to how often they overlap
        
        for(Correspondence corr : inputAlignment){            
            Individual sourceIndividual = source.getIndividual(corr.getEntityOne());
            Individual targetIndividual = target.getIndividual(corr.getEntityTwo());
            if(sourceIndividual == null || targetIndividual == null)
                continue;
            
            Map<String, Set<Property>> objectToProperty = new DefaultHashMap(HashSet.class);
            
            StmtIterator targetStmts = targetIndividual.listProperties();
            while(targetStmts.hasNext()){
                Statement s = targetStmts.next();
                Property p = s.getPredicate();
                if(p.isURIResource() == false)
                    continue;
                if(targetProperties.keySet().contains(p.getURI()) == false)
                    continue;
                RDFNode object = s.getObject();
                if(object.isURIResource()){
                    objectToProperty.get(object.asResource().getURI()).add(p);
                }else if(object.isLiteral()){
                    objectToProperty.get(object.asLiteral().getLexicalForm()).add(p);
                }
            }
            
            StmtIterator stmts = sourceIndividual.listProperties();
            while(stmts.hasNext()){
                Statement s = stmts.next();
                Property pSource = s.getPredicate();
                if(pSource.isURIResource() == false)
                    continue;
                if(sourceProperties.keySet().contains(pSource.getURI()) == false)
                    continue;
                RDFNode object = s.getObject();
                if(object.isURIResource()){
                    //object properties
                    for(Property pTarget : objectToProperty.get(object.asResource().getURI())){
                        Correspondence c = new Correspondence(pSource.getURI(), pTarget.getURI());
                        counter.put(c, counter.getOrDefault(c, 0) + 1);
                    }
                }else if(object.isLiteral()){
                    //datatype properties
                    for(Property pTarget : objectToProperty.get(object.asLiteral().getLexicalForm())){
                        Correspondence c = new Correspondence(pSource.getURI(), pTarget.getURI());
                        counter.put(c, counter.getOrDefault(c, 0) + 1);
                    }
                }
            }
        }
        saveValuesToFile(counter, sourceProperties, targetProperties);
        return new Alignment();
    }
    
    
    private void saveValuesToFile(Map<Correspondence, Integer> propertyAlignment, Map<String, Integer> sourceProperties, Map<String, Integer> targetProperties) throws IOException {
        if(this.debugFile == null)
            return;
        List<Map.Entry<Correspondence, Integer>> list = new ArrayList<>(propertyAlignment.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);
        DecimalFormat df = new DecimalFormat("#0.000"); 
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(this.debugFile))){
            writer.write("source URI,target URI,Same Statements, Statements of P in Source, Statements of P' in Target");
            writer.newLine();
            for(Map.Entry<Correspondence, Integer> t : list){
                writer.write(t.getKey().getEntityOne() + "," + t.getKey().getEntityTwo() + "," + t.getValue().toString()
                    + "," + sourceProperties.getOrDefault(t.getKey().getEntityOne(), 0) + "," + targetProperties.getOrDefault(t.getKey().getEntityTwo(), 0)
                );
                writer.newLine();
            }
        }
    }
    
    protected Map<String, Integer> getPropertyURIs(OntModel m){
        Map<String, Integer> uris = new HashMap();
        ExtendedIterator<OntProperty> properties = m.listAllOntProperties();
        while(properties.hasNext()){
            OntProperty p = properties.next();
            if(p.isURIResource()){
                uris.put(p.getURI(), m.listStatements(null, p, (RDFNode)null).toList().size());
            }
        }
        return uris;
    }
    
    /*
    public static boolean compareLiteralsLoosely(Literal a, Literal b){
        if(a.equals(b))
            return true;
        if(a.getValue().equals(b.getValue()))
            return true;
        if(a.sameValueAs(b))
            return true;
        if(a.getLexicalForm().equals(b.getLexicalForm()))
            return true;
        return false;        
    }
    */
}
