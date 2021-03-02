package de.uni_mannheim.informatik.dws.melt.doclet;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;
import com.sun.tools.doclets.standard.Standard;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;


public class MarkdownDoclet extends Standard {

    
    public static boolean start(RootDoc root) {
        //Standard.start(root); // make normal javadoc
        
        try(BufferedWriter matchers = new BufferedWriter(new FileWriter("1_complete_list_of_matchers.md"));
            BufferedWriter matcherMultiSource = new BufferedWriter(new FileWriter("2_complete_list_of_matchers_multi_source.md"));
            BufferedWriter filters = new BufferedWriter(new FileWriter("3_complete_list_of_filters.md"))){
            
            writeHeader(matchers, "Complete List of Matchers", "/matcher-components/full-matcher-list", 1);
            writeHeader(matcherMultiSource, "Complete List of Multi Source Matchers", "/matcher-components/full-matcher-multi-source-list", 2);
            writeHeader(filters, "Complete List of Filters", "/matcher-components/full-filter-list", 3);
            
            ClassDoc[] classes = root.classes();
            for (int i = 0; i < classes.length; ++i) {
                //System.out.println(classes[i]);
                //bw.write(getMatcherType(classes[i]) + " \t - " + classes[i]); //+ " - " + classes[i].commentText());
                //bw.newLine();
                //classes[i].commentText()
                
                switch(getMatcherType(classes[i])){
                    case FILTER:{
                        writeClassToFile(filters, classes[i]);
                        break;
                    }
                    case MATCHER:{
                        writeClassToFile(matchers, classes[i]);
                        break;
                    }
                    case MATCHER_MULTI_SOURCE:{
                        writeClassToFile(matcherMultiSource, classes[i]);
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            
        }
        return true;
    }
    
    private static final String NEWLINE = System.getProperty("line.separator");
    private static void writeClassToFile(BufferedWriter writer, ClassDoc clazz) throws IOException{
        ////use second headline to alow search to jump to the corresponding matcher
        writer.write(String.format("## %s [Javadoc](%s) / [Source Code](%s)", 
                clazz.simpleTypeName(), getJavadocLink(clazz.qualifiedTypeName()),getSourceCodeLink(clazz.qualifiedTypeName())));
        writer.newLine();
        writer.newLine();
        //writer.write(clazz.getRawCommentText());
        //writer.write(clazz.commentText());
        
        StringBuilder sb = new StringBuilder();
        for(Tag t : clazz.inlineTags()){
            if(t.kind().equals("@code")){
                sb.append(NEWLINE).append("```").append(NEWLINE).append(t.text()).append(NEWLINE).append("```").append(NEWLINE);
            }else{
                sb.append(postProcess(t.text()));
            }
        }
        
        /*
        writer.write("TAGS: ");        
        for(Tag t : clazz.inlineTags()){
            writer.write("    Tag:" + t.kind()  + " | " + t.name() + " | " + t.text() + " | " + t.toString());
            writer.newLine();
        }
        */
        writer.write(sb.toString());
        writer.newLine();
        writer.newLine(); // get a newline in markdown
        writer.write("Keywords: " + splitCamelCase(clazz.simpleTypeName()));
        writer.newLine();
        //writer.write(clazz.commentText());writer.newLine();
        writer.newLine();
    }
    
    private static final Pattern PRE_PATTERN = Pattern.compile("<\\/?pre>");
    private static String postProcess(String text){
        return PRE_PATTERN.matcher(text).replaceAll("");
    }
    
    private static final Pattern CAMEL_CASE_SPLIT = Pattern.compile("(?<!^)(?<!\\s)(?=[A-Z][a-z])");
    private static String splitCamelCase(String text){
        return CAMEL_CASE_SPLIT.matcher(text).replaceAll(" ");
    }
    
    private static String getJavadocLink(String qualifiedTypeName){
        //https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/typetransformer/basetransformers/Properties2URLTransformer.html
        return "https://dwslab.github.io/melt/javadoc_latest/" + qualifiedTypeName.replace(".", "/") + ".html";
    }
    private static final String MELT_PACKAGE = "de.uni_mannheim.informatik.dws.melt.";
    private static final int MELT_PACKAGE_LENGTH = MELT_PACKAGE.length();
    private static String getSourceCodeLink(String qualifiedTypeName){
        //https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/BaselineStringMatcher.java
        int melt_index = qualifiedTypeName.indexOf(MELT_PACKAGE);
        if(melt_index < 0){
            return null;
        }
        int dot_index = qualifiedTypeName.indexOf(".", melt_index + MELT_PACKAGE_LENGTH);
        if(dot_index < 0){
            dot_index = qualifiedTypeName.length();
        }
        String projectName = qualifiedTypeName.substring(melt_index + MELT_PACKAGE_LENGTH, dot_index).replace("_", "-");
        
        return "https://github.com/dwslab/melt/blob/master/" + projectName + "/src/main/java/" + qualifiedTypeName.replace(".", "/") + ".java";
    }
        
    
    //1_complete_list_of_matchers.md
    private static void writeHeader(BufferedWriter writer, String title, String permaLink, int navOrder) throws IOException{
        writer.write("---");writer.newLine();
        writer.write("layout: default");writer.newLine();
        writer.write("title: " + title);writer.newLine();
        writer.write("parent: Available Matchers");writer.newLine();
        writer.write("nav_order: " + Integer.toString(navOrder));writer.newLine();
        writer.write("permalink: " + permaLink);writer.newLine();
        writer.write("---");writer.newLine();
        writer.write("# " + title);writer.newLine();
    }
    
    private static Set<String> MATCHER = new HashSet<>(Arrays.asList(
            "de.uni_mannheim.informatik.dws.melt.matching_base.IMatcher",
            "de.uni_mannheim.informatik.dws.melt.matching_base.IMatcherCaller",
            "eu.sealsproject.platform.res.domain.omt.IOntologyMatchingToolBridge"
    ));
    
    private static Set<String> MATCHER_MULTI_SOURCE = new HashSet<>(Arrays.asList(
            "de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSource",
            "de.uni_mannheim.informatik.dws.melt.matching_base.multisource.IMatcherMultiSourceCaller",
            "de.uni_mannheim.informatik.dws.melt.matching_base.multisource.MatcherMultiSourceURL"
    ));
    
    private static Set<String> FILTER = new HashSet<>(Arrays.asList(
            "de.uni_mannheim.informatik.dws.melt.matching_base.Filter"
    ));   
    private static MatcherType getMatcherType(ClassDoc clazz){
        //breath first search
        Set<ClassDoc> visited = new HashSet<>();
        Queue<ClassDoc> queue = new LinkedList<>();        
        queue.add(clazz);
        visited.add(clazz);
        while(!queue.isEmpty()){
            ClassDoc current = queue.poll();
            
            if(FILTER.contains(current.qualifiedTypeName())){
                return MatcherType.FILTER;
            }else if(MATCHER.contains(current.qualifiedTypeName())){
                return MatcherType.MATCHER;
            }else if(MATCHER_MULTI_SOURCE.contains(current.qualifiedTypeName())){
                return MatcherType.MATCHER_MULTI_SOURCE;
            }
            
            for (ClassDoc next : current.interfaces()) {
                if (next != null && !visited.contains(next)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
            ClassDoc next = current.superclass();
            if (next != null && !visited.contains(next)){ // && next != Object.class ) {
                visited.add(next);
                queue.add(next);
            }
        }
        return MatcherType.NONE;
    }
}