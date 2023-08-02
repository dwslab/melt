package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions.SSSOM;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;


/**
 * The SSSOMParser can parse SSSOM files following the convention described in the
 * <a href="https://github.com/mapping-commons/sssom">SSSOM github</a> and <a href="https://w3id.org/sssom/spec">userguide</a>.
 * @author Sven Hertling
 */
public class SSSOMParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSSOMParser.class);
    
    public static Alignment parse(InputStream s) throws IOException {
        
        Alignment a = new Alignment();        
        BufferedReader reader = new BufferedReader(new InputStreamReader(s, StandardCharsets.UTF_8));

        SSSOMPrefixMap prefixMap = new SSSOMPrefixMap();
        if (hasEmbeddedMetadata(reader) ) {
            Map<String, Object> metadata = new Yaml().load(extractMetadata(reader));   
            Object curieMap = metadata.get("curie_map");
            if(curieMap != null)
                parseSSSOMPrefixMap(curieMap, prefixMap);
            
            Object subjectSource = metadata.get("subject_source");
            if(subjectSource != null){
                a.setOnto1(new OntoInfo("", prefixMap.expand(subjectSource.toString())));
            }
            
            Object objectSource = metadata.get("object_source");
            if(objectSource != null){
                a.setOnto2(new OntoInfo("", prefixMap.expand(objectSource.toString())));
            }
            
            for(Entry<String, Object> entry : metadata.entrySet()){
                SSSOM key = DefaultExtensions.SSSOM.fromName(entry.getKey());
                if(key == null){
                    LOGGER.warn("The key \"{}\" in metadata of the SSSOM file is not defined in the schema. "
                            + "It will nevertheless included with the same prefix URL.", entry.getKey());
                    a.addExtensionValue(SSSOM.BASE_URI + entry.getKey(), entry.getValue());
                }else{
                    a.addExtensionValue(key.toString(), processObject(key, entry.getValue(), prefixMap));
                }
            }
        } // else no metadata found and skipping it
        
        try(CSVParser csvParser = CSVFormat.DEFAULT.withDelimiter('\t').withFirstRecordAsHeader().parse(reader)){
            Set<String> usedKeys = new HashSet<>(Arrays.asList("subject_id", "object_id", "predicate_id", "confidence"));
            for (CSVRecord record : csvParser) {
                String source;
                String target;
                try{
                    source = prefixMap.expand(record.get("subject_id"));
                    target = prefixMap.expand(record.get("object_id"));
                } catch(IllegalArgumentException e){
                    LOGGER.warn("SSSOM file does not contain subject_id or object_id for line {} - skipping it", record.getRecordNumber());
                    continue;
                }
                Correspondence correspondence = new Correspondence(source, target);
                
                try{
                    String relID = prefixMap.expand(record.get("predicate_id"));
                    correspondence.setRelation(CorrespondenceRelation.parse(relID));
                }catch(IllegalArgumentException e){
                    LOGGER.warn("SSSOM file does not contain predicate_id for line {} - use unknown as default.", record.getRecordNumber());
                    correspondence.setRelation(CorrespondenceRelation.UNKNOWN);
                }
                
                try{
                    correspondence.setConfidence(Double.parseDouble(record.get("confidence")));
                }catch(IllegalArgumentException e){
                    LOGGER.warn("SSSOM file does not contain confidence for line {} - use 1.0 as default.", record.getRecordNumber());
                }
                                
                for(Entry<String, Integer> entry : csvParser.getHeaderMap().entrySet()){
                    if(usedKeys.contains(entry.getKey())) // skip keys like subject_id which is already added to correspondence.
                        continue;
                    
                    String value = "";
                    try{
                        value = record.get(entry.getValue());
                    }catch(ArrayIndexOutOfBoundsException ex){
                        LOGGER.warn("Line {} do not have the following key {}", record.getRecordNumber(), entry.getKey());
                        continue;
                    }
                    if(value.isEmpty())
                        continue;
                    
                    SSSOM key = DefaultExtensions.SSSOM.fromName(entry.getKey());
                    if(key == null){
                        LOGGER.warn("The key \"{}\" in SSSOM file is not defined in the SSSOM schema. "
                                + "It will nevertheless included with the same prefix URL.", key);                        
                        correspondence.addExtensionValue(SSSOM.BASE_URI + entry.getKey(), value);
                    }else{
                        correspondence.addExtensionValue(key.toString(), processObject(key, value, prefixMap));
                    }
                }
                
                a.add(correspondence);
            }
        }
        return a;
    }
    
    private static Object processObject(SSSOM sssomKey, Object obj, SSSOMPrefixMap prefixMap) {
        if(sssomKey.isEntityReference()){
            if(sssomKey.getDatatype() == List.class){
                if(obj instanceof List){
                    return prefixMap.expandObject(obj);
                }
                List<String> list = new ArrayList<>();
                for(String s : obj.toString().split("\\|")){
                    list.add(prefixMap.expand(s));
                }
                return list;
            }
            return prefixMap.expandObject(obj);
        }else{
            if(sssomKey.getDatatype() == String.class){
                return obj.toString();
            }else if(sssomKey.getDatatype() == List.class){
                if(obj instanceof List){
                    return obj;
                }
                List<String> list = new ArrayList<>();
                for(String s : obj.toString().split("\\|")){
                    list.add(s);
                }
                return list;
            }else if(sssomKey.getDatatype() == SSSOMEntityType.class){
                return SSSOMEntityType.fromString(obj.toString());
            }else if(sssomKey.getDatatype() == SSSOMPredicateModifier.class){
                return SSSOMPredicateModifier.fromString(obj.toString());
            }else if(sssomKey.getDatatype() == SSSOMMappingCardinality.class){
                return SSSOMMappingCardinality.fromString(obj.toString());
            }else if(sssomKey.getDatatype() == LocalDate.class){
                try{
                    return LocalDate.parse(obj.toString());
                }catch(DateTimeParseException e){
                    try{
                        return LocalDateTime.parse(obj.toString()).toLocalDate();
                    }catch(DateTimeParseException ex){
                        return obj.toString();
                    }
                }
            }else if(sssomKey.getDatatype() == Double.class){
                return Double.parseDouble(obj.toString());
            }
        }
        return obj;
    }
    
    private static void parseSSSOMPrefixMap(Object curi, SSSOMPrefixMap prefixMap) {
        if(curi instanceof Map){
            Map<?, ?> curiMap = (Map<?, ?>)curi;
            for(Object key : curiMap.keySet()){
                if(key instanceof String == false){
                    LOGGER.warn("Skip the key \"{}\" in the curi map of the SSSOM mapping because it is not a string", key);
                    continue;
                }
                String stringkey = (String) key;
                Object value = curiMap.get(stringkey);
                if(value == null){
                    LOGGER.warn("Value to key \"{}\" is not available - skip it.", key);
                    continue;
                }
                if(value instanceof String == false){
                    LOGGER.warn("Skip the key \"{}\" in the curi map of the SSSOM mapping because the correspodnign value \"{}\" is not a string.", key, value);
                    continue;
                }
                prefixMap.add(stringkey, (String)value);
            }
        }
    }
    
    private static boolean hasEmbeddedMetadata(BufferedReader reader) throws IOException {
        boolean ret = false;
        reader.mark(1);
        int c = reader.read();
        if ( c != -1 && ((char) c) == '#' ) {
            ret = true;
        }
        reader.reset();
        return ret;
    }
    
    private static String extractMetadata(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        boolean done = false;
        boolean newLine = true;
        while ( !done ) {
            int c = reader.read();
            if ( c == -1 ) {
                done = true;
            } else {
                if ( newLine ) {
                    newLine = false;
                    if ( c != '#' ) {
                        done = true;
                        reader.reset();
                    }
                } else {
                    sb.append((char) c);
                    if ( c == '\n' ) {
                        newLine = true;
                        reader.mark(1);
                    }
                }
            }
        }
        return sb.toString();
    }
}
    
