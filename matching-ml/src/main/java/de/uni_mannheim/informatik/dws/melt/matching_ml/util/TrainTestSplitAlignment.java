package de.uni_mannheim.informatik.dws.melt.matching_ml.util;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Random;
import java.util.function.Function;

/**
 * A class which can do a train test split for arbitrary data items.
 * It also works with stratification.
 */
public class TrainTestSplitAlignment {

    private Alignment train;
    private Alignment test;    
    
    //with stratification and function
    
    public TrainTestSplitAlignment(Alignment alignment, double train_ratio, Random rnd, Function<Correspondence,?> stratify){
        initializeTrainTest(alignment, new TrainTestSplit<>(alignment, train_ratio, rnd, stratify));
    }
    
    public TrainTestSplitAlignment(Alignment alignment, double train_ratio, long seed, Function<Correspondence,?> stratify){
        this(alignment, train_ratio, new Random(seed), stratify);
    }
    
    public TrainTestSplitAlignment(Alignment alignment, double train_ratio, Function<Correspondence,?> stratify){
        this(alignment, train_ratio, new Random(), stratify);
    }
    
    //without stratification
    
    public TrainTestSplitAlignment(Alignment alignment, double train_ratio, Random rnd){
        initializeTrainTest(alignment, new TrainTestSplit<>(alignment, train_ratio, rnd));
    }
    
    public TrainTestSplitAlignment(Alignment alignment, double train_ratio, long seed){
        this(alignment, train_ratio, new Random(seed));
    }
    
    public TrainTestSplitAlignment(Alignment alignment, double train_ratio){
        this(alignment, train_ratio, new Random());
    }
    
    //helper method
    
    private void initializeTrainTest(Alignment alignment, TrainTestSplit<Correspondence> split){
        this.train = new Alignment(alignment, false);
        this.train.addAll(split.getTrain());      
        
        this.test = new Alignment(alignment, false);
        this.test.addAll(split.getTest());
    }
    
    //getter

    /**
     * Returns the training alignment.
     * This will return each time a new copy of the alignment such that changes to this object will not be reflected in multiple calls to this method.
     * @return training alignment
     */
    public Alignment getTrain() {
        return new Alignment(train) ;
    }

    /**
     * Returns the test alignment.
     * This will return each time a new copy of the alignment such that changes to this object will not be reflected in multiple calls to this method.
     * @return test alignment
     */
    public Alignment getTest() {
        return new Alignment(test);
    }
}
