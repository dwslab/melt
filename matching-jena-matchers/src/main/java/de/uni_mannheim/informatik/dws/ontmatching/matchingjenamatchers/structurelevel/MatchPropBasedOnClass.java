package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.structurelevel;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.CorrespondenceRelation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;

/**
 * Graphbased Matcher: check all matched classes and match also properties
 * between them with mean value of both classes
 *     foo <---already matched with c=0.5---> foo
 *      |                                      |
 *     blub <--new with c=(0.5+0.4)/2=0.45--> bla 
 *      |                                      |
 *      v                                      v
 *    bar <----already matched with c=0.4---> bar
 */
public class MatchPropBasedOnClass extends MatcherYAAAJena {

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        inputAlignment.addAll(getPropertyMatches(source, target, inputAlignment));
        return inputAlignment;
    }

    public Alignment getPropertyMatches(OntModel source, OntModel target, Alignment inputAlignment) {
        Alignment result = new Alignment();
        
        //Build up index of domain of properties from onto2 / target
        Map<String, Set<MatchObject>> domainTarget2PropTarget = new HashMap<>();
        ExtendedIterator<OntProperty> allPropsTarget = target.listAllOntProperties();
        while (allPropsTarget.hasNext()) {
            OntProperty propTarget = allPropsTarget.next();
            if(propTarget.isURIResource() == false)
                continue;
            Set<String> targetDomains = getObjectURIs(propTarget.listProperties(source.getProfile().DOMAIN()));
            Set<String> targetRange = getObjectURIs(propTarget.listProperties(source.getProfile().RANGE()));
            
            for(String targetDomain : targetDomains){
                Set<MatchObject> set = domainTarget2PropTarget.get(targetDomain);
                if (set == null) {
                    set = new HashSet<>();
                    domainTarget2PropTarget.put(targetDomain, set);
                }
                set.add(new MatchObject(propTarget, targetRange));
            }
        }
        
        //Iterate over all properties of source onto        
        ExtendedIterator<OntProperty> allPropsSource = source.listAllOntProperties();
        while (allPropsSource.hasNext()) {
            OntProperty propSource = allPropsSource.next();
            if(propSource.isURIResource() == false)
                continue;

            Set<String> sourceDomains = getObjectURIs(propSource.listProperties(source.getProfile().DOMAIN()));
            Set<String> sourceRanges = getObjectURIs(propSource.listProperties(source.getProfile().RANGE()));

            for (String sourceDomain : sourceDomains) {
                for (Correspondence domainCorrespondence : inputAlignment.getCorrespondencesSourceRelation(sourceDomain, CorrespondenceRelation.EQUIVALENCE)) {
                    String targetDomain = domainCorrespondence.getEntityTwo();
                    for(MatchObject propTarget : domainTarget2PropTarget.getOrDefault(targetDomain, new HashSet<MatchObject>())){
                        for(String targetRange : propTarget.getRange()){
                            for (Correspondence rangeCorrespondence : inputAlignment.getCorrespondencesTargetRelation(targetRange, CorrespondenceRelation.EQUIVALENCE)) {
                                if(sourceRanges.contains(rangeCorrespondence.getEntityOne())){
                                    //create property match
                                    double confidence = (domainCorrespondence.getConfidence() + rangeCorrespondence.getConfidence()) / 2.0;
                                    Correspondence propCorrespodence = new Correspondence(propSource.getURI(), propTarget.getP().getURI(), confidence);
                                    //String reason = String.format("Property %s has domain %s which matches to %s (conf: %d) which is domain of property %s. "
                                    //        + "Range %s matches to %s (conf: %d).",
                                    //        propSource.getURI(), sourceDomain, targetDomain, domainCorrespondence.getConfidence(), propTarget.getP().getURI(), 
                                    //        rangeCorrespondence.getEntityOne(), targetRange, rangeCorrespondence.getConfidence());
                                    //propCorrespodence.addExtensionValue(DefaultExtensions.Argumentation.REASON.toString(), "Property ");
                                    result.add(propCorrespodence);
                                }
                            }
                        }
                    }
                }
            }
            
        }
        return result;
    }
    
    
    private static Set<String> getObjectURIs(StmtIterator i) {
        Set<String> set = new HashSet<>();
        while (i.hasNext()) {
            RDFNode o = i.next().getObject();
            if (o.isURIResource()) {
                set.add(o.asResource().getURI());
            }
        }
        return set;
    }
    
    
    private class MatchObject{
        private OntProperty p;
        private Set<String> range;

        public MatchObject(OntProperty p, Set<String> range) {
            this.p = p;
            this.range = range;
        }

        public OntProperty getP() {
            return p;
        }

        public Set<String> getRange() {
            return range;
        }
    }

}
