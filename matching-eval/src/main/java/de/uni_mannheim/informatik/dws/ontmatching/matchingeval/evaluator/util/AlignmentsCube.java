package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.util;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.IExplainerResource;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.Executor;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.baselineMatchers.BaselineStringMatcher;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.EvaluatorCSV;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.evaluator.explainer.IExplainerResourceWithJenaOntology;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TestCase;
import de.uni_mannheim.informatik.dws.ontmatching.matchingeval.tracks.TrackRepository;
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
     * @param testCase Test case instance.
     * @param matcherName Matcher name.
     * @param analyticalAlignmentInformation
     */
    public AnalyticalAlignmentInformation putAnalyticalMappingInformation(TestCase testCase, String matcherName, AnalyticalAlignmentInformation analyticalAlignmentInformation) {
        return alignmentDataCube.put(new TestCaseMatcher(testCase, matcherName), analyticalAlignmentInformation);
    }

    // TODO delete
    public static void main(String[] args) {
        ExecutionResultSet ers = Executor.run(TrackRepository.Anatomy.Default, new BaselineStringMatcher(), "Base");
        EvaluatorCSV evaluator = new EvaluatorCSV(ers);
        System.out.println(evaluator.getAlignmentsCubeAsShortenedString());
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
     * be better modularized). Similar to {@link AlignmentsCube#toString()} with the difference that URIs are
     * shortened.
     * @return Large String.
     */
    public String toShortString(){
        try {
            StringWriter writer = new StringWriter();
            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            printer.printRecord(getHeader()); // shorter header
            List<List<String>> records = getRecordsToPrintForToStringMethods();

            int typePosition = 0;
            // determine position of the URIs
            int explainerFeatures = 0;
            for (IExplainerResource explainer : resourceExplainers) {
                for(String featureName : explainer.getResourceFeatureNames()) {
                    if(featureName.equals("Type")){
                        if(typePosition == 0) typePosition = explainerFeatures;
                    }
                    explainerFeatures++;
                }
            }
            final int uri_1_typePosition = 3 + typePosition;
            final int uri_2_typePosition = uri_1_typePosition + 3 + typePosition;
            final int uri_1_position = 3 + explainerFeatures;
            final int uri_2_position = uri_1_position + 3;


            StringString mainUris = determineMainUris(records, uri_1_position, uri_2_position);

            for(List<String> record : records){
                String uri_1 = record.get(uri_1_position);
                if(uri_1.contains(mainUris.string_1)){
                    record.set(uri_1_position, uri_1.replace(mainUris.string_1, ":"));
                } else record.set(uri_1_position, PrefixLookup.getPrefix(uri_1));

                String uri_2 = record.get(uri_2_position);
                if(uri_2.contains(mainUris.string_2)){
                    record.set(uri_2_position, uri_2.replace(mainUris.string_2, ":"));
                } else record.set(uri_2_position, PrefixLookup.getPrefix(uri_2));

                //record.set(uri_1_typePosition, PrefixLookup.getPrefix(record.get(uri_1_typePosition)));
                //record.set(uri_2_typePosition, PrefixLookup.getPrefix(record.get(uri_2_typePosition)));

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
     * This methods determines the most frequent URIs in the set of records for ontology 1 and ontology 2.
     * @param records The set of records of which the main URI shall be determined.
     * @param uri_1_position Position of URI 1.
     * @param uri_2_position Position of URI 2.
     * @return The main URI as String.
     */
    private StringString determineMainUris(List<List<String>> records, int uri_1_position, int uri_2_position) {

        HashMap<String, Integer> uriDistribution_1 = new HashMap<>();
        HashMap<String, Integer> uriDistribution_2 = new HashMap<>();

        for(List<String> record : records){
            String uri_1_prefix = PrefixLookup.getPrefix(record.get(uri_1_position));
            String uri_2_prefix = PrefixLookup.getPrefix(record.get(uri_2_position));

            if(uriDistribution_1.containsKey(uri_1_prefix)){
                uriDistribution_1.put(uri_1_prefix, uriDistribution_1.get(uri_1_prefix) + 1);
            } else uriDistribution_1.put(uri_1_prefix, 1);

            if(uriDistribution_2.containsKey(uri_2_prefix)){
                uriDistribution_2.put(uri_2_prefix, uriDistribution_2.get(uri_2_prefix) + 1);
            } else uriDistribution_2.put(uri_2_prefix, 1);
        }

        // return string string
        return new StringString(mostFrequent(uriDistribution_1), mostFrequent(uriDistribution_2));
    }

    /**
     * Internal data structure.
     */
    private class StringString {
        String string_1;
        String string_2;
        StringString(String s1, String s2){
            string_1 = s1;
            string_2 = s2;
        }
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


    /**
     * This method returns the alignment cube in a way that it can be easily printed as String.
     * This internal method is to be used by {@link AlignmentsCube#toString()} and {@link AlignmentsCube#toShortString()}.
     * This method only contains the data (without the CSV header).
     * @return List of the records to be printed.
     */
    private List<List<String>> getRecordsToPrintForToStringMethods(){
        List<List<String>> result = new ArrayList<>();
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

