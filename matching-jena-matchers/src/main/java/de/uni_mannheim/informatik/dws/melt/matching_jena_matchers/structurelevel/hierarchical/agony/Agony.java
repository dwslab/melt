package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for computing hierarchy ranks in directed graphs (with cycles).
 * This is an implementation of the paper:<br>
 * <i>Faster way to agony - Discovering hierarchies in directed graphs by Nikolaj Tatti</i><br>
 * which is an improved version of the paper:<br>
 * <i>Hierarchies in directed networks by Nikolaj Tatti</i><br>
 * Code is available at <a href="https://users.ics.aalto.fi/ntatti/software.shtml">https://users.ics.aalto.fi/ntatti/software.shtml</a>.
 * This implementation has a similar runtime performance than the C++ code and is one to one translated.
 * An evaluation can be found at <a href="https://github.com/zhenv5/breaking_cycles_in_noisy_hierarchies">https://github.com/zhenv5/breaking_cycles_in_noisy_hierarchies</a>.
 */
public class Agony<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Agony.class);
    
    private List<AgonyNode> nodes;
    private List<AgonyEdge> edges;
    
    private AgonyGraph graph;
    private AgonyGraph dag;
    private AgonyGraph euler;
    
    private int dual;
    private int primal;
    
    private List<Queue<AgonyEdge>> slacks;
    private int curslack;
    
    
    public Agony(Map<E, Set<E>> adjacencyList){
        //read function
        this.graph = new AgonyGraph();
        Map<E, Integer> labelmap = new HashMap<>();
        int nodeId = 0;
        int edgeId = 0;
        for(Entry<E, Set<E>> entry: adjacencyList.entrySet()){
            if(labelmap.containsKey(entry.getKey()) == false){
               labelmap.put(entry.getKey(), nodeId++);
            }
            for(E element : entry.getValue()){
                if(labelmap.containsKey(element) == false){
                    labelmap.put(element, nodeId++);
                }
                edgeId++;
            }
        }
        this.nodes = new ArrayList<>(nodeId);
        for(int i = 0; i < nodeId; i++)
            this.nodes.add(new AgonyNode(i));
        for(Entry<E, Integer> entry : labelmap.entrySet()){
            this.nodes.get(entry.getValue()).setLabel(entry.getKey());
        }
        
        this.edges = new ArrayList<>(edgeId);
        for(int i = 0; i < edgeId; i++)
            this.edges.add(new AgonyEdge(i));
        
        
        this.graph.reset(nodeId, edgeId);
        edgeId = 0;
        for(Entry<E, Set<E>> entry: adjacencyList.entrySet()){
            Integer source = labelmap.get(entry.getKey());
            for(E element : entry.getValue()){
                this.graph.bind(edgeId, source, labelmap.get(element));
                edgeId++;
            }
        }
    }
    
    public Agony(List<Entry<E,E>> edges){
        //read function
        this.graph = new AgonyGraph();
        Map<E, Integer> labelmap = new HashMap<>();
        int nodeId = 0;
        int edgeId = 0;
        for(Entry<E, E> entry : edges){
            if(labelmap.containsKey(entry.getKey()) == false){
               labelmap.put(entry.getKey(), nodeId++);
            }
            if(labelmap.containsKey(entry.getValue()) == false){
               labelmap.put(entry.getValue(), nodeId++);
            }
            edgeId++;
        }
        this.nodes = new ArrayList<>(nodeId);
        for(int i = 0; i < nodeId; i++)
            this.nodes.add(new AgonyNode(i));
        for(Entry<E, Integer> entry : labelmap.entrySet()){
            this.nodes.get(entry.getValue()).setLabel(entry.getKey());
        }
        
        this.edges = new ArrayList<>(edgeId);
        for(int i = 0; i < edgeId; i++)
            this.edges.add(new AgonyEdge(i));
        
        
        this.graph.reset(nodeId, edgeId);
        edgeId = 0;
        for(Entry<E, E> entry: edges){
            this.graph.bind(edgeId, labelmap.get(entry.getKey()), labelmap.get(entry.getValue()));
            edgeId++;
        }
    }
    
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[ \t]");
    public static Map<String, Set<String>> readAdjacenyList(File file){
        Map<String, Set<String>> adjacencyList = new HashMap<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line = reader.readLine();
            int lineNumber = 1;
            while(line != null){
                String[] parts = SPLIT_PATTERN.split(line);
                if(parts.length < 2){
                    LOGGER.warn("Found line {} which splitted by whitespace has no engough parts", lineNumber);
                    line = reader.readLine();
                    lineNumber++;
                    continue;
                }
                adjacencyList.computeIfAbsent(parts[0], l -> new HashSet()).add(parts[1]);                
                line = reader.readLine();
                lineNumber++;
            }
        } catch(IOException e) {
            LOGGER.warn("Could not read file", e);
        }
        return adjacencyList;
    }
    
    public static List<Entry<String, String>> readEdges(File file){
        List<Entry<String, String>> edges = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(file))){
            String line = reader.readLine();
            int lineNumber = 1;
            while(line != null){
                String[] parts = SPLIT_PATTERN.split(line);
                if(parts.length < 2){
                    LOGGER.warn("Found line {} which splitted by whitespace has no engough parts", lineNumber);
                    line = reader.readLine();
                    lineNumber++;
                    continue;
                }
                edges.add(new SimpleEntry<String, String>(parts[0], parts[1]));   
                line = reader.readLine();
                lineNumber++;
            }
        } catch(IOException e) {
            LOGGER.warn("Could not read file", e);
        }
        return edges;
    }
    
    
    public Map<E, Integer> computeAgony(){
        this.cycledfs();
        this.initagony();
        this.initrank();
        LOGGER.info("computeAgony: Primal: {} Dual: {}", primal, dual);
        this.minagony();
        LOGGER.info("computeAgony finished: Dual: {}", dual);
        
        return writeagony();
    }
    
    private Map<E, Integer> writeagony(){
        SortedMap<Integer, Integer> ranks = new TreeMap<>();
        for(AgonyNode<E> n : this.nodes){
            ranks.put(n.getRank(), 0);
        }
        int r = 0;
        for(Entry<Integer, Integer> entry: ranks.entrySet()){
            entry.setValue(r++);
        }
        
        Map<E, Integer> map = new HashMap<>();
        for(AgonyNode<E> n : this.nodes){
            map.put(n.getLabel(), ranks.get(n.getRank()));
        }
        return map;
    }
    
    
    
    //public int cost(){} //not called    
    
    private void cycledfs(){
        AgonyGraph dfs = new AgonyGraph(this.graph);
        
        AgonyQueue active = new AgonyQueue(this.nodes);  
        //Queue<AgonyNode> active = new LinkedList<>(this.nodes);
        
        while(!active.isEmpty()){
            AgonyNode seed = active.peek();
            AgonyNode u = seed;
            //System.out.println("cycledfs: " + seed.getLabel().toString());
            u.setParent(null);
            while(u != null){
                //System.out.println("cycledfs u: " + u.getLabel().toString());
                AgonyGraphNode n = dfs.getNode(u.getId());
                if(n.getOut().isEmpty()){
                    active.remove(u);
                    dfs.unbind(n);
                    u = u.getParent();                    
                }else{
                    AgonyGraphEdge e = n.getOut().peek();
                    AgonyGraphNode m = e.getChild();
                    AgonyNode v = getNode(m.getId());
                    
                    if(v.getParent() == null && v != seed){
                        v.setParent(u);
                        v.setParentEdge(e.getId());
                        u = v;
                    }else{
                        for(AgonyNode w = u; w != v; w = w.getParent()){
                            getEdge(w.getParentEdge()).setEulerian(true);
                            dfs.unbind(dfs.getEdge(w.getParentEdge()));
                        }
                        
                        getEdge(e.getId()).setEulerian(true);
                        dfs.unbind(e);
                        
                        AgonyNode wnext = null;
                        for(AgonyNode w = u; w != v; w = wnext){
                            wnext = w.getParent();
                            w.setParent(null);
                        }
                        
                        u = v;
                    }
                }
            }
        }
    }
    
    private void initagony(){
        this.dag = new AgonyGraph(graph);
        this.euler = new AgonyGraph(graph);        
        
        for(int i = 0; i< edges.size(); i++){
            if(getEdge(i).isEulerian()){
                dag.unbind(dag.getEdge(i));
                dual++;
            }else{
                euler.unbind(euler.getEdge(i));
            }
        }
        primal = dual;
    }
    
    private void initrank(){
        Stack<AgonyNode> sources = new Stack<>();
        
        for(int i = 0; i < size(); i++){
            AgonyNode n = getNode(i);
            n.setCount(dag.getNode(i).getInd());
            if(n.getCount() == 0){
                n.setNewrank(0);
                n.setRank(0);
                sources.push(n);
            }
        }
        while(!sources.isEmpty()){
            AgonyNode n = sources.pop();
            AgonyGraphNode u = dag.getNode(n.getId());

            for(AgonyGraphEdge e : u.getOut()){
                AgonyNode m = getNode(e.getChild().getId());
                m.decreaseCount();
                int max = Math.max(m.getRank(), n.getRank() + 1);
                m.setRank(max);
                m.setNewrank(max);
                if(m.getCount() == 0){
                    sources.push(m);
                }
            }
        }
        
        this.slacks = new ArrayList<>(size());
        for(int i = 0; i < size(); i++){
            slacks.add(new LinkedList<>());
        }
        
        this.curslack = -1;        
        for(int i = 0; i < this.edges.size(); i++){
            if(getEdge(i).isEulerian())
                addslack(i);
            curslack = Math.max(slack(i), curslack);
        }
    }
    
    private void minagony(){
        for(int i=0; i < size(); i++){
            AgonyGraphNode n = dag.getNode(i);
            for(AgonyGraphEdge e : n.getOut()){
                assert from(e.getId()).getRank() < to(e.getId()).getRank();
            }
        }
        while(true){
            while(curslack >=0 && slacks.get(curslack).isEmpty()){
                curslack--;
            }
            if(curslack < 0)
                break;
            
            AgonyEdge e = slacks.get(curslack).peek();
            relief(e.getId());
            LOGGER.debug("Primal: {} Dual: {}", primal, dual);
        }
    }
    
    private void relief(int edge){
        AgonyGraphEdge e = euler.getEdge(edge);
        AgonyNode p = getNode(e.getParent().getId());
        AgonyNode s = getNode(e.getChild().getId());
        
        p.setParent(null);
        p.setDiff(slack(p, s));
        assert p.getDiff() > 0;
        
        //add and init queue
        List<AgonyQueue> q = new ArrayList<>(p.getDiff());//List<Queue<AgonyNode>> q = new ArrayList<>(p.getDiff());
        for(int i=0; i < p.getDiff(); i++){
            q.add(new AgonyQueue());//q.add(new LinkedList<>());
        }
        q.get(p.getDiff()-1).add(p);
        int curstack = p.getDiff() - 1;
        
        List<AgonyNode> nl = new LinkedList<>();
        List<AgonyNode> visited = new LinkedList<>();
        nl.add(p);
        
        int bound = 0;
        
        while(true){
            
            while(curstack >= 0 && q.get(curstack).isEmpty()){
                curstack--;
            }
            if(curstack < bound){
                break;
            }
            AgonyNode u = q.get(curstack).poll();
            u.setNewrank(u.getRank() + u.getDiff());
            visited.add(u);
            u.setDiff(0);// diff = 0 means that u is no longer in the stack

            if(u == s){
                break;
            }
            
            AgonyGraphNode n = dag.getNode(u.getId());            
            for(AgonyGraphEdge ee : n.getOut()){
                AgonyNode v = getNode(ee.getChild().getId());
                assert u.getRank() < v.getRank();
                if(v.getNewrank() <= u.getNewrank()){
                    int t = u.getNewrank() + 1 - v.getNewrank();
                    assert t-1 <= curstack;
                    if(v == s){
                        bound = Math.max(bound, t);
                    }
                    if(t > v.getDiff()){
                        if(v.getDiff() > 0){
                            q.get(v.getDiff() - 1).remove(v);
                        }else{
                            nl.add(v);
                        }
                        v.setDiff(t);
                        //add v to queue
                        q.get(v.getDiff() - 1).add(v);
                        v.setParent(u);
                        v.setParentEdge(ee.getId());
                    }
                }
            }
            
            n = euler.getNode(u.getId());
            for(AgonyGraphEdge ee : n.getIn()){
                AgonyNode v = getNode(ee.getParent().getId());
                if(newslack(v, u) > slack(v, u)){
                    int t = newslack(v, u) - slack(v, u);
                    assert t-1 <= curstack;
                    if(v == s){
                        bound = Math.max(bound, t);
                    }
                    if(t > v.getDiff()){
                        if(v.getDiff() > 0){
                            q.get(v.getDiff() - 1).remove(v);
                        }else{
                            nl.add(v);
                        }
                        v.setDiff(t);
                        //add v to queue
                        q.get(v.getDiff() - 1).add(v);
                        v.setParent(u);
                        v.setParentEdge(ee.getId());
                    }
                }
            }
        }
        
        if(curstack >=0){
            shiftrank(visited, curstack + 1);
        }
        //System.out.println("===");
        //for(AgonyNode n : nl){
        //    System.out.println(n.getLabel().toString() + " " + n.getRank() + " " + n.getNewrank() + " " + n.getDiff());
        //}
        updaterelief(nl);
        if(slack(p,s) != 0){
            extractcycle(edge);
        }
    }
    
    private void updaterelief(List<AgonyNode> nl){
        for(AgonyNode n : nl){
            n.setRank(n.getNewrank());
            n.setDiff(0);
        }
        //following is a bit time consuming...
        for(AgonyNode u : nl){
            AgonyGraphNode n = euler.getNode(u.getId());
            for(AgonyGraphEdge e : n.getOut()){
                AgonyNode v = to(e.getId());
                AgonyEdge f = getEdge(e.getId());
                if(slack(u,v) != f.getSlack()){
                    deleteslack(e.getId());
                    addslack(e.getId());
                }
            }
        }
    }
        
    //public void resetrelief(List<AgonyNode> nl){} //not called
    
    private void shiftrank(List<AgonyNode> nl, int shift){
        for(AgonyNode n : nl){
            n.reduceNewrank(shift);
        }
    }
    
    private void extractcycle(int eid){
        AgonyGraphEdge e = euler.getEdge(eid);
        AgonyNode p = getNode(e.getParent().getId());
        AgonyNode s = getNode(e.getChild().getId());
        for(AgonyNode u = s; u != p; u = u.getParent()){
            AgonyEdge f = getEdge(u.getParentEdge());
            if(f.isEulerian()){
                f.setEulerian(false);
                euler.unbind(euler.getEdge(u.getParentEdge()));
                assert u.getRank() < u.getParent().getRank();
                dag.bind(u.getParentEdge(), u.getId(), u.getParent().getId());
                deleteslack(u.getParentEdge());
                dual--;
                primal--;
            }else{
                f.setEulerian(true);                
                dag.unbind(dag.getEdge(u.getParentEdge()));
                euler.bind(u.getParentEdge(), u.getParent().getId(), u.getId());
                addslack(u.getParentEdge());
                dual++;
                primal++;                
            }
        }
        AgonyEdge g = getEdge(eid);
        g.setEulerian(false);
        euler.unbind(e);
        dag.bind(eid, p.getId(), s.getId());
        dual--;
        primal--;
        deleteslack(eid);
    }
    
    private void deleteslack(int eid){
        int t = getEdge(eid).getSlack();
        if(t > 0){
            this.slacks.get(t-1).remove(getEdge(eid));
        }
        this.primal -= t;
    }
    
    private void addslack(int eid){
        int t = slack(eid);
        AgonyEdge e = getEdge(eid);
        e.setSlack(t);
        if(t > 0){
            this.slacks.get(t-1).add(getEdge(eid));
        }
        this.primal += t;
    }
    
    
    //from header
    
    private int size(){
        return this.nodes.size();
    }
    private AgonyNode getNode(int i){
        return this.nodes.get(i);
    }
    private AgonyEdge getEdge(int i){
        return this.edges.get(i);
    }
    
    
    private int slack(AgonyNode v, AgonyNode u){
        if(u.getRank() > v.getRank() + 1){
            return u.getRank() - v.getRank() - 1;
        }
        return 0;
    }
    private int newslack(AgonyNode v, AgonyNode u){
        if(u.getNewrank() > v.getNewrank() + 1){
            return u.getNewrank() - v.getNewrank() - 1;
        }
        return 0;
    }
    private int slack(int eid){
        return slack(from(eid), to(eid));
    }
    
    private AgonyNode from(int eid){
        return getNode(graph.getEdge(eid).getParent().getId());
    }
    private AgonyNode to(int eid){
        return getNode(graph.getEdge(eid).getChild().getId());
    }
}

