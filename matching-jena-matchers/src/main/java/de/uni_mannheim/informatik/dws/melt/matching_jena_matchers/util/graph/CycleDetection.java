package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CycleDetection <T extends Comparable<T>>{
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CycleDetection.class);
    
    private final Map<T, Set<T>> graph;
    
    public CycleDetection(){
        this.graph = new HashMap<>();
    }
    
    public CycleDetection(Map<T, Set<T>> graph){
        this.graph = graph;
    }
    
    public CycleDetection(Collection<T> nodes, Function<T, Set<T>> succesors){
        this.graph = new HashMap<>();
        for(T n : nodes){
            graph.put(n, succesors.apply(n));
        }
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
    
    private Set<T> getAllNodes(){
        Set<T> nodes = new HashSet<>();
        for(Entry<T, Set<T>> entry : graph.entrySet()){
            nodes.add(entry.getKey());
            nodes.addAll(entry.getValue());
        }
        return nodes;
    }
    
    public List<List<T>> getCycles(){
        Set<T> white = new HashSet<>(getAllNodes()); // unvisted
        Set<T> black = new HashSet<>();
        
        Set<List<T>> cycles = new HashSet<>();
        while(!white.isEmpty()){
            T startNode = white.iterator().next();
            Stack<StackNode> stack = new Stack<>();
            SliceableStack callStack = new SliceableStack();
            //LOGGER.info("Start Node: {}", startNode);
            stack.add(new StackNode(false, startNode));
            stack.add(new StackNode(true, startNode));
            
            Set<T> shouldBeMovedToBlack = new HashSet<>();
            while(!stack.isEmpty()){
                StackNode stackNode = stack.pop();
                if(stackNode.isEnteringFunction()){
                    //entering the function
                    T current = stackNode.getNode();
                    shouldBeMovedToBlack.add(current);
                    white.remove(current);
                    callStack.push(current);
                    for(T neighbour : this.graph.getOrDefault(current, new HashSet<>())){
                        if(callStack.contains(neighbour)){
                            //found cycle
                            List<T> cyclePath = callStack.getSlicedList(neighbour);
                            //cyclePath.add(neighbour);
                            cycles.add(normalizePath(cyclePath));
                            //LOGGER.info("\t\tcycle: {}", cyclePath);
                            //LOGGER.info("\t\tcycles: {}", cycles.size());
                        }else if(black.contains(neighbour) == false){
                            stack.add(new StackNode(false, neighbour));
                            stack.add(new StackNode(true, neighbour));  
                            //LOGGER.info("Stack: {}", callStack);
                        }
                    }
                }else{
                    //returning back from function
                    callStack.pop();
                }
            }
            black.addAll(shouldBeMovedToBlack);
            //LOGGER.info("black: {}", black);
        }
        return new ArrayList<>(cycles);
    }
    
    private List<T> normalizePath(List<T> list){
        if(list.isEmpty())
            return new ArrayList<>();
        T minElement = list.get(0);
        int index = 0;
        for(int i = 1; i < list.size(); i++){
            T element = list.get(i);
            if(minElement.compareTo(element) < 0){
                minElement = element;
                index = i;
            }
        }
        
        List<T> normalizedList = new ArrayList<>(list.size());
        for(int i = index; i < list.size(); i++){
            normalizedList.add(list.get(i));
        }
        for(int i = 0; i < index; i++){
            normalizedList.add(list.get(i));
        }
        
        return normalizedList;
    }
    
    public static <T extends Comparable<T>> List<List<T>> normalizePaths(List<List<T>> paths){
        CycleDetection<T> c = new CycleDetection<>();
        List<List<T>> normalized = new ArrayList<>(paths.size());
        for(List<T> path : paths){
            normalized.add(c.normalizePath(path));
        }
        return normalized;
    }
    
    /*
    old which does not work because of graph like  
    A-B; B-C; C-A; B-D; D-A
    either cycle A-B; B-C; C-A; OR A-B; B-D; D-A is found
    thus one cannot move from gray to black when backtracking
    
    public List<List<T>> getCycles(){
        //or this.graph.keySet() ?
        Set<T> white = new HashSet<>(getAllNodes()); // unvisted  
        Set<T> gray = new HashSet<>(); // on the stack
        Set<T> black = new HashSet<>(); //finished
        
        List<List<T>> cycles = new ArrayList<>();
        while(!white.isEmpty()){
            T startNode = white.iterator().next();
            Stack<StackNode> stack = new Stack<>();
            SliceableStack callStack = new SliceableStack();
            //LOGGER.info("Start Node: {}", startNode);
            stack.add(new StackNode(false, startNode));
            stack.add(new StackNode(true, startNode));
            
            while(!stack.isEmpty()){
                StackNode stackNode = stack.pop();
                T current = stackNode.getNode();
                if(black.contains(current)){
                    continue;//already observed
                }
                if(stackNode.isEnteringFunction()){
                    //entering the function
                    //move current from white to gray
                    white.remove(current);
                    gray.add(current);                    
                    callStack.push(current);
                    
                    for(T neighbour : this.graph.getOrDefault(current, new ArrayList<>())){
                        
                        if(white.contains(neighbour)){
                            stack.add(new StackNode(false, neighbour));
                            stack.add(new StackNode(true, neighbour));  
                            //LOGGER.info("Stack: {}", callStack);
                        }else if(gray.contains(neighbour)){
                            //found cycle
                            List<T> cyclePath = callStack.getSlicedList(neighbour);
                            cyclePath.add(neighbour);
                            cycles.add(cyclePath);
                            //LOGGER.info("cycle: {}", cyclePath);
                        }
                    }
                }else{
                    //returning back from function
                    //move current from gray to black
                    gray.remove(current);
                    black.add(current);
                    callStack.pop();
                    //LOGGER.info("black: {}", black);
                }
            }
        }
        return cycles;
    }
    */    
    
   class SliceableStack{
        private final Stack<T> stack;
        private final Map<T, Integer> stackPosition;
        private int currentPosition;
        
        public SliceableStack() {
            this.stack = new Stack<>();
            this.stackPosition = new HashMap<>();
            this.currentPosition = 0;
        }
        
        public void push(T n){
            this.stack.push(n);
            this.stackPosition.put(n, currentPosition);
            currentPosition++;
        }
        
        public void pop(){
            T n = this.stack.pop();
            this.stackPosition.remove(n);
            currentPosition--;
        }
        
        public List<T> getSlicedList(T start){
            /*
            int startIndex = this.stackPosition.getOrDefault(start, 0);            
            List<T> slicedList = new ArrayList<>(currentPosition - startIndex);
            for(int i = startIndex; i < currentPosition; i++){
                slicedList.add(this.stack.get(i));
            }            
            return slicedList;
            */
            return new ArrayList<>(this.stack.subList(this.stackPosition.getOrDefault(start, 0), currentPosition));
        }
        
        public boolean contains(T element){
            //return this.stack.contains(element)
            return this.stackPosition.containsKey(element);
        }

        @Override
        public String toString() {
            return this.stack.toString();
        }
    }
    
    class StackNode {
        private final boolean enteringFunction;
        private final T node;

        public StackNode(boolean enteringFunction, T node) {
            this.enteringFunction = enteringFunction;
            this.node = node;
        }

        public boolean isEnteringFunction() {
            return enteringFunction;
        }

        public T getNode() {
            return node;
        }
    }
}
