package de.uni_mannheim.informatik.dws.melt.matching_ml.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * A class which can do a train test split for arbitrary data items.
 * It also works with stratification.
 */
public class TrainTestSplit <T> {

    private List<T> train;
    private List<T> test;
    
    //with stratification and two lists
    
    public TrainTestSplit(List<T> items, double train_ratio, Random rnd, List<?> stratify){
        if(items.size() != stratify.size()){
            throw new IllegalArgumentException("The size of stratify collections is not the same as items collection");
        }
        Map<Object, List<T>> groups = new HashMap<>();
        for(int i=0; i<items.size(); i++){
            groups.computeIfAbsent(stratify.get(i), __-> new ArrayList<>()).add(items.get(i));
        }
        initializeTrainTestStratified(groups, train_ratio, rnd);
    }
    
    public TrainTestSplit(List<T> items, double train_ratio, long seed, List<?> stratify){
        this(items, train_ratio, new Random(seed), stratify);
    }
    
    public TrainTestSplit(List<T> items, double train_ratio, List<?> stratify){
        this(items, train_ratio, new Random(), stratify);
    }
    
    //with stratification and function
    
    public TrainTestSplit(Collection<T> items, double train_ratio, Random rnd, Function<T,?> stratify){
        Map<Object, List<T>> groups = new HashMap<>();
        for(T item : items){
            Object clazz = stratify.apply(item);
            groups.computeIfAbsent(clazz, __-> new ArrayList<>()).add(item);
        }
        initializeTrainTestStratified(groups, train_ratio, rnd);
    }
    
    public TrainTestSplit(Collection<T> items, double train_ratio, long seed, Function<T,?> stratify){
        this(items, train_ratio, new Random(seed), stratify);
    }
    
    public TrainTestSplit(Collection<T> items, double train_ratio, Function<T,?> stratify){
        this(items, train_ratio, new Random(), stratify);
    }
    
    private void initializeTrainTestStratified(Map<Object, List<T>> groups, double train_ratio, Random rnd){
        if(train_ratio <= 0.0 || train_ratio >= 1.0 || Double.isNaN(train_ratio)) {
            throw new IllegalArgumentException("The variable train_ratio must be in range ]0.0,1.0[");
        }
        
        this.train = new ArrayList<>();
        this.test = new ArrayList<>();
        
        List<List<T>> sortedGroups = new ArrayList<>(groups.values());
        sortedGroups.sort((o1, o2) -> {
            return Integer.compare(o1.size(), o2.size());
        });
        for(List<T> group: sortedGroups){
            if(group.size() <= 1){
                throw new IllegalArgumentException("One class has to few examples (less ot eual one) to be splitted into train and test.");
            }
            //shuffle before to get different examples from one group in train and test
            Collections.shuffle(group, rnd);
            int splitIndex=(int) Math.round(train_ratio*group.size());    
            this.train.addAll(group.subList(0, splitIndex));
            this.test.addAll(group.subList(splitIndex, group.size()));
        }
        if(this.train.isEmpty()){
            throw new IllegalArgumentException("The train set is empty (change train_ratio which was: " + train_ratio);
        }
        if(this.test.isEmpty()){
            throw new IllegalArgumentException("The test set is empty (change train_ratio which was: " + train_ratio);
        }
        
        //shuffle train and test again to mix up the groups
        Collections.shuffle(this.train, rnd);
        Collections.shuffle(this.test, rnd);
    }
    
    
    //without stratification
    
    public TrainTestSplit(Collection<T> items, double train_ratio, Random rnd){
        if(train_ratio <= 0.0 || train_ratio >= 1.0 || Double.isNaN(train_ratio)) {
            throw new IllegalArgumentException("The variable train_ratio must be in range ]0.0,1.0[");
        }        
        if(items.size() < 2){
            throw new IllegalArgumentException("Cannot split the data into train and test because less than 2 examples were provided.");
        }
        List<T> shuffeledList = new ArrayList<>(items);
        Collections.shuffle(shuffeledList, rnd);
        int splitIndex=(int) Math.round(train_ratio*shuffeledList.size());
        this.train = shuffeledList.subList(0, splitIndex);
        if(this.train.isEmpty()){
            throw new IllegalArgumentException("The train set is empty (number of examples: " + shuffeledList.size() + " train_ratio: " + train_ratio);
        }
        this.test = shuffeledList.subList(splitIndex, shuffeledList.size());
        if(this.test.isEmpty()){
            throw new IllegalArgumentException("The test set is empty (number of examples: " + shuffeledList.size() + " train_ratio: " + train_ratio);
        }
    }
    
    public TrainTestSplit(Collection<T> items, double train_ratio, long seed){
        this(items, train_ratio, new Random(seed));
    }
    
    public TrainTestSplit(Collection<T> items, double train_ratio){
        this(items, train_ratio, new Random());
    }
    
    //getter

    /**
     * Returns a list of objects in the train set
     * @return the train list
     */
    public List<T> getTrain() {
        return train;
    }

    /**
     * Returns a list of objects in the test set
     * @return the test list
     */
    public List<T> getTest() {
        return test;
    }
}
