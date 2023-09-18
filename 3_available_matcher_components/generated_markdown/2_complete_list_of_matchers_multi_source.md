---
layout: default
title: Complete List of Multi Source Matchers
parent: Available Matchers
nav_order: 2
permalink: /matcher-components/full-matcher-multi-source-list
---
# Complete List of Multi Source Matchers
## MatcherMultiSourceURL [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/MatcherMultiSourceURL.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/MatcherMultiSourceURL.java)

Multi source matcher which expects URLs as parameters. Better do not use this class but implement the interface IMatcherMultiSource.
 Subclasses of this class also try to implement this interface.

*Keywords: Matcher Multi SourceURL*

## IMatcherMultiSource [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSource.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSource.java)

Generic matcher interface for matching multiple ontologies / knowledge graphs.
 It gets multiple ontologies / knowledge graphs, an input alignment and additional parameters.

*Keywords: I Matcher Multi Source*

## MultiSourcePipelineSequential [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/MultiSourcePipelineSequential.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/MultiSourcePipelineSequential.java)

Executes all multi source matchers one after the other.

*Keywords: Multi Source Pipeline Sequential*

## IMatcherMultiSourceCaller [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSourceCaller.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSourceCaller.java)

Generic matcher interface for matching multiple ontologies / knowledge graphs which calls other matchers itself.
 It gets multiple ontologies / knowledge graphs, an input alignment and additional parameters.

*Keywords: I Matcher Multi Source Caller*

## MatcherMultiSourceYAAAJena [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/multisource/MatcherMultiSourceYAAAJena.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/multisource/MatcherMultiSourceYAAAJena.java)



*Keywords: Matcher Multi SourceYAAA Jena*

## FilterByErrorDegree [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/clustering/FilterByErrorDegree.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/clustering/FilterByErrorDegree.java)

This filter of correspondences is based on the community structure of the correspondences.
 E.g. if many entities are fully connected, then this indicates that all of those correspondences are correct.
 But if tehre are some weakly connected entites, then this might indicate wrong correspondences.

*Keywords: Filter By Error Degree*

## FamerClustering [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/clustering/FamerClustering.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/clustering/FamerClustering.java)

A filter for multi source matching.
 It filters the input alignment by analyzing the structure of the correspondences.
 E.g. if many entities are fully connected, then this indicates that all of those correspondences are correct.
 More information on all possible algorithmn which should be chosen in the constructor can be found at <a href="https://dbs.uni-leipzig.de/file/eswc_0.pdf">Scalable Matching and Clustering of Entities with FAMER</a> .
 <a href="https://git.informatik.uni-leipzig.de/dbs/FAMER/-/tree/master/famer-clustering">The source code can be found at gitlab</a>.

*Keywords: Famer Clustering*

## MultiSourceDispatcherIncrementalMergeByOrder [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByOrder.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByOrder.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge By Order*

## MultiSourceDispatcherTransitivePairsTextBased [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherTransitivePairsTextBased.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherTransitivePairsTextBased.java)

This dispatcher will compare the texts in a model and match the ones which are textually the clostest such that a connection between all ontologies exists.
 Therefore exactly (number of models)-1 matching operations and no merges are executed.

*Keywords: Multi Source Dispatcher Transitive Pairs Text Based*

## MultiSourceDispatcherIncrementalMerge [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMerge.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMerge.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge*

## MultiSourceDispatcherTransitivePairsOrderBased [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherTransitivePairsOrderBased.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherTransitivePairsOrderBased.java)

This dispatcher will match multiple ontologies by selecting a few pairs.
 First the ontologies will be sorted by a given comparator.
 As an example A, B, C, D.
 Afterwards two possible matching strategies are possible:
 <ul>
 <li>firstVsRest (constructor parameter) is true: it will match (A,B) ; (A,C) ; (A,D)</li>
 <li>firstVsRest (constructor parameter) is false: it will match (A,B) ; (B,C) ; (C,D)</li>
 </ul>
 Some comparators can be found at MultiSourceDispatcherIncrementalMergeByOrder as static attributes.

*Keywords: Multi Source Dispatcher Transitive Pairs Order Based*

## MultiSourceDispatcherIncrementalMergeByCluster [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByCluster.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByCluster.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge By Cluster*

## MultiSourceDispatcherIncrementalMergeByClusterText [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByClusterText.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByClusterText.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge By Cluster Text*

## MultiSourceDispatcherUnionToUnion [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherUnionToUnion.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherUnionToUnion.java)



*Keywords: Multi Source Dispatcher Union To Union*

## MultiSourceDispatcherAllPairs [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherAllPairs.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherAllPairs.java)



*Keywords: Multi Source Dispatcher All Pairs*

---
<sub>automatically generated on 2023-09-18 14:46</sub>
