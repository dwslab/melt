package de.uni_mannheim.informatik.dws.melt.demomatcher;

import de.uni_mannheim.informatik.dws.melt.matching_base.MatcherURL;
import java.net.URL;

/**
 * You can also implement a matcher that is not based on YAAA if you want to.
 */
public class MatcherUrlExample extends MatcherURL {

    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        //TODO: read the source and target URL and produce an alignment in alignment format ( http://alignapi.gforge.inria.fr/format.html )
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

