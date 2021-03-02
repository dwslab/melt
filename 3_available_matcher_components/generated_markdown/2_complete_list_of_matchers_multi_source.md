---
layout: default
title: Complete List of Multi Source Matchers
parent: Available Matchers
nav_order: 2
permalink: /matcher-components/full-matcher-multi-source-list
---
# Complete List of Multi Source Matchers
## IMatcherMultiSource [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSource.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSource.java)

Generic matcher interface for matching multiple ontologies / knowledge graphs.
 It gets multiple ontologies / knowledge graphs, an input alignment and additional parameters.

*Keywords: I Matcher Multi Source*

## MatcherMultiSourceURL [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/MatcherMultiSourceURL.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/MatcherMultiSourceURL.java)

Multi source matcher which expects URLs as parameters. Better do not use this class but implement the interface IMatcherMultiSource.
 Subclasses of this class also try to implement this interface.

*Keywords: Matcher Multi SourceURL*

## IMatcherMultiSourceCaller [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSourceCaller.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/multisource/IMatcherMultiSourceCaller.java)

Generic matcher interface for matching multiple ontologies / knowledge graphs which calls other matchers itself.
 It gets multiple ontologies / knowledge graphs, an input alignment and additional parameters.

*Keywords: I Matcher Multi Source Caller*

## MatcherMultiSourceYAAAJena [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena/multisource/MatcherMultiSourceYAAAJena.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena/multisource/MatcherMultiSourceYAAAJena.java)



*Keywords: Matcher Multi SourceYAAA Jena*

## MultiSourceDispatcherIncrementalMerge [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMerge.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMerge.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge*

## MultiSourceDispatcherIncrementalMergeByCluster [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByCluster.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByCluster.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge By Cluster*

## MultiSourceDispatcherAllPairs [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherAllPairs.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherAllPairs.java)



*Keywords: Multi Source Dispatcher All Pairs*

## MultiSourceDispatcherUnionToUnion [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherUnionToUnion.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherUnionToUnion.java)



*Keywords: Multi Source Dispatcher Union To Union*

## MultiSourceDispatcherIncrementalMergeByOrder [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByOrder.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByOrder.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge By Order*

## MultiSourceDispatcherIncrementalMergeByClusterText [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByClusterText.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/multisource/dispatchers/MultiSourceDispatcherIncrementalMergeByClusterText.java)

Matches multiple ontologies / knowledge graphs with an incremental merge approach.
 This means that two ontologies are merged together and then possibly the union is merged with another ontology and so on.
 The order how they are merged is defined by subclasses.

*Keywords: Multi Source Dispatcher Incremental Merge By Cluster Text*

