package de.uni_mannheim.informatik.dws.melt.mldataset;

import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCaseType;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.filter.ConceptType;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.StringProcessing;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MLDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MLDataGenerator.class);
    
    private TestCase tc;
    
    private Alignment systemInput;
    private Alignment reference;
    
    public MLDataGenerator(TestCase tc){
        this.tc = tc;
        this.systemInput = new Alignment();
        this.reference = new Alignment();
        doSplitting();
    }
    
    
    private void doSplitting(){
        //we would like to have 20% train, 10% validation, and 70% test
        double trainRatio = 0.2;
        double validationRatio = 0.1;
        double testRatio = 0.7;
        Random rnd = new Random(1234);
        
        Map<String, List<Correspondence>> groups = new HashMap<>();
        for(Correspondence c : this.tc.getParsedReferenceAlignment()){
            String clazz = stratifyFunction(c);
            groups.computeIfAbsent(clazz, __-> new ArrayList<>()).add(c);
        }
        
        for(Entry<String, List<Correspondence>> entry: groups.entrySet()){
            String groupName = entry.getKey();
            List<Correspondence> group = entry.getValue();
            
            Collections.shuffle(group, rnd);
            int splitIndexOne=(int) Math.round(trainRatio*group.size());
            int splitIndexTwo= splitIndexOne + ((int) Math.round(validationRatio*group.size()));
            
            List<Correspondence> train = group.subList(0, splitIndexOne);
            List<Correspondence> val = group.subList(splitIndexOne, splitIndexTwo);
            List<Correspondence> test = group.subList(splitIndexTwo, group.size());
            
            LOGGER.info("Splitted group {} into {} train, {} validation, and {} test", groupName, train.size(), val.size(), test.size());
            
            if(train.isEmpty())
                LOGGER.warn("Training for group {} is empty", groupName);
            if(val.isEmpty())
                LOGGER.warn("Validation for group {} is empty", groupName);
            if(test.isEmpty())
                LOGGER.warn("Test for group {} is empty", groupName);
            
            for(Correspondence c : train){
                Correspondence forSystemInput = new Correspondence(c);
                forSystemInput.addExtensionValue(DefaultExtensions.MeltExtensions.ML_SPLIT, "train");
                systemInput.add(forSystemInput);
            }
            
            for(Correspondence c : val){
                Correspondence forSystemInput = new Correspondence(c);
                forSystemInput.addExtensionValue(DefaultExtensions.MeltExtensions.ML_SPLIT, "val");
                systemInput.add(forSystemInput);
            }
            
            reference.addAll(test);
        }
                        
    }
    
    public void saveToFolder(File folder) throws IOException{
        save(this.tc.getSource().toURL(), TestCaseType.SOURCE, folder);
        save(this.tc.getTarget().toURL(), TestCaseType.TARGET, folder);  

        this.systemInput.serialize(Paths.get(folder.getAbsolutePath(), this.tc.getName(), TestCaseType.INPUT.toFileName()).toFile());
        this.systemInput.serialize(Paths.get(folder.getAbsolutePath(), this.tc.getName(), TestCaseType.EVALUATIONEXCLUSION.toFileName()).toFile());
        this.reference.serialize(Paths.get(folder.getAbsolutePath(), this.tc.getName(), TestCaseType.REFERENCE.toFileName()).toFile());
        
        if(this.tc.getParameters() != null){
            save(this.tc.getParameters().toURL(), TestCaseType.PARAMETERS, folder);
        }
        //TestCaseType.EVALUATIONEXCLUSION
    }
    
    private void save(URL url, TestCaseType type, File folder) throws IOException{
        FileUtils.copyURLToFile(url, Paths.get(folder.getAbsolutePath(), this.tc.getName(), type.toFileName()).toFile());
    }
    
    private String stratifyFunction(Correspondence c){
        
        //stratify according to:
        //  - entity type (class, property, instance)
        //  - relation type
        //  - difficulty (simple string matcher?)
        //      - very simple = exact name
        //      - near simple token overlap
        //      - hard negative
        
        
        OntModel sourceModel = this.tc.getSourceOntology(OntModel.class);
        OntModel targetModel = this.tc.getTargetOntology(OntModel.class);
        ConceptType sourceType = ConceptType.analyze(sourceModel, c.getEntityOne());
        ConceptType targetType = ConceptType.analyze(targetModel, c.getEntityTwo());
        
        
        Resource sourceResource = sourceModel.getResource(c.getEntityOne());
        Resource targetResource = targetModel.getResource(c.getEntityTwo());
        
        String difficulty = getDifficultyLevel(sourceResource, targetResource);
        //LOGGER.info("difficulty: {}", difficulty);
                
        return sourceType.toString() + "_" + c.getRelation().toString() + "_" + targetType.toString() + "_" + difficulty;
    }
    
    
    private String getDifficultyLevel(Resource sourceResource, Resource targetResource){        
        Set<String> sourceLabels = getLabels(sourceResource);
        Set<String> targetLabels = getLabels(targetResource);
        //LOGGER.info("source {} target: {}", sourceLabels, targetLabels);
        sourceLabels.retainAll(targetLabels);
        if(sourceLabels.isEmpty() == false)
            return "EASY";
        
        Set<String> sourceTokens = getTokens(sourceResource);
        Set<String> targetTokens = getTokens(targetResource);
        sourceTokens.retainAll(targetTokens);
        if(sourceTokens.isEmpty() == false)
            return "MEDIUM";
        
        return "HARD";
    }
    
    private Set<String> getTokens(Resource r){
        Set<String> values = new HashSet<>();
        
        if(r.isURIResource() == false) // extract only from uri resources
            return values;
        
        for(Property p : LABEL_LIKE_PROPERTIES){
            StmtIterator i = r.listProperties(p);
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    values.addAll(StringProcessing.normalizeAndRemoveStopwords(n.asLiteral().getLexicalForm()));
                }
            }
        }
        
        String fragment = URIUtil.getUriFragment(r.getURI()).trim();
        if (StringProcessing.containsMostlyNumbers(fragment) == false) {
            values.addAll(StringProcessing.normalizeAndRemoveStopwords(fragment));
        }
        return values;
    }
    
    private Set<String> getLabels(Resource r){
        Set<String> values = new HashSet<>();
        
        if(r.isURIResource() == false) // extract only from uri resources
            return values;
        
        StmtIterator i = r.listProperties(RDFS.label);
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                String processed = normalizeText(n.asLiteral().getLexicalForm());
                if(StringUtils.isBlank(processed) == false)
                    values.add(processed);
            }
        }
        String fragment = URIUtil.getUriFragment(r.getURI());
        if(StringProcessing.containsMostlyNumbers(fragment) == false){
            String processed = normalizeText(fragment);
            if(StringUtils.isBlank(processed) == false)
                values.add(processed);
        }
        return values;
    }

    public String normalizeText(String text) {
        return String.join(" ", StringProcessing.normalize(text));
    }
    
    public static final Set<Property> LABEL_LIKE_PROPERTIES = new HashSet<>(Arrays.asList(
        ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#label"),
        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#prefLabel"),
        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#altLabel"),
        ResourceFactory.createProperty("http://www.w3.org/2004/02/skos/core#hiddenLabel")
    ));
}