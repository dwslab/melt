package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;

/**
 * Extracts the dataset id from a whole model based on sampling some resources.
 */
public class DatasetIDExtractorHelperJena {
    
    /**
     * Extracts the dataset id by iterating over all resources and return the most appearing dataset id.
     * @param model the model
     * @param extractor the id extractor to use
     * @return the most appearing dataset id.
     */
    public static String getDatasetIDFromModel(Model model, DatasetIDExtractor extractor){
        Counter<String> counter = new Counter<>();
        ResIterator i = model.listSubjects();
        while(i.hasNext()){
            Resource r = i.next();
            if(r.isURIResource() == false)
                continue;
            String id = extractor.getDatasetID(r.getURI());
            if(id != null)
                counter.add(id);
        }
        return counter.mostCommonElement();
    }    
    
     /**
     * Extracts the dataset id by sample 20 resources and return the most appearing dataset id.
     * @param model the model
     * @param extractor the id extractor to use
     * @return the most appearing dataset id.
     */
    public static String getDatasetIDFromModelbySampling(Model model, DatasetIDExtractor extractor){
        return getDatasetIDFromModelbySampling(model, extractor, 20);
    }
    
    /**
     * Extracts the dataset id by sample resources and return the most appearing dataset id.
     * @param model the model
     * @param extractor the id extractor to use
     * @param numSamples the number of samples to use
     * @return the most appearing dataset id.
     */
    public static String getDatasetIDFromModelbySampling(Model model, DatasetIDExtractor extractor, int numSamples){
        Counter<String> counter = new Counter<>();
        ResIterator i = model.listSubjects();
        while(i.hasNext() && counter.getCount() < numSamples){
            Resource r = i.next();
            if(r.isURIResource() == false)
                continue;
            String id = extractor.getDatasetID(r.getURI());
            if(id != null)
                counter.add(id);
        }
        return counter.mostCommonElement();
    }
    
    
    /**
     * Extracts the dataset id by sample resources and return the most appearing dataset id.
     * @param model the model
     * @param extractor the id extractor to use
     * @param numSamples the number of samples to use
     * @param excludedDatasetIDs dataset ids to exclude during counting
     * @return the most appearing dataset id.
     */
    public static String getDatasetIDFromModelbySampling(Model model, DatasetIDExtractor extractor, int numSamples, Set<String> excludedDatasetIDs){
        Counter<String> counter = new Counter<>();
        ResIterator i = model.listSubjects();
        while(i.hasNext() && counter.getCount() < numSamples){
            Resource r = i.next();
            if(r.isURIResource() == false)
                continue;
            String id = extractor.getDatasetID(r.getURI());
            if(id != null && excludedDatasetIDs.contains(id) == false)
                counter.add(id);
        }
        return counter.mostCommonElement();
    }
}
