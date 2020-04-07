package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel.scale;

import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 * Matcher which uses different String Matching approaches (stored in PropertySpecificStringProcessing) with a specific confidence.
 * Multiple normalization are possible as shown below.
 * The highest confidence is taken at the end.
 * <pre>{@code 
 * Function<String, Object> equality = (text) -> text;
 * Function<String, Object> lowercase = (text) -> text.toLowerCase();
 * ScalableStringProcessingMatcher matcherOne = new ScalableStringProcessingMatcher(Arrays.asList(
 *              new PropertySpecificStringProcessing(RDFS.label, equality, 1.0),
 *              new PropertySpecificStringProcessing(RDFS.label, lowercase, 0.9)
 *              new PropertySpecificStringProcessing(SKOS.altLabel, equality, 0.7),
 *              new PropertySpecificStringProcessing(SKOS.altLabel, lowercase, 0.6)
 * ));
 * }</pre>
 */
public class ScalableStringProcessingMatcher extends MatcherYAAAJena{

    protected Iterable<PropertySpecificStringProcessing> processingElements;
    protected Set<Property> usedProperties;
    
    protected boolean matchClasses = true;
    protected boolean matchProperties = true;
    protected boolean matchInstances = true;    
    protected boolean earlyStopping = true;
    
    
    public ScalableStringProcessingMatcher(Iterable<PropertySpecificStringProcessing> processingElements, boolean earlyStopping){
        this.earlyStopping = earlyStopping;
        this.processingElements = processingElements;
        this.usedProperties = new HashSet<>();
        for(PropertySpecificStringProcessing p: processingElements){
            this.usedProperties.addAll(p.getProperties());
        }
    }
    
    public ScalableStringProcessingMatcher(Iterable<PropertySpecificStringProcessing> processingElements){
        this(processingElements, true);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired() && matchClasses)
            matchResources(source.listClasses(), target.listClasses(), inputAlignment);        
        if((OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired()) && matchProperties)
            matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);        
        if(OaeiOptions.isMatchingInstancesRequired() && matchInstances)
            matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
        return inputAlignment;
    }
    
    
    public void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        //index name -> tokens/ids -> (list of resources)
        Map<String,Map<Object, Set<String>>> index = new HashMap<>();

        //source
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            if(source.isURIResource() == false)
                continue;
            String sourceURI = source.getURI();            
            Map<Property, Set<String>> literalMap = extractAllLiterals(source);
            for(PropertySpecificStringProcessing processing : this.processingElements){
                Map<Object, Set<String>> tokenIndex = index.computeIfAbsent(processing.getIndexName(), k->new HashMap<>());
                for(String sourceLabels : getLiterals(processing, literalMap)){
                    if(StringUtils.isBlank(sourceLabels))
                        continue;
                    Object o = processing.getProcessing().apply(sourceLabels);
                    if(isObjectEmpty(o))
                        continue;
                    tokenIndex.computeIfAbsent(o, k-> new HashSet<>()).add(sourceURI);
                }
            }
        }
        
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            if(target.isURIResource() == false)
                continue;
            String targetURI = target.getURI();
            
            Map<Property, Set<String>> literalMap = extractAllLiterals(target);
            for(PropertySpecificStringProcessing processing : this.processingElements){
                Map<Object, Set<String>> tokenIndex = index.get(processing.getIndexName());
                if(tokenIndex == null)
                    continue;
                boolean findMatch = false;
                for(String targetLabel : getLiterals(processing, literalMap)){
                    if(StringUtils.isBlank(targetLabel))
                        continue;
                    Object o = processing.getProcessing().apply(targetLabel);
                    if(isObjectEmpty(o))
                        continue;
                    for(String sourceURI : tokenIndex.getOrDefault(o, new HashSet<>())){
                        findMatch = true;
                        alignment.addOrUseHighestConfidence(sourceURI, targetURI, processing.getConfidence());
                    }
                }
                if(findMatch && earlyStopping)
                    break;
            }
        }
    }
    
    
    protected Set<String> getLiterals(PropertySpecificStringProcessing processing, Map<Property, Set<String>> literalMap){
        Set<String> literals = new HashSet<>();
        for(Property p : processing.getProperties()){
            literals.addAll(literalMap.get(p));
        }
        return literals;
    }
    
    protected Map<Property,Set<String>> extractAllLiterals(Resource r){
        Map<Property, Set<String>> literals = new HashMap<>();
        for(Property p : this.usedProperties){
            literals.put(p, getAllStringValue(r, p));
        }
        return literals;
    }
    
    
    protected Set<String> getAllStringValue(Resource r, Property p){
        Set<String> values = new HashSet<>();
        if(p.equals(PropertySpecificStringProcessing.URL_FRAGMENT)){
            values.add(r.getLocalName().trim());
        } else if(p.equals(PropertySpecificStringProcessing.ALL_LITERALS)){
            StmtIterator i = r.listProperties();
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    String text = n.asLiteral().getLexicalForm().trim();
                    values.add(text);
                }
            }
        }else if(p.equals(PropertySpecificStringProcessing.ALL_STRING_LITERALS)){
            StmtIterator i = r.listProperties();
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    Literal lit = n.asLiteral();
                    if(isLiteralAString(lit)){
                        String text = lit.getLexicalForm().trim();
                        values.add(text);
                    }
                }
            }
        } else{
            StmtIterator i = r.listProperties(p);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    String text = n.asLiteral().getLexicalForm().trim();
                    values.add(text);
                }
            }
        }        
        return values;
    }
    
    private static boolean isLiteralAString(Literal lit){        
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
    
    protected boolean isObjectEmpty(Object o){
        if(o == null)
            return true;
        if(o instanceof String){
            if(StringUtils.isBlank((String)o))
                return true;
        }
        return false;
    }

    public boolean isMatchClasses() {
        return matchClasses;
    }

    public void setMatchClasses(boolean matchClasses) {
        this.matchClasses = matchClasses;
    }

    public boolean isMatchProperties() {
        return matchProperties;
    }

    public void setMatchProperties(boolean matchProperties) {
        this.matchProperties = matchProperties;
    }

    public boolean isMatchInstances() {
        return matchInstances;
    }

    public void setMatchInstances(boolean matchInstances) {
        this.matchInstances = matchInstances;
    }

    public boolean isEarlyStopping() {
        return earlyStopping;
    }

    public void setEarlyStopping(boolean earlyStopping) {
        this.earlyStopping = earlyStopping;
    }
    
}
