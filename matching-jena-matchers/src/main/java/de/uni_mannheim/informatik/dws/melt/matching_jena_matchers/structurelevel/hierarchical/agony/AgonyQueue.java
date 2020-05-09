package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Helper class for {@link Agony}.
 */
class AgonyQueue {
    
    private HashSet<AgonyNode> removed;
    private LinkedList<AgonyNode> queue;
    private int size;
    
    public AgonyQueue(){
        this.queue = new LinkedList<>();
        this.removed = new HashSet<>();
        this.size = 0;
    }
    
    public AgonyQueue(Collection<AgonyNode> nodes){
        this.queue = new LinkedList<>(nodes);
        this.removed = new HashSet<>();
        this.size = nodes.size();
    }
    
    public void add(AgonyNode node){
        this.queue.add(node);
        size++;
    }
    
    public boolean isEmpty(){
        return size == 0;
    }
    
    public AgonyNode poll(){        
        while(true){
            AgonyNode n = this.queue.poll();
            if(removed.contains(n)){
                removed.remove(n);
                continue;
            }
            this.size--;
            return n;
        }
    }
    
    public AgonyNode peek(){        
        while(true){
            AgonyNode n = this.queue.peek();
            if(removed.contains(n)){
                removed.remove(n);
                this.queue.poll();
                continue;
            }
            return n;
        }
    }
    
    public void remove(AgonyNode node){
        this.removed.add(node);
        this.size--;
    }
}
