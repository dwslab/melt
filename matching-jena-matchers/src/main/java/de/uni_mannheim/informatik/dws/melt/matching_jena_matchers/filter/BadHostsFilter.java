package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter removes correspondences where the source or target has not the same host of the OntModels.
 * E.g. it removes rdf:type=rdf:type or foaf:knows=foaf:knows
 */
public class BadHostsFilter extends MatcherYAAAJena implements Filter {


    /**
     * Default logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BadHostsFilter.class);

    /**
     * if true, filter all correspondences where the host can not be determined
     */
    private boolean strict;
    
    /**
     * the function to extract the host URI of a model. Possibilities: BadHostsFilter::getHostURIOfModel, BadHostsFilter::getHostURIOfModelBySampling etc.
     */
    private Function<OntModel, String> hostOfModelFunction;
    
    /**
     * Initialises the BadHostsFilter in a non strict mode.
     * This means if the host of source or target in a correspondence can not be determined, then the correspondence is added to the filtered alignment.
     */
    public BadHostsFilter(){
        this(false, BadHostsFilter::getHostURIOfModel);
    }
    
    /**
     * Constructor
     * @param strict if true, filter all correspondences where the host can not be determined. 
     * If false, also include correspondences where the host could not be determined.
     */
    public BadHostsFilter(boolean strict){
        this(strict, BadHostsFilter::getHostURIOfModel);
    }
    
    /**
     * Constructor
     * @param strict if true, filter all correspondences where the host can not be determined. 
     * If false, also include correspondences where the host could not be determined.
     * @param hostOfModelFunction the function to extract the host URI of a model. Possibilities: BadHostsFilter::getHostURIOfModel, BadHostsFilter::getHostURIOfModelBySampling etc.
     */
    public BadHostsFilter(boolean strict, Function<OntModel, String> hostOfModelFunction){
        this.strict = strict;
        this.hostOfModelFunction = hostOfModelFunction;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(source, target, inputAlignment, this.strict, this.hostOfModelFunction);
    }

    /**
     * Filters the alignment based on similar hosts in a non strict mode
     * (if the host of source or target in a correspondence can not be determined, then the correspondence is added to the filtered alignment).
     * @param source the source ontology
     * @param target the target ontology
     * @param inputAlignment the alignment to be filtered
     * @return the filtered alignment.
     */
    public static Alignment filter(OntModel source, OntModel target, Alignment inputAlignment){
        return filter(source, target, inputAlignment, false, BadHostsFilter::getHostURIOfModel);
    }
    
    /**
     * Filters the alignment based on similar hosts.
     * @param source the source ontology
     * @param target the target ontology
     * @param inputAlignment the alignment to be filtered
     * @param strict if true, filter all correspondences where the host can not be determined
     * @return the filtered alignment.
     */
    public static Alignment filter(OntModel source, OntModel target, Alignment inputAlignment, boolean strict){
        return filter(source, target, inputAlignment, strict, BadHostsFilter::getHostURIOfModel);
    }
    
    /**
     * Filters the alignment based on similar hosts.
     * @param source the source ontology
     * @param target the target ontology
     * @param inputAlignment the alignment to be filtered
     * @param strict if true, filter all correspondences where the host can not be determined
     * @param hostOfModelFunction the function to extract the host URI of a model. Possibilities: BadHostsFilter::getHostURIOfModel, BadHostsFilter::getHostURIOfModelBySampling etc.
     * @return the filtered alignment.
     */
    public static Alignment filter(OntModel source, OntModel target, Alignment inputAlignment, boolean strict, Function<OntModel, String> hostOfModelFunction){
        String sourceHostURI = hostOfModelFunction.apply(source);
        String targetHostURI = hostOfModelFunction.apply(target);
        return filter(sourceHostURI, targetHostURI, inputAlignment, strict);
    }
    
    /**
     * Filters the alignment based on similar hosts.
     * @param expectedSourceHost the expected source host (can be extracted from an ontModel with getHostURIOfModel).
     * @param expectedTargetHost the expected target host (can be extracted from an ontModel with getHostURIOfModel).
     * @param inputAlignment the alignment to be filtered
     * @param strict if true, filter all correspondences where the host can not be determined
     * @return the filtered alignment.
     */
    public static Alignment filter(String expectedSourceHost, String expectedTargetHost, Alignment inputAlignment, boolean strict){
        if(expectedSourceHost == null || expectedTargetHost == null | expectedSourceHost.isEmpty() || expectedTargetHost.isEmpty()){
            LOGGER.warn("Source or target host URI is not defined. BadHostsFilter will not filter anything.");
            return inputAlignment;
        }
        Alignment resultAlignment = new Alignment();
        for(Correspondence c : inputAlignment){
            String sourceHost = getHostOfURI(c.getEntityOne());
            String targetHost = getHostOfURI(c.getEntityTwo());
            if(sourceHost.isEmpty() || targetHost.isEmpty()){
                if(strict == false)
                    resultAlignment.add(c); // could not check if host is equals -> do not filter it
                continue;
            }
            if(sourceHost.equals(expectedSourceHost) && targetHost.equals(expectedTargetHost)){
                resultAlignment.add(c);
            }else{
                LOGGER.trace("Correspondence {} is removed by BadHostsFilter", c);
            }
        }
        return resultAlignment;
    }
    
    public static String getHostOfURI(String uri){
        if(uri == null || uri.isEmpty())
            return "";
        try {
            String host = new URI(uri).getHost();
            if(host == null)
                return "";
            return host;
	} catch (URISyntaxException e) {
            LOGGER.warn("Could not determine host URI of {}.", uri);
            return "";
        }
    }
    
    /**
     * Extracts the host URI of the model.
     * This implementation defaults to {@link #getHostURIOfModelByPrefixOrFirstURI(org.apache.jena.ontology.OntModel) }.
     * @param m the model
     * @return the hostURI of most resources in this model.
     */
    public static String getHostURIOfModel(OntModel m){
        return getHostURIOfModelByPrefixOrFirstURI(m);
    }
    
    /**
     * Extracts the host URI of the model by using the prefix of <code>:</code>.
     * Or search for the first class and extracts the host of this URL.
     * @param m
     * @return 
     */
    public static String getHostURIOfModelByPrefixOrFirstURI(OntModel m){
        String prefix = m.getNsPrefixURI("");
        if(prefix!=null)
            return getHostOfURI(prefix);
        
        ExtendedIterator<OntClass> i = m.listClasses();
        while(i.hasNext()){
            String uri = i.next().getURI();
            if(uri != null){
                return getHostOfURI(uri);
            }
        }
        return "";
    }
    
    public static String getHostURIOfModelBySampling(OntModel m){
        return getHostURIOfModelBySampling(m, 50); // allow up to 25 "wrong" URIs per 
    }
    public static String getHostURIOfModelBySampling(OntModel m, int sampleSize){
        Counter<String> counter = new Counter<>();
        counter.addAll(getURIHostSample(m.listClasses(), sampleSize));
        counter.addAll(getURIHostSample(m.listAllOntProperties(), sampleSize));
        counter.addAll(getURIHostSample(m.listIndividuals(), sampleSize));
        String mostCommon = counter.mostCommonElement();
        if(mostCommon == null)
            return "";
        return mostCommon;
    }
    
    private static List<String> getURIHostSample(Iterator<? extends OntResource> i, int sampleSize){
        List<String> uris = new ArrayList<>();
        while(i.hasNext()){
            String uri = i.next().getURI();
            if(uri != null){
                uris.add(getHostOfURI(uri));
            }
            if(uris.size() >= sampleSize)
                break;
        }
        return uris;
    }
    
    public static String getHostURIOfModelByFullAnalysis(OntModel m){
        Counter<String> counter = new Counter<>();
        counter.addAll(getURIHosts(m.listClasses()));
        counter.addAll(getURIHosts(m.listAllOntProperties()));
        counter.addAll(getURIHosts(m.listIndividuals()));
        String mostCommon = counter.mostCommonElement();
        if(mostCommon == null)
            return "";
        return mostCommon;
    }
    
    private static List<String> getURIHosts(Iterator<? extends OntResource> i){
        List<String> uris = new ArrayList<>();
        while(i.hasNext()){
            String uri = i.next().getURI();
            if(uri != null){
                uris.add(getHostOfURI(uri));
            }
        }
        return uris;
    }
}
