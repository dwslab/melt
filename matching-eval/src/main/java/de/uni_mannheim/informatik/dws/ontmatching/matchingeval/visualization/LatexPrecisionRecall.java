package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.visualization;

import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResult;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.metric.cm.ConfusionMatrix;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatexPrecisionRecall {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LatexPrecisionRecall.class);
    private static final double THRESHOLD = .0001;
    

  
    
    static class PrecRecPoint implements Comparable<PrecRecPoint>{
        
        private double x;
        private double y;
        private String formattedString;
        
        public PrecRecPoint(double x, double y){
            this.x = x * 10;
            this.y = y * 10;
            this.formattedString = String.format(Locale.ENGLISH, "(%.2f, %.2f)", this.x, this.y);
        }
        
        
        public String formatFMeasureLine(){
            return formattedString;
        }
        
        public String extactRepresentation(){
            return "(" + Double.toString(x) + ", " + Double.toString(y) + ")";
        }
        
        public String textPosition(){
            return "(" + Double.toString(x + 0.01) + ", " + Double.toString(y + 0.01) + ")";
        }

        @Override
        public int compareTo(PrecRecPoint o) {
            return new Double(this.x).compareTo(o.x);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 11 * hash + Objects.hashCode(this.formattedString);
            return hash;
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
            final PrecRecPoint other = (PrecRecPoint) obj;
            if (!Objects.equals(this.formattedString, other.formattedString)) {
                return false;
            }
            return true;
        }
    }

    private static PrecRecPoint getPolarCoord( double p, double r ) {
	double pp = ((p * p) - (r * r) + 1) / 2;
	double pr = Math.sqrt(Math.abs((p * p) - (pp * pp)));
        return new PrecRecPoint(pp, pr);
    }
    
      // Provides the Recall given a particular F-measure and Precision
    // F = 2PR/P+R
    // => FP+FR = 2PR
    // => FP = 2PR - FR
    // => FP/R = 2P-F
    // => R = FP/(2P-F)
    // (same for P given the symmetry of the formula)
    private static double getRGivenFAndP( double f, double p ) {
	double r = p * f / (2 * p - f);
	if ( r <= 0 || r >= 1.0 ) return -1.;
	return r;
    }
    
    public static String writeFMeasure(double value, double stepSize){
        if(value > 1.0 || value < 0.0){
            LOGGER.warn("Can not write line for fmeasure because value less than zero or greater than one.");
            return "";
        }
        List<PrecRecPoint> points = new ArrayList<>();        
        for (double recall = 1.0; recall >= 0.499; recall-=stepSize) {
            double precision = getRGivenFAndP(value, recall);
            System.out.println(precision);
            if(precision >= 0.0){
                points.add(getPolarCoord(precision, recall));
            }
        }
        
        for (double precision = 0.5 + stepSize; precision <= 1.01; precision+=stepSize) {
            double recall = getRGivenFAndP(value, precision);
            if(recall >= 0.0){
                points.add(getPolarCoord(precision, recall));
            }
        }        
        if(points.isEmpty())
            return "";
        points = new ArrayList<>(new HashSet<>(points));
        Collections.sort(points);
        
        String formattedPoints = points.stream()
                .map(point -> point.formatFMeasureLine())
                .collect( Collectors.joining( " " ) );
        
        return String.format("\\draw[very thin,dashed] plot[smooth] coordinates { %s };%n\\draw %s node[anchor=south west] {\\tiny{F=%s}};", 
                formattedPoints,
                points.get(0).formatFMeasureLine(),
                muliplied.format(value));
    }
    
    private static DecimalFormat normalized = new DecimalFormat(".####", new DecimalFormatSymbols(Locale.ENGLISH));
    private static DecimalFormat muliplied = new DecimalFormat("###.##", new DecimalFormatSymbols(Locale.ENGLISH));
        
    public static String writeRecall(double value){
        if(value > 1.0 || value < 0.0){
            LOGGER.warn("Can not write line for recall because value less than zero or greater than one.");
            return "";
        }
        String style = Math.abs(1.0 - value) < THRESHOLD ? "dashed" : "dotted,very thin";
        double multipliedValue = value * 10.0;
        double angle = 90 - (3.0 * multipliedValue);
        return String.format("\\draw[%s] (%s,0) arc (0:%s:%scm) node[anchor=south east] {{\\tiny R=%s}};",
                style,
                muliplied.format(multipliedValue),
                muliplied.format(angle),
                muliplied.format(multipliedValue),
                normalized.format(value));
    }
    
    public static String writePrecision(double value){
        if(value > 1.0 || value < 0.0){
            LOGGER.warn("Can not write line for precision because value less than zero or greater than one.");
            return "";
        }
        String style = Math.abs(1.0 - value) < THRESHOLD ? "dashed" : "dotted,very thin";
        double multipliedValue = value * 10.0;
        double angle = 90 + (3.0 * multipliedValue);
        return String.format("\\draw[%s] (%s,0) arc (180:%s:%scm) node[anchor=south west] {{\\tiny P=%s}};",
                style,
                muliplied.format(10.0 - multipliedValue),
                muliplied.format(angle),
                muliplied.format(multipliedValue),
                normalized.format(value));
    }
    
    public static String writeMatcher(String value, double precision, double recall){
        PrecRecPoint coordinates = getPolarCoord(precision, recall);
        return String.format("\\draw plot[mark=+,] coordinates {%s};%n\\draw %s node[anchor=south west] {%s};", 
                coordinates.extactRepresentation(),
                coordinates.textPosition(),
                value);
    }
    
    public static List<Double> range(double start, double end, double step){
        List<Double> list = new ArrayList<>();
        for(double d = start; d <= end; d += step){
            list.add(d);
        }
        return list;
    }
    
    
    public static void write(Map<ExecutionResult, ConfusionMatrix> results, File f){
        try(PrintWriter w = new PrintWriter(f)){
            LatexPrecisionRecall.write(results, w);
        } catch (FileNotFoundException ex) {
            LOGGER.error("File to write latex precision recall plot not found.", ex);
        }
    }
    
    public static void write(Map<ExecutionResult, ConfusionMatrix> results, PrintWriter writer){
        write(results, writer, range(0.1, 1.0, 0.1), range(0.1, 1.0, 0.1), range(0.5, 0.9, 0.1));
    }

    
    public static void write(Map<ExecutionResult, ConfusionMatrix> results, PrintWriter writer, List<Double> precisonLineValues, List<Double> recallLineValues, List<Double> fMeasureLineValues){

        writer.println("\\documentclass[11pt]{book}");
	writer.println();
	writer.println("\\usepackage{pgf}");
	writer.println("\\usepackage{tikz}");
	writer.println();
	writer.println("\\begin{document}");
	writer.println("\\date{today}");
	writer.println("");
	writer.println("\n%% Plot generated by GroupEval of alignapi");
	writer.println("\\begin{tikzpicture}[cap=round]");
	writer.println("% Draw grid");
	writer.println("\\draw[|-|] (-0,0) -- (10,0);");
	writer.println("\\draw[dashed,very thin] (10,0) arc (0:60:10cm);");
	writer.println("\\draw[dashed,very thin] (0,0) arc (180:120:10cm);");
        
	writer.println("%% Level lines for recall");
        for(Double d : recallLineValues){
            writer.println(writeRecall(d));
        }
        writer.println("\\draw (0,-0.3) node {$recall$};");
        
        writer.println("%% Level lines for precision");
        for(Double d : precisonLineValues){
            writer.println(writePrecision(d));
        }
        writer.println("\\draw (10,-0.3) node {$precision$};");
        
        writer.println("%% Level lines for F-measure");
        for(Double d : fMeasureLineValues){
            writer.println(writeFMeasure(d, 0.05));
        }
        
        writer.println("% Plots");
        for(Map.Entry<ExecutionResult, ConfusionMatrix> result : results.entrySet()){
            double precision = result.getValue().getPrecision();
	    double recall = result.getValue().getRecall();
            String matcher = result.getKey().getMatcherName();
            writer.println(writeMatcher(matcher, precision, recall));
        }
        
	writer.println("\\end{tikzpicture}");
	writer.println();
	writer.println("\\end{document}");
    }
    
}
