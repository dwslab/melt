---
layout: default
title: SEALS
parent: Matcher Packaging
nav_order: 1
permalink: /matcher-packaging/seals
---

# Packaging Matchers for SEALS
SEALS is a packaging format supported by most OAEI tracks.
MELT allows to package and to evaluate SEALS matchers.

## Package Your Matcher as SEALS Package with MELT

### Package Your First Matcher
**Prerequisites**: Maven and Java must be installed.

1. Copy the project in [examples/simpleSealsMatcher](https://github.com/dwslab/melt/tree/master/examples/simpleSealsMatcher) to your workspace.
2. Execute `mvn clean package` or `mvn clean install`.
3. Check in the `/target` directory for your zip file (`simple-SealsMatcher-1.0-seals_external.zip`).

### In More Detail
You can use the [examples/simpleSealsMatcher](https://github.com/dwslab/melt/tree/master/examples/simpleSealsMatcher) as a template and change it to your needs. However, if you take a close look, you can see that it is sufficient to use the `pom.xml` as template since the packaging is managed through maven.

You specify the matcher class in the `<oaei.mainClass>` tag.
The matcher must implement interface [`IMatcher <ModelClass, AlignmentClass, ParameterClass>`](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcher.java). Note that the easiest way to develop your system is to extend any of the convenience matcher classes such as `MatcherURL`, `MatcherYAAAJena` or `MatcherYAAAOwlApi` rather than implementing the interface yourself.


## Evaluate Your SEALS Package Using the OAEI SEALS Client 
You can set up the <a href="https://github.com/DanFaria/OAEI_SealsClient">SEALS client</a> locally and evaluate your matcher. You can find the documentation
of the client <a href="https://github.com/DanFaria/OAEI_SealsClient/blob/691b85003da0f6f391a04de85ad820b8a52b6118/SealsClientTutorial.pdf">here</a>.


## Evaluate and Re-Use a SEALS Package With MELT

### Evaluation and Re-Use with `MatcherSeals`
**Prerequisites**: Java 8 (not necessarily as system Java distribution), maven project with dependency [`matching-eval`](https://mvnrepository.com/artifact/de.uni-mannheim.informatik.dws.melt/matching-eval).

It is recommended to use class `MatcherSeals` for wrapping any SEALS zip file as matcher. Since `MatcherSeals` implements `MatcherURL`, you can use an instantiated matcher with the default `Executor` to evaluate it (even together with non-SEALS matchers). You can also combine a `MatcherSeals` instance with any other matcher e.g. in a matching pipeline.

### Evaluation Using `ExecutorSeals`
**Prerequisites**: Java 8 (not necessarily as system Java distribution), successful installation of SEALS, maven project with dependency [`matching-eval`](https://mvnrepository.com/artifact/de.uni-mannheim.informatik.dws.melt/matching-eval).

If you merely want to evaluate SEALS packages, you can use [`ExecutorSeals`]((https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/ExecutorSeals.java)) (alternatively to `MatcherSeals`) .
You may have to give execution rights to the SEALS jar (`chmod +x seals-omt-client.jar`).

*Example*
```java
import de.uni_mannheim.informatik.dws.melt.matching_data.TrackRepository;
import de.uni_mannheim.informatik.dws.melt.matching_eval.ExecutorSeals;
import de.uni_mannheim.informatik.dws.melt.matching_eval.evaluator.EvaluatorCSV;

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