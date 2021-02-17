package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.statistics;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.LabelToConceptLinker;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class allows to analyze the concept coverage given a data source.
 */
public class Coverage {


    /**
     * Calculate the partial coverage - that is also allow for matching only concept parts (sub-strings).
     * @param linker Linker to be used.
     * @param entities Entities to be matched.
     * @return A coverage result instance.
     */
    @NotNull
    public static CoverageResult getCoveragePartialLabel(LabelToConceptLinker linker, Set<String> entities){
        Map<String, Set<String>>  conceptsFound = new HashMap<>();
        Set<String> conceptsNotFound = new HashSet<>();
        for (String concept : entities){
            Set<String> links = linker.linkToPotentiallyMultipleConcepts(concept);
            if(links == null || links.size() == 0){
                conceptsNotFound.add(concept);
            } else {
                conceptsFound.put(concept, links);
            }
        }
        float coverageScore = (float) conceptsFound.size() / entities.size();
        return  new CoverageResult(coverageScore, conceptsFound, conceptsNotFound);
    }

    /**
     * Calculate the coverage given a linker and a set of concepts that are to be linked.
     * @param linker The linker to be used.
     * @param entities The entities to be looked up.
     * @return A coverage result instance.
     */
    @NotNull
    public static CoverageResult getCoverageFullLabel(LabelToConceptLinker linker, Set<String> entities){
        Map<String, Set<String>>  conceptsFound = new HashMap<>();
        Set<String> conceptsNotFound = new HashSet<>();
        for (String concept : entities){
            Set<String> links = new HashSet<>();
            String link = linker.linkToSingleConcept(concept);
            if(link == null || link.equals("")){
                conceptsNotFound.add(concept);
            } else {
                links.add(link);
                conceptsFound.put(concept, links);
            }
        }
        float coverageScore = (float) conceptsFound.size() / entities.size();
        return  new CoverageResult(coverageScore, conceptsFound, conceptsNotFound);
    }
}
