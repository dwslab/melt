package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
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
     * Initialises the BadHostsFilter in a non strict mode.
     * This means if the host of source or target in a correspondence can not be determined, then the correspondence is added to the filtered alignment.
     */
    public BadHostsFilter(){
        this.strict = false;
    }
    
    /**
     * Constructor
     * @param strict if true, filter all correspondences where the host can not be determined. 
     * If false, also include correspondences where the host could not be determined.
     */
    public BadHostsFilter(boolean strict){
        this.strict = strict;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(source, target, inputAlignment, this.strict);
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
        return filter(source, target, inputAlignment, false);
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
        String sourceHostURI = getHostURIOfModel(source);
        String targetHostURI = getHostURIOfModel(target);
        if(sourceHostURI.isEmpty() || targetHostURI.isEmpty()){
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
            if(sourceHost.equals(sourceHostURI) && targetHost.equals(targetHostURI)){
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
    
    public static String getHostURIOfModel(OntModel m){
        String prefix = m.getNsPrefixURI("");
        if(prefix==null){
            ExtendedIterator<OntClass> i = m.listClasses();
            if(i.hasNext())
                prefix = i.next().getNameSpace();
            else
                prefix = "";
        }
        return getHostOfURI(prefix);
    }
}
