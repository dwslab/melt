package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import java.util.Collection;
import java.util.Map;

/**
 * Interface which gets a track and uris.
 * These URIs should then be partitioned into the source or target of a testcase.
 */
public interface Partitioner {


    Map<TestCase, SourceTargetURIs> partition(Collection<String> uris);
}
