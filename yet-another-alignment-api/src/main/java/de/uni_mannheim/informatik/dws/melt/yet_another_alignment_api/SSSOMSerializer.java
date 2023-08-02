package de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api;

import static de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentSerializer.serialize;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.DefaultExtensions.SSSOM;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
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
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.Yaml;


/**
 * The SSSOMSerializer can serialize to SSSOM files following the convention described in the
 * <a href="https://github.com/mapping-commons/sssom">SSSOM github</a> and <a href="https://w3id.org/sssom/spec">userguide</a>.
 * @author Sven Hertling
 */
public class SSSOMSerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSSOMSerializer.class);
    
    /**
     * Constant for UTF-8 encoding.
     */
    private static final Charset ENCODING = StandardCharsets.UTF_8;
    
    
    /**
     * Method to write the specified alignment to the specified file in the <a href="https://w3id.org/sssom/spec">SSSOM format</a>.
     * @param alignment The alignment that shall be written.
     * @param file The file to which the alignment shall be written.
     * @throws IOException Exception that occurred while serializing the alignment.
     */
    public static void serialize(Alignment alignment, File file) throws IOException {
        
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        
        try (FileOutputStream out = new FileOutputStream(file)) {
            serialize(alignment, out);
        }
    }
    
    /**
     * Serializes an alignment as String.
     * @param alignment The alignment to be serialized.
     * @return Alignment as String.
     * @throws java.io.IOException in case of an io error
     */
    public static String serialize(Alignment alignment) throws IOException {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()){
            serialize(alignment, stream);
            return stream.toString(ENCODING.toString());            
        }
    }
    
    /**
     * Serializes an alignment as String.
     * @param alignment The alignment to be serialized.
     * @param includeNonSSSOMAttributes if true include also non SSSOM attributes in the output.
     * @return Alignment as String.
     * @throws java.io.IOException in case of an io error
     */
    public static String serialize(Alignment alignment, boolean includeNonSSSOMAttributes) throws IOException {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()){
            serialize(alignment, stream, includeNonSSSOMAttributes);
            return stream.toString(ENCODING.toString());            
        }
    }
    
    
    private static final Map<String, List<String>> SSSOM_ALTERNATIVES = initAlternatives();
    private static Map<String, List<String>> initAlternatives(){
        Map<String, List<String>> alternatives = new HashMap<>();
        
        alternatives.put(SSSOM.MAPPING_SET_VERSION.toString(), Arrays.asList(
                DefaultExtensions.StandardApi.VERSION.toString()
        ));
                
        alternatives.put(SSSOM.MAPPING_TOOL.toString(), Arrays.asList(
                DefaultExtensions.StandardApi.METHOD.toString(),
                DefaultExtensions.OmvMetadata.USED_METHOD.toString(),
                DefaultExtensions.OmvMetadata.MAPPING_METHOD.toString()
        ));
        
        alternatives.put(SSSOM.MAPPING_TOOL_VERSION.toString(), Arrays.asList(
                DefaultExtensions.StandardApi.METHOD_VERSION.toString()
        ));
        
        alternatives.put(SSSOM.MAPPING_TOOL_VERSION.toString(), Arrays.asList(
                DefaultExtensions.StandardApi.METHOD_VERSION.toString()
        ));
        
        alternatives.put(SSSOM.SUBJECT_LABEL.toString(), Arrays.asList(
                DefaultExtensions.StandardApi.LABEL_1.toString()
        ));
        
        alternatives.put(SSSOM.OBJECT_LABEL.toString(), Arrays.asList(
                DefaultExtensions.StandardApi.LABEL_2.toString()
        ));
        
        alternatives.put(SSSOM.AUTHOR_LABEL.toString(), Arrays.asList(
                DefaultExtensions.DublinCore.CREATOR.toString(),
                DefaultExtensions.OmvMetadata.HAS_CREATOR.toString()
        ));
        
        alternatives.put(SSSOM.MAPPING_DATE.toString(), Arrays.asList(
                DefaultExtensions.DublinCore.DATE.toString(),
                DefaultExtensions.OmvMetadata.CREATION_DATE.toString()
        ));
        
        alternatives.put(SSSOM.COMMENT.toString(), Arrays.asList(
                DefaultExtensions.DublinCore.DESCRIPTION.toString()
        ));
        
        alternatives.put(SSSOM.LICENSE.toString(), Arrays.asList(
                DefaultExtensions.DublinCore.RIGHTS.toString()
        ));
        
        alternatives.put(SSSOM.MAPPING_SET_ID.toString(), Arrays.asList(
                DefaultExtensions.DublinCore.IDENTIFIER.toString(),
                DefaultExtensions.AlignmentServer.ALID.toString()
        ));
        
        alternatives.put(SSSOM.OBJECT_SOURCE.toString(), Arrays.asList(
                DefaultExtensions.AlignmentServer.OURI2.toString()
        ));
        
        alternatives.put(SSSOM.SUBJECT_SOURCE.toString(), Arrays.asList(
                DefaultExtensions.AlignmentServer.OURI1.toString()
        ));
        
        alternatives.put(SSSOM.MAPPING_JUSTIFICATION.toString(), Arrays.asList(
                DefaultExtensions.Argumentation.REASON.toString()
        ));
        
        return alternatives;
    }
    
    /**
     * Method to write the specified alignment to the specified file in the <a href="https://w3id.org/sssom/spec">SSSOM format</a>.
     * The provided {@link OutputStream} is not closed.
     * @param alignment The alignment that shall be written.
     * @param stream the stream where the serialized alignment should be written to (the stream is not closed)
     * @throws IOException Exception that occurred while serializing the alignment.
     */
    public static void serialize(Alignment alignment, OutputStream stream) throws IOException {
        serialize(alignment, stream, false);
    }
    
    /**
     * Method to write the specified alignment to the specified file in the <a href="https://w3id.org/sssom/spec">SSSOM format</a>.
     * The provided {@link OutputStream} is not closed.
     * @param alignment The alignment that shall be written.
     * @param stream the stream where the serialized alignment should be written to (the stream is not closed)
     * @param includeNonSSSOMAttributes if true include also non SSSOM attributes in the output.
     * @throws IOException Exception that occurred while serializing the alignment.
     */
    public static void serialize(Alignment alignment, OutputStream stream, boolean includeNonSSSOMAttributes) throws IOException {
        
        try(OutputStreamWriter streamWriter = new OutputStreamWriter(stream, ENCODING)){
            SSSOMPrefixMap prefixMap = new SSSOMPrefixMap();
            try{
                Map<String, String> map = alignment.getExtensionValueCasted(SSSOM.CURIE_MAP.toString());
                prefixMap.addAll(map);
            }catch(ClassCastException e){}

            //write alignment level extension keys:
            writeMetadata(prefixMap, alignment, streamWriter, includeNonSSSOMAttributes);

            Set<String> extensionKeys = alignment.getDistinctCorrespondenceExtensionKeys();

            Set<String> sssomKeys = new HashSet<>();        
            for(String key : extensionKeys){
                if(key.startsWith(SSSOM.BASE_URI)){
                    sssomKeys.add(key);
                }
            }
            //check alternatives
            for(Entry<String,List<String>> alternative : SSSOM_ALTERNATIVES.entrySet()){
                for(String s : alternative.getValue()){
                    if(extensionKeys.contains(s)){
                        sssomKeys.add(alternative.getKey());
                    }
                }
            }
            Set<String> alreadyUsedHeaders = sssomKeys.stream().map(AlignmentSerializer::getExtensionLabel).collect(Collectors.toSet());
            sssomKeys.remove(SSSOM.SUBJECT_ID.toString());
            sssomKeys.remove(SSSOM.OBJECT_ID.toString());
            sssomKeys.remove(SSSOM.PREDICATE_ID.toString());
            sssomKeys.remove(SSSOM.CONFIDENCE.toString());

            List<String> nonSSSOMKeys = new ArrayList<>();
            if(includeNonSSSOMAttributes){
                for(String key : extensionKeys){
                    String keyLabel = AlignmentSerializer.getExtensionLabel(key); //extracts last part of uri which is used as header name
                    if(key.startsWith(SSSOM.BASE_URI) == false && 
                            alreadyUsedHeaders.contains(keyLabel) == false){
                        nonSSSOMKeys.add(key);
                        alreadyUsedHeaders.add(keyLabel);
                    }
                }
            }
            nonSSSOMKeys.sort(String.CASE_INSENSITIVE_ORDER);//make deterministic
            //create order
            List<String> orderedSSSOMKeys = new ArrayList<>(sssomKeys);
            orderedSSSOMKeys.sort(String.CASE_INSENSITIVE_ORDER);//make deterministic

            List<String> header = new ArrayList<>();
            header.add("subject_id");
            header.add("object_id");
            header.add("predicate_id");
            header.add("confidence");
            header.addAll(orderedSSSOMKeys.stream().map(AlignmentSerializer::getExtensionLabel).collect(Collectors.toList())); // only last part of uri
            header.addAll(nonSSSOMKeys.stream().map(AlignmentSerializer::getExtensionLabel).collect(Collectors.toList()));

            CSVPrinter csvPrinter = CSVFormat.TDF.print(streamWriter);
            csvPrinter.printRecord(header);
            for(Correspondence correspondence : alignment){
                List<String> row = new ArrayList<>(header.size());

                row.add(prefixMap.shorten(correspondence.getEntityOne()));
                row.add(prefixMap.shorten(correspondence.getEntityTwo()));
                row.add(prefixMap.shorten(correspondence.getRelation().getRdfRepresentation()));
                row.add(Double.toString(correspondence.getConfidence()));

                //TODO: prefixes
                for(String extensionKey : orderedSSSOMKeys){

                    Object value = correspondence.getExtensionValue(extensionKey);
                    if(value == null){
                        for(String alternativeKey : SSSOM_ALTERNATIVES.getOrDefault(extensionKey, new ArrayList<>())){
                            value = correspondence.getExtensionValue(alternativeKey);
                            if(value != null)
                                break;
                        }
                        if(value == null)
                            value = "";
                    }
                    SSSOM sssomextensionKey = SSSOM.fromURL(extensionKey);
                    if(sssomextensionKey != null){
                        if(sssomextensionKey.isEntityReference()){
                            value = prefixMap.shortenObject(value);
                        }
                    }
                    if(value instanceof List){
                        StringJoiner joiner = new StringJoiner("|");
                        for (Object o : (List) value) {
                            joiner.add(o.toString());
                        }
                        value = joiner.toString();
                    }
                    row.add(value.toString());
                }

                for(String extensionKey : nonSSSOMKeys){
                    Object value = correspondence.getExtensionValue(extensionKey);
                    if(value == null)
                        value = "";
                    row.add(value.toString());
                }
                csvPrinter.printRecord(row);
            }
        }
    }
    
    
    private static final String NEWLINE = System.getProperty("line.separator");
    private static void writeMetadata(SSSOMPrefixMap prefixMap, Alignment a, OutputStreamWriter writer, boolean includeNonSSSOMAttributes) throws IOException{
        
        LineBreak lb = DumperOptions.LineBreak.getPlatformLineBreak();
        
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        dumperOptions.setSplitLines(false);
        dumperOptions.setLineBreak(lb);
        Yaml yaml = new Yaml(dumperOptions);
                
        a.addExtensionValue(SSSOM.CURIE_MAP.toString(), prefixMap.getPrefixMap());
        
        //check alternatives
        Set<String> extensionKeySet = a.getExtensions().keySet();
        for(Entry<String,List<String>> alternative : SSSOM_ALTERNATIVES.entrySet()){
            for(String s : alternative.getValue()){
                if(extensionKeySet.contains(s)){
                    a.addExtensionValue(alternative.getKey(), a.getExtensions().get(s));
                }
            }
        }        
        
        List<Entry<String, Object>> sortedExtensions = a.getExtensions().entrySet().stream()
                .sorted((one, two) -> String.CASE_INSENSITIVE_ORDER.compare(one.getKey(), two.getKey()))
                .collect(Collectors.toList());        
        for(Entry<String, Object> extension : sortedExtensions){
            if(includeNonSSSOMAttributes == false){
                if(extension.getKey().startsWith(SSSOM.BASE_URI) == false)
                    continue;
            }
            
            Object value = extension.getValue();
            
            SSSOM key = SSSOM.fromURL(extension.getKey());
            if(key != null){
                if(key.isEntityReference()){
                    value = prefixMap.shortenObject(value);
                }
            }
            String sssomKey = AlignmentSerializer.getExtensionLabel(extension.getKey());
            
            Map<String, Object> sssomExtensions = new HashMap<>();
            sssomExtensions.put(sssomKey, value);
            for(String s : yaml.dump(sssomExtensions).split(lb.getString())){
                writer.write("#");
                writer.write(s);
                writer.write(lb.getString());
            }
        }
    }
    
    
    
}
    
