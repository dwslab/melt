package de.uni_mannheim.informatik.dws.melt.matching_jena_matchers.structurelevel.hierarchical.agony;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for graph analysis.
 * @deprecated use DotGraphUtil instead.
 */
public class AgonyUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgonyUtil.class);
    
    public static <E> void writeDotFile(File dotFile, List<Entry<E, E>> edges){
        writeDotFile(dotFile, edges, e->e.toString());
    }
    public static <E> void writeDotFile(File dotFile, List<Entry<E, E>> edges, Function<E, String> labelGenerator){
        try(BufferedWriter w = new BufferedWriter(new FileWriter(dotFile))){
            w.write("digraph hierarchy {");
            w.newLine();
            w.write("    rankdir=BT;");
            w.newLine();
            for(Entry<E, E> edge : edges){
                w.write("    " + 
                        normalizeNodeId(labelGenerator.apply(edge.getKey())) + " -> " + 
                        normalizeNodeId(labelGenerator.apply(edge.getValue())) + ";"
                );
                w.newLine();
            }
            w.write("}");
        } catch (IOException ex) {
            LOGGER.warn("Could not write dot file.", ex);
        }
    }
    
    
    private static String normalizeNodeId(String nodeId){
        return "\"" + nodeId.replace("\"", "\\\"" ) + "\"";
    }
}
