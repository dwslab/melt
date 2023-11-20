---
layout: default
title: Complete List of Matchers
parent: Available Matchers
nav_order: 1
permalink: /matcher-components/full-matcher-list
---
# Complete List of Matchers
## IMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcher.java)

Generic matcher interface which just implements one method called match.
 It gets a source and a target ontology / knowledge graph, an input alignment and additional parameters.

*Keywords: I Matcher*

## MatcherURL [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherURL.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherURL.java)

RawMatcher which implements the minimal interface for being executed under
 the SEALS platform. The only method which should be implemented is the
 align(URL, URL, URL) method.

*Keywords: MatcherURL*

## MatcherString [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherString.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherString.java)



*Keywords: Matcher String*

## IMatcherCaller [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcherCaller.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcherCaller.java)

A matcher interface which allows the matcher to call other matchers as well.

*Keywords: I Matcher Caller*

## MatcherFile [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherFile.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherFile.java)

For this matcher the results file that shall be written can be specified.

*Keywords: Matcher File*

## MatcherPipelineSequential [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherPipelineSequential.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherPipelineSequential.java)

Executes all matchers one after the other.

*Keywords: Matcher Pipeline Sequential*

## MatcherCombination [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherCombination.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherCombination.java)

Combines multiple matchers.
 This can be very inefficient because the alignment has to be serialized after each matcher.
 Better use a more specialized MatcherCombination like: TODO

*Keywords: Matcher Combination*

## SealsWrapper [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/receiver/SealsWrapper.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/receiver/SealsWrapper.java)

This class implements the SEALS interface (via MatcherURL) and calls the provided matcher class
 (the matcher class is provided via a file in the SEALS package in folder /conf/extenal/main_class.txt ).
 If this class is renamed or moved, then the name needs to be adjusted in matching assembly project
 in file SealsDescriptorHandler.java (method finalizeArchiveCreation - line 45).

*Keywords: Seals Wrapper*

## MatcherDockerFile [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/docker/MatcherDockerFile.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/docker/MatcherDockerFile.java)

This matcher creates a docker container based on a given docker image name.
 Within this container a matcher server should be started.
 Therefore it will use the MatcherHTTPCall internally to run the matcher.
 For this Matcher to work you have to add the following dependency to YOUR pom:
 
```
 
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-core</artifactId>
    <version>3.2.7</version><!--maybe update version-->
</dependency>
<dependency>
    <groupId>com.github.docker-java</groupId>
    <artifactId>docker-java-transport-httpclient5</artifactId>
    <version>3.2.7</version><!--maybe update version-->
</dependency>
 
```

With this in place everything should work.

*Keywords: Matcher Docker File*

## MatcherSeals [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/seals/MatcherSeals.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/seals/MatcherSeals.java)

This matcher wraps the SEALS client such that a SEALS zip file or folder can be executed.
 If multiple matcher should be instantiated, have a look at MatcherSealsBuilder buildFromFolder.

*Keywords: Matcher Seals*

## MatcherCLI [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLI.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLI.java)

Matcher for running external matchers (require the subclass to create a command to execute).

*Keywords: MatcherCLI*

## MatcherCLIFromFile [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLIFromFile.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLIFromFile.java)

Read the file "external/external_command.txt" and start an external process. The whole content of the file is used and newlines are ignored.
 For replacements in this string have a look at MatcherCLI#getCommand() 

*Keywords: MatcherCLI From File*

## MatcherHTTPCall [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/http/MatcherHTTPCall.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/http/MatcherHTTPCall.java)

This class wraps a matcher service.

*Keywords: MatcherHTTP Call*

## ReferenceMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_eval/util/ReferenceMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/util/ReferenceMatcher.java)

A matcher which tries to detect the testcase and return the reference alignment.
 This matcher is only for testing purposes.

*Keywords: Reference Matcher*

## AddPositivesWithReference [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_eval/matchers/AddPositivesWithReference.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/matchers/AddPositivesWithReference.java)

This matcher will detect the test case given in the input and 
 use the reference (gold standard) to sample from it with the given rate which is added to the input alignment.
 This matcher is contained in the eval package because it uses the information from the reference.
 This matcher should not be included in real matching systems.

*Keywords: Add Positives With Reference*

## MatcherYAAA [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAA.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAA.java)

A matcher template for matchers that are based on the YAAA Framework.

*Keywords: MatcherYAAA*

## MatcherPipelineYAAA [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAA.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAA.java)

A matcher template for matchers that are based on YAAA.

*Keywords: Matcher PipelineYAAA*

## MatcherPipelineYAAAJena [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJena.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJena.java)

Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc

*Keywords: Matcher PipelineYAAA Jena*

## MatcherPipelineYAAAJenaConstructor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJenaConstructor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJenaConstructor.java)

Better use MatcherPipelineYAAA because it can combine matchers which use different APIS like Jena and
 OWLAPI etc.

*Keywords: Matcher PipelineYAAA Jena Constructor*

## MatcherYAAAJena [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAAJena.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAAJena.java)

A matcher template for matchers that are based on Apache Jena.

*Keywords: MatcherYAAA Jena*

## AddNegativesRandomly [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomly.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomly.java)

Abstract class which is the base class for all AddNegatives which are based on random sampling.

*Keywords: Add Negatives Randomly*

## AddNegativesRandomlyOneOneAssumption [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomlyOneOneAssumption.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomlyOneOneAssumption.java)

This component adds negative samples to the alignment.
 For each positive correspondences a configurable number of negative correspondences are added.

*Keywords: Add Negatives Randomly One One Assumption*

## AddNegativesRandomlyAbsolute [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomlyAbsolute.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomlyAbsolute.java)

This component adds negative samples to the alignment.
 The number of negative samples is defined by an absolute number.

*Keywords: Add Negatives Randomly Absolute*

## AddNegativesViaAlignment [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesViaAlignment.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesViaAlignment.java)

This component adds negative correspondences to the input alignment via an alignment (generated by a recall optimized matcher).
 This matcher is only suitable if the whole matching system is executed only for one test case.
 Why? Because the alignment (which is test case specific) need to be passed in the constructor.
 Thus it will only give useful results if only used for the correct test case.
 
 The input alignment should contain positive correspondences.
 The negatives are generated by looking at the provided alignment in the constructor.
 All correspondences which are contained in the input alignment (ground truth) and in the given alignment
 (which has a high recall - meaning many correspondences) are assumned to be positives and all
 others correspondences which maps a concept of the positive correspondence  to another concept are assumed to be negatives.
 
 The returned alignment consists of positives (= relation) and negatives (% incompat relation) which can be used to 
 train supervised matchers.

*Keywords: Add Negatives Via Alignment*

## AddNegativesViaMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesViaMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesViaMatcher.java)

This component adds negative correspondences to the input alignment via a recall optimized matcher.
 This also means that the input alignment should contain positive correspondences.
 After applying the recallMatcher given in the constructor, a new alignment is returned which
 contains positive (equivalence relation) and negative(incompat relation) correspondences.
 With the help of this alignment, supervised matchers can be trained.

*Keywords: Add Negatives Via Matcher*

## AddNegativesRandomlyShare [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomlyShare.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives/AddNegativesRandomlyShare.java)

This component adds negative samples to the alignment.
 Negatives are added as long as the share of negatives is not fullfilled.

*Keywords: Add Negatives Randomly Share*

## UriInterfaceWrapper [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/UriInterfaceWrapper.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/UriInterfaceWrapper.java)

This matcher implements the URL interface and wrapps a MatcherYAAA.
 This is useful for ParisMatcher if the input is already a NT file.

*Keywords: Uri Interface Wrapper*

## ParisMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/ParisMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/ParisMatcher.java)

This is a wrapper for <a href="http://webdam.inria.fr/paris/">PARIS matching system</a> by Fabian Suchanek et al.
 The corresponding paper is called <a href="https://arxiv.org/abs/1111.7164">PARIS: Probabilistic Alignment of Relations, Instances, and Schema</a>.
 It will download the matcher if not already done and execute it as an external process. The equivalence files of the last iteration 
 are then read into a YAAA aligment. It is tested to run with java 1.7 and 1.8.

*Keywords: Paris Matcher*

## MatchPropBasedOnInstances [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchPropBasedOnInstances.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchPropBasedOnInstances.java)

Matches properties based on same subject and object and the distribution.

*Keywords: Match Prop Based On Instances*

## MatchClassBasedOnInstances [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchClassBasedOnInstances.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchClassBasedOnInstances.java)

A matcher which matches classes based on already instance matches.

*Keywords: Match Class Based On Instances*

## MatchPropBasedOnClass [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/structurelevel/MatchPropBasedOnClass.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/structurelevel/MatchPropBasedOnClass.java)

Graph-based Matcher: Checks all matched classes and matches also properties
 between them (domain and range) with mean value of both classes.
 Example:
 
     foo  ---already matched with c=0.5---  foo
      |                                      |
     blub  --new with c=(0.5+0.4)/2=0.45--  bla 
      |                                      |
      v                                      v
     bar  ----already matched with c=0.4--- bar
 

  In the example blub and bla are properties.

*Keywords: Match Prop Based On Class*

## BoundedPathMatching [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/structurelevel/BoundedPathMatching.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/structurelevel/BoundedPathMatching.java)

Structure based matcher which allows to find matches in hierarchies which are between two already matched entities.
 Example:
 
     SourceSuperClass  ---already matched with c=0.8---  TargetSuperClass
      ^                                                         ^
      |                                                         |
    rdfs:subClassOf                                         rdfs:subClassOf
      |                                                         |
  SourceIntermediateClass  --new with c=(0.8+0.9)/2=0.85-- TargetIntermediateClass 
      ^                                                         ^
      |                                                         |
    rdfs:subClassOf                                         rdfs:subClassOf
      |                                                         |
     SourceSubclass  ----already matched with c=0.9---    TargetSubclass
 

 Per default it only matches the class hierarchy but it can be customized for further
 hierarchies like properties via the configurations object within this class.

*Keywords: Bounded Path Matching*

## MachineLearningWEKAFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/MachineLearningWEKAFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/MachineLearningWEKAFilter.java)

Non functional code.

*Keywords: Machine LearningWEKA Filter*

## AlignmentSaveMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AlignmentSaveMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AlignmentSaveMatcher.java)

Just saves the ontologies in a specific format.

*Keywords: Alignment Save Matcher*

## NoOpMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/NoOpMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/NoOpMatcher.java)

Matcher which does nothing but returning a valid empty alignment.

*Keywords: No Op Matcher*

## AddAlignmentExtensions [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AddAlignmentExtensions.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AddAlignmentExtensions.java)

Adds the provided extensions to the alignment when the matcher is executed.

*Keywords: Add Alignment Extensions*

## AdditionalConfidenceByFunction [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AdditionalConfidenceByFunction.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AdditionalConfidenceByFunction.java)

Adds an additional confidence by a user chosen function which gets a ontResource and has to return a double.
 The value will be computed for source and target.
 If you want to add confidences for a correspondence then implement your own matcher and iterate over the alignment.

*Keywords: Additional Confidence By Function*

## ForwardAlwaysMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ForwardAlwaysMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ForwardAlwaysMatcher.java)

This is a simple matcher that forwards a given alignment always (even if the input alignment is available).
 In case the input alignment should be used when availabel, use ForwardMatcher .

*Keywords: Forward Always Matcher*

## AddAlignmentMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AddAlignmentMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AddAlignmentMatcher.java)

This is a simple matcher that adds a given alignment to the inputAlignment.
 The given alignemnt has advantage.

*Keywords: Add Alignment Matcher*

## ForwardMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ForwardMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ForwardMatcher.java)

This is a simple matcher that forwards a given alignment if the input alignment is not available.
 In case you want to always use the given alignemnt even if the input alignment is available, then use
 ForwardAlwaysMatcher.

*Keywords: Forward Matcher*

## TrainingAlignmentGenerator [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/TrainingAlignmentGenerator.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/TrainingAlignmentGenerator.java)

This matcher assumes that the input alignment is a kind of reference alignment.
 After applying the recallMatcher given in the constructor, a new alignment is returned which
 contains positive (equivalence relation) and negative(incompat relation) correspondences.
 With the help of this alignment, supervised matchers can be trained.

*Keywords: Training Alignment Generator*

## FileSaveMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/FileSaveMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/FileSaveMatcher.java)

Just saves the ontologies in a specific format.

*Keywords: File Save Matcher*

## BackgroundMatcherStandAlone [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/matcher/BackgroundMatcherStandAlone.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/matcher/BackgroundMatcherStandAlone.java)

Matcher which applies String matching and matches then with the provided background knowledge source and strategy.

*Keywords: Background Matcher Stand Alone*

## BackgroundMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/matcher/BackgroundMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/matcher/BackgroundMatcher.java)

Template matcher where the background knowledge and the exploitation strategy (represented as ImplementedBackgroundMatchingStrategies) can be plugged-in.
 This matcher can be used as matching component. It is sensible to use a simple string matcher before running this
 matcher to increase the performance by filtering out simple matches. If you want a pre-packaged stand-alone
 background-based matching system, you can try out BackgroundMatcherStandAlone.
 <br>
 This matcher relies on a similarity metric that is implemented within the background source and used in
 BackgroundMatcher#compare(String, String).

*Keywords: Background Matcher*

## SimpleStringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/matcher/SimpleStringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/external/matcher/SimpleStringMatcher.java)

A relatively simple matcher that can be used before running BackgroundMatcher to filter out simple matches.

*Keywords: Simple String Matcher*

## StopwordExtraction [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StopwordExtraction.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StopwordExtraction.java)

Extracts corpus dependent stopwords from instances, classes and properties.

*Keywords: Stopword Extraction*

## BaselineStringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/BaselineStringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/BaselineStringMatcher.java)

A very basic string matcher that can be used as baseline for matchers.

*Keywords: Baseline String Matcher*

## StringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StringMatcher.java)



*Keywords: String Matcher*

## SynonymTextMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/SynonymTextMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/SynonymTextMatcher.java)

Matches resource A (source) to B (target) iff they have at least one label in the same synset.
 The text used for the resources can be defined (e.g. rdfs:label etc).
 The processing can also be adjusted by subclassing this class and override method processString.

*Keywords: Synonym Text Matcher*

## HighPrecisionMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/HighPrecisionMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/HighPrecisionMatcher.java)

A high precision matcher which focuses on URI fragment and label (only element based string comparison).
 It achives the following scores:
 
 | Track              | PREC   | REC    | F1     |
 |--------------------|--------|--------|--------|
 | anatomy            | 0.9905 | 0.6174 | 0.7607 |
 | conference (micro) | 0.8046 | 0.4590 | 0.5845 |
 | largebio           | 0.9911 | 0.4233 | 0.5932 |
 

*Keywords: High Precision Matcher*

## ExactStringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/ExactStringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/ExactStringMatcher.java)

Matcher which creates correspondences based on exact string match.

*Keywords: Exact String Matcher*

## ScalableStringProcessingMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/scale/ScalableStringProcessingMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/scale/ScalableStringProcessingMatcher.java)

Matcher which uses different String Matching approaches (stored in PropertySpecificStringProcessing) with a specific confidence.
 Multiple normalization are possible as shown below.
 The highest confidence is taken at the end.
 
```
 Function<String, Object> equality = (text) -> text;
 Function<String, Object> lowercase = (text) -> text.toLowerCase();
 ScalableStringProcessingMatcher matcherOne = new ScalableStringProcessingMatcher(Arrays.asList(
              new PropertySpecificStringProcessing(equality, 1.0, RDFS.label),
              new PropertySpecificStringProcessing(lowercase, 0.9, RDFS.label)
              new PropertySpecificStringProcessing(equality, 0.7, SKOS.altLabel),
              new PropertySpecificStringProcessing(lowercase, 0.6, SKOS.altLabel)
 ));
```


*Keywords: Scalable String Processing Matcher*

## Doc2vecModelMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/Doc2vecModelMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/Doc2vecModelMatcher.java)

Updates the confidence of already matched resources.
 It writes a textual representation of each resource to a csv file (text generation can be modified by subclassing and overriding getResourceText method).

*Keywords: Doc2vec Model Matcher*

## DocumentSimilarityBase [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/DocumentSimilarityBase.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/DocumentSimilarityBase.java)

A base class for all matchers which write a csv file where every line 
 represents a resource with with cell as identifier like URI and 
 second cell the corresponding tokens (whitespace separated).

*Keywords: Document Similarity Base*

## VectorSpaceModelMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/VectorSpaceModelMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/VectorSpaceModelMatcher.java)

Updates the confidence of already matched resources.
 It writes a textual representation of each resource to a csv file (text generation can be modified by subclassing and overriding getResourceText method).

*Keywords: Vector Space Model Matcher*

## SentenceTransformersMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/SentenceTransformersMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/SentenceTransformersMatcher.java)

This matcher uses the <a href="https://github.com/UKPLab/sentence-transformers">Sentence Transformers library</a> to build an embedding space for each resource given a textual representation of it.
 Thus this matcher does not filter anything but generates matching candidates based on the text.

*Keywords: Sentence Transformers Matcher*

## SentenceTransformersFineTuner [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/SentenceTransformersFineTuner.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/SentenceTransformersFineTuner.java)

This matcher uses the <a href="https://github.com/UKPLab/sentence-transformers">Sentence Transformers library</a> to build an embedding space for each resource given a textual representation of it.
 Thus this matcher does not filter anything but generates matching candidates based on the text.

*Keywords: Sentence Transformers Fine Tuner*

## TransformersBaseFineTuner [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersBaseFineTuner.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersBaseFineTuner.java)

This is a base class for all Transformers fine tuners.
 It just contains some variables and getter and setters.

*Keywords: Transformers Base Fine Tuner*

## TransformersBase [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersBase.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersBase.java)

This is a base class for all Transformers.
 It just contains some variables and getter and setters.

*Keywords: Transformers Base*

## OpenEAMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/openea/OpenEAMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/openea/OpenEAMatcher.java)

This matching module uses the <a href="https://github.com/nju-websoft/OpenEA">OpenEA</a> library to match entities.
 It uses all correspondences which are provided through either the constructor or match method(inputalignment)
 with equivalence relation to train the approach. It only need positive correspondences and samples negative correspondences on its own.
 <br>
 If you apply your own configuration you can use the parameters from <a href="https://github.com/nju-websoft/OpenEA/blob/master/run/main_with_args.py#L30">openEA</a> and also
 the following additional parameters:
 <ul>
 <li>predict_top_k - the number of matching entities which should at least retrived for one single entity</li>
 <li>predict_min_sim_value - the similarity measure which should be applied for every correspondence. All sim values are greater than the given one (not equal or greater).</li>
 </ul>

*Keywords: OpenEA Matcher*

## MatcherYAAA [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherYAAA.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherYAAA.java)

A matcher template for matchers that are based on the YAAA Framework.

*Keywords: MatcherYAAA*

## MatcherPipelineYAAAOwlApi [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherPipelineYAAAOwlApi.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherPipelineYAAAOwlApi.java)

Better use MatcherYAAAPipeline because it can combine matchers which use different APIs like Jena and OWLAPI etc.

*Keywords: Matcher PipelineYAAA Owl Api*

## MatcherYAAAOwlApi [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherYAAAOwlApi.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherYAAAOwlApi.java)



*Keywords: MatcherYAAA Owl Api*

---
<sub>automatically generated on 2023-11-20 14:34</sub>
