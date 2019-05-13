package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import java.util.List;
import java.util.Properties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;

public class LevenshteinMatcher extends MatcherYAAAJena {
    
    private double threshold;
    
    public LevenshteinMatcher() {
        this.threshold = 1.0;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties p) throws Exception {
        Alignment alignment = new Alignment();
        matchResources(source.listClasses().toList(), target.listClasses().toList(), alignment);//match only classes
        return alignment;
    }
    
    private void matchResources(List<? extends OntResource> sourceResources, List<? extends OntResource> targetResources, Alignment alignment) {
        //simple cartesian product -> might be very slow
        for(OntResource source : sourceResources){
            String sourceText = getStringRepresentation(source);
            if(sourceText == null)
                continue;
            for(OntResource target : targetResources){
                String targetText = getStringRepresentation(target);
                if(targetText == null)
                    continue;
                double confidence = normalizedLevenshteinDistance(sourceText, targetText);
                if (confidence >= threshold) {
                    alignment.add(source.getURI(), target.getURI(), confidence);
                }
            }
        }
    }
    
    private String getStringRepresentation(OntResource resource) {
        String arbitraryLabel = resource.getLabel(null);
        if(arbitraryLabel != null)
            return arbitraryLabel;
        return resource.getLocalName();
    }
    
    
    private static double normalizedLevenshteinDistance(String a, String b) {
        double distance = (double) levenshteinDistance(a, b);        
        return 1.0d - (distance / Math.max(a.length(), b.length()));
    }
    
    /**
     * Compute levenstein string distance
     * @see https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
     */
    private static int levenshteinDistance (CharSequence lhs, CharSequence rhs) {
        int len0 = lhs.length() + 1;                                                     
        int len1 = rhs.length() + 1;                                                    
        int[] cost = new int[len0];                                                     
        int[] newcost = new int[len0];                              
        for (int i = 0; i < len0; i++) cost[i] = i;
        
        for (int j = 1; j < len1; j++) {                      
            newcost[0] = j;
            for(int i = 1; i < len0; i++) {
                int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;            
                int cost_replace = cost[i - 1] + match;                                 
                int cost_insert  = cost[i] + 1;                                         
                int cost_delete  = newcost[i - 1] + 1;
                newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
            }                                           
            int[] swap = cost; cost = newcost; newcost = swap;                          
        }
        return cost[len0 - 1];                                                          
    }
    
    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
}
