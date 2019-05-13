package de.uni_mannheim.informatik.dws.ontmatching.matchingeval.resultswriter.mappingdesc;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Correspondence;
import org.apache.jena.ontology.OntModel;

public interface IMappingDescription {
    public String getValue(Correspondence c, OntModel source, OntModel target);
    public String getName();
}
