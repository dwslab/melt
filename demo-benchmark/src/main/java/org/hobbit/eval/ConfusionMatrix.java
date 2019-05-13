/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hobbit.eval;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Mapping;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.MappingCell;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sven Hertling
 */
public class ConfusionMatrix {
    private List<Integer> truePositiveList;
    private List<Integer> falsePositiveList;
    private List<Integer> falseNegativeList;
    private int systemSize;
    
    public ConfusionMatrix(){
        this.truePositiveList = new ArrayList<>();
        this.falsePositiveList = new ArrayList<>();
        this.falseNegativeList = new ArrayList<>();
        this.systemSize = 0;
    }
    
    public void addEval(Mapping ref, Mapping system){
        int truePositive = 0;
        int falsePositive = 0;
        int falseNegative = 0;
        
        this.systemSize += system.size();
        
        Map<String, Set<String>> systemSourceTarget = new HashMap<>();
        Map<String, Set<String>> systemTargetSource = new HashMap<>();
        for(MappingCell systemCell : system){
            addToMap(systemSourceTarget, systemCell.getEntityOne(), systemCell.getEntityTwo());
            addToMap(systemTargetSource, systemCell.getEntityTwo(), systemCell.getEntityOne());
        }
        
        for(MappingCell refCell : ref){
            if(refCell.getEntityTwo().equals("null")){
                falsePositive += systemSourceTarget.getOrDefault(refCell.getEntityOne(), new HashSet<>()).size();
            }
            else if(refCell.getEntityOne().equals("null")){
                falsePositive += systemTargetSource.getOrDefault(refCell.getEntityTwo(), new HashSet<>()).size();
            } 
            else{
                Set<String> systemTargets = systemSourceTarget.getOrDefault(refCell.getEntityOne(), new HashSet<>());
                Set<String> systemSources = systemTargetSource.getOrDefault(refCell.getEntityTwo(), new HashSet<>());

                if(systemTargets.contains(refCell.getEntityTwo())){
                    truePositive++;
                    falsePositive+=systemSources.size() - 1;
                    falsePositive+=systemTargets.size() - 1;
                }else{
                    falseNegative++;
                    falsePositive+=systemSources.size();
                    falsePositive+=systemTargets.size();
                }
            }
        }
        this.truePositiveList.add(truePositive);
        this.falsePositiveList.add(falsePositive);
        this.falseNegativeList.add(falseNegative);  
    }
    private static void addToMap(Map<String, Set<String>> map, String one, String two){
        Set<String> s = map.get(one);
        if(s == null){
            map.put(one, new HashSet<>(Arrays.asList(two)));
        }else{
            s.add(two);
        }
    }
    
    
    public double[] getMicroEval(){
        int truePositive = sum(this.truePositiveList);
        int falsePositive = sum(this.falsePositiveList);
        int falseNegative = sum(this.falseNegativeList);
        
        double precision = divideWithTwoDenominators(truePositive, truePositive, falsePositive);
        double recall = divideWithTwoDenominators(truePositive, truePositive, falseNegative);
        
        return new double[]{
            precision,
            recall,
            divideWithTwoDenominators((2.0 * recall * precision), recall, precision)//fmeasure
        };
    }
    
    public double[] getMacroEval(){
        int size = this.truePositiveList.size();
        double precision = 0.0;
        double recall = 0.0;
        for(int i=0; i < size; i++){
            precision += divideWithTwoDenominators(this.truePositiveList.get(i), this.truePositiveList.get(i), this.falsePositiveList.get(i));
            recall += divideWithTwoDenominators(this.truePositiveList.get(i), this.truePositiveList.get(i), this.falseNegativeList.get(i));
        }
        //normalise:
        precision = precision / size;
        recall = recall / size;
        return new double[]{
            precision,
            recall,
            divideWithTwoDenominators((2.0 * recall * precision), recall, precision)//fmeasure
        };
    }
       
    private static double divideWithTwoDenominators(double numerator, double denominatorOne, double denominatorTwo){
        if ((denominatorOne + denominatorTwo) > 0.0) {
            return numerator / (denominatorOne + denominatorTwo);
        }
        else{
            return 0.0;
        }
    }

    private static int sum(List<Integer> list) {
        int sum = 0;
        for (int i: list) {
            sum += i;
        }
        return sum;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TP:");sb.append(truePositiveList.toString());
        sb.append(" FP:");sb.append(falsePositiveList.toString());
        sb.append(" FN:");sb.append(falseNegativeList.toString());
        sb.append(" size:");sb.append(systemSize);
        return sb.toString();
    }

    public int getSystemSize() {
        return systemSize;
    }

    
    
}
