package de.uni_mannheim.informatik.dws.melt.matching_eval.refinement;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This refiner will create the closure of the system and reference alignment.
 * If will compute the closure with the help of the alignment itself and the source and target ontology.
 * An example: source ontology defines A subclass B and target ontology defines X subclass Y and alignment is A - sub - X,
 * then it will also contain A - sub- Y.
 */
public class AlignmentClosureRefiner implements Refiner {    
    private static final Logger LOGGER = LoggerFactory.getLogger(AlignmentClosureRefiner.class);

    
    @Override
    public ExecutionResult refine(ExecutionResult toBeRefined) {
        OntModel sourceOntModel = toBeRefined.getSourceOntology(OntModel.class);
        OntModel targetOntModel = toBeRefined.getTargetOntology(OntModel.class);
        
        Alignment refinedSystem = refineAlignment(sourceOntModel, targetOntModel, toBeRefined.getSystemAlignment());
        Alignment refinedReference = refineAlignment(sourceOntModel, targetOntModel, toBeRefined.getReferenceAlignment());
       
        return new ExecutionResult(toBeRefined, refinedSystem, refinedReference, this);
    }
    
    public static Alignment refineAlignment(Model kgOne, Model kgTwo, Alignment alignment){
        Map<String, Set<String>> subClassOfMap = new HashMap<>();
        
        // copy all correspondences in alignment - they are defintely included also in the inferred alignment
        Alignment inferredAlignment = new Alignment(alignment); 
        for(Correspondence c : alignment){
            switch(c.getRelation()){
                case EQUIVALENCE:{
                    subClassOfMap.computeIfAbsent(c.getEntityOne(), __->new HashSet<>()).add(c.getEntityTwo());
                    subClassOfMap.computeIfAbsent(c.getEntityTwo(), __->new HashSet<>()).add(c.getEntityOne());
                    break;
                }
                case SUBSUME:{
                    subClassOfMap.computeIfAbsent(c.getEntityTwo(), __->new HashSet<>()).add(c.getEntityOne());
                    break;
                }
                case SUBSUMED:{
                    subClassOfMap.computeIfAbsent(c.getEntityOne(), __->new HashSet<>()).add(c.getEntityTwo());
                    break;
                }
            }
            //make sure that entity one is in source and entity two in target
            if(kgOne.getResource(c.getEntityOne()) == null){
                LOGGER.warn("Entity one of correspondence can not be found in source ontology - please check: {}", c.getEntityOne());
            }
            if(kgTwo.getResource(c.getEntityTwo()) == null){
                LOGGER.warn("Entity two of correspondence can not be found in target ontology - please check: {}", c.getEntityTwo());
            }
        }        
        addModelInformation(kgOne, subClassOfMap);
        addModelInformation(kgTwo, subClassOfMap);
        
        for(String startingPoint : subClassOfMap.keySet()){
            boolean startInSource = isContained(kgOne, startingPoint);
            boolean startInTarget = isContained(kgTwo, startingPoint);
            if(startInSource == false && startInTarget == false){
                LOGGER.warn("URI: {} appear not in source and not in target - continue.", startingPoint);
                continue;
            }
            for(String reachable : bfs(subClassOfMap, startingPoint)){
                boolean reachableInSource = isContained(kgOne, reachable);
                boolean reachableInTarget = isContained(kgTwo, reachable);
                if(reachableInSource == false && reachableInTarget == false){
                    LOGGER.warn("URI: {} appear not in source and not in target - continue.", reachable);
                    continue;
                }
                
                if(startInSource && reachableInTarget){
                    inferredAlignment.add(startingPoint, reachable, CorrespondenceRelation.SUBSUMED);
                }
                if(reachableInSource && startInTarget){
                    inferredAlignment.add(reachable, startingPoint, CorrespondenceRelation.SUBSUME);
                }
            }
        }
        
        Alignment additonalEquivalence = new Alignment();
        for(Correspondence c : inferredAlignment){
            if(c.getRelation().equals(CorrespondenceRelation.SUBSUME)){
                if(inferredAlignment.getCorrespondence(c.getEntityOne(), c.getEntityTwo(), CorrespondenceRelation.SUBSUMED) != null){
                    additonalEquivalence.add(c.getEntityOne(), c.getEntityTwo(), CorrespondenceRelation.EQUIVALENCE);
                }                
            }else if(c.getRelation().equals(CorrespondenceRelation.SUBSUMED)){
                if(inferredAlignment.getCorrespondence(c.getEntityOne(), c.getEntityTwo(), CorrespondenceRelation.SUBSUME) != null){
                    additonalEquivalence.add(c.getEntityOne(), c.getEntityTwo(), CorrespondenceRelation.EQUIVALENCE);
                } 
            }
        }
        inferredAlignment.addAll(additonalEquivalence);
        
        return inferredAlignment;
    }
    
    private static boolean isContained(Model model, String uri){
        return model.containsResource(ResourceFactory.createResource(uri));
    }
    
    private static void addModelInformation(Model m, Map<String, Set<String>> subClassOfMap){
        StmtIterator stmts = m.listStatements(null, RDFS.subClassOf, (RDFNode)null);
        while(stmts.hasNext()){
            Statement s = stmts.next();
            if(s.getObject().isResource() == false)
                continue;
            String left = s.getSubject().toString();
            String right = s.getObject().asResource().toString();
            
            subClassOfMap.computeIfAbsent(left, __->new HashSet<>()).add(right);
        }
        
        stmts = m.listStatements(null, OWL.equivalentClass, (RDFNode)null);
        while(stmts.hasNext()){
            Statement s = stmts.next();
            if(s.getObject().isResource() == false)
                continue;
            String left = s.getSubject().toString();
            String right = s.getObject().asResource().toString();
            
            subClassOfMap.computeIfAbsent(left, __->new HashSet<>()).add(right);
            subClassOfMap.computeIfAbsent(right, __->new HashSet<>()).add(left);
        }
    }
    
    private static Set<String> bfs(Map<String, Set<String>> edges, String startPoint){
        //breath first search
        Set<String> visited = new HashSet<>();
        Queue<String> q = new LinkedList<>();
        q.add(startPoint);
        visited.add(startPoint);
        while(!q.isEmpty()){
            String current = q.poll();
            for(String succ : edges.getOrDefault(current, new HashSet<String>())){
                if(visited.contains(succ) == false){
                    visited.add(succ);                    
                    q.add(succ);
                }
            }
        }
        visited.remove(startPoint);
        return visited;
    }

    @Override
    public int hashCode() {
        return 541245;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AlignmentClosureRefiner";
    }    
}
