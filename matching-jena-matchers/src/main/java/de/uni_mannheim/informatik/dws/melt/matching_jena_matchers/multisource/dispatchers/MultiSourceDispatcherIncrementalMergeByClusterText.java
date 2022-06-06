package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers;

import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.multisource.dispatchers.clustermerge.ClusterLinkage;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformationException;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation.JenaTransformerHelper;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.URIUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
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
    private boolean debug;
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Object oneToOneMatcher, ClusterLinkage linkage, double mindf, double maxdf) {
        super(oneToOneMatcher, linkage);
        checkDocumentFrequency(mindf);
        this.mindf = mindf;
        checkDocumentFrequency(maxdf);
        this.maxdf = maxdf;
        this.debug = false;
    }
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Object oneToOneMatcher, ClusterLinkage linkage) {
        this(oneToOneMatcher, linkage, 0.0, 1.0);
    }
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Object oneToOneMatcher) {
        this(oneToOneMatcher, ClusterLinkage.SINGLE, 0.0, 1.0);
    }
    
    //with supplier instead of matcher object 
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Supplier<Object> matcherSupplier, ClusterLinkage linkage, double mindf, double maxdf) {
        super(matcherSupplier, linkage);
        checkDocumentFrequency(mindf);
        this.mindf = mindf;
        checkDocumentFrequency(maxdf);
        this.maxdf = maxdf;
        this.debug = false;
    }
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Supplier<Object> matcherSupplier, ClusterLinkage linkage) {
        this(matcherSupplier, linkage, 0.0, 1.0);
    }
    
    public MultiSourceDispatcherIncrementalMergeByClusterText(Supplier<Object> matcherSupplier) {
        this(matcherSupplier, ClusterLinkage.SINGLE, 0.0, 1.0);
    }
    
    @Override
    public double[][] getClusterFeatures(List<Set<Object>> models, Object parameters){
        Properties p = TypeTransformerRegistry.getTransformedPropertiesOrNewInstance(parameters);
        
        LOGGER.info("Compute BOW for each KG.");
        Counter<String> documentFrequency = new Counter<>();
        List<Counter<String>> documents = new ArrayList<>(models.size());
        for(int i=0; i < models.size(); i++){
            try{
                Set<Object> modelRepresentations = models.get(i);
                LOGGER.debug("Computing BOW {}/{} for {}", i, models.size(), JenaTransformerHelper.getModelRepresentation(modelRepresentations));
                //LOGGER.debug("Load Model");
                Model m = (Model)TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(modelRepresentations, OntModel.class, p);
                if(m == null){
                    LOGGER.warn("Initial model is null. Can't compute the similarities between the ontologies/knowledge graphs.");
                    return new double[0][0];
                }
                //LOGGER.debug("Compute BOW");
                Counter<String> bow = getBagOfWords(m);

                documents.add(bow);
                documentFrequency.addAll(bow.getDistinctElements());
                if(this.isRemoveUnusedJenaModels()){
                    MergeExecutor.removeOntModelFromSet(modelRepresentations);
                }
            }catch(TypeTransformationException ex){
                LOGGER.warn("Conversion to OntModel/Model did not work. Can't compute the similarities between the ontologies/knowledge graphs.", ex);
                return new double[0][0];
            }
            if(i % 500 == 0)
                LOGGER.info("Computing BOW {}/{}", i, models.size());
        }
        //create features
        
        if(this.debug){
            documentFrequency.toJson(new File("documentFrequency.json"));
        }
        
        Set<String> selectedWords;
        if(this.mindf == 0.0 && this.maxdf == 1.0){
            //use all
            selectedWords = documentFrequency.getDistinctElements();
            LOGGER.info("Select all {} words as feature", selectedWords.size());
        }else{
            selectedWords = documentFrequency.betweenFrequencyRelativeToTotalReturningElements(this.mindf, this.maxdf, models.size());
            LOGGER.info("Select words between frequency {} and {} which are: {} words as feature", this.mindf, this.maxdf, selectedWords.size());
        }
        if(selectedWords.isEmpty()){
            selectedWords = documentFrequency.getDistinctElements();
            LOGGER.info("The selection of words results in no features. Use all words as features (backup version). This results in {} words/features.", selectedWords.size());
        }
        
        String[] features = selectedWords.toArray(new String[0]);
        //tf-idf
        LOGGER.info("Compute TF-IDF vector for each KG.");
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
    
    
    private static BreakIterator SENTENCE_SPLITTER = BreakIterator.getSentenceInstance(Locale.US);
    private static SimpleNormalizer NORMALIZER = SimpleNormalizer.getInstance();
    public Counter<String> getBagOfWords(Model m){
        SimpleTokenizer tokenizer = new SimpleTokenizer(true);
        PorterStemmer porter = new PorterStemmer();
        Counter<String> bow = new Counter<>();
        //LOGGER.debug("List statements of model");
        StmtIterator i = m.listStatements();
        //int counter = 0;
        while(i.hasNext()){
            //LOGGER.debug("Iterate over statement {}", counter++);
            RDFNode n = i.next().getObject();
            //LOGGER.debug("Object of statement is {}", n);
            if(n.isLiteral()){
                Literal lit = n.asLiteral();
                if(isLiteralAString(lit)){
                    String text = lit.getLexicalForm();
                    
                    String source = NORMALIZER.normalize(text);
                    SENTENCE_SPLITTER.setText(source);
                    int start = SENTENCE_SPLITTER.first();
                    for (int end = SENTENCE_SPLITTER.next(); end != java.text.BreakIterator.DONE; start = end, end = SENTENCE_SPLITTER.next()) {
                        Iterator<String> words = Arrays.stream(tokenizer.split(source.substring(start,end)))
                            .filter(w -> !(EnglishStopWords.DEFAULT.contains(w.toLowerCase()) || EnglishPunctuations.getInstance().contains(w)))
                            .map(porter::stem)
                            .map(String::toLowerCase)
                            .iterator();
                        bow.addAll(words);
                    }
                    /*
                    String[] sentences = SimpleSentenceSplitter.getInstance().split(NORMALIZER.normalize(text));
                    Iterator<String> words = Arrays.stream(sentences)
                            .flatMap(s -> Arrays.stream(tokenizer.split(s)))
                            .filter(w -> !(EnglishStopWords.DEFAULT.contains(w.toLowerCase()) || EnglishPunctuations.getInstance().contains(w)))
                            .map(porter::stem)
                            .map(String::toLowerCase)
                            .iterator();
                    bow.addAll(words);
                    */
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

    /**
     * Returns the minimum document frequency (relative) a token needs to have, to be included as a feature.
     * Default is 0.0 (to include all tokens).
     * @return the relative minimumg document frequency. 
     */
    public double getMindf() {
        return mindf;
    }

    /**
     * Sets the minimum document frequency (relative) a token needs to have, to be included as a feature.
     * Default is 0.0 (to include all tokens).
     * @param mindf the minimum document frequency (relative). This needs to be between 0.0 and 1.0.
     */
    public void setMindf(double mindf) {
        checkDocumentFrequency(mindf);
        this.mindf = mindf;
    }

    /**
     * Returns the maximum document frequency (relative) a token needs to have, to be included as a feature.
     * Default is 1.0 (to include all tokens).
     * @return the relative maximum document frequency.
     */
    public double getMaxdf() {
        return maxdf;
    }

    /**
     * Sets the maximum document frequency (relative) a token needs to have, to be included as a feature.
     * Default is 1.0 (to include all tokens).
     * @param maxdf the maximum document frequency (relative). This needs to be between 0.0 and 1.0.
     */
    public void setMaxdf(double maxdf) {
        checkDocumentFrequency(maxdf);
        this.maxdf = maxdf;
    }

    /**
     * Returns true, if debug files are written.
     * Default is false.
     * @return true, if debug files are written
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * If set to true, write some file which contains helpful information e.g. documentFrequency.json file which contains all information about
     * all words and their document frequency.
     * Default is false.
     * @param debug if true, write debug files
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    private void checkDocumentFrequency(double df){
        if(df > 1.0 || df < 0.0){
            throw new IllegalArgumentException("Document frequency needs to be between 0.0 and 1.0 but was " + mindf);
        }
    }
    
}