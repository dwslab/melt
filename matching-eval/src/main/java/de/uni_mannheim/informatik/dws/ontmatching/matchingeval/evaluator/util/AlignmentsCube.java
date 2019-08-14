package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.IExplainerResource;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.IExplainerResourceWithJenaOntology;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * Analytical Store for alignments.
 *
 * @author Jan Portisch
 */
public class AlignmentsCube {

    /**
     * Internal, wrapped data structure.
     */
    private HashMap<TestCaseMatcher, AnalyticalAlignmentInformation> alignmentDataCube;

    /**
     * Constructor
     */
    public AlignmentsCube() {
        alignmentDataCube = new HashMap<>();
    }

    /**
     * Default logger.
     */
    private static Logger LOGGER = LoggerFactory.getLogger(AlignmentsCube.class);

    /**
     * A list of resource explainers that shall be used to further describe resources in the detailed mapping report.
     */
    private List<IExplainerResource> resourceExplainers;


    /**
     * Add analytical alignment information.
     *
     * @param testCase                       Test case.
     * @param matcher                        Matcher name.
     * @param analyticalAlignmentInformation
     */
    public AnalyticalAlignmentInformation putAnalyticalMappingInformation(TestCase testCase, String matcher, AnalyticalAlignmentInformation analyticalAlignmentInformation) {
        return alignmentDataCube.put(new TestCaseMatcher(testCase, matcher), analyticalAlignmentInformation);
    }


    /**
     * Get analytical mapping information for testcase and matcher name.
     * This method will never return null. If the AnalyticalAlignmentInformation cannot be found, it will be initialized.
     *
     * @param testCase Test Case
     * @param matcher  Matcher name.
     * @return Analytical Alignment Information if exists
     */
    public AnalyticalAlignmentInformation getAnalyticalMappingInformation(TestCase testCase, String matcher) {
        TestCaseMatcher lookupKey = new TestCaseMatcher(testCase, matcher);
        AnalyticalAlignmentInformation result = alignmentDataCube.get(lookupKey);
        if (result == null) {
            result = new AnalyticalAlignmentInformation();
            alignmentDataCube.put(new TestCaseMatcher(testCase, matcher), result);
            return result;
        } else return result;
    }


    /**
     * Number of available analytical mappign information.
     *
     * @return Number as int.
     */
    public int size() {
        return alignmentDataCube.size();
    }


    /**
     * Write the Alignment Cube as CSV.
     *
     * @param baseDirectory
     */
    public void write(File baseDirectory) {
        try {
            File fileToBeWritten = new File(baseDirectory, "alignmentCube.csv");
            fileToBeWritten.getParentFile().mkdirs();
            CSVPrinter printer = new CSVPrinter(new FileWriter(fileToBeWritten, false), CSVFormat.DEFAULT);
            List<String> header = getHeader();
            printer.printRecord(header);

            for (HashMap.Entry<TestCaseMatcher, AnalyticalAlignmentInformation> cubeComponent : this.alignmentDataCube.entrySet()) {
                String trackName = cubeComponent.getKey().testCase.getTrack().getName();
                String testCaseName = cubeComponent.getKey().testCase.getName();
                String matcherName = (cubeComponent.getKey().matcher);
                LOGGER.info("Writing " + trackName + " | " + testCaseName + " | " + matcherName);

                for (HashMap.Entry<Correspondence, HashMap<String, String>> mappingInformationEntry : cubeComponent.getValue().getMappingInformation().entrySet()) {
                    List<String> record = new LinkedList<>();
                    record.add(trackName);
                    record.add(testCaseName);
                    record.add(matcherName);

                    // resource feature uri1
                    for (IExplainerResource explainer : resourceExplainers) {
                        if (explainer instanceof IExplainerResourceWithJenaOntology) {
                            ((IExplainerResourceWithJenaOntology) explainer).setOntModel(cubeComponent.getKey().testCase.getSourceOntology(OntModel.class));
                            Map<String, String> resourceFeatures = explainer.getResourceFeatures(mappingInformationEntry.getKey().getEntityOne());
                            if(resourceFeatures == null) {
                                LOGGER.warn("No resource features for " + mappingInformationEntry.getKey().getEntityOne());
                            } else {
                                for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                    String feature = resourceFeatures.get(resourceFeatureName);
                                    if (feature == null) {
                                        LOGGER.info("Could not find feature " + resourceFeatureName + " ignoring it.");
                                        record.add("");
                                    } else record.add(feature);
                                }
                            }
                        }
                    }

                    record.add(mappingInformationEntry.getKey().getEntityOne());
                    record.add(mappingInformationEntry.getKey().getRelation().toString());
                    record.add(Double.toString(mappingInformationEntry.getKey().getConfidence()));
                    record.add(mappingInformationEntry.getKey().getEntityTwo());

                    // resource feature uri2
                    for (IExplainerResource explainer : resourceExplainers) {
                        if (explainer instanceof IExplainerResourceWithJenaOntology) {
                            ((IExplainerResourceWithJenaOntology) explainer).setOntModel(cubeComponent.getKey().testCase.getTargetOntology(OntModel.class));
                            Map<String, String> resourceFeatures = explainer.getResourceFeatures(mappingInformationEntry.getKey().getEntityTwo());
                            if(resourceFeatures == null) {
                                LOGGER.warn("No resource features for " + mappingInformationEntry.getKey().getEntityTwo());
                            } else {
                                for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                    String feature = resourceFeatures.get(resourceFeatureName);
                                    if (feature == null) {
                                        LOGGER.info("Could not find feature " + resourceFeatureName + " ignoring it.");
                                        record.add("");
                                    } else record.add(feature);
                                }
                            }
                        }
                    }

                    // add feature values
                    for(String featureName : getFeatureNames()){
                        String featureValue = mappingInformationEntry.getValue().get(featureName);
                        if(featureValue == null){
                            record.add("");
                        } else record.add(featureValue);
                    }
                    printer.printRecord(record);
                } // end of loop over mapping information entry
            } // end of loop over mapping information entries a.k.a. cubeComponent

            printer.flush();
            printer.close();
        } catch (IOException ioe) {
            LOGGER.error("Could not write alignments cube.", ioe);
            ioe.printStackTrace();
        }
    }


    /**
     * Same logic/code as in {@link AlignmentsCube#write(File)} but worse memory behavior (that's why the code cannot
     * be better modularized).
     * @return Large String.
     */
    @Override
    public String toString(){

        try {
            StringWriter writer = new StringWriter();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);

            printer.printRecord(getHeader());

            for (HashMap.Entry<TestCaseMatcher, AnalyticalAlignmentInformation> cubeComponent : this.alignmentDataCube.entrySet()) {
                String trackName = cubeComponent.getKey().testCase.getTrack().getName();
                String testCaseName = cubeComponent.getKey().testCase.getName();
                String matcherName = (cubeComponent.getKey().matcher);
                LOGGER.info("Writing " + trackName + " | " + testCaseName + " | " + matcherName);

                for (HashMap.Entry<Correspondence, HashMap<String, String>> mappingInformationEntry : cubeComponent.getValue().getMappingInformation().entrySet()) {
                    List<String> record = new LinkedList<>();
                    record.add(trackName);
                    record.add(testCaseName);
                    record.add(matcherName);

                    // resource feature uri1
                    for (IExplainerResource explainer : resourceExplainers) {
                        if (explainer instanceof IExplainerResourceWithJenaOntology) {
                            ((IExplainerResourceWithJenaOntology) explainer).setOntModel(cubeComponent.getKey().testCase.getSourceOntology(OntModel.class));
                            Map<String, String> resourceFeatures = explainer.getResourceFeatures(mappingInformationEntry.getKey().getEntityOne());
                            if (resourceFeatures == null) {
                                LOGGER.warn("No resource features for " + mappingInformationEntry.getKey().getEntityOne());
                            } else {
                                for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                    String feature = resourceFeatures.get(resourceFeatureName);
                                    if (feature == null) {
                                        LOGGER.info("Could not find feature " + resourceFeatureName + " ignoring it.");
                                        record.add("");
                                    } else record.add(feature);
                                }
                            }
                        }
                    }

                    record.add(mappingInformationEntry.getKey().getEntityOne());
                    record.add(mappingInformationEntry.getKey().getRelation().toString());
                    record.add(Double.toString(mappingInformationEntry.getKey().getConfidence()));
                    record.add(mappingInformationEntry.getKey().getEntityTwo());

                    // resource feature uri2
                    for (IExplainerResource explainer : resourceExplainers) {
                        if (explainer instanceof IExplainerResourceWithJenaOntology) {
                            ((IExplainerResourceWithJenaOntology) explainer).setOntModel(cubeComponent.getKey().testCase.getTargetOntology(OntModel.class));
                            Map<String, String> resourceFeatures = explainer.getResourceFeatures(mappingInformationEntry.getKey().getEntityTwo());
                            if (resourceFeatures == null) {
                                LOGGER.warn("No resource features for " + mappingInformationEntry.getKey().getEntityTwo());
                            } else {
                                for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                    String feature = resourceFeatures.get(resourceFeatureName);
                                    if (feature == null) {
                                        LOGGER.info("Could not find feature " + resourceFeatureName + " ignoring it.");
                                        record.add("");
                                    } else record.add(feature);
                                }
                            }
                        }
                    }

                    // add feature values
                    for (String featureName : getFeatureNames()) {
                        String featureValue = mappingInformationEntry.getValue().get(featureName);
                        if (featureValue == null) {
                            record.add("");
                        } else record.add(featureValue);
                    }
                    printer.printRecord(record);
                } // end of loop over mapping information entry
            } // end of loop over mapping information entries a.k.a. cubeComponent

            // note: writer does not have to be closed.
            return writer.toString();
        } catch (IOException ioe){
            LOGGER.error("Could not transform AlignmentsCube to String.", ioe);
            return null;
        }
    }



    /**
     * Get the header of the cube, i.e. all describing attributes.
     *
     * @return Cube header as String List.
     */
    public List<String> getHeader() {
        List<String> header = new ArrayList<>();
        header.add("Track");
        header.add("TestCase");
        header.add("Matcher");
        for (IExplainerResource explainer : resourceExplainers) {
            for(String featureName : explainer.getResourceFeatureNames()) {
                header.add(featureName +" Left");
            }
        }
        header.add("URI Left");
        header.add("Relation");
        header.add("Confidence");
        header.add("URI Right");
        for (IExplainerResource explainer : resourceExplainers) {
            for(String featureName : explainer.getResourceFeatureNames()) {
                header.add(featureName +" Right");
            }
        }
        for (String featureName : getFeatureNames()) {
            header.add(featureName);
        }
        return header;
    }


    /**
     * Get feature names used.
     *
     * @return a list of all feature names.
     */
    private ArrayList<String> getFeatureNames() {
        ArrayList<String> result = new ArrayList<>();
        HashSet<String> featureNames = new HashSet<>();
        for (HashMap.Entry<TestCaseMatcher, AnalyticalAlignmentInformation> entry : this.alignmentDataCube.entrySet()) {
            featureNames.addAll(entry.getValue().getMappingFeatureNames());
        }
        // keeping a certain order (relevant if cube is looked at in excel file
        if (featureNames.contains(AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString())) {
            result.add(AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString());
        }
        if (featureNames.contains(AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString())) {
            result.add(AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString());
        }
        for (String featureName : featureNames) {
            // already added:
            if (featureName.equals(AnalyticalAlignmentInformation.DefaultFeatures.RESIDUAL.toString())) continue;
            if (featureName.equals(AnalyticalAlignmentInformation.DefaultFeatures.EVALUATION_RESULT.toString()))
                continue;

            result.add(featureName);
        }
        return result;
    }


    //-------------------------------------------------------------------------------------------
    // Data Structures
    //-------------------------------------------------------------------------------------------

    /**
     * TestCaseMatcher Structure
     */
    private class TestCaseMatcher {
        TestCase testCase;
        String matcher;

        public TestCaseMatcher(TestCase testCase, String matcher) {
            this.testCase = testCase;
            this.matcher = matcher;
        }


        @Override
        public int hashCode() {
            int hash = 8;
            hash = hash + testCase.getName().hashCode();
            hash = hash + matcher.hashCode();
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || (obj.getClass() != this.getClass())) {
                return false;
            } else if (((TestCaseMatcher) obj).testCase.getName().equals(this.testCase.getName()) && ((TestCaseMatcher) obj).matcher.equals(this.matcher)) {
                return true;
            } else return false;
        }
    }


    //-------------------------------------------------------------------------------------------
    // Getters and Setters
    //-------------------------------------------------------------------------------------------


    public List<IExplainerResource> getResourceExplainers() {
        return resourceExplainers;
    }

    public void setResourceExplainers(List<IExplainerResource> resourceExplainers) {
        this.resourceExplainers = resourceExplainers;
    }
}

