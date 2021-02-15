
package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.tracks.Track;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PartitionerFromDatasetIdExtractor implements Partitioner{
    private static final Logger LOGGER = LoggerFactory.getLogger(PartitionerFromDatasetIdExtractor.class);

    private Track track;
    private DatasetIDExtractor idExtractor;    
    private Map<String, Set<TestCasePart>> domainToTestcaseParts;
    
    public PartitionerFromDatasetIdExtractor(Track track, DatasetIDExtractor idExtractor) {
        this.track = track;
        this.idExtractor = idExtractor;
        
        this.domainToTestcaseParts = new HashMap<>();        
        for(TestCase tc : track.getTestCases()){
            String[] sourceTarget = tc.getName().split("-");
            if(sourceTarget.length != 2){
                LOGGER.warn("Test case name contains none or more than one '-' character which is not possible."
                        + " We just skip this test case. Name of the test case: {} Name of the track: {}", tc.getName(), tc.getTrack().getName());
                continue;
            }
            this.domainToTestcaseParts.computeIfAbsent(sourceTarget[0], __->new HashSet<>()).add(new TestCasePart(tc, true));
            this.domainToTestcaseParts.computeIfAbsent(sourceTarget[1], __->new HashSet<>()).add(new TestCasePart(tc, false));
        }
    }
    
    @Override
    public Map<TestCase, SourceTargetURIs> partition(Collection<String> uris) {
        Map<TestCase, SourceTargetURIs>  testcaseMap = new HashMap<>();
        for(String uri : uris){
            String domain = this.idExtractor.getDatasetID(uri);
            for(TestCasePart tcp : domainToTestcaseParts.getOrDefault(domain, new HashSet<>())){
                SourceTargetURIs sourceTarget = testcaseMap.computeIfAbsent(tcp.getTestcase(), __-> new SourceTargetURIs());
                if(tcp.isSource()){
                    sourceTarget.addSourceURI(uri);
                }else{
                    sourceTarget.addTargetURI(uri);
                }
            }
        }
        return testcaseMap;
    }
    
}
