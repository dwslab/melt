package de.uni_mannheim.informatik.dws.melt.matching_owlapi_matchers.logmap;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import de.uni_mannheim.informatik.dws.melt.matching_owlapi.MatcherYAAAOwlApi;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.Properties;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.ox.krr.logmap2.LogMap2_RepairFacility;


/**
 * This is the logmap repair filter.
 * More information can be found <a href="https://code.google.com/archive/p/logmap-matcher/wikis/LogMapRepairFromApplications.wiki">at the wiki</a>
 * and in the <a href="https://github.com/ernestojimenezruiz/logmap-matcher">github repository</a>.
 * 
 * In case you want to use this filter, make the dependency of matching-owlapi-matchers to exclude the following:
 * <pre>{@code
 * <exclusions>
    <exclusion>
        <groupId>com.github.ansell.pellet</groupId>
        <artifactId>pellet-modularity</artifactId>
    </exclusion>
</exclusions>
 * }</pre>
 * 
 * and also include OWLAPI with version 4.1.3
 * <pre>{@code
 * <dependency>
    <groupId>net.sourceforge.owlapi</groupId>
    <artifactId>owlapi-distribution</artifactId>
    <version>4.1.3</version>
</dependency>
 * }</pre>
 */
public class LogMapRepairFilter extends MatcherYAAAOwlApi implements Filter {

    /**
     * If the intersection or overlapping of the ontologies are extracted before the repair
     */
    private boolean overlapping;
    
    /**
     * If the repair is performed in a two steps process (optimal) or in one cleaning step (more aggressive)
     */
    private boolean optimal;

    /**
     * Constructor which accepts all possible parameters (overkapping and optimal).
     * @param overlapping If the intersection or overlapping of the ontologies are extracted before the repair
     * @param optimal If the repair is performed in a two steps process (optimal) or in one cleaning step (more aggressive)
     */
    public LogMapRepairFilter(boolean overlapping, boolean optimal) {
        this.overlapping = overlapping;
        this.optimal = optimal;
    }
    
    /**
     * Default constructor which sets overlapping to and optimal to . like in <a href="https://github.com/ernestojimenezruiz/logmap-matcher/blob/master/src/test/java/UsingLogMapRepairFacility.java">
     * the example of LogMap</a>.
     */
    public LogMapRepairFilter() {
        this(true, false);
    }
    
    
    
    
    
    @Override
    public Alignment match(OWLOntology source, OWLOntology target, Alignment inputAlignment, Properties p) throws Exception {
        //TODO: don't use Alignment but Set<MappingObjectStr> to be able to concat multiple Logmap modules together.
        LogMap2_RepairFacility logmap2_repair = new LogMap2_RepairFacility(
							source,				//Ontology1 
							target,				//Ontology2
							Alignment2LogmapMapping.transformAlignment(inputAlignment, source, target), //Input Mappings
							overlapping,                    //If the intersection or overlapping of the ontologies are extracted before the repair
							optimal);			//If the repair is performed in a two steps process (optimal) or in one cleaning step (more aggressive)
        return LogmapMapping2Alignment.transformAlignment(logmap2_repair.getCleanMappings(), inputAlignment);
    }
}
