---
layout: default
title: Complete List of Matchers
parent: Available Matchers
nav_order: 1
permalink: /matcher-components/full-matcher-list
---
# Complete List of Matchers
## MatcherURL [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherURL.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherURL.java)

RawMatcher which implements the minimal interface for being executed under
 the SEALS platform. The only method which should be implemented is the
 align(URL, URL, URL) method.

Keywords: MatcherURL

## MatcherCombination [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherCombination.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherCombination.java)

Combines multiple matchers.
 This can be very inefficient because the alignment has to be serialized after each matcher.
 Better use a more specialized MatcherCombination like: TODO

Keywords: Matcher Combination

## IMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcher.java)

Generic matcher interface which just implements one method called match.
 It gets a source and a target ontology / knowledge graph, an input alignment and additional parameters.

Keywords: I Matcher

## IMatcherCaller [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcherCaller.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/IMatcherCaller.java)

A matcher interface which allows the matcher to call other matchers as well.

Keywords: I Matcher Caller

## MatcherFile [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherFile.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherFile.java)

For this matcher the results file that shall be written can be specified.

Keywords: Matcher File

## MatcherString [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherString.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherString.java)



Keywords: Matcher String

## MatcherPipelineSequential [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherPipelineSequential.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/MatcherPipelineSequential.java)

Executes all matchers one after the other.

Keywords: Matcher Pipeline Sequential

## MatcherHTTPCall [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/http/MatcherHTTPCall.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/http/MatcherHTTPCall.java)

This class wraps a matcher service.

Keywords: MatcherHTTP Call

## MatcherSeals [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/seals/MatcherSeals.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/seals/MatcherSeals.java)

This matcher wraps the SEALS client such that a SEALS zip file or folder can be executed.
 If multiple matcher should be instantiated, have a look at MatcherSealsBuilder buildFromFolder.

Keywords: Matcher Seals

## MatcherCLIFromFile [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLIFromFile.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLIFromFile.java)

Read the file "external/external_command.txt" and start an external process. The whole content of the file is used and newlines are ignored.
 For replacements in this string have a look at MatcherCLI#getCommand() 

Keywords: MatcherCLI From File

## MatcherCLI [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLI.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/external/cli/MatcherCLI.java)

Matcher for running external matchers (require the subclass to create a command to execute).

Keywords: MatcherCLI

## ReferenceMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_eval/util/ReferenceMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-eval/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_eval/util/ReferenceMatcher.java)

A matcher which tries to detect the testcase and return the reference alignment.
 This matcher is only for testing purposes.

Keywords: Reference Matcher

## MatcherYAAAJena [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAAJena.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAAJena.java)

A matcher template for matchers that are based on Apache Jena.

Keywords: MatcherYAAA Jena

## MatcherPipelineYAAAJena [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJena.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJena.java)

Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc

Keywords: Matcher PipelineYAAA Jena

## MatcherPipelineYAAAJenaConstructor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJenaConstructor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAAJenaConstructor.java)

Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc

Keywords: Matcher PipelineYAAA Jena Constructor

## MatcherPipelineYAAA [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAA.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherPipelineYAAA.java)

A matcher template for matchers that are based on YAAA.

Keywords: Matcher PipelineYAAA

## MatcherYAAA [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAA.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/MatcherYAAA.java)

A matcher template for matchers that are based on the YAAA Framework.

Keywords: MatcherYAAA

## MatchPropBasedOnClass [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/structurelevel/MatchPropBasedOnClass.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/structurelevel/MatchPropBasedOnClass.java)

Graph-based Matcher: Checks all matched classes and matches also properties
 between them (domain and range) with mean value of both classes.


 Example:
 
```
foo <---already matched with c=0.5---> foo
      |                                      |
     blub <--new with c=(0.5+0.4)/2=0.45--> bla 
      |                                      |
      v                                      v
    bar <----already matched with c=0.4---> bar
  
```


  In the example blub and bla are properties.

Keywords: Match Prop Based On Class

## StopwordExtraction [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StopwordExtraction.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StopwordExtraction.java)

Extracts corpus dependent stopwords from instances, classes and properties.

Keywords: Stopword Extraction

## StringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/StringMatcher.java)



Keywords: String Matcher

## SynonymTextMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/SynonymTextMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/SynonymTextMatcher.java)

Matches resource A (source) to B (target) iff they have at least one label in the same synset.
 The text used for the resources can be defined (e.g. rdfs:label etc).
 The processing can also be adjusted by subclassing this class and override method processString.

Keywords: Synonym Text Matcher

## ExactStringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/ExactStringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/ExactStringMatcher.java)

Matcher which creates correspondences based on exact string match.

Keywords: Exact String Matcher

## BaselineStringMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/BaselineStringMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/elementlevel/BaselineStringMatcher.java)

A very basic string matcher that can be used as baseline for matchers.

Keywords: Baseline String Matcher

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


Keywords: Scalable String Processing Matcher

## MatchPropBasedOnInstances [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchPropBasedOnInstances.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchPropBasedOnInstances.java)

Matches properties based on same subject and object and the distribution.

Keywords: Match Prop Based On Instances

## MatchClassBasedOnInstances [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchClassBasedOnInstances.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/instancelevel/MatchClassBasedOnInstances.java)

A matcher which matches classes based on already instance matches.

Keywords: Match Class Based On Instances

## ParisMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/ParisMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/wrapper/ParisMatcher.java)

Call the paris matcher.

Keywords: Paris Matcher

## AlignmentSaveMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AlignmentSaveMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AlignmentSaveMatcher.java)

Just saves the ontologies in a specific format.

Keywords: Alignment Save Matcher

## AdditionalConfidenceByFunction [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AdditionalConfidenceByFunction.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/AdditionalConfidenceByFunction.java)

Adds an additional confidence by a user chosen function which gets a ontResource and has to return a double.
 The value will be computed for source and target.
 If you want to add confidences for a correspondence then implement your own matcher and iterate over the alignment.

Keywords: Additional Confidence By Function

## ForwardMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ForwardMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ForwardMatcher.java)

This is a simple matcher that forwards a given alignment.

Keywords: Forward Matcher

## NoOpMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/NoOpMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/NoOpMatcher.java)

Matcher which does nothing but returning a valid empty alignment.

Keywords: No Op Matcher

## MachineLearningWEKAFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/MachineLearningWEKAFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/MachineLearningWEKAFilter.java)

Non functional code.

Keywords: Machine LearningWEKA Filter

## FileSaveMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/FileSaveMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/FileSaveMatcher.java)

Just saves the ontologies in a specific format.

Keywords: File Save Matcher

## VectorSpaceModelMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/VectorSpaceModelMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/VectorSpaceModelMatcher.java)

Updates the confidence of already matched resources.
 It writes a textual representation of each resource to a csv file (text generation can be modified by subclassing and overriding getResourceText method).

Keywords: Vector Space Model Matcher

## DocumentSimilarityBase [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/DocumentSimilarityBase.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/DocumentSimilarityBase.java)

A base class for all matchers which write a csv file where every line 
 represents a resource with with cell as identifier like URI and 
 second cell the corresponding tokens (whitespace separated).

Keywords: Document Similarity Base

## Doc2vecModelMatcher [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/Doc2vecModelMatcher.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/Doc2vecModelMatcher.java)

Updates the confidence of already matched resources.
 It writes a textual representation of each resource to a csv file (text generation can be modified by subclassing and overriding getResourceText method).

Keywords: Doc2vec Model Matcher

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

Keywords: OpenEA Matcher

## MatcherYAAAOwlApi [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherYAAAOwlApi.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherYAAAOwlApi.java)



Keywords: MatcherYAAA Owl Api

## MatcherPipelineYAAAOwlApi [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherPipelineYAAAOwlApi.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi/MatcherPipelineYAAAOwlApi.java)

Better use MatcherYAAAPipeline because it can combine matchers which use different APIS like Jena and OWLAPI etc

Keywords: Matcher PipelineYAAA Owl Api

