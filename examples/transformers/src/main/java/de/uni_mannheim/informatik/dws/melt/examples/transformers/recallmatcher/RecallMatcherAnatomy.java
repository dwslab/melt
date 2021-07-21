package de.uni_mannheim.informatik.dws.melt.examples.transformers.recallmatcher;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.matcher.SimpleStringMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

import java.util.Properties;

import org.apache.jena.ontology.OntModel;

import java.util.*;

import org.apache.jena.ontology.OntResource;
import org.apache.jena.util.iterator.ExtendedIterator;


public class RecallMatcherAnatomy extends MatcherYAAAJena {


    @Override
    public Alignment match(OntModel source, OntModel target, Alignment alignment, Properties p) throws Exception {
        SimpleStringMatcher smatch = new SimpleStringMatcher();
        alignment.addAll(smatch.match(source, target, alignment, p));
        matchResources(source.listClasses(), target.listClasses(), alignment);
        matchResources(source.listDatatypeProperties(), target.listDatatypeProperties(), alignment);
        matchResources(source.listObjectProperties(), target.listObjectProperties(), alignment);
        matchResources(source.listIndividuals(), target.listIndividuals(), alignment);
        return alignment;
    }

    private void matchResources(ExtendedIterator<? extends OntResource> sourceResources, ExtendedIterator<? extends OntResource> targetResources, Alignment alignment) {
        List<? extends OntResource> list1 = sourceResources.toList();
        List<? extends OntResource> list2 = targetResources.toList();

        //initial lists to get the whole ontology without the already aligned ones
        List<OntResource> sourcesToMatch = new ArrayList<>();
        List<OntResource> targetsToMatch = new ArrayList<>();

        if(alignment == null){
            alignment = new Alignment();
        }

        //add only not already aligned URIs for O1
        for (OntResource r1 : list1) {
            String r1uri = r1.getURI();
            if(r1uri == null){
                continue;
            }
            if (!alignment.isSourceContained(r1uri)) {
                sourcesToMatch.add(r1);
            }
        }

        // add only not already aligned URIs for O2
        for (OntResource r2 : list2) {
            String r2uri = r2.getURI();
            if(r2uri == null){
                continue;
            }
            if (!alignment.isTargetContained(r2uri)) {
                targetsToMatch.add(r2);
            }
        }

        for (OntResource r1 : sourcesToMatch) {
            String r1uri = r1.getURI();
            String labelLeft = getLabel(r1);
            List<String> labelLeftTokens = new ArrayList<>();
            if (labelLeft != null) {
                //split the left label into an list
                if (labelLeft.contains(" ")) {
                    labelLeftTokens = Arrays.asList(labelLeft.toLowerCase().split(" "));
                } else {
                    labelLeftTokens.add(labelLeft.toLowerCase());
                }
            }

            for (OntResource r2 : targetsToMatch) {
                String r2uri = r2.getURI();
                int containingCounter = 0;
                String labelRight = getLabel(r2);
                List<String> labelRightTokens = new ArrayList<>();

                //split the right label into an list
                if (labelRight != null) {
                    if (labelRight.contains("_")) {
                        labelRightTokens = Arrays.asList(labelRight.toLowerCase().split("_"));
                    } else {
                        labelRightTokens.add(labelRight.toLowerCase());
                    }

                    //get the length of the shorter label list
                    int length = labelLeftTokens.size();
                    if (labelLeftTokens.size() > labelRightTokens.size()) {
                        length = labelRightTokens.size();
                    }

                    //find how many are equal in both label lists
                    for (String t : labelLeftTokens) {
                        if (labelRightTokens.contains(t)) {
                            containingCounter++;
                        }
                    }

                    //check if more than 50% of the label lists are the same
                    if (containingCounter >= (int) Math.ceil((double) length / 2)) {
                        if (r1uri != null && r2uri != null) {
                            /*if(labelLeft.length() < 30 && labelRight.length() < 30) {
                                double confidence = normalizedLevenshteinDistance(labelLeft, labelRight);
                                candidates.put(confidence, r2uri);
                            } else {*/
                            alignment.add(r1uri, r2uri, 0.1);
                        }
                    }
                }
            }
        }

    }

    public String getLabel(OntResource r) {
        if (r.getLabel(null) == null) {
            return r.getLocalName();
        } else {
            return r.getLabel(null);
        }
    }
}
