package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util;

/**
 * Provides utility functions for URIs such as getting the fragment
 */
public class URIUtil {
    
    /**
     * Return the fragment of the URI which is the part after the last hashtag (#) or last slash(/).
     * If the uri contains no hashtag nor slash it will return the full unmodified uri.
     * @param uri The full uri as string
     * @return fragment
     */
    public static String getUriFragment(String uri){
        int lastIndex = uri.lastIndexOf('#');
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf('/');
        if(lastIndex >= 0){
            return uri.substring(lastIndex + 1);
        }
        return uri;
    }
    
}
