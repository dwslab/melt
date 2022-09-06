package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.graph;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony.Agony;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

/**
 * This class removes cycles based on the Agony algorithm.
 * @param <T> The type of node
 */
public class CycleRemoval <T extends Comparable<T>> {
    private final Map<T, Set<T>> graph;
    private final Function<Map<T, Set<T>>, List<List<T>>> cycleDetection;

    /**
     * Constructor which accepts the graph and precomputed cycles.
     * Important: Use this only when no edges are added or removed (via addEdge and removeEdge).
     * @param graph the graph
     * @param precomputedCycles the precomputed cycles.
     */
    public CycleRemoval(Map<T, Set<T>> graph, List<List<T>> precomputedCycles){
        this.graph = graph;
        this.cycleDetection = g->precomputedCycles;
    }
    
    /**
     * Constructor which accepts the graph and a function which computes cycles.
     * This can be {@link CycleDetection } or jgrapht algorithms:
     * 
     * @param graph
     * @param cycleDetection 
     */
    public CycleRemoval(Map<T, Set<T>> graph, Function<Map<T, Set<T>>, List<List<T>>> cycleDetection){
        this.graph = graph;
        this.cycleDetection = cycleDetection;
    }
    
    public CycleRemoval(Map<T, Set<T>> graph){
        this(graph, g->new CycleDetection<T>(g).getCycles());
    }
    
    public CycleRemoval(){
        this(new HashMap<>());
    }
    
    public void addEdge(T source, T target){
        Set<T> targets = this.graph.get(source);
        if(targets == null){
            targets = new HashSet<>();
            this.graph.put(source, targets);
        }
        targets.add(target);
    }
    
    public void removeEdge(T source, T target){
        Set<T> targets = this.graph.get(source);
        if(targets == null){
            return;
        }
        targets.remove(target);
    }
    
    
    public Set<Entry<T,T>> getEdgesToBeRemoved(){
        
        List<List<T>> cycles = cycleDetection.apply(graph);
        if(cycles.isEmpty())
            return new HashSet<>();
        
        Agony<T> agony = new Agony<>(graph);
        Map<T, Integer> rankPerNode = agony.computeAgony();

        //simple approach - remove an edge from each cycle in any order of the cycles - just keep track of which edges are removed
        Set<Entry<T, T>> edgesToBeRemoved = new HashSet<>();
        for(List<T> cycle : cycles){
            if(cycle.isEmpty())
                continue;
            if(cycle.size() == 1){
                edgesToBeRemoved.add(new SimpleEntry<>(cycle.get(0), cycle.get(0))); //reflexive edge
                continue;
            }
            findEdgeToBeRemoved(cycle, edgesToBeRemoved, rankPerNode);
        }
        
        //remove the edge with the highest rank diff (meaning the worst edge) over all cycles
        //does it make a difference in comparison to the simple approach?
        
        /*
        //Count how often each edge appears in cycles
        Counter<Entry<T, T>> edgeCounterInCycle = new Counter<>();
        for(List<T> cycle : cycles){
            if(cycle.size() < 2)
                continue;
            for(int i=1; i < cycle.size(); i++){
                edgeCounterInCycle.add(new SimpleEntry<>(cycle.get(i-1), cycle.get(i)));
            }
            edgeCounterInCycle.add(new SimpleEntry<>(cycle.get(cycle.size()-1), cycle.get(0)));
        }
        */
        return edgesToBeRemoved;
    }
    
    private void findEdgeToBeRemoved(List<T> cycle, Set<Entry<T, T>> edgesToBeRemoved, Map<T, Integer> rankPerNode){
        Entry<T, T> currentRemoval = new SimpleEntry<>(cycle.get(cycle.size()-1), cycle.get(0));
        if(edgesToBeRemoved.contains(currentRemoval))
            return;
        int currentDiff = getRankDiff(currentRemoval, rankPerNode);
        for(int i=1; i < cycle.size(); i++){
            Entry<T, T> newRemoval = new SimpleEntry<>(cycle.get(i-1), cycle.get(i));
            if(edgesToBeRemoved.contains(newRemoval)){
                return;
            }
            int newDiff = getRankDiff(newRemoval, rankPerNode);
            if(newDiff > currentDiff){
                currentDiff = newDiff;
                currentRemoval = newRemoval;
            }
        }
        edgesToBeRemoved.add(currentRemoval);        
    }
    
    private int getRankDiff(Entry<T, T> edge, Map<T, Integer> rankPerNode){
        Integer rankSrc = rankPerNode.get(edge.getKey());
        Integer rankTgt = rankPerNode.get(edge.getValue());
        return rankSrc - rankTgt;
    }
    
    
    
    public Map<T, Set<T>> getCycleFreeGraph(){
        Map<T, Set<T>> modifiedGraph = new HashMap<>();
        for(Entry<T, Set<T>> entry: this.graph.entrySet()){
            modifiedGraph.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        
        for(Entry<T,T> edge : getEdgesToBeRemoved()){
            Set<T> succ = modifiedGraph.get(edge.getKey());
            if(succ != null)
                succ.remove(edge.getValue());
        }
        return modifiedGraph;
    }
    
}
