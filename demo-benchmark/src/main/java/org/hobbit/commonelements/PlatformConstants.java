/** *****************************************************************************
 * Copyright 2017 by the Department of Informatics (University of Oslo)
 *
 *    This file is part of the Ontology Services Toolkit
 *
 ****************************************************************************** */
package org.hobbit.commonelements;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author ernesto Created on 9 Jan 2018
 *
 */
public class PlatformConstants {
    
    public static final String NAME = "knowledgegraph";
    private static final String capitalisedName = NAME.substring(0,1).toUpperCase() + NAME.substring(1).toLowerCase();
    public static final String VERSION = "V1.0";
    
    public static final String BENCHMARK_CONTROLLER_CONTAINER_IMAGE = "git.project-hobbit.eu:4567/oaei_knowledgegraph/controller";
    public static final String DATA_GENERATOR_CONTAINER_IMAGE = "git.project-hobbit.eu:4567/oaei_knowledgegraph/datagenerator";
    public static final String TASK_GENERATOR_CONTAINER_IMAGE = "git.project-hobbit.eu:4567/oaei_knowledgegraph/taskgenerator";
    public static final String EVALUATION_MODULE_CONTAINER_IMAGE = "git.project-hobbit.eu:4567/oaei_knowledgegraph/evaluationmodule";

    // =============== DATA GENERATOR CONSTANTS ===============
    public static final String TASK_ENV_NAME = NAME + "_task";
    
    
    // =============== URIS (used in benchmark.ttl) ===============
    public static final String bench_ns = "http://w3id.org/bench#";

    public static final String SUBTASK_CLASS_URI = bench_ns + "SubTask";
    public static final String API_URI = bench_ns + NAME;
    public static final String Benchmark_URI = bench_ns + NAME + "_benchmark";
    public static final String Benchmark_LABEL = "OAEI " + capitalisedName + " benchmark";
    public static final String Benchmark_COMMENT = "OAEI " + capitalisedName + " benchmark";
        
    //KPIs
    public static final String CLASS_PRECISION_URI = bench_ns + "class_precision";
    public static final String CLASS_RECALL_URI = bench_ns + "class_recall";
    public static final String CLASS_FMEASURE_URI = bench_ns + "class_fmeasure";
    public static final String CLASS_SIZE_URI = bench_ns + "class_size";
    
    public static final String PROPERTIES_PRECISION_URI = bench_ns + "properties_precision";
    public static final String PROPERTIES_RECALL_URI = bench_ns + "properties_recall";
    public static final String PROPERTIES_FMEASURE_URI = bench_ns + "properties_fmeasure";
    public static final String PROPERTIES_SIZE_URI = bench_ns + "properties_size";
    
    public static final String INSTANCES_PRECISION_URI = bench_ns + "instances_precision";
    public static final String INSTANCES_RECALL_URI = bench_ns + "instances_recall";
    public static final String INSTANCES_FMEASURE_URI = bench_ns + "instances_fmeasure";
    public static final String INSTANCES_SIZE_URI = bench_ns + "instances_size";
    
    public static final String TIME_URI = bench_ns + "timePerformance";
    public static final String[] KPI_URIS = new String[]{
        CLASS_PRECISION_URI, CLASS_RECALL_URI, CLASS_FMEASURE_URI, CLASS_SIZE_URI,
        PROPERTIES_PRECISION_URI, PROPERTIES_RECALL_URI, PROPERTIES_FMEASURE_URI, PROPERTIES_SIZE_URI, 
        INSTANCES_PRECISION_URI, INSTANCES_RECALL_URI, INSTANCES_FMEASURE_URI, INSTANCES_SIZE_URI,
        TIME_URI};

    //Tasks
    static {
        OaeiTask.matchClassDefault = true;
        OaeiTask.matchObjectPropDefault = true;
        OaeiTask.matchDataPropDefault = true;
        OaeiTask.matchInstancesDefault = true;
        OaeiTask.allowedInstanceTypesDefault = new HashSet<>();
    }
    private static final Set<OaeiTask> all_tasks = new HashSet<>(Arrays.asList(
        new OaeiTask("darkscape-oldschoolrunescape",    "darkscape.xml", "oldschoolrunescape.xml",  "darkscape~oldschoolrunescape~evaluation.xml"),
        new OaeiTask("runescape-darkscape",             "runescape.xml", "darkscape.xml",           "runescape~darkscape~evaluation.xml"),
        new OaeiTask("runescape-oldschoolrunescape",    "runescape.xml", "oldschoolrunescape.xml",  "runescape~oldschoolrunescape~evaluation.xml"),
        
        new OaeiTask("heykidscomics-dc",        "heykidscomics.xml",    "dc.xml",               "heykidscomics~dc~evaluation.xml"),
        new OaeiTask("marvel-dc",               "marvel.xml",           "dc.xml",               "marvel~dc~evaluation.xml"),
        new OaeiTask("marvel-heykidscomics",    "marvel.xml",           "heykidscomics.xml",    "marvel~heykidscomics~evaluation.xml"),
        
        new OaeiTask("memory-alpha-memory-beta",    "memory-alpha.xml",     "memory-beta.xml",  "memory-alpha~memory-beta~evaluation.xml"),
        new OaeiTask("memory-alpha-stexpanded",     "memory-alpha.xml",     "stexpanded.xml",   "memory-alpha~stexpanded~evaluation.xml"),
        new OaeiTask("memory-beta-stexpanded",      "memory-beta.xml",      "stexpanded.xml",   "memory-beta~stexpanded~evaluation.xml")
    ));
    
    
    
    public static final Map<String, Set<OaeiTask>> TASK_MAPPING = initialiseTaskMapping();
    private static Map<String, Set<OaeiTask>> initialiseTaskMapping(){
        Map<String, Set<OaeiTask>> result = new HashMap<>();
        for(OaeiTask t : all_tasks){
            result.put(t.getTaskQueueName(), new HashSet(Arrays.asList(t)));//add all tasks as single task
        }
        //add an "ALL" task:
        result.put("All", all_tasks);
        return Collections.unmodifiableMap(result);        
    }
    
    

    //Parameters
    public static final String PARAMETER_TASK_URI = bench_ns + NAME + "Task";
    public static final String DEFAULT_TASK_URI = bench_ns + "darkscape-oldschoolrunescape";
    public static final String PARAMETER_TASK_LABEL = "The name of the task within the track.";
    public static final String PARAMETER_TASK_COMMENT = "The name of the task within the track. Options: " + TASK_MAPPING.keySet().toString();

    //Serialization Formats
    private static final String RDFXML = "RDFXML";
    private static final String TURTLE = "Turtle";
    public static final String FORMAT = RDFXML;
    
    public static String DATASET_PATH = "/maven/datasets/";//"src/main/oaei-resources/datasets" + File.separator; //System.getProperty("user.dir") + File.separator + "datasets" + File.separator;
    
    
    
}
