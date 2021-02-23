---
layout: default
title: SEALS
parent: Matcher Packaging
nav_order: 1
---

# Packaging matchers for SEALS

## Steps
1. Have a look at [examples/simpleJavaMatcher](/examples/simpleJavaMatcher)
2. Adjust settings in pom.xml to your needs.
3. Execute `mvn clean package` or `mvn clean install` and look in the `/target` directory for your zip file.

## Evaluate Your SEALS Package Using the OAEI SEALS Client 
You can set up the <a href="https://github.com/DanFaria/OAEI_SealsClient">SEALS client</a> locally and evaluate your matcher. You can find the documentation
of the client <a href="https://github.com/DanFaria/OAEI_SealsClient/blob/691b85003da0f6f391a04de85ad820b8a52b6118/SealsClientTutorial.pdf">here</a>.

## Evaluate a SEALS Package Using MELT
You can evaluate any SEALS packaged matcher using the [`ExecutorSeals`]((https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/ExecutorSeals.java)).
You may have to give execution rights to the SEALS jar (`chmod +x seals-omt-client.jar `).

*Example*
```java
// imports...
public class SealsPlayground {
    public static void main(String[] args) {
        String sealsClientJar = "<path to SEALS jar>";
        String sealsHome = "<path to SEALS home directory>";
        
        // the SEALS client requires java 8
        String java8command = "<java 8 command>";
    
        // you do not have to unzip (but you can)
        String pathToSealsPackage = "<zipped or unzipped seals package>";
        
        // just one of many constructors:
        ExecutorSeals es = new ExecutorSeals(java8command, sealsClientJar, sealsHome);
        
        // using default evaluation capabilities
        EvaluatorCSV evaluatorCSV = new EvaluatorCSV(es.run(TrackRepository.Anatomy.Default, pathToSealsPackage));
        evaluatorCSV.writeToDirectory();
    }
}
```