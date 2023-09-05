package de.uni_mannheim.informatik.dws.melt.matching_eval.refinement;

import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResult;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.CorrespondenceRelation;
import java.util.Objects;

/**
 * The relation type refiner refines all execution results in such a way that only the specified reltion type is used.
 */
public class RelationTypeRefiner implements Refiner {

    protected CorrespondenceRelation relation;
    /**
     * If true, the refined results contains only the provided relation.
     * If false the refine result contain all but not the provided relation.
     */
    protected boolean includeRelation; 

    /**
     * Constructor
     * @param relation the relation to filter for
     * @param includeRelation if true, the refined results contains only the provided relation. If false the refine result contain all but not the provided relation.
     */
    public RelationTypeRefiner(CorrespondenceRelation relation, boolean includeRelation) {
        this.relation = relation;
        this.includeRelation = includeRelation;
    }
    
    
    public RelationTypeRefiner(CorrespondenceRelation relation){
        this.relation = relation;
        this.includeRelation = true;
    }
    
    @Override
    public ExecutionResult refine(ExecutionResult toBeRefined) {
        Alignment refinedSystem = refineMapping(toBeRefined.getSystemAlignment());
        Alignment refinedReference = refineMapping(toBeRefined.getReferenceAlignment());
        return new ExecutionResult(toBeRefined, refinedSystem, refinedReference, this);
    }
    
    public Alignment refineMapping(Alignment originalAlignment){
        Alignment refinedAlignment = new Alignment(originalAlignment, false);
        for(Correspondence correspondence : originalAlignment){
            if(correspondence.getRelation().equals(this.relation)){
                refinedAlignment.add(correspondence);
            }
        }
        return refinedAlignment;
    }

    public CorrespondenceRelation getRelation() {
        return relation;
    }

    /**
     * Returns if the relation is included or excluded from the refined result.
     * If true, the refined results contains only the provided relation.
     * If false the refine result contain all but not the provided relation.
     * @return if the relation is included or excluded
     */
    public boolean isIncludeRelation() {
        return includeRelation;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.relation);
        hash = 29 * hash + (this.includeRelation ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RelationTypeRefiner other = (RelationTypeRefiner) obj;
        if (this.includeRelation != other.includeRelation) {
            return false;
        }
        return this.relation == other.relation;
    }

    
    @Override
    public String toString() {
        return "RelationTypeRefiner(" + relation + ",include:" + includeRelation +')';
    }
}
