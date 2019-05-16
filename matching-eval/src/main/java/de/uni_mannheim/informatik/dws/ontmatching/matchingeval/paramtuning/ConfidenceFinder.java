package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.paramtuning;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;


public class ConfidenceFinder {
    
    
    public static Set<Double> getSteps(double start, double end, double stepwidth){
        Set<Double> set = new HashSet<>();
        for(double d = start; d <= end; d += stepwidth){
            set.add(d);
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            set.add(c);
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a, double begin, double end){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            if(c >= begin && c <= end){
                set.add(c);
            }
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a, int decimalPrecision){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            BigDecimal bd = new BigDecimal(c);
            bd = bd.setScale(decimalPrecision, RoundingMode.HALF_UP);
            set.add(bd.doubleValue());
        }
        return set;
    }
    
    public static Set<Double> getOccurringConfidences(Alignment a, int decimalPrecision, double begin, double end){
        Set<Double> set = new HashSet<>();
        for(Double c : a.getDistinctConfidences()){
            BigDecimal bd = new BigDecimal(c);
            bd = bd.setScale(decimalPrecision, RoundingMode.HALF_UP);
            double d = bd.doubleValue();
            if(d >= begin && d <= end){
                set.add(d);
            }
        }
        return set;
    }
    
    
}
