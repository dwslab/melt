package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.external.services.labelToConcept.stringModifiers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CapitalizeFirstLettersModifier implements StringModifier {


    /**
     * Constructor
     * @param delimiter The delimiter that is to be used. Every character after the delimiter (+ the very first character)
     *                  will be upper-cased.
     */
    public CapitalizeFirstLettersModifier(String delimiter){
        this.delimiter = delimiter;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(StringModifier.class);


    private String delimiter;

    @Override
    public String modifyString(String stringToBeModified) {
        if (stringToBeModified.equals("")) return stringToBeModified;
        try {
            Pattern pattern = Pattern.compile("(?<=" + delimiter + ")[a-z]");
            List<Integer> positions = new ArrayList<>();

            Matcher m = pattern.matcher(stringToBeModified);

            while (m.find()) {
                positions.add(m.start());
            }


            char[] charArray = stringToBeModified.toCharArray();

            // upper-case position one
            charArray[0] = Character.toUpperCase(charArray[0]);

            for (int position : positions) {
                charArray[position] = Character.toUpperCase(charArray[position]);
            }

            return new String(charArray);
        } catch (Exception e){
            LOGGER.error("Exception occurred with concept to be modified for linking: " + stringToBeModified + "\nResolution: return original string.");
            return stringToBeModified;
        }
    }

    @Override
    public String getName() {
        return "CapitalizeFirstLettersModifier";
    }
}
