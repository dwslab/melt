package de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.util;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.IExplainerResourceWithJenaOntology;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentSerializer;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.matching_base.IExplainerResource;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.explainer.ExplainerResourceProperty;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.ontology.OntModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

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
        resourceExplainers = new ArrayList<>();
        correspondenceExtensions = new ArrayList<>();
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
     * Correspondence extension.
     */
    private List<String> correspondenceExtensions;

    /**
     * If true no correspondence extensions will be printed.
     */
    private boolean isPrintCorrespondenceExtensions = true;


    /**
     * Add analytical alignment information.
     *
     * @param testCase Test case instance.
     * @param matcherName Matcher name.
     * @param analyticalAlignmentInformation The analytical alignment information data set to be added.
     * @return The previous value associated with key, or null if there was no mapping for key. (A null return can also indicate that the map previously associated null with key.)
     */
    public AnalyticalAlignmentInformation putAnalyticalMappingInformation(TestCase testCase, String matcherName, AnalyticalAlignmentInformation analyticalAlignmentInformation) {
        return alignmentDataCube.put(new TestCaseMatcher(testCase, matcherName), analyticalAlignmentInformation);
    }


    /**
     * Get analytical mapping information for testcase and matcher name.
     * This method will never return null. If the AnalyticalAlignmentInformation cannot be found, it will be initialized.
     *
     * @param testCase Test Case
     * @param matcher Matcher name.
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
     * @param baseDirectory The base directory to which the alignment cube shall be written.
     */
    public void write(File baseDirectory) {

        try {
            File fileToBeWritten = new File(baseDirectory, "alignmentCube.csv");
            fileToBeWritten.getParentFile().mkdirs();
            CSVPrinter printer = CSVFormat.DEFAULT.print(fileToBeWritten, StandardCharsets.UTF_8);
            List<String> header = getHeader();
            printer.printRecord(header);

            for (HashMap.Entry<TestCaseMatcher, AnalyticalAlignmentInformation> cubeComponent : this.alignmentDataCube.entrySet()) {
                String trackName = cubeComponent.getKey().testCase.getTrack().getName();
                String testCaseName = cubeComponent.getKey().testCase.getName();
                String matcherName = (cubeComponent.getKey().matcher);
                LOGGER.info("Writing " + trackName + " | " + testCaseName + " | " + matcherName + " [to file: " + fileToBeWritten.getCanonicalPath() + "]");

                for (HashMap.Entry<Correspondence, HashMap<String, String>> mappingInformationEntry : cubeComponent.getValue().getMappingInformation().entrySet()) {
                    List<String> record = new LinkedList<>();
                    record.add(cutAfterThirtyTwoThousandCharacters(trackName));
                    record.add(cutAfterThirtyTwoThousandCharacters(testCaseName));
                    record.add(cutAfterThirtyTwoThousandCharacters(matcherName));

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
                                    } else record.add(cutAfterThirtyTwoThousandCharacters(feature));
                                }
                            }
                        }
                    }

                    record.add(cutAfterThirtyTwoThousandCharacters(mappingInformationEntry.getKey().getEntityOne()));
                    record.add(cutAfterThirtyTwoThousandCharacters(mappingInformationEntry.getKey().getRelation().toString()));
                    record.add(cutAfterThirtyTwoThousandCharacters(Double.toString(mappingInformationEntry.getKey().getConfidence())));
                    record.add(cutAfterThirtyTwoThousandCharacters(mappingInformationEntry.getKey().getEntityTwo()));

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
                                    } else record.add(cutAfterThirtyTwoThousandCharacters(feature));
                                }
                            }
                        }
                    }

                    // add feature values
                    for(String featureName : getFeatureNames()){
                        String featureValue = mappingInformationEntry.getValue().get(featureName);
                        if(featureValue == null){
                            record.add("");
                        } else record.add(cutAfterThirtyTwoThousandCharacters(featureValue));
                    }

                    // add correspondence extensions
                    if(isPrintCorrespondenceExtensions) {
                        Map<String, Object> extensionsForCorrespondence = mappingInformationEntry.getKey().getExtensions();
                        if (extensionsForCorrespondence != null) {
                            for (String extensionValue : correspondenceExtensions) {
                                Object value = extensionsForCorrespondence.get(extensionValue);
                                if (value != null) {
                                    record.add(cutAfterThirtyTwoThousandCharacters(value.toString()));
                                } else record.add("");
                            }
                        }
                    }

                    printer.printRecord(record);
                } // end of loop over mapping information entry
            } // end of loop over mapping information entries a.k.a. cubeComponent

            printer.flush();
            printer.close();
        } catch (IOException ioe) {
            LOGGER.error("Could not write alignments cube.", ioe);
        }
    }


    /**
     * This method cuts Strings after 32000 characters.
     * @param stringToCut The string to be cut.
     * @return Cut string. If a cut operation was performed, this will be printed as warning in the log.
     */
    public static String cutAfterThirtyTwoThousandCharacters(String stringToCut){
        if(stringToCut.length() > 32000){
            LOGGER.warn("String cut because of > 32 000 characters.");
            return stringToCut.substring(0, 32000);
        } else return stringToCut;
    }


    /**
     * Same logic/code as in {@link AlignmentsCube#write(File)} but worse memory behavior (that's why the code cannot
     * be better modularized). Similar to {@link AlignmentsCube#toString()} with the difference that URIs are
     * shortened.
     *
     * @return Large String.
     */
    public String toShortString(){
        HashMap<TestCaseMatcher, PrefixLookup> leftURIs = new HashMap<>();
        HashMap<TestCaseMatcher, PrefixLookup> rightURIs = new HashMap<>();
        for(Entry<TestCaseMatcher, AnalyticalAlignmentInformation> entry : this.alignmentDataCube.entrySet()){
            Set<String> leftUris = new HashSet<>();
            Set<String> rightUris = new HashSet<>();            
            for(Correspondence c : entry.getValue().getMappingInformation().keySet()){
                leftUris.add(c.getEntityOne());
                rightUris.add(c.getEntityTwo());
            }
            leftURIs.put(entry.getKey(), new PrefixLookup(leftUris));
            rightURIs.put(entry.getKey(), new PrefixLookup(rightUris));            
        }
        
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord(getHeader()); // shorter header
            List<List<String>> records = getRecordsToPrintForToStringMethods(leftURIs, rightURIs, true);
            for(List<String> record : records){
                printer.printRecord(record);
            }
            // note: writer does not have to be closed.
            return writer.toString();
        } catch (IOException ioe){
            LOGGER.error("Could not transform AlignmentsCube to String.", ioe);
            return null;
        }
    }


    /**
     * Internal data structure.
     */
    private class StringString {
        String string_1 = "";
        String string_2 = "";
        StringString(String s1, String s2){
            string_1 = s1;
            string_2 = s2;
        }
        StringString(){}
    }

    /**
     * Determines the most frequent object of a distribution.
     * @param distribution The distribution of which the most frequent object shall be determined.
     * @param <O> Return type.
     * @return Most frequent object.
     */
    private <O> O mostFrequent(HashMap<O, Integer> distribution){
        int highestScore = 0;
        O mostFrequentObject = null;
        for(HashMap.Entry<O, Integer> entry : distribution.entrySet()){
            if(entry.getValue() > highestScore){
                mostFrequentObject = entry.getKey();
                highestScore = entry.getValue();
            }
        }
        return mostFrequentObject;
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
            for(List<String> record : getRecordsToPrintForToStringMethods()){
                printer.printRecord(record);
            }
            // note: writer does not have to be closed.
            return writer.toString();
        } catch (IOException ioe){
            LOGGER.error("Could not transform AlignmentsCube to String.", ioe);
            return null;
        }
    }


    private List<List<String>> getRecordsToPrintForToStringMethods(){
        return getRecordsToPrintForToStringMethods(null, null, false);
    }

    /**
     * This method returns the alignment cube in a way that it can be easily printed as String.
     * This internal method is to be used by {@link AlignmentsCube#toString()} and {@link AlignmentsCube#toShortString()}.
     * This method only contains the data (without the CSV header).
     *
     * @param left Left side.
     * @param right Right side.
     * @param printShort Indicator whether prefixes shall be used in URIs.
     * @return List of the records to be printed.
     */
    private List<List<String>> getRecordsToPrintForToStringMethods(HashMap<TestCaseMatcher, PrefixLookup> left, HashMap<TestCaseMatcher, PrefixLookup> right, boolean printShort){
        List<List<String>> result = new ArrayList<>();
        for (HashMap.Entry<TestCaseMatcher, AnalyticalAlignmentInformation> cubeComponent : this.alignmentDataCube.entrySet()) {
            String trackName = cubeComponent.getKey().testCase.getTrack().getName();
            String testCaseName = cubeComponent.getKey().testCase.getName();
            String matcherName = (cubeComponent.getKey().matcher);
            
            PrefixLookup leftPrefixLookup = PrefixLookup.EMPTY;
            if(printShort){
                leftPrefixLookup = left.get(cubeComponent.getKey());
                if(leftPrefixLookup == null)
                    leftPrefixLookup = PrefixLookup.DEFAULT;
            }
            
            PrefixLookup rightPrefixLookup = PrefixLookup.EMPTY;
            if(printShort){
                rightPrefixLookup = right.get(cubeComponent.getKey());
                if(rightPrefixLookup == null)
                    rightPrefixLookup = PrefixLookup.DEFAULT;
            }         
            
            for (HashMap.Entry<Correspondence, HashMap<String, String>> mappingInformationEntry : cubeComponent.getValue().getMappingInformation().entrySet()) {
                List<String> record = new LinkedList<>();
                record.add(cutAfterThirtyTwoThousandCharacters(trackName));
                record.add(cutAfterThirtyTwoThousandCharacters(testCaseName));
                record.add(cutAfterThirtyTwoThousandCharacters(matcherName));

                // resource feature uri1
                for (IExplainerResource explainer : resourceExplainers) {  
                    
                    if (explainer instanceof ExplainerResourceProperty)
                        ((ExplainerResourceProperty)explainer).setUriPrefixLookup(leftPrefixLookup);
                    
                    if (explainer instanceof IExplainerResourceWithJenaOntology) {
                        ((IExplainerResourceWithJenaOntology) explainer).setOntModel(cubeComponent.getKey().testCase.getSourceOntology(OntModel.class));
                        Map<String, String> resourceFeatures = explainer.getResourceFeatures(mappingInformationEntry.getKey().getEntityOne());
                        if (resourceFeatures == null) {
                            LOGGER.warn("No resource features for " + mappingInformationEntry.getKey().getEntityOne());
                            for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                record.add("");
                            }
                        } else {
                            for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                String feature = resourceFeatures.get(resourceFeatureName);
                                if (feature == null) {
                                    LOGGER.info("Could not find feature " + resourceFeatureName + " ignoring it.");
                                    record.add("");
                                } else record.add(cutAfterThirtyTwoThousandCharacters(feature));
                            }
                        }
                    }
                }

                record.add(cutAfterThirtyTwoThousandCharacters(leftPrefixLookup.getPrefix(mappingInformationEntry.getKey().getEntityOne())));
                record.add(cutAfterThirtyTwoThousandCharacters(mappingInformationEntry.getKey().getRelation().toString()));
                record.add(cutAfterThirtyTwoThousandCharacters(Double.toString(mappingInformationEntry.getKey().getConfidence())));
                record.add(cutAfterThirtyTwoThousandCharacters(rightPrefixLookup.getPrefix(mappingInformationEntry.getKey().getEntityTwo())));

                // resource feature uri2
                for (IExplainerResource explainer : resourceExplainers) {
                    if (explainer instanceof ExplainerResourceProperty)
                        ((ExplainerResourceProperty)explainer).setUriPrefixLookup(rightPrefixLookup);
                    
                    if (explainer instanceof IExplainerResourceWithJenaOntology) {
                        ((IExplainerResourceWithJenaOntology) explainer).setOntModel(cubeComponent.getKey().testCase.getTargetOntology(OntModel.class));
                        Map<String, String> resourceFeatures = explainer.getResourceFeatures(mappingInformationEntry.getKey().getEntityTwo());
                        if (resourceFeatures == null) {
                            LOGGER.warn("No resource features for " + mappingInformationEntry.getKey().getEntityTwo());
                            for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                record.add("");
                            }
                        } else {
                            for (String resourceFeatureName : explainer.getResourceFeatureNames()) {
                                String feature = resourceFeatures.get(resourceFeatureName);
                                if (feature == null) {
                                    LOGGER.info("Could not find feature " + resourceFeatureName + " ignoring it.");
                                    record.add("");
                                } else record.add(cutAfterThirtyTwoThousandCharacters(feature));
                            }
                        }
                    }
                }

                // add feature values
                for (String featureName : getFeatureNames()) {
                    String featureValue = mappingInformationEntry.getValue().get(featureName);
                    if (featureValue == null) {
                        record.add("");
                    } else record.add(cutAfterThirtyTwoThousandCharacters(featureValue));
                }

                // add correspondence extensions
                if(isPrintCorrespondenceExtensions) {
                    Map<String, Object> extensionsForCorrespondence = mappingInformationEntry.getKey().getExtensions();
                    if (extensionsForCorrespondence != null) {
                        for (String extensionValue : correspondenceExtensions) {
                            Object value = extensionsForCorrespondence.get(extensionValue);
                            if (value != null) {
                                record.add(cutAfterThirtyTwoThousandCharacters(value.toString()));
                            } else record.add("");
                        }
                    }
                }

                result.add(record);
            } // end of loop over mapping information entry
        } // end of loop over mapping information entries a.k.a. cubeComponent
        return result;
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
        header.add("Confidence (Matcher)");
        header.add("URI Right");
        for (IExplainerResource explainer : resourceExplainers) {
            for(String featureName : explainer.getResourceFeatureNames()) {
                header.add(featureName +" Right");
            }
        }
        for (String featureName : getFeatureNames()) {
            header.add(featureName);
        }

        if(isPrintCorrespondenceExtensions) {
            for (String extensionName : getCorrespondenceExtensions()) {
                header.add(AlignmentSerializer.getExtensionLabel(extensionName));
            }
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

    public List<String> getCorrespondenceExtensions() {
        return correspondenceExtensions;
    }

    public void setCorrespondenceExtensions(List<String> correspondenceExtensions) {
        this.correspondenceExtensions = correspondenceExtensions;
    }

    public boolean isPrintCorrespondenceExtensions() {
        return isPrintCorrespondenceExtensions;
    }

    public void setPrintCorrespondenceExtensions(boolean printCorrespondenceExtensions) {
        this.isPrintCorrespondenceExtensions = printCorrespondenceExtensions;
    }
}

