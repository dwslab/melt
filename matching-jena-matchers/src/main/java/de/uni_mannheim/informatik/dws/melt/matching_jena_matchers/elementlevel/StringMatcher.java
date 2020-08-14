package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

import de.uni_mannheim.informatik.dws.melt.matching_base.OaeiOptions;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;
import java.util.function.Function;

public class StringMatcher extends MatcherYAAAJena {

    /**
     * Constructor
     * @param transformationFunction Transformation to be applied to string.
     * @param properties Properties to be used to obtain strings.
     */
    public StringMatcher(Function<String, String> transformationFunction, Property... properties){
        this.transformationFunction = transformationFunction;
        this.properties = Arrays.asList(properties);
    }

    /**
     * Constructor
     * RDFS.label will be used to get strings.
     * @param transformationFunction The transformation function to be applied.
     */
    public StringMatcher(Function<String, String> transformationFunction){
        this(transformationFunction, RDFS.label);
    }

    /**
     * Transformation to be used with Strings.
     */
    private Function<String, String> transformationFunction;

    private Collection<Property> properties;

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        if(OaeiOptions.isMatchingClassesRequired())
            matchResources(source.listClasses(), target.listClasses(), inputAlignment);
        if(OaeiOptions.isMatchingDataPropertiesRequired() || OaeiOptions.isMatchingObjectPropertiesRequired())
            matchResources(source.listAllOntProperties(), target.listAllOntProperties(), inputAlignment);
        if(OaeiOptions.isMatchingInstancesRequired())
            matchResources(source.listIndividuals(), target.listIndividuals(), inputAlignment);
        return inputAlignment;
    }

    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        Map<String, Set<String>> text2URI = new HashMap<>();
        while (sourceResources.hasNext()) {
            OntResource source = sourceResources.next();
            String sourceURI = source.getURI();
            for(String sourceText : getStringRepresentations(source)){
                Set<String> uris = text2URI.get(sourceText);
                if(uris == null){
                    uris = new HashSet<>();
                    text2URI.put(sourceText, uris);
                }
                uris.add(sourceURI);
            }
        }
        while (targetResources.hasNext()) {
            OntResource target = targetResources.next();
            for(String targetText : getStringRepresentations(target)){
                Set<String> sourceURIs = text2URI.get(targetText);
                if(sourceURIs != null){
                    for(String sourceURI : sourceURIs){
                        alignment.add(sourceURI, target.getURI());
                    }
                }
            }
        }
    }

    protected Set<String> getStringRepresentations(Resource r){
        Set<String> values = new HashSet<>();
        if(r.isURIResource() == false)
            return values;
        for(Property p : properties){
            StmtIterator i = r.listProperties(p);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    String processed =  this.transformationFunction.apply(n.asLiteral().getLexicalForm());
                    if(StringUtils.isBlank(processed) == false)
                        values.add(processed);
                }
            }
        }
        return values;
    }

    public Function<String, String> getTransformationFunction() {
        return transformationFunction;
    }
}
