package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Helper class for {@link Agony}.
 */
class AgonyQueue <E>{
    
    private final HashSet<AgonyNode<E>> removed;
    private final LinkedList<AgonyNode<E>> queue;
    private int size;
    
    public AgonyQueue(){
        this.queue = new LinkedList<>();
        this.removed = new HashSet<>();
        this.size = 0;
    }
    
    public AgonyQueue(Collection<AgonyNode<E>> nodes){
        this.queue = new LinkedList<>(nodes);
        this.removed = new HashSet<>();
        this.size = nodes.size();
    }
    
    public void add(AgonyNode<E> node){
        this.queue.add(node);
        size++;
    }
    
    public boolean isEmpty(){
        return size == 0;
    }
    
    public AgonyNode<E> poll(){        
        while(true){
            AgonyNode<E> n = this.queue.poll();
            if(removed.contains(n)){
                removed.remove(n);
                continue;
            }
            this.size--;
            return n;
        }
    }
    
    public AgonyNode<E> peek(){        
        while(true){
            AgonyNode<E> n = this.queue.peek();
            if(removed.contains(n)){
                removed.remove(n);
                this.queue.poll();
                continue;
            }
            return n;
        }
    }
    
    public void remove(AgonyNode<E> node){
        this.removed.add(node);
        this.size--;
    }
}
