package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringCleaners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlodStringCleaner implements StringCleaner{

    static Logger LOG = LoggerFactory.getLogger(AlodStringCleaner.class);


    /**
     * This method removes illegal characters of a string when used in a SPARQL query.
     * @param stringToClean The String that shall be cleaned.
     * @return The cleaned String.
     */
    @Override
    public String cleanString(String stringToClean) {
        String outputString = stringToClean;

        // irregular character replacement
        outputString = outputString.replace("\"", "");
        outputString = outputString.replace(":", "");
        outputString = outputString.replace("{", "");
        outputString = outputString.replace("}", "");
        outputString = outputString.replace("-", " ");
        outputString = outputString.replace("\\", "\\\\");
        outputString = outputString.replace("\n", " ");

        if(stringToClean.length() < outputString.length()) {
            LOG.debug("String " + stringToClean + " cleaned to " + outputString);
        }
        return outputString;
    }
}
