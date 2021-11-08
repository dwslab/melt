package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;

/**
 *Helper class for test {@link MultiSourceDispatcherIncrementalMergeTest}.
 */
public class NoOpIMatcher implements IMatcher<Object, Object, Object> {
    
    @Override
    public Object match(Object source, Object target, Object inputAlignment, Object parameters) throws Exception {
        return new Alignment();
    }
}