package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smile.math.MathEx;
import smile.nlp.dictionary.EnglishPunctuations;
import smile.nlp.dictionary.EnglishStopWords;
import smile.nlp.normalizer.SimpleNormalizer;
import smile.nlp.stemmer.PorterStemmer;
import smile.nlp.tokenizer.SimpleSentenceSplitter;
import smile.nlp.tokenizer.SimpleTokenizer;

/**
 * Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 * This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 * The order how they are merged is defined by subclasses.
 */
public class MultiSourceDispatcherIncrementalMergeByClusterText extends MultiSourceDispatcherIncrementalMergeByCluster{
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSourceDispatcherIncrementalMergeByClusterText.class);
    
    
    private double mindf;
    private double maxdf;
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Object oneToOneMatcher, ClusterLinkage linkage, double mindf, double maxdf) {
        super(oneToOneMatcher, linkage);
        this.mindf = mindf;
        this.maxdf = maxdf;
    }
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Object oneToOneMatcher, ClusterLinkage linkage) {
        this(oneToOneMatcher, linkage, 0.0, 1.0);
    }
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Object oneToOneMatcher) {
        this(oneToOneMatcher, ClusterLinkage.SINGLE, 0.0, 1.0);
    }
    
    @Override
    public double[][] getClusterFeatures(List<Set<Object>> models, Object parameters){
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        
        LOGGER.info("Compute BOW for each KG.");
        Counter<String> documentFrequency = new Counter<>();
        List<Counter<String>> documents = new ArrayList<>(models.size());
        for(int i=0; i < models.size(); i++){
            try{
                Model m = (Model)TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(models.get(i), OntModel.class, p);
                if(m == null){
                    LOGGER.warn("Initial model is null. Can't compute the similarities between the ontologies/knowledge graphs.");
                    return new double[0][0];
                }
                Counter<String> bow = getBagOfWords(m);

                documents.add(bow);
                documentFrequency.addAll(bow.getDistinctElements());
            }catch(TypeTransformationException ex){
                LOGGER.warn("Conversion to OntModel/Model did not work. Can't compute the similarities between the ontologies/knowledge graphs.", ex);
                return new double[0][0];
            }
            if(i % 500 == 0)
                LOGGER.info("Computing BOW {}/{}", i, models.size());
        }
        //create features
        
        Set<String> selectedWords;
        if(this.mindf == 0.0 && this.maxdf == 1.0){
            //use all
            selectedWords = documentFrequency.getDistinctElements();
            LOGGER.info("Select all {} words as feature", selectedWords.size());
        }else{
            selectedWords = documentFrequency.betweenFrequencyReturningElements(this.mindf, this.maxdf);
            LOGGER.info("Select words between frequency {} and {} which are: {} words as feature", this.mindf, this.maxdf, selectedWords.size());
        }
        if(selectedWords.isEmpty()){
            selectedWords = documentFrequency.getDistinctElements();
            LOGGER.info("The selection of words results in no features. Use all words as features (backup version). This results in {} words/features.", selectedWords.size());
        }
        
        String[] features = selectedWords.toArray(new String[0]);
        //tf-idf
        LOGGER.info("Compute TF-IDF vectore for each KG.");
        long n = documents.size();
        int[] featuresDocumentFrequency = Arrays.stream(features).mapToInt(f->documentFrequency.getCount(f)).toArray();
        double[][] data = documents.stream().map(bag -> {
                double[] featureVector = new double[features.length];
                for (int i = 0; i < featureVector.length; i++)
                    featureVector[i] = bag.getCount(features[i]);
                double maxtf = MathEx.max(featureVector);
                double[] x = new double[featureVector.length];
                if(maxtf != 0){
                    for (int i = 0; i < x.length; i++) {
                        x[i] = (featureVector[i] / maxtf) * Math.log((1.0 + n) / (1.0 + featuresDocumentFrequency[i]));
                    }
                    MathEx.unitize(x);
                }
                return x;
        }).toArray(double[][]::new);
        LOGGER.info("Finished computing TF-IDF vector for each KG.");
        return data;
    }
    
    
    public Counter<String> getBagOfWords(Model m){
        SimpleTokenizer tokenizer = new SimpleTokenizer(true);
        PorterStemmer porter = new PorterStemmer();
        Counter<String> bow = new Counter<>();
        StmtIterator i = m.listStatements();
        while(i.hasNext()){
            RDFNode n = i.next().getObject();
            if(n.isLiteral()){
                Literal lit = n.asLiteral();
                if(isLiteralAString(lit)){
                    String text = lit.getLexicalForm();
                    
                    String[] sentences = SimpleSentenceSplitter.getInstance().split(SimpleNormalizer.getInstance().normalize(text));
                    Iterator<String> words = Arrays.stream(sentences)
                            .flatMap(s -> Arrays.stream(tokenizer.split(s)))
                            .filter(w -> !(EnglishStopWords.DEFAULT.contains(w.toLowerCase()) || EnglishPunctuations.getInstance().contains(w)))
                            .map(porter::stem)
                            .map(String::toLowerCase)
                            .iterator();
                    bow.addAll(words);
                }
            }
        }
        
        //add also URI fragments of subjects if they exist.
        ResIterator r = m.listSubjects();
        while(r.hasNext()){
            Resource resource = r.next();
            String resURI = resource.getURI();
            if(resURI == null){
                continue;
            }
            String fragment = URIUtil.getUriFragment(resURI);
            if(StringUtils.isBlank(fragment)){
                continue;
            }
            fragment = splitFragment(fragment);
            Iterator<String> words = Arrays.stream(tokenizer.split(fragment))
                    .filter(w -> !(EnglishStopWords.DEFAULT.contains(w.toLowerCase()) || EnglishPunctuations.getInstance().contains(w)))
                    .map(porter::stem)
                    .map(String::toLowerCase)
                    .iterator();
            bow.addAll(words);
        }
        
        return bow;
    }
    private static final Pattern URI_SEPARATOR = Pattern.compile("[-_~|]");
    private static final Pattern CAMEL_CASE_SPLIT = Pattern.compile("(?<!^)(?<!\\s)(?=[A-Z][a-z])");

    private static String splitFragment(String text) {
        String s = CAMEL_CASE_SPLIT.matcher(text).replaceAll(" ");
        return URI_SEPARATOR.matcher(s).replaceAll(" ");
    }
    
    private static final String NEWLINE = System.getProperty("line.separator");
    private void writeTextualRepresentationOfModel(Model m, File f){
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            StmtIterator i = m.listStatements();
            while(i.hasNext()){
                RDFNode n = i.next().getObject();
                if(n.isLiteral()){
                    Literal lit = n.asLiteral();
                    if(isLiteralAString(lit)){
                        String text = lit.getLexicalForm().trim();
                        if(!text.isEmpty()){
                            bw.write(text);
                            bw.write(NEWLINE);
                        } 
                    }
                }
            }            
        } catch (IOException ex) {
            LOGGER.error("Could not write the textual representation of a model.", ex);
        }
    }
    
    private static boolean isLiteralAString(Literal lit){
        String dtStr = lit.getDatatypeURI() ;
        if (dtStr != null){
            if(dtStr.equals(XSDDatatype.XSDstring.getURI()))
                return true;
            if(dtStr.equals(RDF.dtLangString.getURI()))
                return true;
        }
        //datatype == null -> check for language tag
        String lang = lit.getLanguage();
        if ( lang != null  && ! lang.equals(""))
            return true;
        return false;
    }
}