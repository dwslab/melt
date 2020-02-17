package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import java.util.HashMap;

/**
 * Data structure storing further information about an ontology.
 *
 * @author Sven Hertling
 */
public class OntoInfo {
    
    protected String formalism;
    protected String formalismURI;
    protected String ontoID;
    protected String ontoLocation;
    protected HashMap<String, String> extensions;


    public OntoInfo() {
        this.formalism = "";
        this.formalismURI = "";
        this.ontoID = "";
        this.ontoLocation = "";
    }
    
    public OntoInfo(String ontoID, String ontoLocation) {
        this.formalism = "";
        this.formalismURI = "";
        this.ontoID = ontoID;
        this.ontoLocation = ontoLocation;
    }
    
    public OntoInfo(OntoInfo oi) {
        this.formalism = oi.formalism;
        this.formalismURI = oi.formalismURI;
        this.ontoID = oi.ontoID;
        this.ontoLocation = oi.ontoLocation;
    }

    public String getFormalism() {
        return formalism;
    }

    public void setFormalism(String formalism) {
        this.formalism = formalism;
    }

    public String getFormalismURI() {
        return formalismURI;
    }

    public void setFormalismURI(String formalismURI) {
        this.formalismURI = formalismURI;
    }

    public String getOntoID() {
        return ontoID;
    }

    public void setOntoID(String ontoID) {
        this.ontoID = ontoID;
    }

    public String getOntoLocation() {
        return ontoLocation;
    }

    public void setOntoLocation(String ontoLocation) {
        this.ontoLocation = ontoLocation;
    }

    /**
     * Obtain the value of an extension.
     * @param extensionUri The URI identifying the extension.
     * @return The value of the extension as String, null if there is no value.
     */
    public String getExtensionValue(String extensionUri){
        if(extensions == null) return null;
        return extensions.get(extensionUri);
    }

    /**
     * Set the value for an extension.
     * @param extensionUri The URI identifying the extension.
     * @param extensionValue The value of the extension to be set.
     */
    public void addExtensionValue(String extensionUri, String extensionValue){
        if(extensions == null) extensions = new HashMap<>();
        extensions.put(extensionUri, extensionValue);
    }

    public HashMap<String, String> getExtensions() { return this.extensions; }

}
