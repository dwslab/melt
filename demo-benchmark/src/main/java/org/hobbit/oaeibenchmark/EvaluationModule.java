package org.hobbit.oaeibenchmark;

import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.AlignmentParser;
import de.uni_mannheim.informatik.dws.ontmatching.yetanotheralignmentapi.Mapping;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.commonelements.PlatformConstants;
import org.hobbit.eval.ConfusionMatrix;
import org.hobbit.eval.KnowledgeGraphTrackEval;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class EvaluationModule extends AbstractEvaluationModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationModule.class);

    private Model finalModel;
    private Map<String, Property> rdfPropertyMap;
    
    private KnowledgeGraphTrackEval eval;
    private long time;

    @Override
    public void init() throws Exception {
        LOGGER.info("Initializing Evaluation Module started...");
        super.init();
        
        this.finalModel = ModelFactory.createDefaultModel(); //in case init is called multiple times this will enforce new variables
        this.rdfPropertyMap = new HashMap<>();
        this.eval = new KnowledgeGraphTrackEval();
        this.time = 0;
        
        for(String kpi : PlatformConstants.KPI_URIS){
            rdfPropertyMap.put(kpi, this.finalModel.createProperty(kpi));
        }
        
        LOGGER.info("Initializing Evaluation Module ended...");
    }

    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        long currentTime = getTimePerformance(responseReceivedTimestamp, taskSentTimestamp);
        this.time += currentTime;
        LOGGER.info("currentTime in ms: " + currentTime);

        Mapping refAlignment = getExpectedAlignment(expectedData);
        Mapping systemAlignment = getSystemAlignment(receivedData);
        
        LOGGER.info("refAlignment size={}", refAlignment.size());
        LOGGER.info("systemAlignment size={}", systemAlignment.size());
        
        this.eval.addEval(refAlignment, systemAlignment);
    }
    
    @Override
    public Model summarizeEvaluation() throws Exception {
        LOGGER.info("Summary of Evaluation begins.");
        if (this.experimentUri == null)
            this.experimentUri = System.getenv().get(Constants.HOBBIT_EXPERIMENT_URI_KEY);

        Resource experiment = this.finalModel.createResource(experimentUri);        
        this.finalModel.add(experiment, RDF.type, HOBBIT.Experiment);
        
        
        this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(PlatformConstants.TIME_URI), time);
        
        addEval(experiment, this.eval.getClasses(), 
                PlatformConstants.CLASS_PRECISION_URI, 
                PlatformConstants.CLASS_RECALL_URI, 
                PlatformConstants.CLASS_FMEASURE_URI, 
                PlatformConstants.CLASS_SIZE_URI);
        addEval(experiment, this.eval.getProperties(),
                PlatformConstants.PROPERTIES_PRECISION_URI, 
                PlatformConstants.PROPERTIES_RECALL_URI, 
                PlatformConstants.PROPERTIES_FMEASURE_URI, 
                PlatformConstants.PROPERTIES_SIZE_URI);
        addEval(experiment, this.eval.getInstances(), 
                PlatformConstants.INSTANCES_PRECISION_URI, 
                PlatformConstants.INSTANCES_RECALL_URI, 
                PlatformConstants.INSTANCES_FMEASURE_URI, 
                PlatformConstants.INSTANCES_SIZE_URI);
                
        //or do it one by one
        //this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(PlatformConstants.FMEASURE_URI), fmeasure);
        //this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(PlatformConstants.RECALL_URI), recall);
        //etc...
        
        LOGGER.info("Summary of Evaluation is over.");
        return this.finalModel;
    }
    
    private void addEval(Resource experiment, ConfusionMatrix m, String precision_url, String recall_url, String fmeasure_url, String size_url){
        double[] precisionRecallFmeasure = m.getMicroEval(); //we choose here micro
        this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(precision_url), precisionRecallFmeasure[0]);
        this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(recall_url), precisionRecallFmeasure[1]);
        this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(fmeasure_url), precisionRecallFmeasure[2]);
        this.finalModel.addLiteral(experiment, this.rdfPropertyMap.get(size_url), m.getSystemSize());        
    }
    
    
    private static long getTimePerformance(long responseReceivedTimestamp, long taskSentTimestamp){
        long time = responseReceivedTimestamp - taskSentTimestamp;
        if (time < 0) {
            time = 0;
        }
        return time;
    }
    
    private static Mapping getExpectedAlignment(byte[] expectedData) throws IOException, SAXException{
        ByteBuffer buffer_exp = ByteBuffer.wrap(expectedData);
        String format = RabbitMQUtils.readString(buffer_exp);
        String path = RabbitMQUtils.readString(buffer_exp);
        byte[] expected = RabbitMQUtils.readByteArray(buffer_exp);
        
        return AlignmentParser.parse(new ByteArrayInputStream(expected));
    }
    
    private static Mapping getSystemAlignment(byte[] receivedData) throws IOException, SAXException{
        ByteBuffer buffer_sys = ByteBuffer.wrap(receivedData);     
        byte[] received = RabbitMQUtils.readByteArray(buffer_sys);
        return AlignmentParser.parse(new ByteArrayInputStream(received));
    }
    
}
