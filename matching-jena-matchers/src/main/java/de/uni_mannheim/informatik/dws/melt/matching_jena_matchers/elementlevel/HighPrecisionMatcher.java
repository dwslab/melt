package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.elementlevel;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ResourcesExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.NtoMCorrespondenceFilter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.resourcesExtractors.ResourcesExtractorDefault;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A high precision matcher which focuses on URI fragment and label (only element based string comparison).
 * It achives the following scores:
 * <pre>
 * | Track              | PREC   | REC    | F1     |
 * |--------------------|--------|--------|--------|
 * | anatomy            | 0.9905 | 0.6174 | 0.7607 |
 * | conference (micro) | 0.8046 | 0.4590 | 0.5845 |
 * | largebio           | 0.9911 | 0.4233 | 0.5932 |
 * </pre>
 */
public class HighPrecisionMatcher extends MatcherYAAAJena {
    private static final Logger LOGGER = LoggerFactory.getLogger(HighPrecisionMatcher.class);
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        LOGGER.debug("Start matching");
        for(ResourcesExtractor extractor : ResourcesExtractorDefault.getDefaultExtractors()){
            LOGGER.debug("Match {}", extractor.getClass().getSimpleName());
            matchResources(extractor.extract(source, properties), extractor.extract(target, properties), inputAlignment);
        }
        LOGGER.debug("Remove N:M correspondences");
        inputAlignment = NtoMCorrespondenceFilter.filter(inputAlignment);
        LOGGER.debug("Finish");
        return inputAlignment;
    }

    private void matchResources(Iterator<? extends OntResource> sourceResources, Iterator<? extends OntResource> targetResources, Alignment alignment) {
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
        if(r.isURIResource() == false) // extract only from uri resources
            return values;

        StmtIterator i = r.listProperties(RDFS.label);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                String processed = normalizeText(n.asLiteral().getLexicalForm());
                if(StringUtils.isBlank(processed) == false)
                    values.add(processed);
            }
        }
        String fragment = URIUtil.getUriFragment(r.getURI());
        if(StringProcessing.containsMostlyNumbers(fragment) == false){
            String processed = normalizeText(fragment);
            if(StringUtils.isBlank(processed) == false)
                values.add(processed);
        }
        return values;
    }

    public String normalizeText(String text) {
        return String.join(" ", StringProcessing.normalize(text));
    }
}
