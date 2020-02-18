/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hobbit.eval;


import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Alignment;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.AlignmentParser;
import de.uni_mannheim.informatik.dws.melt.yet_another_alignment_api.Correspondence;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xml.sax.SAXException;

/**
 *
 * @author Sven Hertling
 */
public class KnowledgeGraphTrackEval {
    
    private ConfusionMatrix classes;
    private ConfusionMatrix properties;
    private ConfusionMatrix instances;
    
    public KnowledgeGraphTrackEval(){
        classes = new ConfusionMatrix();
        properties = new ConfusionMatrix();
        instances = new ConfusionMatrix();
    }
    
    
    public void addEval(Alignment ref, Alignment system){
        Alignment refClass = new Alignment();
        Alignment refProp = new Alignment();
        Alignment refInst = new Alignment();
                
        getAlignmentTypes(ref, refClass, refProp, refInst);
        
        Alignment systemClass = new Alignment();
        Alignment systemProp = new Alignment();
        Alignment systemInst = new Alignment();
        
        getAlignmentTypes(system, systemClass, systemProp, systemInst);
        
        classes.addEval(refClass, systemClass);
        properties.addEval(refProp, systemProp);
        instances.addEval(refInst, systemInst);
    }
    
    private static Set<String> classTypes = new HashSet(Arrays.asList("class", "ontology", "null"));
    private static Set<String> propTypes = new HashSet(Arrays.asList("property", "ontology", "null"));
    private static Set<String> instanceTypes = new HashSet(Arrays.asList("resource", "null"));    
    private static final Pattern p = Pattern.compile("http:\\/\\/dbkwik\\.webdatacommons\\.org\\/.+?\\/(.+?)\\/.*");
    private static void getAlignmentTypes(Alignment m, Alignment clazz, Alignment props, Alignment instance){
        for(Correspondence c : m){
            String sourceType = getType(c.getEntityOne());
            String targetType = getType(c.getEntityTwo());
            
            if(classTypes.contains(sourceType) && classTypes.contains(targetType)){
                clazz.add(c);
            }else if (propTypes.contains(sourceType) && propTypes.contains(targetType)){
                props.add(c);
            }else if (instanceTypes.contains(sourceType) && instanceTypes.contains(targetType)){
                instance.add(c);
            }
            //all others are ignored intensionally and will not be evaluated (we only have recall precision and fmeasure for these three classes)
        }
    }
    
    private static String getType(String uri){
        if(uri.equals("null"))
            return "null";
        Matcher matcher = p.matcher(uri);
        if (matcher.find())
            return matcher.group(1);
        return "";
    }

    public ConfusionMatrix getClasses() {
        return classes;
    }

    public ConfusionMatrix getProperties() {
        return properties;
    }

    public ConfusionMatrix getInstances() {
        return instances;
    }
    
    
    
    
    public static void main(String[] args) throws SAXException, IOException {
        Alignment refAlignment = AlignmentParser.parse(new FileInputStream("C:\\dev\\OntMatching\\demo_mappings\\ref.xml"));
        Alignment systemAlignment = AlignmentParser.parse(new FileInputStream("C:\\dev\\OntMatching\\demo_mappings\\system.xml"));
        Alignment system2Alignment = AlignmentParser.parse(new FileInputStream("C:\\dev\\OntMatching\\demo_mappings\\system2.xml"));
        
        KnowledgeGraphTrackEval eval = new KnowledgeGraphTrackEval();
        eval.addEval(refAlignment, systemAlignment);
        eval.addEval(refAlignment, system2Alignment);
        
        System.out.println("Classes");
        System.out.println(eval.classes.toString());
        System.out.println(Arrays.toString(eval.classes.getMacroEval()));
        System.out.println(Arrays.toString(eval.classes.getMicroEval()));
        
        System.out.println("properties");
        System.out.println(eval.properties.toString());
        System.out.println(Arrays.toString(eval.properties.getMacroEval()));
        System.out.println(Arrays.toString(eval.properties.getMicroEval()));
        
        System.out.println("instances");
        System.out.println(eval.instances.toString());
        System.out.println(Arrays.toString(eval.instances.getMacroEval()));
        System.out.println(Arrays.toString(eval.instances.getMicroEval()));
    }
}
