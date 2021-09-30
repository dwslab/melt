---
layout: default
title: WEB
parent: Matcher Packaging
nav_order: 3
permalink: /matcher-packaging/web
---
# Web Interface (HTTP Matching Interface)

The Web packaging requires the matcher to provide an HTTP endpoint. This can be achieved with a docker container or any other server. The packaging format is independent of the MELT framework (a matcher does not have to be developed in MELT or use any other MELT technology) - however, MELT offers advanced services for [packaging](#melt-web-packaging) and [evaluation](#evaluate-and-re-use-a-running-web-service).

There are two possible `content types` (below: drop-down menu for "Request body"): (1) An URL encoded form and (2) multipart upload. For the first option, URLs are provided pointing to the data files. In this case, the matching server will access the URL and download the data. For the second option, the files are directly contained in the HTTP POST request. 

`source` and  `target` are plain RDF files (RDF/XML, Turtle, N3). 

`inputAlignment` is an alignment in the [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html).

`parameters` are used to provide task-specific information (for example "match only classes"). Some keys are already defined by [MELT](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/ParameterConfigKeys.java). The parameters can be transferred as JSON (preferred) or YAML.

The system directly returns the resulting alignment in the [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html).

Interactive matching is not supported.


## Swagger documentation of the Web API
The swagger documentation is displayed below / 
[[open full screen documentation in a new tab]](https://dwslab.github.io/melt/6_matcher_packaging/swagger_ui_melt.html){:target="_blank"}

<iframe src="https://dwslab.github.io/melt/6_matcher_packaging/swagger_ui_melt.html" scrolling="no"
    style="border:0; width:100%; height:2750px; overflow:hidden;">
</iframe>

# MELT Web Packaging
Any matcher which extends a MELT matcher class can be packaged using the Web format. 
MELT will generate a (Jetty) server for your matching system and will package everything in a docker tarball that you can share together with the image name. 

## Package Your First Matcher with MELT
**Prerequisites**: Maven, Java, and Docker must be installed. Docker must be running during the build process.

1. Copy the project in [examples/simpleWebMatcher/](https://github.com/dwslab/melt/tree/master/examples/simpleWebMatcher) to your workspace.
2. Execute `mvn clean package` or `mvn clean install`.
3. Search in the `/target` directory for the tar.gz file. The file name represents the docker image name.

**Common Errors**
-  `Failed to execute goal io.fabric8:docker-maven-plugin:0.36.0:build`<br/>
Make sure that docker is running.


# Evaluate and Re-Use a Web Interface Matcher in MELT

## Evaluate and Re-Use the Docker File
If you have a docker tar.gz together with the image name, you can use class `MatcherDockerFile` to wrap the docker tar as matcher.
You can then re-use the instance in any matching pipeline or evaluate the instance as shown in the following code example:

```java
import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;

import java.io.File;

public class EvaluationDockerMatcher {
    
    public static void main(String[] args) throws Exception {
        File dockerFile = new File("<provide path to tar file>");
        MatcherDockerFile dockerMatcher = new MatcherDockerFile("<provide docker image name>", dockerFile);

        // running the matcher on any task
        ExecutionResultSet ers = Executor.run(TrackRepository.Conference.V1.getFirstTestCase(), dockerMatcher);
        
        // evaluating our system
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);

        // we should close the docker matcher so that docker cab shut down the container
        dockerMatcher.close();

        // writing evaluation results to disk
        evaluatorCSV.writeToDirectory();
    }
}
```

**Debugging possibilities**
To see what is happening within the docker and get the log of the matcher running inside the container,
you can add the following lines after calling `Executor.run`:
```
ExecutionResultSet ers = Executor.run(TrackRepository.Conference.V1.getFirstTestCase(), dockerMatcher);

Thread.sleep(20000); // just to be sure that all logs are written.
dockerMatcher.logAllLinesFromContainer();  // this will output the log of the container

// evaluating our system ...
```

**Debugging possibilities without MELT**
If you want to run the docker image totally without MELT to check if it is working, you can run the following commands.
1. Change to a folder with the saved matcher file (in the example the name of the image file is `simplewebmatcher-1.0-web-latest.tar.gz`)
1. Execute `docker load -i ./simplewebmatcher-1.0-web-latest.tar.gz` and replacing the last path with the actual file name. This will load the image from the file into the local docker registry.
   - in case the image is already loaded in the local registry, you do not need to run this step
1. Run the container by executing `docker run --rm --publish 8080:8080 simplewebmatcher-1.0-web` (replace the image name by the name which appeared during the previous step)
   - this will start the container in an attached mode such that you see all log output 
   - you cannot execute further commands in this terminal because all input and output is redirect to the running docker container
1. Open a new terminal and change to a folder with two ontologies (which will be the input for the matcher) and execute `curl -F 'source=@cmt.rdf' -F 'target=@conference.rdf' http://127.0.0.1:8080/match > alignment.rdf`
   - replace `cmt.rdf` with the source file name and `conference.rdf` with the target file name
   - the result will be written to the generated `alignment.rdf` file
1. The matcher should now run and you should see some log output.
   -  to stop the matcher press `Ctrl+C` in the terminal where you executed the `docker run` command


**Common Errors and Problems**
-  I have the docker tar.gz but I am not sure about the image name.<br/>
If you package the matcher with MELT and the file name is e.g. `simplewebmatcher-1.0-web-latest.tar.gz` then the image name
is `simplewebmatcher-1.0-web` (thus without the tag `latest` and file ending `tar.gz`).
Note here that the matcher name (`simplewebmatcher`) and version (`1.0`) is different for your matcher.
In case you call the [constructor `MatcherDockerFile(File dockerImageFile)`](https://github.com/dwslab/melt/blob/6fe6d924a7bfe8c06a244fcaa2363572adb382e1/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/docker/MatcherDockerFile.java#L153), the image name is extracted automatically for you.
Another option to find out the image name is to call `docker load -i <file>`  which will print the image name on the console.
If you already know parts of the name and the image is build locally (when packaging a matcher on the same device) you can also call `docker images` which will list all images with their names.

- In case of a `ClassNotFoundException: com.github.dockerjava.core.DockerClientConfig` <br/>
Please have a look at [FAQ](https://dwslab.github.io/melt/faq#evaluation-of-a-docker-based-matcher)

## Evaluate and Re-Use a Running Web Service
If you do not have a docker tar.gz but instead you want to use a running Web service, you can use class `MatcherHTTPCall` to wrap the Web service as a matcher. In the constructor, you have to specify the endpoint URL on which the service runs.
The service must implement the [Web Interface](#web-interface-(http-matching-interface)).
You can then re-use the instance in any matching pipeline or evaluate the instance as shown in the following code exmaple:

```java
import de.uni_mannheim.informatik.dws.melt.matching_base.external.http.MatcherHTTPCall;
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutionResultSet;
import de.uni_mannheim.informatik.dws.melt.matching_eval.Executor;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;

import java.net.URI;

public class EvaluationEndpoint {

    public static void main(String[] args) throws Exception {
        // wrap our web service (running in this case locally on port 8080)
        URI matcherServiceUri = new URI("http://127.0.0.1:8080/match");
        MatcherHTTPCall matcher = new MatcherHTTPCall(matcherServiceUri, true);
        
        // let's run the matcher
        ExecutionResultSet ers = Executor.run(TrackRepository.Conference.V1.getFirstTestCase(), matcher);
        
        // let's evaluate the execution result set
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(ers);
        
        // let's serialize the results
        evaluatorCSV.writeToDirectory();
    }
}
```
