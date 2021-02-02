package org.hobbit.oaeibenchmark;

import org.apache.jena.rdf.model.NodeIterator;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractBenchmarkController;
import org.hobbit.commonelements.PlatformConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BenchmarkController extends AbstractBenchmarkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkController.class);

    @Override
    public void init() throws Exception {
        LOGGER.info("Initializing Benchmark Controller...");
        super.init();

        //The name of the task is given to the data generator to get the required task resources (source, target and reference alignments)
        String task_uri = getTask();
        LOGGER.info("Task URI is" + task_uri);
        
        // data generators environmental values
        String[] envVariablesDataGenerator = new String[]{
            PlatformConstants.TASK_ENV_NAME + "=" + task_uri
        };

        //Create data generators.
        int numberOfDataGenerators = 1;
        createDataGenerators(PlatformConstants.DATA_GENERATOR_CONTAINER_IMAGE, numberOfDataGenerators, envVariablesDataGenerator);
        LOGGER.info("Data Generators created successfully.");

        //Create task generators. 
        int numberOfTaskGenerators = 1;
        createTaskGenerators(PlatformConstants.TASK_GENERATOR_CONTAINER_IMAGE, numberOfTaskGenerators, new String[]{});
        LOGGER.info("Task Generators created successfully.");

        // Create evaluation storage
        createEvaluationStorage();
        LOGGER.info("Evaluation Storage created successfully.");

        waitForComponentsToInitialize();
        LOGGER.info("All components initilized.");
    }
    
    private String getTask(){
        NodeIterator iterator = benchmarkParamModel.listObjectsOfProperty(benchmarkParamModel.getProperty(PlatformConstants.PARAMETER_TASK_URI));
        if (iterator.hasNext() == false)
            return PlatformConstants.DEFAULT_TASK_URI;
        return iterator.next().asResource().getLocalName();
    }
    
    
    @Override
    protected void executeBenchmark() throws Exception {

        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);
        LOGGER.info("Start signals sent to Data and Task Generators");

        LOGGER.info("Waiting for the data generators to finish their work.");
        waitForDataGenToFinish();
        LOGGER.info("Data generators finished.");

        LOGGER.info("Waiting for the task generators to finish their work.");
        waitForTaskGenToFinish();
        LOGGER.info("Task generators finished.");

        LOGGER.info("Waiting for the system to terminate.");
        waitForSystemToFinish();
        LOGGER.info("System terminated.");

        LOGGER.info("Will now create the evaluation module.");
        createEvaluationModule(PlatformConstants.EVALUATION_MODULE_CONTAINER_IMAGE, new String[]{});
        LOGGER.info("Evaluation module was created.");

        LOGGER.info("Waiting for the evaluation to finish.");
        waitForEvalComponentsToFinish();
        LOGGER.info("Evaluation finished.");

        LOGGER.info("Sending results to the platform controller.");
        sendResultModel(this.resultModel);
        LOGGER.info("Evaluated results sent to the platform controller.");
    }

}
