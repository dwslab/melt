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

## ScaleConfidence [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleConfidence.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleConfidence.java)

Scales the correspondence confidence values linearly to an given interval (by default [0,1]).

*Keywords: Scale Confidence*

## ConfidenceCombiner [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ConfidenceCombiner.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ConfidenceCombiner.java)

Combines the additional confidences and set the overall correspondence confidence to be the mean of the selected
 confidences.

*Keywords: Confidence Combiner*

## ScaleAdditionalConfidence [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleAdditionalConfidence.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/metalevel/ScaleAdditionalConfidence.java)

Scales the additional correspondence confidence values (that were produced by other filters/matchers) linearly to a
 given interval (by default [0,1]). Each additional confidence is scaled separately and only the specified
 additional confidences are scaled. If all of them should be scaled, then leave the set of keys empty.

*Keywords: Scale Additional Confidence*

## CardinalityFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/CardinalityFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/CardinalityFilter.java)

This filter returns only the alignments with the highest confidence if there are n-to-m matched elements.
 This might not be the best solution.

*Keywords: Cardinality Filter*

## BaseFilterWithSetComparison [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BaseFilterWithSetComparison.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BaseFilterWithSetComparison.java)

Basic filter for instances which compares sets like neighbours or properties.

*Keywords: Base Filter With Set Comparison*

## BadHostsFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BadHostsFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/BadHostsFilter.java)

This filter removes correspondences where the source or target has not the same host of the OntModels.
 E.g. it removes rdf:type=rdf:type or foaf:knows=foaf:knows

*Keywords: Bad Hosts Filter*

## AnonymousNodeFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AnonymousNodeFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AnonymousNodeFilter.java)

This filter removes correspondences where the source or target has not the same host of the OntModels.
 E.g. it removes rdf:type=rdf:type or foaf:knows=foaf:knows

*Keywords: Anonymous Node Filter*

## TypeFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/TypeFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/TypeFilter.java)

Filters only class, instance or property matches.

*Keywords: Type Filter*

## MixedTypFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/MixedTypFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/MixedTypFilter.java)

Asserts a homogenous alignment (i.e. only the same type is matched).
 For instance, correspondences between instances and classes will be deleted.

 Not Allowed (examples):
 - class, instance
 - datatype property, object property
 - rdf property, datatype property

 Allowed are only exact matches.

*Keywords: Mixed Typ Filter*

## ReflexiveCorrespondenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ReflexiveCorrespondenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ReflexiveCorrespondenceFilter.java)

Removes all reflexive edges (which maps A to A) from an alignment.

*Keywords: Reflexive Correspondence Filter*

## NtoMCorrespondenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/NtoMCorrespondenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/NtoMCorrespondenceFilter.java)

A filter which removes correspondences where source or target is matched to more than one entity.
 All such correspondences will be removed.
 As an example: if alignment looks like
 <ul>
 <li>A, B</li>
 <li>C, D</li>
 <li>C, E</li>
 <li>F, D</li>
 </ul>
 then the last three are removed because C and D are matched multiple times.

*Keywords: NtoM Correspondence Filter*

## ConfidenceCurvatureFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceCurvatureFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceCurvatureFilter.java)

Filters the alignment by computing the inflection point of the sorted
 confidences. To make it more stable a smoothing (spline interpolation) can be
 used. Furthermore also the elbow point of the confidences can be used to
 filter them.

*Keywords: Confidence Curvature Filter*

## TopXFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/TopXFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/TopXFilter.java)

This filter keeps only the top X correspondences according to confidence.
 The filter can be configured to be source-based (keep only the top X correspondences for each source node).
 The filter can be configured to be target-based (keep only the top X correspondences for each target node).
 The filter can be configured to be size-based (based on the smaller or larger side of the alignment).

*Keywords: TopX Filter*

## ConfidenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/ConfidenceFilter.java)

This filter returns only alignments with confidence greater or equals than a
 specific threshold. Default is 0.9.

 Thresholds can be set per type.

*Keywords: Confidence Filter*

## AdditionalConfidenceFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AdditionalConfidenceFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/AdditionalConfidenceFilter.java)

It filters based on the additional confidence. The key and threshold should be provided.

*Keywords: Additional Confidence Filter*

## CommonPropertiesFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/CommonPropertiesFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/CommonPropertiesFilter.java)

Filter which deletes instance mappings if they have no matched properties in common.

*Keywords: Common Properties Filter*

## SimilarNeighboursFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarNeighboursFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarNeighboursFilter.java)

Checks for each instance mapping, how many already matched neighbours it has.
 
 Source_Subject ------Source_Property------Source_Object
      |                                         |
 subjectCorrespondence                     objectCorrespondence
      |                                         |
 Target_Subject ------Target_Property------Target_Object
 

*Keywords: Similar Neighbours Filter*

## SimilarTypeFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarTypeFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarTypeFilter.java)

Checks for each instance mapping, how many already matched types it has in common.
 For comparing a type hierarchy, choose SimilarHierarchyFilter.

*Keywords: Similar Type Filter*

## BagOfWordsSetSimilarityFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/BagOfWordsSetSimilarityFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/BagOfWordsSetSimilarityFilter.java)

Filters individual/instance mappings by comparing literals.
 The literals are selected by the corresponding properties (leave empty to select all).
 The set of tokens created for each individual are compared with the SetSimilarity.

*Keywords: Bag Of Words Set Similarity Filter*

## SimilarHierarchyFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarHierarchyFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/instance/SimilarHierarchyFilter.java)

Check if already matched individuals have a similar hierarchy (class hierarchy).
 For different computation methods see SimilarHierarchyFilterApproach.

*Keywords: Similar Hierarchy Filter*

## MaxWeightBipartiteExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/MaxWeightBipartiteExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/MaxWeightBipartiteExtractor.java)

Faster implementation than HungarianExtractor for generating a one-to-one alignment.
 The implementation is based on http://www.mpi-inf.mpg.de/~mehlhorn/Optimization/bipartite_weighted.ps (page 13-19).

*Keywords: Max Weight Bipartite Extractor*

## NaiveAscendingExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveAscendingExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveAscendingExtractor.java)

Naive ascending extraction as shown in "Analyzing Mapping Extraction Approaches" (C. Meilicke, H. Stuckenschmidt).
 It iterates over the sorted (ascending) correspondences and and uses the correspondence with the highest confidence.
 Afterwards removes every other correspondence with the same source or target.

*Keywords: Naive Ascending Extractor*

## NaiveDescendingExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveDescendingExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/NaiveDescendingExtractor.java)

Naive descending extraction as shown in "Analyzing Mapping Extraction Approaches" (C. Meilicke, H. Stuckenschmidt).
 It iterates over the sorted (descending) correspondences and and uses the correspondence with the highest confidence.
 Afterwards removes every other correspondence with the same source or target.
 Previously it was called CardinalityFilter aka GreedyExtractor.

*Keywords: Naive Descending Extractor*

## HungarianExtractor [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/HungarianExtractor.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/filter/extraction/HungarianExtractor.java)

This implementation uses the Hungarian algorithm to find a one to one mapping.
 The runtime highly depends on the lower number of concepts (source or target) of the alignment as well as the number of correspondences.
 If a better runtime is needed, use MaxWeightBipartiteExtractor.

*Keywords: Hungarian Extractor*

## MachineLearningScikitFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/MachineLearningScikitFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/MachineLearningScikitFilter.java)

This filter learns and applies a classifier given a training sample and an existing alignment.

*Keywords: Machine Learning Scikit Filter*

## TransformersFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFilter.java)

This filter extracts the corresponding text for a resource (with the specified and customizable extractor) given all correspondences in the input alignment.
 The texts of the two resources are fed into the specified transformer model and the prediction is added in form of a confidence to the correspondence.
 No filtering is applied in this class.

*Keywords: Transformers Filter*

## TransformersFineTunerHpSearch [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFineTunerHpSearch.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFineTunerHpSearch.java)



*Keywords: Transformers Fine Tuner Hp Search*

## TransformersFineTuner [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFineTuner.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFineTuner.java)

This class is used to fine-tune a transformer model based on a generated dataset.
 In every call to the match method, the training data will be generated and appended to a temporary file.
 When you call the TransformersFineTuner#finetuneModel()  method, then a model is fine-tuned and the
 training file is deleted.

*Keywords: Transformers Fine Tuner*

## AlcomoFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi_matchers/AlcomoFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi_matchers/AlcomoFilter.java)

Filter which makes and alignment coherent. When using this component, please cite:
 Christian Meilicke. Alignment Incoherence in Ontology Matching. University Mannheim 2011.

 This filter wraps the original implementation.

*Keywords: Alcomo Filter*

## LogMapRepairFilter [Javadoc](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_owlapi_matchers/logmap/LogMapRepairFilter.html) / [Source Code](https://github.com/dwslab/melt/blob/master/matching-owlapi-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_owlapi_matchers/logmap/LogMapRepairFilter.java)

This is the logmap repair filter.
 More information can be found <a href="https://code.google.com/archive/p/logmap-matcher/wikis/LogMapRepairFromApplications.wiki">at the wiki</a>
 and in the <a href="https://github.com/ernestojimenezruiz/logmap-matcher">github repository</a>.
 
 In case you want to use this filter, make the dependency of matching-owlapi-matchers to exclude the following:
 
```

 <exclusions>
    <exclusion>
        <groupId>com.github.ansell.pellet</groupId>
        <artifactId>pellet-modularity</artifactId>
    </exclusion>
</exclusions>
 
```

 
 and also include OWLAPI with version 4.1.3
 
```

 <dependency>
    <groupId>net.sourceforge.owlapi</groupId>
    <artifactId>owlapi-distribution</artifactId>
    <version>4.1.3</version>
</dependency>
 
```


*Keywords: Log Map Repair Filter*

---
<sub>automatically generated on 2023-07-31 11:55</sub>
