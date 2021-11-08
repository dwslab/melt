package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher;
import de.uni_mannheim.informatik.dws.melt.matching_base.multisource.DatasetIDExtractor;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.util.iterator.ExtendedIterator;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Matcher which stores the dataset id distribution for each call of the match function.
 * This is only for testing purposes.
 */
public class SaveOrderMatcherForTest implements IMatcher<OntModel, Alignment, Object> {
    private List<Map.Entry<Counter<String>,Counter<String>>> order;
    private DatasetIDExtractor extractor;
    
    public SaveOrderMatcherForTest(DatasetIDExtractor extractor){
        this.order = new ArrayList<>();
        this.extractor = extractor;
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Object parameters) throws Exception {
        this.order.add(new AbstractMap.SimpleEntry<>(
                getDatasetIdDistribution(source),
                getDatasetIdDistribution(target)
        ));        
        return inputAlignment;
    }
    private Counter<String> getDatasetIdDistribution(OntModel m){
        Counter<String> counter = new Counter<>();
        ExtendedIterator<OntClass> i = m.listClasses();
        while(i.hasNext()){
            String uri = i.next().getURI();
            if(uri != null)
                counter.add(extractor.getDatasetID(uri));
        }
        return counter;
    }
    
    public void clear(){
        this.order.clear();
    }
    
    public void assertOrder(int step, Counter<String> one, Counter<String> two){
        Counter<String> source = order.get(step).getKey();
        Counter<String> target = order.get(step).getValue();
        assertTrue((source.equals(one) && target.equals(two)) || (source.equals(two) && target.equals(one)));
    }
    
    public int numberOfMatches(){
        return order.size();
    }
}
