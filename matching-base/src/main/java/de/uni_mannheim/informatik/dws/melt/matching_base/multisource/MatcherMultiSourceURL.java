package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

import java.net.URL;
import java.util.List;


/**
 * Multi source matcher which expects URLs as parameters. Better do not use this class but implement the interface {@link IMatcherMultiSource}.
 * Subclasses of this class also try to implement this interface.
 */
public abstract class MatcherMultiSourceURL {

    /**
     * Matches multiple ontologies/knowledge graphs together.
     * @param models the ontologies/knowledge graphs as URLs
     * @param inputAlignment the input alignment as URL (<a href="https://moex.gitlabpages.inria.fr/alignapi/format.html">alignment API format</a>)
     * @param parameters the parameters file url. Format are currently json or yaml.
     * @return an alignment as URL (most often as file URL) the format is again the <a href="https://moex.gitlabpages.inria.fr/alignapi/format.html">alignment API format</a>.
     * @throws Exception in case something went wrong
     */
    public abstract URL match(List<URL> models, URL inputAlignment, URL parameters) throws Exception;
    
    /**
     * Returns a boolean value if the matcher needs a transitive closure for evaluation.
     * E.g. some matchers match only A-B-C and the testcase asks for A-C then this is only true,
     * if the transitive closure is computed
     * @return true if the transitive closure is need, false otherwise
     */
    public boolean needsTransitiveClosureForEvaluation() {
        return false;
    }
    
}