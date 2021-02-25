package de.uni_mannheim.informatik.dws.melt.matching_eval.util;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutorMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A matcher which tries to detect the testcase and return the reference alignment.
 * This matcher is only for testing purposes.
 */
public class ReferenceMatcher extends MatcherYAAAJena{
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceMatcher.class);
    
    private Map<String, Alignment> map;
    private DatasetIDExtractor idExtractor;
    
    public ReferenceMatcher(Track track){
        this.map = new HashMap<>();
        this.idExtractor = ExecutorMultiSource.getMostSpecificDatasetIdExtractor(track);
        for(TestCase tc : track.getTestCases()){
            this.map.put(tc.getName(), tc.getParsedReferenceAlignment());
        }
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        String datasetIDSource = getDatasetID(source);
        String datasetIDTarget = getDatasetID(target);
        
        String testCaseName = datasetIDSource + "-" + datasetIDTarget;
        Alignment a = this.map.get(testCaseName);
        if(a == null){
            //logger testCaseName
            LOGGER.warn("Did not find an reference alignment for testcase {}. Returning empty alignment.", testCaseName);
            return new Alignment();
        }
        return a;
    }
    
    private String getDatasetID(OntModel m){
        Counter<String> counter = new Counter<>();        
        StmtIterator i = m.listStatements();
        while(i.hasNext()){
            String uri = i.next().getSubject().getURI();
            if(uri != null){
                counter.add(this.idExtractor.getDatasetID(uri));
            }
        }
        return counter.mostCommonElement();
    }
}
