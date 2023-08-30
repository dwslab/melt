---
layout: default
title: FAQ
nav_order: 10
permalink: /faq
---

# Frequently Asked Questions (FAQs)

## I have a multiple SEALS packages and I want to use MELT's group evaluation functionalities. What is the simplest way to do so?
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

## I am running a SEALS matcher that was packaged with MELT and uses some python component. On my system, the default python command does not refer to Python 3. How can this situation be resolved?
A folder `melt-resouces` in the working directory (perhaps `$SEALS_HOME`) has to be created. In there a file `python_command.txt` containing your full 
python path should be placed. This applies to all MELT packaged matchers that use the ML module. 
In other cases, you can also try to create a directory `oaei-resources` rather than `melt-resources`
and place the python_command.txt` there.

## Is there more documentation?
MELT is far more powerful than documented here. This user guide is intended to give an overview of the framework. We extend this guide over time. However, if you have any feedback or want to contribute, feel free to do so.
For specific code snippets, have a look at the examples. Note that classes, interfaces, and methods are extensively documented using [JavaDoc](/javadoc_latest/index.html).


# Common Errors

## Building a combined jar with jena dependencies

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
Or this:
```
java.lang.SecurityException: Invalid signature file digest for Manifest main attributes.
```
Or this:
```
Exception in thread "main" java.lang.ExceptionInInitializerError
	at ......
	at ......
Caused by: java.lang.NullPointerException
	at org.apache.jena.tdb.sys.EnvTDB.processGlobalSystemProperties(EnvTDB.java:33)
	at org.apache.jena.tdb.TDB.init(TDB.java:254)
	at org.apache.jena.tdb.sys.InitTDB.start(InitTDB.java:29)
	at org.apache.jena.sys.JenaSystem.lambda$init$2(JenaSystem.java:117)
	at java.util.ArrayList.forEach(ArrayList.java:1257)
	at org.apache.jena.sys.JenaSystem.forEach(JenaSystem.java:192)
	at org.apache.jena.sys.JenaSystem.forEach(JenaSystem.java:169)
	at org.apache.jena.sys.JenaSystem.init(JenaSystem.java:115)
	at org.apache.jena.rdf.model.ModelFactory.<clinit>(ModelFactory.java:49)
	... 2 more
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
                  <filters>
                    <filter>
                      <artifact>*:*</artifact>
                      <excludes>
                        <!-- Some jars are signed but shading breaks that. Don't include signing files. 
                        Otherwise you get: java.lang.SecurityException: Invalid signature file digest for Manifest main attributes.-->
                        <exclude>META-INF/*.SF</exclude>
                        <exclude>META-INF/*.DSA</exclude>
                        <exclude>META-INF/*.RSA</exclude>
                      </excludes>
                    </filter>
                  </filters>
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


## Using largebio dataset without unlimiting entity expansion

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
which sets the property for the current JVM execution (but not for child processes).



## ToolException when using SEALS Matcher

In case you get a `ToolException` in SEALS Matcher which somehow talks about:
```the functionality of called method is not supported```
then this is a hint that the provided SEALS package is not able to use an input alignment.
Within the MatcherSeals in MELT you can set an attribute `doNotUseInputAlignment` to `true`. 
This will not pass an input alignment to the SEALS client even when one is available.
This applies for example to LogMap.


## Evaluation of a docker based matcher: `ClassNotFoundException: com.github.dockerjava.core.DockerClientConfig`

In case you evaluate a web/docker based matcher and you get a warning like:
```
Caused by: java.lang.ClassNotFoundException: com.github.dockerjava.core.DockerClientConfig
	at java.net.URLClassLoader.findClass(URLClassLoader.java:382)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:355)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
```
Then you should include the following dependencies in your evaluation script:
```
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-core</artifactId>
    <version>3.2.7</version>
</dependency>
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-transport-httpclient5</artifactId>
    <version>3.2.7</version>
</dependency>
```
This can also help if other classes from the same namespace are missing:
- `com.github.dockerjava.core.DockerClientConfig`
- `com.github.dockerjava.api.async.ResultCallback`


## Evaluation of a docker based matcher: `java.lang.NoSuchMethodError: javax.servlet.ServletContext.createServlet`

This error is due to a depency clash of `servlet-api` dependencies. One way to resolve this is to delete one of the dependencies in the docker container.

This can be achieved as follows:


*Generic Template*
```bash
# first we load the image; the image name will be printed in the console
docker load -i {.tar.gz file}  

# let's create an updated image
docker build -t {image name} - <<END            
FROM {image name}
RUN rm /maven/lib/servlet-api-2.5.jar
END

# let's write a new .tar.gz file
docker save {image name} | gzip > {.tar.gz file}
```

*Concrete Example*
```bash
# first we load the image (the printed image name is 'alod2vecmatcher-1.0-web')
docker load -i ./alod2vecmatcher-1.0-web-latest.tar.gz

# let's create a new image
docker build -t alod2vecmatcher-1.0-web - <<END            
FROM alod2vecmatcher-1.0-web
RUN rm /maven/lib/servlet-api-2.5.jar
END

# let's write a new .tar.gz file
docker save alod2vecmatcher-1.0-web | gzip > alod2vecmatcher-1.0-web-latest.tar.gz
```


## Find dependencies in maven central with full class name

In case your previous project was not build with maven or the like, you need to find out which dependencies you need.
If you require a specific class, you can use the advanced search functionality of maven central localted at [https://search.maven.org](https://search.maven.org).
Just click on advanced options, and you see that you can enter `fc:` followed by the full class name.
E.g. search for [`fc:de.uni_mannheim.informatik.dws.melt.matching_owlapi.MatcherYAAAOwlApi`](https://search.maven.org/search?q=fc:de.uni_mannheim.informatik.dws.melt.matching_owlapi.MatcherYAAAOwlApi). Then you get all artifacts which contains this class.

## Install jar file locally which is not in maven central

In case you have a specific jar file which is not available in maven central but is necessary for your matcher to run,
then you need to install it in your local repository.
You need to 'invent' a group id, artifact id, and version which is later used in your project.
To install such a jar file, execute:
``` 
mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=jar`
```
For more information see [Guide to installing 3rd party JARs](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html).

As an example:
You have a jar file named `mycustomjarfile.jar`. Now invent some group and artifact id for it and install it by running the following command in the folder where the jar file is located:
``` 
mvn install:install-file -Dfile=mycustomjarfile.jar -DgroupId=com.example.ownjarfile -DartifactId=mycustomjarfile -Dversion=1.0 -Dpackaging=jar`
```

You can then depend on that jar file within your project with:

```
<dependency>
    <groupId>com.example.ownjarfile</groupId>
    <artifactId>mycustomjarfile</artifactId>
    <version>1.0</version>
</dependency>
```


