package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.metalevel;

import de.uni_mannheim.informatik.dws.melt.matching_yaaa.MatcherYAAA;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.net.URL;
import java.util.Properties;

/**
 * Matcher which does nothing but returning a valid empty alignment.
 */
public class NoOpMatcher extends MatcherYAAA {
    @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties properties) throws Exception {
        return new Alignment();
    }
}
