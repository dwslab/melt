package de.uni_mannheim.informatik.dws.melt.matching_eval.matchers;

import de.uni_mannheim.informatik.dws.melt.matching_data.SealsTrack;
import de.uni_mannheim.informatik.dws.melt.matching_data.TestCase;
import de.uni_mannheim.informatik.dws.melt.matching_data.Track;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_jena.MatcherYAAAJena;
import de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.util.Counter;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This matcher will detect the test case given in the input and 
 * use the reference (gold standard) to sample from it with the given rate which is added to the input alignment.
 * This matcher is contained in the eval package because it uses the information from the reference.
 * This matcher should not be included in real matching systems.
 */
public class AddPositivesWithReference extends MatcherYAAAJena{

    private static final Logger LOGGER = LoggerFactory.getLogger(AddPositivesWithReference.class);
    
    private double fraction;
    private int randomSeed;    
    
    private Map<String, CSVRecord> testCaseKeyToTestCase;
    private Map<String, CSVRecord> specificTestCaseKeyToTestCase;
    
    public AddPositivesWithReference(double fraction, int randomSeed) {
        this.fraction = fraction;
        this.randomSeed = randomSeed;
        initializeMapping();
    }

    public AddPositivesWithReference(double fraction) {
        this(fraction, 1234);
    }
    
    public AddPositivesWithReference(){
        this(0.2f);
    }
    
    @Override
    public Alignment match(OntModel source, OntModel target, Alignment inputAlignment, Properties properties) throws Exception {
        
        CSVRecord record = getCSVRecord(source, target);
        if(record == null){
            LOGGER.error("Could not found testcase. Will return a NON modified input alignment.");
            return inputAlignment;
        }        
        TestCase tc = getTestCaseFromCSVRecord(record);
        ArrayList<Correspondence> correspondenceList = new ArrayList<>(tc.getParsedReferenceAlignment());
        correspondenceList.sort(Comparator.comparing(x -> x.getEntityOne() + x.getRelation().toString() + x.getEntityTwo()));
        Collections.shuffle(correspondenceList, new Random(randomSeed));
        int splitPoint = (int) Math.round((double) correspondenceList.size() * fraction);

        //add to input alignment
        inputAlignment.addAll(correspondenceList.subList(0, splitPoint));    
        
        return inputAlignment;
    }
    
    private CSVRecord getCSVRecord(OntModel source, OntModel target){
        Counter<String> srcCounter = getUriCounter(source);
        Counter<String> targetCounter = getUriCounter(target);
        
        CSVRecord record = this.specificTestCaseKeyToTestCase.get(getSpecificKey(srcCounter, targetCounter));
        if(record != null){
            return record;
        }
        //LOGGER.info("Did not find the specific key for the input data. Try out the more generic key.");
        return this.testCaseKeyToTestCase.get(getKey(srcCounter, targetCounter));        
    }
    
    private TestCase getTestCaseFromCSVRecord(CSVRecord record){
        return new SealsTrack(record.get("location"), record.get("name"), record.get("version")).getTestCase(record.get("testcase"));
    }
    
    private static String getSpecificKey(Counter<String> srcCounter, Counter<String> tgtCounter){
        if(srcCounter.isEmpty() && tgtCounter.isEmpty()){
            return "null-null";
        }        
        if(srcCounter.isEmpty()){
            return "null-" + getRepresentation(tgtCounter.mostCommon(1).get(0));
        }            
        if(tgtCounter.isEmpty()){
            return getRepresentation(srcCounter.mostCommon(1).get(0)) + "-null";
        }        
        return getRepresentation(srcCounter.mostCommon(1).get(0)) + "-" + getRepresentation(tgtCounter.mostCommon(1).get(0));
    }
    
    public static String getSpecificKey(OntModel src, OntModel tgt){
        return getSpecificKey(getUriCounter(src), getUriCounter(tgt));
    }
    
    private static String getRepresentation(Entry<String, Integer> entry){
        return entry.getKey() + "(" + entry.getValue() + ")";
    }
    
    
    private static String getKey(Counter<String> srcCounter, Counter<String> tgtCounter){       
        if(srcCounter.isEmpty() && tgtCounter.isEmpty())
            return "null-null";
        if(srcCounter.isEmpty())
            return "null-" + tgtCounter.mostCommonElement();
        if(tgtCounter.isEmpty())
            return srcCounter.mostCommonElement() + "-null";        
        return srcCounter.mostCommonElement() + "-" + tgtCounter.mostCommonElement();
    }
    
    public static String getKey(OntModel src, OntModel tgt){
        return getKey(getUriCounter(src), getUriCounter(tgt));
    }
    
    private static Counter<String> getUriCounter(OntModel m){
        Counter<String> counter = new Counter<>();
        StmtIterator stmts = m.listStatements();
        while(stmts.hasNext()){
            Statement s = stmts.next();
            if(s.getSubject().isURIResource()){
                counter.add(getPath(s.getSubject().getURI()));
            }            
            if(s.getPredicate().isURIResource()){
                counter.add(getPath(s.getPredicate().getURI()));
            }            
            if(s.getObject().isURIResource()){
                counter.add(getPath(s.getObject().asResource().getURI()));
            }
        }
        //remove default ones:
        counter.removeAll("http://www.w3.org/1999/02/22-rdf-syntax-ns#");//rdf
        counter.removeAll("http://www.w3.org/2000/01/rdf-schema#");//rdfs
        counter.removeAll("http://www.w3.org/2002/07/owl#");//owl
        counter.removeAll("http://purl.org/dc/elements/1.1/"); // dc
        counter.removeAll("http://www.w3.org/2001/XMLSchema#");//xml
        counter.removeAll("http://"); // empty URI
        
        //domain ontologies:
        counter.removeAll("http://purl.obolibrary.org/obo/"); // obo
        counter.removeAll("http://www.geneontology.org/formats/oboInOwl#"); // oboinowl
                
        return counter;
    }
    
    private static String getPath(String uri){
        int lastIndex = uri.lastIndexOf('#');
        if(lastIndex >= 0){
            return uri.substring(0, lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf('/');
        if(lastIndex >= 0){
            return uri.substring(0, lastIndex + 1);
        }
        return uri;
    }
    
    private void initializeMapping(){
        this.testCaseKeyToTestCase = new HashMap<>();
        this.specificTestCaseKeyToTestCase = new HashMap<>();
        InputStream inputStream = AddPositivesWithReference.class.getClassLoader().getResourceAsStream("mapping/mapping.csv");
        try(CSVParser csvParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8))){
            for (CSVRecord record : csvParser) {
                this.testCaseKeyToTestCase.put(record.get("key"), record);
                this.specificTestCaseKeyToTestCase.put(record.get("specifickey"), record);
            }
        } catch (IOException ex) {
            LOGGER.error("Could not load csv file. No reference alignment will be added.", ex);
        }
    }
    
    //the above map is generated with:
    
    /*
    public static void main(String[] args) throws Exception{
        LOGGER.info("Start");
        TrackRepository.Largebio.unlimitEntityExpansion(); 
        TrackRepository.setCacheFolder(new File("/ceph/shertlin/oaei_track_cache"));
        
        
        
        Map<String, Track> tracks = new HashMap<>();
        tracks.put("TrackRepository.Anatomy.Default", TrackRepository.Anatomy.Default);
        tracks.put("TrackRepository.Biodiv.V2021", TrackRepository.Biodiv.V2021);
        tracks.put("TrackRepository.CommonKG.NELL_DBPEDIA_V1", TrackRepository.CommonKG.NELL_DBPEDIA_V1);
        tracks.put("TrackRepository.CommonKG.YAGO_WIKIDATA_V1", TrackRepository.CommonKG.YAGO_WIKIDATA_V1);
        tracks.put("TrackRepository.Conference.V1", TrackRepository.Conference.V1);
        tracks.put("TrackRepository.Food.V1", TrackRepository.Food.V1);
        tracks.put("TrackRepository.Knowledgegraph.V4", TrackRepository.Knowledgegraph.V4);
        tracks.put("TrackRepository.Largebio.V2016.ALL", TrackRepository.Largebio.V2016.ALL);
        tracks.put("TrackRepository.Multifarm.ALL_IN_ONE_TRACK", TrackRepository.Multifarm.ALL_IN_ONE_TRACK);
        tracks.put("TrackRepository.Phenotype.V2017.DOID_ORDO", TrackRepository.Phenotype.V2017.DOID_ORDO);
        tracks.put("TrackRepository.Phenotype.V2017.HP_MP", TrackRepository.Phenotype.V2017.HP_MP);
        
        
        Set<String> seenKeys = new HashSet<>();
        Set<String> multipleKeys = new HashSet<>();
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("mapping.txt"), StandardCharsets.UTF_8)){
            for(Map.Entry<String, Track> track : tracks.entrySet()){
                LOGGER.info("compute track {}", track.getKey());
                for(TestCase tc : track.getValue().getTestCases()){
                    Counter<String> src = getUriCounter(tc.getSourceOntology(OntModel.class));
                    Counter<String> tgt = getUriCounter(tc.getTargetOntology(OntModel.class));
                    
                    String key = src.mostCommonElement() + "-" + tgt.mostCommonElement();
                    if(seenKeys.contains(key)){
                        multipleKeys.add(key);
                    }
                    seenKeys.add(key);
                    writer.write("myMap.put(\""+ key +"\", " + track.getKey() + ".getTestCase(\"" + tc.getName() + "\"));");
                    writer.newLine();
                    LOGGER.info("key:{}  track:{} testcase:{}  -> counter src: {}", key, tc.getTrack().getName(), tc.getName(), src);
                    LOGGER.info("key:{}  track:{} testcase:{}  -> counter tgt: {}", key, tc.getTrack().getName(), tc.getName(), tgt);
                    //writer.write(tc.getTrack().getName() + "  ->  " + tc.getName());writer.newLine();
                    //OntModel src = tc.getSourceOntology(OntModel.class);
                    //writer.write("\t src: " +  getUriCounter(src));writer.newLine();
                    //OntModel tgt = tc.getTargetOntology(OntModel.class);
                    //writer.write("\t tgt: " + getUriCounter(tgt));writer.newLine();
                }
                
            }
        }
        LOGGER.info("Multiple keys to check: {}", multipleKeys);
    }
*/
    
    public static void main(String[] args) throws Exception{
        LOGGER.info("Start");
        TrackRepository.Largebio.unlimitEntityExpansion(); 
        TrackRepository.setCacheFolder(new File("/ceph/shertlin/oaei_track_cache"));
        
        List<Track> tracks = new ArrayList<>();
        tracks.add(TrackRepository.Anatomy.Default);
        //tracks.add(TrackRepository.Biodiv.V2021);
        tracks.add(TrackRepository.CommonKG.NELL_DBPEDIA_V1);
        tracks.add(TrackRepository.CommonKG.YAGO_WIKIDATA_V1);
        tracks.add(TrackRepository.Conference.V1);
        tracks.add(TrackRepository.Food.V1);
        tracks.add(TrackRepository.Knowledgegraph.V4);
        tracks.add(TrackRepository.Largebio.V2016.ALL);
        tracks.add(TrackRepository.Multifarm.ALL_IN_ONE_TRACK);
        tracks.add(TrackRepository.Phenotype.V2017.DOID_ORDO);
        tracks.add(TrackRepository.Phenotype.V2017.HP_MP);
        
        
        Set<String> seenKeys = new HashSet<>();
        Set<String> multipleKeys = new HashSet<>();
        Set<String> seenSpecificKeys = new HashSet<>();
        Set<String> multipleSpecificKeys = new HashSet<>();
        try(CSVPrinter csvPrinter = CSVFormat.DEFAULT.withHeader("key", "specifickey", "location", "name", "version", "testcase").print(new File("mapping.csv"), StandardCharsets.UTF_8)){
            for(Track track : tracks){
                if(track instanceof SealsTrack == false){
                    LOGGER.error("track {} is not of type seals and does not work - it will be skipped.", track.getName());
                    continue;
                }
                SealsTrack sealsTrack = (SealsTrack)track;
                for(TestCase tc : track.getTestCases()){
                    Counter<String> src = getUriCounter(tc.getSourceOntology(OntModel.class));
                    Counter<String> tgt = getUriCounter(tc.getTargetOntology(OntModel.class));
                    
                    String key = getKey(src, tgt);
                    if(seenKeys.contains(key)){
                        multipleKeys.add(key);
                    }
                    seenKeys.add(key);
                    
                    String specificKey = getSpecificKey(src, tgt);
                    if(seenSpecificKeys.contains(specificKey)){
                        multipleSpecificKeys.add(specificKey);
                    }
                    seenSpecificKeys.add(specificKey);
                    
                    //writer.write("myMap.put(\""+ key +"\", " + track.getKey() + ".getTestCase(\"" + tc.getName() + "\"));");
                    csvPrinter.printRecord(key, specificKey, sealsTrack.getTdrsLocation(), sealsTrack.getTestDataCollectionName(), sealsTrack.getTestDataVersionNumber(), tc.getName());
                    
                    LOGGER.info("key:{}  track:{} testcase:{}  -> counter src: {}", key, tc.getTrack().getName(), tc.getName(), src);
                    LOGGER.info("key:{}  track:{} testcase:{}  -> counter tgt: {}", key, tc.getTrack().getName(), tc.getName(), tgt);
                }
            }
        }
        LOGGER.info("Multiple keys to check: {}", multipleKeys);
        LOGGER.info("Multiple specific keys to check: {}", multipleSpecificKeys);
    }
}
