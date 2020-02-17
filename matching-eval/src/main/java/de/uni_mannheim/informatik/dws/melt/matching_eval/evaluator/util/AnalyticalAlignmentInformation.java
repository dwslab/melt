package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util;

import de.uni_mannheim.informatik.dws.melt.matching_base.IExplainerMapping;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;

import java.util.*;


/**
 * This data structure can be used to store all kind of analytical information about one alignment.
 *
 * @author Jan Portisch
 */
public class AnalyticalAlignmentInformation implements IExplainerMapping {

    /**
     * Core data structure where all the analytical mapping information is held.
     * Cell -&gt; Feature Map &lt;FeatureName, FeatureValue&gt;&lt;/FeatureName,&gt;
     */
    private HashMap<Correspondence, HashMap<String, String>> mappingInformation;

    /**
     * This data structure holds the FeatureNames.
     */
    private HashSet<String> mappingFeatureNames;


    public AnalyticalAlignmentInformation() {
        this.mappingFeatureNames = new HashSet<>();
        this.mappingInformation = new HashMap<>();
    }

    /**
     * Add information about a cell.
     * If the information exists already, it will be overwritten.
     * If there has not been a data set entry, the entry will be created.
     *
     * @param cell         The cell for which information shall be persisted.
     * @param featureName  The name of the feature.
     * @param featureValue The feature value.
     */
    public void add(Correspondence cell, String featureName, String featureValue) {
        if (mappingInformation.containsKey(cell)) {
            if (mappingInformation.get(cell) == null) mappingInformation.put(cell, new HashMap<>());
        } else mappingInformation.put(cell, new HashMap<>());
        mappingInformation.get(cell).put(featureName, featureValue);
        mappingFeatureNames.add(featureName);
    }


    /**
     * Add multiple cells that have the same feature value.
     *
     * @param cells        Set of cells.
     * @param featureName  Feature name.
     * @param featureValue Feature value.
     */
    public void addAll(Iterable<Correspondence> cells, String featureName, String featureValue) {
        for (Correspondence cell : cells) {
            add(cell, featureName, featureValue);
        }
    }


    /**
     * Add multiple cells that have the same feature values.
     *
     * @param cells    Set of cells.
     * @param features Features
     */
    public void addAll(Iterable<Correspondence> cells, HashMap<String, String> features) {
        for (Correspondence cell : cells) {
            for (HashMap.Entry<String, String> entry : features.entrySet()) {
                add(cell, entry.getKey(), entry.getValue());
            }
        }
    }


    @Override
    public Map<String, String> getMappingFeatures(String uriOne, String uriTwo, String relation, double confidence) {
        Correspondence target = new Correspondence(uriOne, uriTwo, confidence, CorrespondenceRelation.parse(relation));
        return mappingInformation.get(target);
    }

    @Override
    public List<String> getMappingFeatureNames() {
        return new LinkedList<>(mappingFeatureNames);
    }

    public HashMap<Correspondence, HashMap<String, String>> getMappingInformation() {
        return mappingInformation;
    }

    /**
     * Data structure for some often used features.
     */
    public enum DefaultFeatures {
        RESIDUAL, EVALUATION_RESULT;

        @Override
        public String toString() {
            switch (this) {
                case RESIDUAL:
                    return "Residual True Positive";
                case EVALUATION_RESULT:
                    return "Evaluation Result";
                default:
                    return "<no label>";
            }
        }
    }
}
