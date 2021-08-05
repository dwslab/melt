---
layout: default
title: Local Tracks
parent: Matcher Evaluation
nav_order: 4
permalink: /matcher-evaluation/local-track
---

# Evaluation of a Local Track

You can also evaluate matcher against a local track i.e. a track which is no public available but only exists on your disk.
To create a local track, all test cases need to exist in a folder structure similar to the following one:

```
track-name
├── test-case-name-one
│   ├── source.rdf
│   ├── target.rdf
│   ├── reference.rdf
├── test-case-name-two
│   ├── source.rdf
│   ├── target.rdf
│   ├── reference.rdf 
```
You can add as many test cases you want.
The root folder represents the track whereas the child folders of it represents the test cases.
The name of the test case fodlers directly correspond to the name of the test case. The name of the track is given in the constructor of the [`LocalTrack`](https://github.com/dwslab/melt/blob/master/matching-data/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_data/LocalTrack.java).
In each of the test case folders, the following files can exist:
- a source file which needs to be called `source.rdf`
- a target file which needs to be called `target.rdf`
- a reference alignment file (in [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html)) which needs to be called `reference.rdf`
- a parameters file file which needs to be called `parameters.rdf`

The source and target files are neccessary whereas the reference alignment and parameters files are optional.
For a proper evaluation the reference alignment should be specified.
The format of the source and target can be any RDF serialization format as long as the matcher is able to parse it.
Many matchers use Jena or OWLAPI. Thus it is a good idea to have a look at their parsers.
The parameters file contains additional key value pairs for the matcher. Some keys are already specified in file [ParameterConfigKeys](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/ParameterConfigKeys.java)
but any other key can be used as well. Despite the file extension `.rdf` of the parameters file, the content can be formatted as [json or yaml](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/typetransformer/basetransformers/URL2PropertiesTransformer.java).

When creating the [LocalTrack](https://github.com/dwslab/melt/blob/master/matching-data/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_data/LocalTrack.java)
object, the name, version and the path (File object) to the root folder is required.


## Execution of a Matching System with Two Ontologies

If you only want to excute a matcher with two local ontologies, you can directly call the matcher with the required arguments.
Most matchers only expect a URL which can be a URL pointing to a file.
For example to call a SEALS matcher you can run the following:

```java
File ontoOne = new File("onto1.rdf");
File ontoTwo = new File("onto2.rdf");        
MatcherSeals matcher = new MatcherSeals(new File("./mysealsMatcher.zip"));
URL alignmentFile = matcher.align(ontoOne.toURI().toURL(), ontoTwo.toURI().toURL());
```
