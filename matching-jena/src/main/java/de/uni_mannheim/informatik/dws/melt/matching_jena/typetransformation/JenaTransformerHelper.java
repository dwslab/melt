
package de.uni_mannheim.informatik.dws.melt.matching_jena.typetransformation;

import de.uni_mannheim.informatik.dws.melt.matching_base.ParameterConfigKeys;
import de.uni_mannheim.informatik.dws.melt.matching_base.typetransformer.TypeTransformerRegistry;
import de.uni_mannheim.informatik.dws.melt.matching_jena.OntologyCacheJena;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.assembler.assemblers.OntModelSpecAssembler;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for jena transformers.
 * It usually just retrieves some properties for the transformation.
 */
public class JenaTransformerHelper {


    private static final Logger LOGGER = LoggerFactory.getLogger(JenaTransformerHelper.class);
    
    public static OntModelSpec getSpec(Properties parameters){
        String spec = parameters.getProperty(ParameterConfigKeys.JENA_ONTMODEL_SPEC);
        if(spec == null)
            return OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC;
        
        OntModelSpec ontModelSpec = OntModelSpecAssembler.getOntModelSpecField(spec);
        if(ontModelSpec == null){
            LOGGER.warn("The value provided by ParameterConfigKeys.JENA_ONTMODEL_SPEC is not a valid OntModelSpec. Defaulting to OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC");
            return OntologyCacheJena.DEFAULT_JENA_ONT_MODEL_SPEC;
        }
        return ontModelSpec;
    }
    
    private static final Lang DEFAULT_HINT_LANG = Lang.RDFXML;
    public static Lang hintLang(Properties parameters){
        Object lang = parameters.get(ParameterConfigKeys.HINT_LANG);
        if(lang == null)
            return DEFAULT_HINT_LANG;
        if(lang instanceof String){
            Lang l = RDFLanguages.nameToLang((String)lang);
            if(l == null)
                return DEFAULT_HINT_LANG;
            return l;
        }else{
            LOGGER.warn("The value provided by ParameterConfigKeys.HINT_LANG is not of type string. Defaulting to RDFXML.");
            return DEFAULT_HINT_LANG;
        }
    }
    
    public static List<String> getShortNameForModelRepresentations(List<Set<Object>> models){
        List<String> names = new ArrayList<>(models.size());
        for(Set<Object> m : models){
            names.add(getShortNameForModelRepresentation(m));
        }
        return names;
    }
    
    public static String getShortNameForModelRepresentation(Set<Object> model){
        if(model.isEmpty()){
            return "<empty>";
        }
        for(Object o : model){
            if(o instanceof URL){
                String name = FilenameUtils.getName(((URL)o).getPath());
                if(StringUtils.isBlank(name) == false)
                    return name;
            }else if(o instanceof URI){
                String name = FilenameUtils.getName(((URI)o).getPath());
                if(StringUtils.isBlank(name) == false)
                    return name;
            }
        }
        for(Object o : model){
            if(o instanceof Model){
                Model m = (Model)o;
                String nsPrefix = m.getNsPrefixURI("");
                if(StringUtils.isBlank(nsPrefix) == false)
                    return nsPrefix;
                List<Entry<String, Integer>> list = getDomainCountsFromSubjects(m, 200);
                if(list.isEmpty() == false){
                    return list.get(0).getKey();
                }
            }
        }
        return model.iterator().next().toString();
        /*
        StringBuilder sb = new StringBuilder();
        for(Object o : model){
            if(o instanceof URL){
                String name = FilenameUtils.getName(((URL)o).getPath());
                if(StringUtils.isBlank(name) == false)
                    sb.append(name);
            }
        }
        sb.append("(");
        sb.append(TypeTransformerRegistry.getTransformedObjectMultipleRepresentations(model, Model.class).size());
        sb.append(")");
        return sb.toString();
        */
    }
    
    public static String getModelRepresentation(Set<Object> model){
        for(Object o : model){
            if(o instanceof URL || o instanceof URI){
                return o.toString();
            }
        }
        for(Object o : model){
            if(o instanceof Model){
                return jenaModelToString((Model)o);
            }
        }
        if(model.isEmpty()){
            return "<empty>";
        }
        return model.iterator().next().toString();
    }
    
    public static String jenaModelToString(Model m){
        
        
        StringBuilder sb = new StringBuilder();
        sb.append("Jena Model{ ");
        
        String nsPrefix = m.getNsPrefixURI("");
        if(nsPrefix != null)
            sb.append("default namespace (prefix): ").append(nsPrefix).append(" ");
        
        List<Entry<String, Integer>> list = getDomainCountsFromSubjects(m, 200);
        int counter = 1;
        sb.append("top 3 domains (of 200 resources): ");
        for (Entry<String, Integer> entry : list) {
            sb.append(entry.getKey()).append("(").append(entry.getValue()).append(") ");
            if(counter >= 3){
                break;
            }
            counter++;
        }
        sb.append("}");
        return sb.toString();
    }
    
    private static List<Entry<String, Integer>> getDomainCountsFromSubjects(Model m, int sampleSize){
        Map<String, Integer> domains = new HashMap<>();
        ResIterator i =  m.listSubjects();
        int counter = 1;
        while(i.hasNext()){
            Resource r = i.next();
            if(r.isURIResource() == false){
                continue;
            }
            String domain = getUriDomain(r.getURI());
            if(domain == null || domain.equals("http://"))
                continue;
            domains.put(domain, domains.computeIfAbsent(domain, __-> 0) + 1);
            if(counter >= sampleSize){
                break;
            }
            counter++;
        }
        List<Entry<String, Integer>> list = new ArrayList<>(domains.entrySet());
        list.sort(Entry.comparingByValue(Comparator.reverseOrder()));
        return list;
    }
    
    
    private static String getUriDomain(String uri){
        int lastIndex = uri.lastIndexOf("#");
        if(lastIndex >= 0){
        return uri.substring(0, lastIndex + 1);
        }
        lastIndex = uri.lastIndexOf("/");
        if(lastIndex >= 0){
        return uri.substring(0, lastIndex + 1);
        }
        return uri;
    }
}
