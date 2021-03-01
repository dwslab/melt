package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

/**
 * Small utilities for dataset id extraction.
 */
public class DatasetIDHelper {

    /**
     * Will take a url and return the host of it.
     * @param url the URL as a string
     * @return the host as string
     */
    public static String getHost(String url) {
        if (url == null || url.length() == 0) {
            return "";
        }
        int doubleslash = url.indexOf("//");
        if (doubleslash == -1) {
            doubleslash = url.indexOf(":");
            if (doubleslash == -1) 
                doubleslash = 0;
            else
                doubleslash += 1;
        } else {
            doubleslash += 2;
        }

        int end = url.indexOf('/', doubleslash);
        end = end >= 0 ? end : url.length();

        int port = url.indexOf(':', doubleslash);
        end = (port > 0 && port < end) ? port : end;
        return url.substring(doubleslash, end);
    }
}
