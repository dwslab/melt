/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.hobbit.benchmarkfilegenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.hobbit.commonelements.OaeiTask;
import org.hobbit.commonelements.PlatformConstants;

/**
 *
 * @author Sven Hertling
 */
public class BenchmarkFileGenerator {
    
    public static void main(String[] args) throws IOException{
        //checkFiles(PlatformConstants.TASK_MAPPING);
        generateBenchmarkFile("benchmark.ttl");
    }
    
    private static void generateBenchmarkFile(String path) throws IOException{
        Model model = ModelFactory.createDefaultModel();
        model.withDefaultMappings(PrefixMapping.Standard);
        model.setNsPrefix( "hobbit", Hobbit.uri );
        model.setNsPrefix( "bench", "http://w3id.org/bench#" );
        
        //make subtasks
        
        Resource subtaskClass = model.createResource(PlatformConstants.SUBTASK_CLASS_URI);
        subtaskClass.addProperty(RDF.type, RDFS.Class).addProperty(RDF.type, OWL.Class);
        
        for(String taskid : PlatformConstants.TASK_MAPPING.keySet()){            
            Resource subtask = model.createResource(PlatformConstants.bench_ns + taskid);
            subtask.addProperty(RDF.type, subtaskClass);
            subtask.addLiteral(RDFS.label, taskid);
        }
        
        //make a parameter
        Resource parameterTask = model.createResource(PlatformConstants.PARAMETER_TASK_URI);
        parameterTask.addProperty(RDF.type, Hobbit.Parameter)
                .addProperty(RDF.type, Hobbit.FeatureParameter)
                .addProperty(RDF.type, Hobbit.ConfigurableParameter);
        parameterTask.addLiteral(RDFS.label, PlatformConstants.PARAMETER_TASK_LABEL);
        parameterTask.addLiteral(RDFS.comment, PlatformConstants.PARAMETER_TASK_COMMENT);
        parameterTask.addProperty(RDFS.domain, Hobbit.Experiment).addProperty(RDFS.domain, Hobbit.Challenge);
        parameterTask.addProperty(RDFS.range, subtaskClass);
        parameterTask.addProperty(Hobbit.defaultValue, model.createResource(PlatformConstants.DEFAULT_TASK_URI));
        
        //make api
        Resource api = model.createResource(PlatformConstants.API_URI);
        api.addProperty(RDF.type, Hobbit.API);
        
        
        //make benchmark
        Resource bench = model.createResource(PlatformConstants.Benchmark_URI);
        bench.addProperty(RDF.type, Hobbit.Benchmark);
        bench.addLiteral(RDFS.label, PlatformConstants.Benchmark_LABEL);
        bench.addLiteral(RDFS.comment, PlatformConstants.Benchmark_COMMENT);
        bench.addLiteral(Hobbit.imageName, PlatformConstants.BENCHMARK_CONTROLLER_CONTAINER_IMAGE);
        
        bench.addLiteral(Hobbit.usesImage, PlatformConstants.BENCHMARK_CONTROLLER_CONTAINER_IMAGE);
        bench.addLiteral(Hobbit.usesImage, PlatformConstants.DATA_GENERATOR_CONTAINER_IMAGE);
        bench.addLiteral(Hobbit.usesImage, PlatformConstants.TASK_GENERATOR_CONTAINER_IMAGE);
        bench.addLiteral(Hobbit.usesImage, PlatformConstants.EVALUATION_MODULE_CONTAINER_IMAGE);
        
        bench.addLiteral(Hobbit.version, PlatformConstants.VERSION);
        bench.addProperty(Hobbit.hasAPI, api);
        bench.addProperty(Hobbit.hasParameter, parameterTask);
        
        //Make metrics
        for(String kpi : PlatformConstants.KPI_URIS){
            Resource kpiResource = model.createResource(kpi);
            kpiResource.addProperty(RDF.type, Hobbit.KPI);
            kpiResource.addLiteral(RDFS.label, kpi.substring(kpi.indexOf("#")+1));
            kpiResource.addProperty(RDFS.range, XSD.xdouble);
            
            bench.addProperty(Hobbit.measuresKPI, kpiResource);
        }
        
        
        try(OutputStream fw = new FileOutputStream(path)){
            RDFDataMgr.write(fw, model, Lang.TURTLE);
        }
        System.out.println("Generate file successfully: " + path.toString());
    }
    
    
    
    private static void checkFiles(Map<String, OaeiTask> result){  
        for(Map.Entry<String, OaeiTask> entry : result.entrySet()){
            checkFiles(entry.getValue().getSourceFileName());
            checkFiles(entry.getValue().getTargetFileName());
            checkFiles(entry.getValue().getReferenceFileName());
        }
        System.out.println("Checked all files.");
    }
    private static void checkFiles(String filepath){        
        File f = new File(PlatformConstants.DATASET_PATH + filepath);
        if(!(f.exists() && !f.isDirectory())){
            System.out.println("File not found: " + f.toString());
        }   
    }
    
}
