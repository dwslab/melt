package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.knowledge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Universal language enum for all background knowledge data sets.
 */
public enum Language {

    ARABIC, CHINESE, CZECH, DUTCH, ENGLISH, FRENCH, GERMAN, ITALIAN, PORTUGESE, RUSSIAN, SPANISH, UNKNOWN;

    private static Logger LOGGER = LoggerFactory.getLogger(Language.class);


    /**
     * Assumed ISO 609-1 codes (https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes).
     * @return Character two representation of the language.
     */
    public String toSparqlChar2(){
        switch(this) {
            case ARABIC:
                return "ar";
            case CHINESE:
                return "zh";
            case CZECH:
                return "cs";
            case DUTCH:
                return "nl";
            case ENGLISH:
                return "en";
            case FRENCH:
                return "fr";
            case GERMAN:
                return "de";
            case ITALIAN:
                return "it";
            case PORTUGESE:
                return "pt";
            case RUSSIAN:
                return "ru";
            case SPANISH:
                return "es";
            default:
                return "[ ]";
        }
    }

    /**
     * Transform the language to the char3 format that dbnary is using.
     * @return The char3 language.
     */
    public String toWiktionaryChar3(){
        switch(this) {
            case ARABIC:
                return "ara";
            case CHINESE:
                return "cmn"; // Wiktionary requires "cmn"; also common: "zho", "chi"
            case CZECH:
                return "ces"; // also "ces", "cze"
            case DUTCH:
                return "nld"; // also "dut"
            case ENGLISH:
                return "eng";
            case FRENCH:
                return "fra"; // also "fre"
            case GERMAN:
                return "deu"; // also "ger"
            case ITALIAN:
                return "ita";
            case PORTUGESE:
                return "por"; // Wiktionary requires "por"; also common: "ptr"
            case RUSSIAN:
                return "rus";
            case SPANISH:
                return "spa"; // Wiktionary requires "spa"; also common: "esp"
            default:
                return "[ ]";
        }
    }

    /**
     * Transform the language to the char2 format that dbnary is using.
     * @return The char2 language.
     */
    public String toWiktionaryLanguageTag(){ // eigentlich: to language annotation tag
        switch(this) {
            case ARABIC:
                return "ar";
            case CHINESE:
                return "cn";
            case CZECH:
                return "cs"; // cz does not work for translations
            case DUTCH:
                return "nl";
            case ENGLISH:
                return "en";
            case FRENCH:
                return "fr";
            case GERMAN:
                return "de";
            case ITALIAN:
                return "it";
            case PORTUGESE:
                return "pt";
            case RUSSIAN:
                return "ru";
            case SPANISH:
                return "es";
            default:
                return "[]";
        }
    }

    /**
     * Infer the correct enum instance given a char 2 language String.
     * @param language Language String.
     * @return Enum instance.
     */
    public static Language inferLanguageChar2(String language){
        language = language.toLowerCase();
        switch(language){
            case "ar":
                return ARABIC;
            case "cn":
            case "zh":
                return CHINESE;
            case "cz":
            case "cs": // seems also to be czech
                return CZECH;
            case "nl":
                return DUTCH;
            case "en":
                return ENGLISH;
            case "fr":
                return FRENCH;
            case "de":
                return GERMAN;
            case "it":
                return ITALIAN;
            case "pt":
                return PORTUGESE;
            case "ru":
                return RUSSIAN;
            case "es":
                return SPANISH;
            case "":
                return UNKNOWN;
            default:
                LOGGER.warn("Language " + language + " not found.");
                return UNKNOWN;
        }
    }

}


