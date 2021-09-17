/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_mannheim.informatik.dws.melt.multisourceexperiment.analysis;

import de.uni_mannheim.informatik.dws.melt.matching_base.MeltUtil;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.metric.cm.ConfusionMatrixMetric;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.ExecutorMultiSource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.Partitioner;
import de.uni_mannheim.informatik.dws.melt.matching_eval.multisource.SourceTargetURIs;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.TransitiveClosure;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class analyzes if the reference alignment contains the transitive closure.
 * E.g. if A-B, and B-C in the references alignment, then also A-C.
 */
public class AnalyzeTransitiveClosureOfReference {
    static{ System.setProperty("log4j.skipJansi", "false"); MeltUtil.logWelcomeMessage();}
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzeTransitiveClosureOfReference.class);
    
    public static void main(String[] args){
        checkTransitiveClosureOfReference(TrackRepository.Conference.V1);
        checkTransitiveClosureOfReference(TrackRepository.Knowledgegraph.V3);
        //checkTransitiveClosureOfReference(TrackRepository.Largebio.V2016.ONLY_WHOLE);
    }
    
    public static void checkTransitiveClosureOfReference(Track track){
        LOGGER.info("analyze track: {}", track.getName());
        TransitiveClosure<String> closure = new TransitiveClosure<>();
        for(TestCase testCase : track.getTestCases()){
            for(Correspondence correspondence : testCase.getParsedReferenceAlignment()){
                closure.add(correspondence.getEntityOne(), correspondence.getEntityTwo());
            }
        }
        
        Partitioner partitioner = ExecutorMultiSource.getMostSpecificPartitioner(track);
        Map<TestCase, Alignment> testcaseToTransitiveAlignment = new HashMap<>();
        for(Set<String> sameAs : closure.getClosure()){
            Map<TestCase, SourceTargetURIs> map = partitioner.partition(sameAs);
            for(Map.Entry<TestCase, SourceTargetURIs> entry : map.entrySet()){
                SourceTargetURIs sourceTargetUris = entry.getValue();
                if(sourceTargetUris.containsSourceAndTarget() == false)
                    continue;
                Alignment alignment = testcaseToTransitiveAlignment.computeIfAbsent(entry.getKey(), __->new Alignment());
                for(String sourceURI : sourceTargetUris.getSourceURIs()){
                    for(String targetURI : sourceTargetUris.getTargetURIs()){
                        alignment.add(sourceURI, targetURI);
                    }
                }
            }
        }
        
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for(TestCase testCase : track.getTestCases()){
            Alignment reference = testCase.getParsedReferenceAlignment();
            Alignment transitiveAlignment = testcaseToTransitiveAlignment.get(testCase);
            
            if(reference.equals(transitiveAlignment)){
                LOGGER.info("All transitive alignments in test case {} are also in reference.", testCase.getName());
                stats.addValue(0);
            }else{
                Alignment difference = Alignment.subtraction(transitiveAlignment, reference);
                LOGGER.info("In test case {} the following correspondences are transitvely infered but NOT in the refrencealignment {}", testCase.getName(), difference.size());
                LOGGER.info(difference.toStringMultiline());
                stats.addValue(difference.size());
            }
        }
        LOGGER.info("Stats for whole track: {}", stats);
    }
    
}
