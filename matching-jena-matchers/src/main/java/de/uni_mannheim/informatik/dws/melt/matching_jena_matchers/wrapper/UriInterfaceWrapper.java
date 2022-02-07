package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.wrapper;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAA;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.net.URL;
import java.util.Properties;

/**
 * This matcher implements the URL interface and wrapps a MatcherYAAA.
 * This is useful for ParisMatcher if the input is already a NT file.
 */
public class UriInterfaceWrapper implements IMatcher<URL, Alignment, Properties>{
    private MatcherYAAA matcher;
    
    public UriInterfaceWrapper(MatcherYAAA matcher){
        this.matcher = matcher;
    }
    
    @Override
    public Alignment match(URL source, URL target, Alignment inputAlignment, Properties parameters) throws Exception {
        return matcher.match(source, target, inputAlignment, parameters);
    }
}
