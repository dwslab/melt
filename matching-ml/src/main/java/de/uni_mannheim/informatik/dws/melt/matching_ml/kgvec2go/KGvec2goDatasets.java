package de.uni_mannheim.informatik.dws.melt.matching_ml.kgvec2go;

/**
 * The available datasets on <a href="http://kgvec2go.org">KGvec2go</a>.
 */
public enum KGvec2goDatasets {

    ALOD, DBPEDIA, WORDNET, WIKTIONARY;

    /**
     * Check whether the specified string describes a valid data set.
     * @param datasetString The string to be checked.
     * @return True if valid, else false.
     */
    public static boolean isValidString(String datasetString){
        if(datasetString == null){
            return false;
        }
        datasetString = datasetString.toLowerCase();
        if (datasetString.equals("alod") || datasetString.equals("dbpedia") || datasetString.equals("wordnet") || datasetString.equals("wiktionary")){
            return true;
        }
        return false;
    }

}
