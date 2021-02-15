
package de.uni_mannheim.informatik.dws.melt.matching_eval.multisource;

import java.util.HashSet;
import java.util.Set;

/**
 * A simple helper class for assigning URIs to testcases (and if it appears as source or target in the testcase).
 * Used for multi source execution.
 */
public class SourceTargetURIs {
    private Set<String> sourceURIs;
    private Set<String> targetURIs;

    public SourceTargetURIs() {
        this.sourceURIs = new HashSet<>();
        this.targetURIs = new HashSet<>();
    }

    public void addSourceURI(String uri){
        this.sourceURIs.add(uri);
    }
    public void addTargetURI(String uri){
        this.targetURIs.add(uri);
    }

    public Set<String> getSourceURIs() {
        return sourceURIs;
    }

    public Set<String> getTargetURIs() {
        return targetURIs;
    }
    
    public void clear(){
        this.sourceURIs.clear();
        this.targetURIs.clear();
    }
    
    public boolean containsSourceAndTarget(){
        if(this.sourceURIs.isEmpty() || this.targetURIs.isEmpty()){
            return false;
        }
        return true;
        //return this.sourceURIs.isEmpty() == false && this.targetURIs.isEmpty() == false
    }
}
