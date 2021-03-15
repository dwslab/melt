package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept;


import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_jena.ValueExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.stringOperations.StringOperations.writeSetToFile;

/**
 * Service class writing the links of a test case / track to a file.
 * (This can be helpful in some situations e.g. when pre-downloading certain external information or for
 * analyses.)
 *
 * If you are interest in linker coverage, use
 * {@link de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics.Coverage}.
 */
public class LinksToFile {


    /**
     * Write all links of a track to file (UTF-8 encoded).
     * @param fileToWrite The file that shall be written.
     * @param track The track that shall be linked.
     * @param extractor The extractor that shall be used to extract labels.
     * @param linker The linker that shall be used.
     * @param maxLabelTokenLength The maximum token length for token linking (to avoid linking description texts).
     */
    public static void writeLinksToFile(@NotNull File fileToWrite,
                                        @NotNull Track track,
                                        @NotNull ValueExtractor extractor,
                                        @NotNull LabelToConceptLinker linker,
                                        int maxLabelTokenLength){
        Set<String> allLinks = new HashSet<>();
        for(TestCase tc : track.getTestCases()){
            allLinks.addAll(getLinks(tc, extractor, linker, maxLabelTokenLength));
        }
        writeSetToFile(fileToWrite, allLinks);
    }

    /**
     * Write all links of a track to file (UTF-8 encoded).
     * @param fileToWrite The file that shall be written.
     * @param testCase The testCase that shall be linked.
     * @param extractor The extractor that shall be used to extract labels.
     * @param linker The linker that shall be used.
     * @param maxLabelTokenLength The maximum token length for token linking (to avoid linking description texts).
     */
    public static void writeLinksToFile(@NotNull File fileToWrite,
                                        @NotNull TestCase testCase,
                                        @NotNull ValueExtractor extractor,
                                        @NotNull LabelToConceptLinker linker,
                                        int maxLabelTokenLength){
        Set<String> allLinks = new HashSet<>(getLinks(testCase, extractor, linker, maxLabelTokenLength));
        writeSetToFile(fileToWrite, allLinks);
    }

    static Set<String> getLinks(TestCase testCase, ValueExtractor extractor,
                                LabelToConceptLinker linker,
                                int maxLabelTokenLength){
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
        result.addAll(getLinksForIterator(ontModel.listClasses(), extractor,linker, maxLabelTokenLength));
        result.addAll(getLinksForIterator(ontModel.listDatatypeProperties(), extractor,linker, maxLabelTokenLength));
        result.addAll(getLinksForIterator(ontModel.listObjectProperties(), extractor,linker, maxLabelTokenLength));
        return result;
    }

    static @NotNull Set<String> getLinksForIterator(ExtendedIterator<? extends OntResource> iterator,
                                                    ValueExtractor valueExtractor, LabelToConceptLinker linker,
                                                    int maxLabelTokenLength){
        Set<String> result = new HashSet<>();
        while (iterator.hasNext()) {
            OntResource r1 = iterator.next();
            Set<String> labels = valueExtractor.extract(r1);
            if (labels != null && labels.size() > 0) {
                for(String label : labels){
                    String link = linker.linkToSingleConcept(label);
                    if(link != null){
                        // full link could be found
                        result.add(link);
                    } else {
                        // go for partial links
                        String[] tokens = StringOperations.tokenizeBestGuess(label);
                        if(tokens.length < maxLabelTokenLength){
                            Set<String> linkedConcepts = linker.linkToPotentiallyMultipleConcepts(label);
                            if(linkedConcepts != null){
                                result.addAll(linkedConcepts);
                            }

                            // just to be sure: link all tokens
                            for(String token : tokens){
                                String tokenLink = linker.linkToSingleConcept(token);
                                if(tokenLink != null){
                                    result.add(tokenLink);
                                }
                            }
                        }
                    }
                }
            }
        } // end of while loop
        return result;
    }
}
