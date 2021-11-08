package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import java.util.List;
import java.util.Set;

/**
 * Helper class for test {@link MultiSourceDispatcherIncrementalMergeTest}
 */
public class MatcherFixedMergeTree extends MultiSourceDispatcherIncrementalMerge {
    
    private int[][] mergeTree;
    
    public MatcherFixedMergeTree(Object oneToOneMatcher, int[][] mergeTree) {
        super(oneToOneMatcher);
        this.mergeTree = mergeTree;
    }
    
    public MatcherFixedMergeTree(int[][] mergeTree) {
        this(new NoOpIMatcher(), mergeTree);
    }
    
    public MatcherFixedMergeTree() {
        this(new int[][]{
            {0,1},
            {3,2}
        });
    }

    @Override
    public int[][] getMergeTree(List<Set<Object>> models, Object parameters) {
        return this.mergeTree;
    }
}