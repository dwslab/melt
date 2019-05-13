package de.uni_mannheim.informatik.dws.ontmatching.demomatcher;

import de.uni_mannheim.informatik.dws.ontmatching.matchingbase.MatcherURL;
import java.net.URL;

public class MatcherUrlExample extends MatcherURL{

    @Override
    public URL match(URL source, URL target, URL inputAlignment) throws Exception {
        //TODO: read the source and target URL and produce an alignment in alignment format ( http://alignapi.gforge.inria.fr/format.html )
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

