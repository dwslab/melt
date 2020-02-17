package de.uni_mannheim.informatik.dws.melt.matching_eval.resultswriter.mappingdesc;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import org.apache.jena.ontology.OntModel;

public interface IMappingDescription {
    public String getValue(Correspondence c, OntModel source, OntModel target);
    public String getName();
}
