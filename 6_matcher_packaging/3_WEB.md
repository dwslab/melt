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

### Package Your First Matcher with MELT
**Prerequisites**: Maven, Java, and Docker must be installed. Docker must be running during the build process.

1. Copy the project in [examples/simpleHobbitMatcher/](https://github.com/dwslab/melt/tree/master/examples/simpleWebMatcher) to your workspace.
2. Execute `mvn clean package` or `mvn clean install`.
3. Search in the `/target` directory for the tar.gz file. The file name represents the docker image name.

**Common Errors**
-  `Failed to execute goal io.fabric8:docker-maven-plugin:0.36.0:build`<br/>
Make sure that docker is running.


# Evaluate and Re-Use a Web Interface Matcher in MELT

### Evaluate and Re-Use the Docker File
If you have a docker tar.gz together with the image name, you can use class `MatcherDockerFile` to wrap the docker tar as matcher.
You can then re-use the instance in any matching pipeline or evaluate the instance as shown in the following code exmaple:

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

**Common Errors and Problems**
-  I have the docker tar.gz but I am not sure about the image name.<br/>
Unfortunately, the MELT framework cannot extract the image name from the tar.gz automatically. However, you can load the docker image and the image name will be printed on the console (`docker load -i <file>`). Note that
that MELT names the tar.gz file according to the image name.


### Evaluate and Re-Use a Running Web Service
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