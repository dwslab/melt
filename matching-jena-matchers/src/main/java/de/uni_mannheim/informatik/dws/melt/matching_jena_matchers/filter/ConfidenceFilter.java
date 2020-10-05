package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;

import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntModel;

/**
 * This filter returns only alignments with confidence greater or equals than a
 * specific threshold. Default is 0.9.
 *
 * Thresholds can be set per type.
 */
public class ConfidenceFilter extends MatcherYAAAJena implements Filter {

    private double thresholdClass;
    private double thresholdObjectProperty;
    private double thresholdDatatypeProperty;
    private double thresholdIndividual;
    private double thresholdMixed;

    public ConfidenceFilter() {
        setThreshold(0.9);
    }

    public ConfidenceFilter(double threshold) {
        setThreshold(threshold);
    }

    public ConfidenceFilter(double thresholdClass, double thresholdObjectProperty, double thresholdDatatypeProperty, double thresholdIndividual, double thresholdMixed) {
        this.thresholdClass = thresholdClass;
        this.thresholdObjectProperty = thresholdObjectProperty;
        this.thresholdDatatypeProperty = thresholdDatatypeProperty;
        this.thresholdIndividual = thresholdIndividual;
        this.thresholdMixed = thresholdMixed;
    }


    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment, source, target);
    }
    

    public Alignment filter(Alignment inputAlignment, OntModel source, OntModel target) {
        if (areAllEqual(this.thresholdClass, this.thresholdObjectProperty, this.thresholdDatatypeProperty, this.thresholdIndividual, this.thresholdMixed)) {
            return inputAlignment.cut(this.thresholdClass);
        }

        Alignment result = new Alignment(inputAlignment, false);
        for (Correspondence c : inputAlignment) {
            if(c.getConfidence() >= getThreshold(c, source, target)){
                result.add(c);
            }
        }
        return result;
    }

    private double getThreshold(Correspondence c, OntModel ont1, OntModel ont2) {        
        if (ont1.getOntClass(c.getEntityOne()) != null && ont2.getOntClass(c.getEntityTwo()) != null) {
            return this.thresholdClass;
        } else if (ont1.getObjectProperty(c.getEntityOne()) != null && ont2.getObjectProperty(c.getEntityTwo()) != null) {
            return this.thresholdObjectProperty;
        } else if (ont1.getDatatypeProperty(c.getEntityOne()) != null && ont2.getDatatypeProperty(c.getEntityTwo()) != null) {
            return this.thresholdDatatypeProperty;
        } else if (ont1.getIndividual(c.getEntityOne()) != null && ont2.getIndividual(c.getEntityTwo()) != null) {
            return this.thresholdIndividual;
        } else {
            return this.thresholdMixed;
        }
    }

    public final void setThreshold(double confidence) {
        this.thresholdClass = confidence;
        this.thresholdObjectProperty = confidence;
        this.thresholdDatatypeProperty = confidence;
        this.thresholdIndividual = confidence;
        this.thresholdMixed = confidence;
    }

    //Setter Getter for specific thresholds:

    public double getThresholdClass() {
        return thresholdClass;
    }

    public void setThresholdClass(double thresholdClass) {
        this.thresholdClass = thresholdClass;
    }

    public double getThresholdObjectProperty() {
        return thresholdObjectProperty;
    }

    public void setThresholdObjectProperty(double thresholdObjectProperty) {
        this.thresholdObjectProperty = thresholdObjectProperty;
    }

    public double getThresholdDatatypeProperty() {
        return thresholdDatatypeProperty;
    }

    public void setThresholdDatatypeProperty(double thresholdDatatypeProperty) {
        this.thresholdDatatypeProperty = thresholdDatatypeProperty;
    }

    public double getThresholdIndividual() {
        return thresholdIndividual;
    }

    public void setThresholdIndividual(double thresholdIndividual) {
        this.thresholdIndividual = thresholdIndividual;
    }

    public double getThresholdMixed() {
        return thresholdMixed;
    }

    public void setThresholdMixed(double thresholdMixed) {
        this.thresholdMixed = thresholdMixed;
    }
    

    //helper:
    private static boolean areAllEqual(double checkValue, double... otherValues) {
        for (double value : otherValues) {
            if (value != checkValue) {
                return false;
            }
        }
        return true;
    }
}
