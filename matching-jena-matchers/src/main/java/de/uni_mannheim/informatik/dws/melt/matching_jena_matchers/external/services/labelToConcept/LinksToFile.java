package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept;


import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.MultiConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.valueExtractors.ValueExtractorAllAnnotationProperties;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations.writeSetToFile;

/**
 * Service class writing the links of a test case / track to a file.
 * (This can be helpful in some situations e.g. when pre-downloading certain external information or for
 * analyses.)
 * <br>
 * If you are interest in linker coverage, use
 * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics.Coverage}.
 */
public class LinksToFile {


    private static final Logger LOGGER = LoggerFactory.getLogger(LinksToFile.class);

    /**
     * Write all links of the tracks/testcases to one file (UTF-8 encoded).
     *
     * @param fileToWrite The file that shall be written.
     * @param list        List of tracks or test cases (you cannot mix them).
     * @param linker      The linker that shall be used.
     * @param <T>         T must be {@link Track} or {@link TestCase} - otherwise this method will not work.
     */
    public static <T> void writeLinksToFile(@NotNull String fileToWrite,
                                            @NotNull List<T> list,
                                            @NotNull LabelToConceptLinker linker) {
        writeLinksToFile(new File(fileToWrite), list, new ValueExtractorAllAnnotationProperties(), linker, 7);
    }

    /**
     * Write all links of the tracks/testcases to one file (UTF-8 encoded).
     *
     * @param fileToWrite The file that shall be written.
     * @param list        List of tracks or test cases (you cannot mix them).
     * @param linker      The linker that shall be used.
     * @param <T>         T must be {@link Track} or {@link TestCase} - otherwise this method will not work.
     */
    public static <T> void writeLinksToFile(@NotNull File fileToWrite,
                                            @NotNull List<T> list,
                                            @NotNull LabelToConceptLinker linker) {
        writeLinksToFile(fileToWrite, list, new ValueExtractorAllAnnotationProperties(), linker, 7);
    }

    /**
     * Write all links of the tracks/testcases to one file (UTF-8 encoded).
     *
     * @param fileToWrite         The file that shall be written.
     * @param list                List of tracks or test cases (you cannot mix them).
     * @param extractor           The extractor that shall be used to extract labels.
     * @param linker              The linker that shall be used.
     * @param maxLabelTokenLength The maximum token length for token linking (to avoid linking description texts).
     * @param <T>                 T must be {@link Track} or {@link TestCase} - otherwise this method will not work.
     */
    public static <T> void writeLinksToFile(@NotNull File fileToWrite,
                                            @NotNull List<T> list,
                                            @NotNull ValueExtractor extractor,
                                            @NotNull LabelToConceptLinker linker,
                                            int maxLabelTokenLength) {
        if (list.size() > 0) {
            if (list.get(0).getClass() == TestCase.class) {
                writeSetToFile(fileToWrite, getLinksTestcases((List<TestCase>) list, extractor, linker,
                        maxLabelTokenLength));
            } else if (list.get(0).getClass() == Track.class || list.get(0).getClass().getSuperclass() == Track.class) {
                writeSetToFile(fileToWrite, getLinksTracks((List<Track>) list, extractor, linker,
                        maxLabelTokenLength));
            } else {
                LOGGER.error("The provided list is neither of type Track nor of type TestCase. Links cannot be " +
                        "written to file. ABORTING operation.");
            }
        }
    }

    static Set<String> getLinksTracks(@NotNull List<Track> trackList,
                                      @NotNull ValueExtractor extractor,
                                      @NotNull LabelToConceptLinker linker,
                                      int maxLabelTokenLength) {
        List<TestCase> allTestCases = new ArrayList<>();
        for (Track track : trackList) {
            allTestCases.addAll(track.getTestCases());
        }
        return getLinksTestcases(allTestCases, extractor, linker, maxLabelTokenLength);
    }

    /**
     * Write all links of a track to file (UTF-8 encoded).
     *
     * @param fileToWrite         The file that shall be written.
     * @param track               The track that shall be linked.
     * @param extractor           The extractor that shall be used to extract labels.
     * @param linker              The linker that shall be used.
     * @param maxLabelTokenLength The maximum token length for token linking (to avoid linking description texts).
     */
    public static void writeLinksToFile(@NotNull File fileToWrite,
                                        @NotNull Track track,
                                        @NotNull ValueExtractor extractor,
                                        @NotNull LabelToConceptLinker linker,
                                        int maxLabelTokenLength) {
        writeSetToFile(fileToWrite, getLinksTestcases(track.getTestCases(), extractor, linker, maxLabelTokenLength));
    }

    static Set<String> getLinksTestcases(@NotNull List<TestCase> testCaseList,
                                         @NotNull ValueExtractor extractor,
                                         @NotNull LabelToConceptLinker linker,
                                         int maxLabelTokenLength) {
        Set<String> allLinks = new HashSet<>();
        for (TestCase tc : testCaseList) {
            allLinks.addAll(getLinks(tc, extractor, linker, maxLabelTokenLength));
        }
        return allLinks;
    }

    /**
     * Write all links of a track to file (UTF-8 encoded).
     *
     * @param fileToWrite         The file that shall be written.
     * @param testCase            The testCase that shall be linked.
     * @param extractor           The extractor that shall be used to extract labels.
     * @param linker              The linker that shall be used.
     * @param maxLabelTokenLength The maximum token length for token linking (to avoid linking description texts).
     */
    public static void writeLinksToFile(@NotNull File fileToWrite,
                                        @NotNull TestCase testCase,
                                        @NotNull ValueExtractor extractor,
                                        @NotNull LabelToConceptLinker linker,
                                        int maxLabelTokenLength) {
        Set<String> allLinks = new HashSet<>(getLinks(testCase, extractor, linker, maxLabelTokenLength));
        writeSetToFile(fileToWrite, allLinks);
    }

    static Set<String> getLinks(TestCase testCase, ValueExtractor extractor,
                                LabelToConceptLinker linker,
                                int maxLabelTokenLength) {
        Set<String> result = new HashSet<>();

        result.addAll(getLinksForOntModel(testCase.getSourceOntology(OntModel.class), extractor, linker,
                maxLabelTokenLength));
        result.addAll(getLinksForOntModel(testCase.getTargetOntology(OntModel.class), extractor, linker,
                maxLabelTokenLength));
        return result;
    }

    static @NotNull Set<String> getLinksForOntModel(OntModel ontModel, ValueExtractor extractor,
                                                    LabelToConceptLinker linker,
                                                    int maxLabelTokenLength) {
        Set<String> result = new HashSet<>();
        result.addAll(getLinksForIterator(ontModel.listClasses(), extractor, linker, maxLabelTokenLength));
        result.addAll(getLinksForIterator(ontModel.listDatatypeProperties(), extractor, linker, maxLabelTokenLength));
        result.addAll(getLinksForIterator(ontModel.listObjectProperties(), extractor, linker, maxLabelTokenLength));
        return result;
    }

    static @NotNull Set<String> getLinksForIterator(ExtendedIterator<? extends OntResource> iterator,
                                                    ValueExtractor valueExtractor, LabelToConceptLinker linker,
                                                    int maxLabelTokenLength) {
        Set<String> result = new HashSet<>();
        while (iterator.hasNext()) {
            OntResource r1 = iterator.next();
            Set<String> labels = valueExtractor.extract(r1);
            if (labels != null && labels.size() > 0) {
                for (String label : labels) {
                    String link = linker.linkToSingleConcept(label);
                    if (link != null) {
                        result.addAll(getUris(link, linker));
                    } else {
                        // go for partial links
                        String[] tokens = StringOperations.tokenizeBestGuess(label);
                        if (tokens.length < maxLabelTokenLength) {
                            Set<String> linkedConcepts = linker.linkToPotentiallyMultipleConcepts(label);
                            if (linkedConcepts != null) {
                                for (String multiConceptSingleLink : linkedConcepts) {
                                    result.addAll(getUris(multiConceptSingleLink, linker));
                                }
                            }

                            // just to be sure: link all tokens
                            for (String token : tokens) {
                                String tokenLink = linker.linkToSingleConcept(token);
                                if (tokenLink != null) {
                                    result.addAll(getUris(tokenLink, linker));
                                }
                            }
                        }
                    }
                }
            }
        } // end of while loop
        return result;
    }

    static @NotNull Set<String> getUris(String link, LabelToConceptLinker linker) {
        Set<String> result = new HashSet<>();
        if (linker instanceof MultiConceptLinker) {
            Set<String> uris = ((MultiConceptLinker) linker).getUris(link);
            if(uris != null) {
                result.addAll(uris);
            } else {result.add(link);}
        } else {
            result.add(link);
        }
        return result;
    }
}
