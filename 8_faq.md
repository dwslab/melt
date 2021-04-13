---
layout: default
title: FAQ
nav_order: 8
permalink: /faq
---

# Frequently Asked Questions (FAQs)

### I have a multiple SEALS packages and I want to use MELT's group evaluation functionalities. What is the simplest way to do so?
SEALS packages were wrapped for the SEALS platform. If the matchers were not developed using MELT or you are not sure 
whether they were developed with MELT, one option is to create the alignment files by executing the matchers 
using the SEALS client. Afterwards, you can read the alignment files (e.g. method `loadFromFolder` of class 
[`Executor`](https://github.com/dwslab/melt/tree/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/Executor.java)).<br/>
Alternatively (and more easily), you can install the SEALS client and run the SEALS packages from within MELT using 
[`ExecutorSeals`](https://github.com/dwslab/melt/tree/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/ExecutorSeals.java). This executor 
will start the evaluation in SEALS directly from the framework and can be used to conveniently evaluate one or more
matchers. Like the default `Executor`, `ExecutorSeals` will return an `ExecutionResultSet` that can then be further processed by 
any evaluator. When calling `run()`, system alignment files and any output will also be stored on disk and can be reused at 
a later point in time. You can also set the maximum time you want MELT to allocate to a particular matcher. If the matcher
does not finish within the given time limit, MELT will stop the process and proceed with the next test case or matcher.
`ExecutorSeals` can read zipped, unzipped (or a mix of both) SEALS packages.<br/>

### I am running a SEALS matcher that was packaged with MELT and uses some python component. On my system, the default python command does not refer to Python 3. How can this situation be resolved?
A folder `melt-resouces` in the working directory (perhaps `$SEALS_HOME`) has to be created. In there a file `python_command.txt` containing your full 
python path should be placed. This applies to all MELT packaged matchers that use the ML module. 
In other cases, you can also try to create a directory `oaei-resources` rather than `melt-resources`
and place the python_command.txt` there.

### Is there more documentation?
MELT is far more powerful than documented here. This user guide is intended to give an overview of the framework. We extend this guide over time, however, if you have any feedback or want to contribute, feel free to do so.
For specific code snippets, have a look at the examples. Note that classes, interfaces, and methods are extensively documented using [JavaDoc](/javadoc_latest/index.html).


# Common Errors which might appear

### Building a combined jar with jena dependencies

If you build a combined jar ("uber-jar" or "fat-jar", jar with dependencies) to try out your matchers especially with jena,
then make sure you do not override the service initializers of jena.
The Jena documentation has also a page about [building a combined jar with jena](https://jena.apache.org/documentation/notes/jena-repack.html).
The error **can** look like this:
```
Caused by: java.lang.NullPointerException
        at org.apache.jena.tdb.sys.EnvTDB.processGlobalSystemProperties(EnvTDB.java:33)
        at org.apache.jena.tdb.TDB.init(TDB.java:254)
        at org.apache.jena.tdb.sys.InitTDB.start(InitTDB.java:29)
        at org.apache.jena.sys.JenaSystem.lambda$init$2(JenaSystem.java:117)
        at java.util.ArrayList.forEach(Unknown Source)
        at org.apache.jena.sys.JenaSystem.forEach(JenaSystem.java:192)
        at org.apache.jena.sys.JenaSystem.forEach(JenaSystem.java:169)
        at org.apache.jena.sys.JenaSystem.init(JenaSystem.java:115)
        ...
```
In such a case use the [maven shade plugin](https://maven.apache.org/plugins/maven-shade-plugin/index.html) with a [ServicesResourceTransformer](https://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html)
to [build an exectuable jar](https://maven.apache.org/plugins/maven-shade-plugin/examples/executable-jar.html).
.
An example could look like the following
```
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.4</version>
                <configuration>
                  <shadedArtifactAttached>true</shadedArtifactAttached>
                  <shadedClassifierName>jar-with-dependencies</shadedClassifierName>
                  <transformers>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                      <mainClass>{your_full_main_class_here}</mainClass>
                    </transformer>
                    <transformer implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer" />
                  </transformers>
                </configuration>
                <executions>
                  <execution>
                    <phase>package</phase>
                    <goals>
                      <goal>shade</goal>
                    </goals>
                  </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```


### Using largebio dataset without unlimiting entity expansion

If you encounter the following error:
```
JAXP00010001: The parser has encountered more than "64000" entity expansions in this document; this is the limit imposed by the JDK.
```
or 
```
JAXP00010001: Der Parser hat mehr als 64000 Entityerweiterungen in diesem Dokument gefunden. Dies ist der von JDK vorgeschriebene Grenzwert.
```
during parsing ontologies in large bio, then it is time to [set an additional java system property](https://community.appway.com/screen/kb/article/jdk-bug-xml-readers-share-the-same-entity-expansion-counter-1482810869950#).
Set `jdk.xml.entityExpansionLimit` to `0`. This can be done at the command line:
```
-Djdk.xml.entityExpansionLimit=0
```
or by calling 
```
TrackRepository.Largebio.unlimitEntityExpansion();
```
which sets the property for the current JVM execution (but not for child processes.



### ToolException when using SEALS Matcher

In case you get a `ToolException` in SEALS Matcher which somehow talks about:
```the functionality of called method is not supported```
then this is a hint that the provided SEALS package is not able to use an input alignment.
Within the MatcherSeals in MELT you can set an attribute `doNotUseInputAlignment` to `true`. 
This will not pass an input alignment to the SEALS client even when one is available.
This applies for example to LogMap.
