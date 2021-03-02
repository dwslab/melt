package de.uni_mannheim.informatik.dws.melt.matching_base.multisource;

import java.util.function.Function;

/**
 * Extracts the dataset id given a URL pattern which is currently a prefix and infix.
 * All information between prefix (which has to be at the beginning of the string/url) and postfix are extracted as a dataset ID.
 */
public class DatasetIDExtractorUrlPattern implements DatasetIDExtractor {
    
    protected String prefix;
    protected int prefixLength;
    protected String infix;
    protected Function<String,String> postProcessing;
    
    public DatasetIDExtractorUrlPattern(String prefix, String infix, Function<String,String> postProcessing){
        this.prefix = prefix;
        this.prefixLength = prefix.length();
        this.infix = infix;
        this.postProcessing = postProcessing;
    }
    
    
    @Override
    public String getDatasetID(String uri) {
        if(uri.startsWith(this.prefix)){
            int infixIndex = uri.indexOf(this.infix, this.prefixLength);
            if(infixIndex > 0){
                return postProcessing.apply(uri.substring(this.prefixLength, infixIndex));
            }
        }
        return postProcessing.apply(DatasetIDHelper.getHost(uri)); // "default" - better get host of uri which often represents the dataset
    }
}
