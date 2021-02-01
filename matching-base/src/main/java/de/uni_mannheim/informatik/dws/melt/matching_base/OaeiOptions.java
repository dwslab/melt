package de.uni_mannheim.informatik.dws.melt.matching_base;

import java.util.HashSet;
import java.util.Set;

/**
 * Class which holds options for OAEI evaluation (usually set in HOBBIT wrapper).
 * @deprecated use parameters file instead.
 */
public class OaeiOptions {
    
    private static String format = "";
    private static String sourceName = "";
    private static String targetName = "";
    private static boolean matchingClassesRequired = true;
    private static boolean matchingDataPropertiesRequired = true;
    private static boolean matchingObjectPropertiesRequired = true;
    private static boolean matchingInstancesRequired = true;
    private static Set<String> allowedInstanceTypes = new HashSet<>();
    
    public static void resetToDefault(){
        format = "";
        sourceName = "";
        targetName = "";
        matchingClassesRequired = true;
        matchingDataPropertiesRequired = true;
        matchingObjectPropertiesRequired = true;
        matchingInstancesRequired = true;
        allowedInstanceTypes = new HashSet<>();
    }

    public static void setFormat(String aFormat) {
        format = aFormat;
    }

    public static void setSourceName(String aSourceName) {
        sourceName = aSourceName;
    }

    public static void setTargetName(String aTargetName) {
        targetName = aTargetName;
    }

    public static void setMatchingClassesRequired(boolean aMatchingClassesRequired) {
        matchingClassesRequired = aMatchingClassesRequired;
    }

    public static void setMatchingDataPropertiesRequired(boolean aMatchingDataPropertiesRequired) {
        matchingDataPropertiesRequired = aMatchingDataPropertiesRequired;
    }

    public static void setMatchingObjectPropertiesRequired(boolean aMatchingObjectPropertiesRequired) {
        matchingObjectPropertiesRequired = aMatchingObjectPropertiesRequired;
    }

    public static void setMatchingInstancesRequired(boolean aMatchingInstancesRequired) {
        matchingInstancesRequired = aMatchingInstancesRequired;
    }

    public static void setAllowedInstanceTypes(Set<String> aAllowedInstanceTypes) {
        allowedInstanceTypes = aAllowedInstanceTypes;
    }

    public static String getFormat() {
        return format;
    }

    public static String getSourceName() {
        return sourceName;
    }

    public static String getTargetName() {
        return targetName;
    }

    public static boolean isMatchingClassesRequired() {
        return matchingClassesRequired;
    }

    public static boolean isMatchingDataPropertiesRequired() {
        return matchingDataPropertiesRequired;
    }

    public static boolean isMatchingObjectPropertiesRequired() {
        return matchingObjectPropertiesRequired;
    }

    public static boolean isMatchingInstancesRequired() {
        return matchingInstancesRequired;
    }

    public static Set<String> getAllowedInstanceTypes() {
        return allowedInstanceTypes;
    }
    
    public static String toStringStatic(){
        StringBuilder sb = new StringBuilder();;
        sb.append("Format:");sb.append(format);
        sb.append(" Source:");sb.append(sourceName);
        sb.append(" Target:");sb.append(targetName);
        sb.append(" matchingClassesRequired:");sb.append(matchingClassesRequired);
        sb.append(" matchingDataPropertiesRequired:");sb.append(matchingDataPropertiesRequired);
        sb.append(" matchingObjectPropertiesRequired:");sb.append(matchingObjectPropertiesRequired);
        sb.append(" matchingInstancesRequired:");sb.append(matchingInstancesRequired);
        sb.append(" allowedInstanceTypes:");sb.append(allowedInstanceTypes.toString());
        return sb.toString();
    }
}
