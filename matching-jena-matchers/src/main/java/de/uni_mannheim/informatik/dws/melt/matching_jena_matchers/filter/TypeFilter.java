package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter;

import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.util.Properties;

import de.uni_mannheim.informatik.dws.melt.matching_base.Filter;
import org.apache.jena.ontology.OntModel;

/**
 * Filters only class, instance or property matches.
 */
public class TypeFilter extends MatcherYAAAJena implements Filter {

    private ConceptType type;
    private boolean subsumeProperties;

    /**
     * Constructor which needs the type to remain in the alignment and if datatype and object properties should be subsumed as RDF_PROPERTY.
     * @param type the type which remains in the alignment
     * @param subsumeProperties if true only CLASS, RDF_PROPERTY, INSTANCE or UNKNOWN are valid types
     */
    public TypeFilter(ConceptType type, boolean subsumeProperties) {
        this.type = type;
        this.subsumeProperties = subsumeProperties;
    }
    
    /**
     * Choose type between CLASS, RDF_PROPERTY, or INSTANCE
     * @param type should be one of CLASS, RDF_PROPERTY, or INSTANCE
     */
    public TypeFilter(ConceptType type) {
        this(type, true);
    }
    
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {        
        Alignment alignment = new Alignment(inputAlignment, false);
        if(this.subsumeProperties){
            for(Correspondence c : inputAlignment){
                if(ConceptType.subsumeProperties(ConceptType.analyze(source, c.getEntityOne())) != this.type)
                    continue;
                if(ConceptType.subsumeProperties(ConceptType.analyze(target, c.getEntityTwo())) != this.type)
                    continue;
                alignment.add(c);
            }
        }else{
            for(Correspondence c : inputAlignment){
                if(ConceptType.analyze(source, c.getEntityOne()) != this.type)
                    continue;
                if(ConceptType.analyze(target, c.getEntityTwo()) != this.type)
                    continue;
                alignment.add(c);
            }
        }
        return alignment;
    }
    
    
}
