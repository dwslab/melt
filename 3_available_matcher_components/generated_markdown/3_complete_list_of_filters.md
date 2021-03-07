---
layout: default
title: Complete List of Filters
parent: Available Matchers
nav_order: 3
permalink: /matcher-components/full-filter-list
---
# Complete List of Filters
## Filter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_base/Filter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/Filter.java)

Interface for filters. A filter is a matcher that does not add new correspondences to the alignment but instead
 further processes the given alignment by (1) removing correspondences and/or (2) adding new feature weights to
 existing correspondences.

*Keywords: Filter*

## ConfidenceCombiner [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ConfidenceCombiner.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ConfidenceCombiner.java)

Combines the additional confidences and set the overall correspondence confidence to be the mean of the selected confidences.
 Can also be used to set the

*Keywords: Confidence Combiner*

## ScaleConfidence [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleConfidence.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleConfidence.java)

Scales the correspondence confidence values linearly to an given interval (by default [0,1]).

*Keywords: Scale Confidence*

## ScaleAdditionalConfidence [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleAdditionalConfidence.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleAdditionalConfidence.java)

Scales the additional correspondence confidence values (that were produced by other filters/matchers) linearly to a
 given interval (by default [0,1]). Each additional confidence is scaled separately and only the specified
 additional confidences are scaled. If all of them should be scaled, then leave the set of keys empty.

*Keywords: Scale Additional Confidence*

## CardinalityFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/CardinalityFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/CardinalityFilter.java)

This filter returns only the alignments with the highest confidence if there are n-to-m matched elements.
 This might not be the best solution.

*Keywords: Cardinality Filter*

## ConfidenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceFilter.java)

This filter returns only alignments with confidence greater or equals than a
 specific threshold. Default is 0.9.

 Thresholds can be set per type.

*Keywords: Confidence Filter*

## BaseFilterWithSetComparison [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BaseFilterWithSetComparison.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BaseFilterWithSetComparison.java)

Basic filter for instances which compares sets like neighbours or properties.

*Keywords: Base Filter With Set Comparison*

## MixedTypFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/MixedTypFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/MixedTypFilter.java)

Asserts a homogenous alignment (i.e. only the same type is matched).
 For instance, correspondences between instances and classes will be deleted.

 Not Allowed (examples):
 - class, instance
 - datatype property, object property
 - rdf property, datatype property

 Allowed are only exact matches.

*Keywords: Mixed Typ Filter*

## AnnonymousNodeFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AnnonymousNodeFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AnnonymousNodeFilter.java)

This filter removes correspondences where the source or target has not the same host of the OntModels.
 E.g. it removes rdf:type=rdf:type or foaf:knows=foaf:knows

*Keywords: Annonymous Node Filter*

## AdditionalConfidenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AdditionalConfidenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AdditionalConfidenceFilter.java)

It filters based on the additional confidence. The key and threshold should be provided.

*Keywords: Additional Confidence Filter*

## BadHostsFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BadHostsFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BadHostsFilter.java)

This filter removes correspondences where the source or target has not the same host of the OntModels.
 E.g. it removes rdf:type=rdf:type or foaf:knows=foaf:knows

*Keywords: Bad Hosts Filter*

## ReflexiveCorrespondenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ReflexiveCorrespondenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ReflexiveCorrespondenceFilter.java)

Removes all reflexive edges (which maps A to A) from an alignment.

*Keywords: Reflexive Correspondence Filter*

## TypeFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/TypeFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/TypeFilter.java)

Filters only class, instance or property matches.

*Keywords: Type Filter*

## HungarianExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/HungarianExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/HungarianExtractor.java)

This implementation uses the Hungarian algorithm to find a one to one mapping.
 The runtime highly depends on the lower number of concepts (source or target) of the alignment as well as the number of correspondences.
 If a better runtime is needed, use MaxWeightBipartiteExtractor.

*Keywords: Hungarian Extractor*

## NaiveDescendingExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveDescendingExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveDescendingExtractor.java)

Naive descending extraction as shown in "Analyzing Mapping Extraction Approaches" (C. Meilicke, H. Stuckenschmidt).
 It iterates over the sorted (descending) correspondences and and uses the correspondence with the highest confidence.
 Afterwards removes every other correspondence with the same source or target.
 Previously it was called CardinalityFilter aka GreedyExtractor.

*Keywords: Naive Descending Extractor*

## NaiveAscendingExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveAscendingExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveAscendingExtractor.java)

Naive ascending extraction as shown in "Analyzing Mapping Extraction Approaches" (C. Meilicke, H. Stuckenschmidt).
 It iterates over the sorted (ascending) correspondences and and uses the correspondence with the highest confidence.
 Afterwards removes every other correspondence with the same source or target.

*Keywords: Naive Ascending Extractor*

## MaxWeightBipartiteExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/MaxWeightBipartiteExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/MaxWeightBipartiteExtractor.java)

Faster implementation than HungarianExtractor for generating a one-to-one alignment.
 The implementation is based on http://www.mpi-inf.mpg.de/~mehlhorn/Optimization/bipartite_weighted.ps (page 13-19).

*Keywords: Max Weight Bipartite Extractor*

## BagOfWordsSetSimilarityFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/BagOfWordsSetSimilarityFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/BagOfWordsSetSimilarityFilter.java)

Filters individual/instance mappings by comparing literals.
 The literals are selected by the corresponding properties (leave empty to select all).
 The set of tokens created for each individual are compared with the SetSimilarity.

*Keywords: Bag Of Words Set Similarity Filter*

## SimilarTypeFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarTypeFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarTypeFilter.java)

Checks for each instance mapping, how many already matched types it has in common.
 For comparing a type hierarchy, choose SimilarHierarchyFilter.

*Keywords: Similar Type Filter*

## SimilarNeighboursFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarNeighboursFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarNeighboursFilter.java)

Checks for each instance mapping, how many already matched neighbours it has.
 
 Source_Subject ------Source_Property------Source_Object
      |                                         |
 subjectCorrespondence                     objectCorrespondence
      |                                         |
 Target_Subject ------Target_Property------Target_Object

*Keywords: Similar Neighbours Filter*

## CommonPropertiesFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/CommonPropertiesFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/CommonPropertiesFilter.java)

Filter which deletes instance mappings if they have no matched properties in common.

*Keywords: Common Properties Filter*

## SimilarHierarchyFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarHierarchyFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarHierarchyFilter.java)

Check if already matched individuals have a similar hierarchy (class hierarchy).
 For different computation methods see SimilarHierarchyFilterApproach.

*Keywords: Similar Hierarchy Filter*

## MachineLearningScikitFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/MachineLearningScikitFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/MachineLearningScikitFilter.java)

This filter learns and applies a classifier given a training sample and an existing alignment.

*Keywords: Machine Learning Scikit Filter*

