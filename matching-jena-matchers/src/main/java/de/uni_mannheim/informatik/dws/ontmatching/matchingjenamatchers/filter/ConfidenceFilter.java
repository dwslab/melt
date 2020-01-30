package de.uni_mannheim.informatik.dws.ontmatching.matchingjenamatchers.filter;

import de.uni_mannheim.informatik.dws.ontmatching.matchingjena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.ontmatching.matchingyaaa.MatcherYAAA;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Alignment;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.CorrespondenceConfidenceComparator;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import org.apache.jena.ontology.OntModel;

/**
 * This filter returns only aligments with confidence greater or equals than a
 * specific threshold. Default is 0.9.
 */
public class ConfidenceFilter extends MatcherYAAAJena {

    private double confidenceThresholdClasses;
    private double confidenceThresholdObjects;
    private double confidenceThresholdDatatypes;
    private double confidenceThresholdMixed;

    public ConfidenceFilter() {
        setThreshold(0.9);
    }

    public ConfidenceFilter(double threshold) {
        setThreshold(threshold);
    }

    public ConfidenceFilter(double confidenceThresholdClasses, double confidenceThresholdObjects, double confidenceThresholdDatatypes, double confidenceThresholdMixed) {
        this.confidenceThresholdClasses = confidenceThresholdClasses;
        this.confidenceThresholdObjects = confidenceThresholdObjects;
        this.confidenceThresholdDatatypes = confidenceThresholdDatatypes;
        this.confidenceThresholdMixed = confidenceThresholdMixed;
    }

    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        return filter(inputAlignment, source, target);
    }
    

    public Alignment filter(Alignment inputAlignment, OntModel source, OntModel target) {
        if (areAllEqual(this.confidenceThresholdClasses, this.confidenceThresholdObjects, this.confidenceThresholdDatatypes, this.confidenceThresholdMixed)) {
            return inputAlignment.cut(this.confidenceThresholdClasses);
        }

        Alignment result = new Alignment();
        for (Correspondence c : inputAlignment) {
            if(c.getConfidence() >= getThreshold(c, source, target)){
                result.add(c);
            }
        }
        return result;
    }

    private double getThreshold(Correspondence c, OntModel ont1, OntModel ont2) {
        boolean obj1isDataType = ont1.getDatatypeProperty(c.getEntityOne()) != null;
        boolean obj2isDataType = ont2.getDatatypeProperty(c.getEntityTwo()) != null;
        boolean obj1isClass = ont1.getOntClass(c.getEntityOne()) != null;
        boolean obj2isClass = ont2.getOntClass(c.getEntityTwo()) != null;
        boolean obj1isObject = ont1.getObjectProperty(c.getEntityOne()) != null;
        boolean obj2isObject = ont2.getObjectProperty(c.getEntityTwo()) != null;

        if (obj1isDataType == true && obj2isDataType == true) {
            return this.confidenceThresholdDatatypes;
        } else if (obj1isClass == true && obj2isClass == true) {
            return this.confidenceThresholdClasses;
        } else if (obj1isObject == true && obj2isObject == true) {
            return this.confidenceThresholdObjects;
        } else {
            return this.confidenceThresholdMixed;
        }
    }

    public final void setThreshold(double confidence) {
        this.confidenceThresholdClasses = confidence;
        this.confidenceThresholdObjects = confidence;
        this.confidenceThresholdDatatypes = confidence;
        this.confidenceThresholdMixed = confidence;
    }

    //Setter Getter for specific thresholds:
    public void setThresholdClasses(double confidence) {
        this.confidenceThresholdClasses = confidence;
    }

    public double getThresholdClasses() {
        return this.confidenceThresholdClasses;
    }

    public double getConfidenceThresholdObjects() {
        return this.confidenceThresholdObjects;
    }

    public void setThresholdObjects(double confidenceThresholdObjects) {
        this.confidenceThresholdObjects = confidenceThresholdObjects;
    }

    public double getThresholdDatatypes() {
        return this.confidenceThresholdDatatypes;
    }

    public void setThresholdDatatypes(double confidenceThresholdDatatypes) {
        this.confidenceThresholdDatatypes = confidenceThresholdDatatypes;
    }

    public double getThresholdMixed() {
        return this.confidenceThresholdMixed;
    }

    public void setThresholdMixed(double confidenceThresholdMixed) {
        this.confidenceThresholdMixed = confidenceThresholdMixed;
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
