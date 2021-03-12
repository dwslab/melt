package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.extraction;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Faster implementation than {@link HungarianExtractor} for generating a one-to-one alignment.
 * The implementation is based on http://www.mpi-inf.mpg.de/~mehlhorn/Optimization/bipartite_weighted.ps (page 13-19).
 * @see <a href="http://ceur-ws.org/Vol-551/om2009_Tpaper5.pdf">Paper: Efficient Selection of Mappings and Automatic Quality-driven Combination of Matching Methods</a>
 * @see <a href="https://github.com/agreementmaker/agreementmaker/tree/master/projects/core/src/main/java/am/app/mappingEngine/oneToOneSelection">Implementation at Agreementmaker</a>
 */
public class MaxWeightBipartiteExtractor extends MatcherYAAAJena implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MaxWeightBipartiteExtractor.class);
    
    private final static int DEFAULT_MULTIPLIER = 10000;
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment);
    }
    
    public static Alignment filter(Alignment inputAlignment){
        return filter(inputAlignment, MwbInitHeuristic.NAIVE, DEFAULT_MULTIPLIER);
    }
    
    public static Alignment filter(Alignment inputAlignment, MwbInitHeuristic heuristic){
        return filter(inputAlignment, heuristic, DEFAULT_MULTIPLIER);
    }
    
    /**
     * Filters the alignment by computing a maximal one to one alignment. 
     * Unfortunately we need to convert the double confidences to integers (double are multiplied by multiplier). Default is to use 4 digits after decimal. 
     * For further reference see page 6 Arithmetic Demand at <a href="http://www.mpi-inf.mpg.de/~mehlhorn/Optimization/bipartite_weighted.ps">http://www.mpi-inf.mpg.de/~mehlhorn/Optimization/bipartite_weighted.ps</a>.
     * @param inputAlignment the alignment to filter.
     * @param heuristic the heuristic to use.
     * @param multiplier the multiplier to use (how many digits of confidence are used.
     * @return the filtered alignment.
     */
    public static Alignment filter(Alignment inputAlignment, MwbInitHeuristic heuristic, int multiplier){
        if(inputAlignment.isEmpty())
            return inputAlignment;
        
        Map<String, MwbNode> sourceNodeMapping = new HashMap<>();
        Map<String, MwbNode> targetNodeMapping = new HashMap<>();        
        //switch source target depending on which one is larger
        if(inputAlignment.getDistinctSourcesAsSet().size() > inputAlignment.getDistinctTargetsAsSet().size()){
            for(Correspondence c : inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE)){
                MwbNode source = sourceNodeMapping.computeIfAbsent(c.getEntityTwo(), __ -> new MwbNode());
                MwbNode target = targetNodeMapping.computeIfAbsent(c.getEntityOne(), __ -> new MwbNode());
                source.addSuccesor(new MwbEdge(source, target, c, convertDoubleToInt(c.getConfidence(), multiplier))); //directed edge from source(A) to target(B)
            }
        } else {
            for(Correspondence c : inputAlignment.getCorrespondencesRelation(CorrespondenceRelation.EQUIVALENCE)){
                MwbNode source = sourceNodeMapping.computeIfAbsent(c.getEntityOne(), __ -> new MwbNode());
                MwbNode target = targetNodeMapping.computeIfAbsent(c.getEntityTwo(), __ -> new MwbNode());
                source.addSuccesor(new MwbEdge(source, target, c, convertDoubleToInt(c.getConfidence(), multiplier))); //directed edge from source(A) to target(B)
            }
        }

        switch(heuristic){
            case NAIVE:
                int maxConfidence = convertDoubleToInt(Collections.max(inputAlignment.getDistinctConfidencesAsSet()), multiplier);
                for(MwbNode a : sourceNodeMapping.values()){
                    a.setPotential(maxConfidence);
                }
                break;
            case SIMPLE:
                for(MwbNode a : sourceNodeMapping.values()){
                    MwbEdge eMax = null;
                    int cMax = 0;
                    for(MwbEdge e : a.getSuccessor()){
                        if(e.getWeight() > cMax){
                            eMax = e;
                            cMax = e.getWeight();
                        }
                    }
                    a.setPotential(cMax);
                    //following is commented because it doesn't make sense to choose this edge so early (tests fail)
                    /*
                    if(eMax != null && eMax.getTarget().isFree()){
                        eMax.reverse();//reverseEdge(eMax);
                        a.setFree(false);
                        eMax.getTarget().setFree(false);
                    }
                    */
                }
                break;
            case REFINED:
                throw new UnsupportedOperationException("Not implemented yet.");
        }
        
        //shortest path augmentation
        PriorityQueue<MwbNode> PQ = new PriorityQueue<>();
        for(MwbNode a : sourceNodeMapping.values()){
            if(a.isFree())
                augment(a, PQ);
        }
        
        //selected correspondences are edges from target(B) to source(A)
        Alignment result = new Alignment(inputAlignment, false);
        for(MwbNode b : targetNodeMapping.values()){
            Set<MwbEdge> selectedEdges = b.getSuccessor();
            if(selectedEdges.size() > 1){
                LOGGER.warn("There is more than one match - this should not happen... (Correspondence: {})", selectedEdges.iterator().next().getCorrespondence());
            }
            for(MwbEdge e: selectedEdges){
                result.add(e.getCorrespondence());
            }
        }
        return result;
    }
    
    private static int convertDoubleToInt(double d, int multiplier){
        double d2 = d * multiplier;
        int i2 = (int)d2;
        double residue = d2 - i2;
        if(residue < 0.5){
                return i2;
        }
        return i2+1;
    }
    
    
    private static void augment(MwbNode a, PriorityQueue<MwbNode> PQ){
        
        //initialization
        a.setDistance(0);
        MwbNode bestNodeInA = a;
        int minA = a.getPotential();
        int delta;
        Stack<MwbNode> RA = new Stack<>();
        RA.push(a);
        Stack<MwbNode> RB = new Stack<>();
        
        relaxAllEdges(a,RB, PQ);
        
        while(true){
            //select from PQ the node b with minimal distance db
            MwbNode b;
            int db;
            if(PQ.isEmpty()){
                b = null;
                db = 0; //just any value
            }
            else{
                b = PQ.poll();
                db = b.getDistance();
            }
            //distinguish three cases
            if(b == null || db >= minA){
                delta = minA;
                //augmentation by path to best node in A
                augmentPathTo(bestNodeInA);
                a.setFree(false);
                bestNodeInA.setFree(true);//order is important if a is best node in a we want it to be true
                break;
            } else if(b.isFree()){
                delta = db;
                augmentPathTo(b);
                a.setFree(false);
                b.setFree(false);
                break;
            }else{
                //continue the shortest path computation                
                Iterator<MwbEdge> edges = b.getSuccessor().iterator();
                if(edges.hasNext() == false){
                    LOGGER.error("Nod eb should have successor. This should not happen.");
                    continue;                    
                }
                MwbEdge e = edges.next();
                MwbNode a1 = e.getTarget();
                a1.setPredecessor(e);
                RA.push(a1);
                a1.setDistance(db);
                if(db + a1.getPotential() < minA){
                    bestNodeInA = a1;
                    minA = db + a1.getPotential();
                }
                relaxAllEdges(a1,RB, PQ);
            }
        }
        //potential update and reinitialization
        while(!RA.isEmpty()){
            MwbNode x = RA.pop();
            x.setPredecessor(null);
            int potChange = delta - x.getDistance();
            if(potChange > 0)
                x.setPotential(x.getPotential() - potChange);			
        }
        while(!RB.isEmpty()){
            MwbNode x = RB.pop();
            x.setPredecessor(null);
            PQ.remove(x);
            int potChange = delta - x.getDistance();
            if(potChange > 0)
                x.setPotential(x.getPotential() + potChange);
        }
    }
    
    private static void augmentPathTo(MwbNode v){
        MwbEdge e = v.getPredecessor();
        while(e != null){
            e.reverse();//reverseEdge(e);            
            e = e.getTarget().getPredecessor();//not source
        }
    }
    
    private static void relaxAllEdges(MwbNode a1, Stack<MwbNode> RB, PriorityQueue<MwbNode> PQ){
        for(MwbEdge e : a1.getSuccessor()) {
            MwbNode b = e.getTarget();
            int db = a1.getDistance() + (a1.getPotential() + b.getPotential() - e.getWeight());
            if(b.getPredecessor() == null){
                b.setDistance(db);
                b.setPredecessor(e);
                RB.push(b);
                PQ.add(b);
            }else if(db < b.getDistance()){
                //decrease p
                PQ.remove(b);
                b.setDistance(db);
                b.setPredecessor(e);
                PQ.add(b);                
            }
        }
    }
}
