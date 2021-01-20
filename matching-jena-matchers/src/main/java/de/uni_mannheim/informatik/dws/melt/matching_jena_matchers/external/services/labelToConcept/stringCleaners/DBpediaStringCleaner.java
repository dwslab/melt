package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringCleaners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBpediaStringCleaner implements StringCleaner{

    static Logger LOG = LoggerFactory.getLogger(DBpediaStringCleaner.class);


    /**
     * This method removes illegal characters of a string when used in a SPARQL query.
     * @param stringToClean String to be processed.
     * @return Cleaned String.
     */
    @Override
    public String cleanString(String stringToClean) {
        String outputString = stringToClean;

        // illegal characters
        outputString = outputString.replace("<", "");
        outputString = outputString.replace(">", "");
        outputString = outputString.replace("|", "");
        outputString = outputString.replace("\"", "");
        //outputString = outputString.replace("/", "");

        // space replacement
        outputString = outputString.replace(" ", "_");
        LOG.info("String " + stringToClean + " cleaned to " + outputString);

        return outputString;
    }

}
