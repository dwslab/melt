---
layout: default
title: Matching with ML
parent: Matcher Development
nav_order: 7
permalink: /matcher-development/with-ml

---

# Matching with Machine Learning | General Information


## Python Integration
The MELT-ML module exposes some machine learning functionality that is implemented in Python. This is achieved
through the start of a python process within Java. The communication is performed through local HTTP calls. This
is also shown in the following figure. 

<img src="https://raw.githubusercontent.com/dwslab/melt/gh-pages/media/melt_ml_architecture.png" alt="MELT Machine Learning module (MELT-ML module) architectural view.">

The program will use the default `python` command of your system path.
Note that Python 3 is required together with the dependencies listed 
in [/matching-ml/melt-resources/requirements.txt](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/resources/requirements.txt).

If you want to use a special python environment, you can create a file named `python_command.txt`
in your `melt-resources` directory (create if not existing) containing the path to your python executable. You can, for example,
use the executable of a certain Anaconda environment. 

*Example*:
```
C:\Users\myUser\Anaconda3\envs\matching\python.exe
```
Here, an Anaconda environment, named `matching` will be used.

## Obtaining Training Data
Machine learning requires data for training. For positive examples, a share from the reference can be sampled (e.g. using method [`generateTrackWithSampledReferenceAlignment`](https://dwslab.github.io/melt/javadoc_latest/de/uni_mannheim/informatik/dws/melt/matching_data/TrackRepository.html#generateTrackWithSampledReferenceAlignment(de.uni_mannheim.informatik.dws.melt.matching_data.Track,double))) or a high-precision matcher can be used. Multiple strategies exist to generate negative samples:

For example, negatives can be generated randomly using a absolute number of negatives to be generated (class `AddNegativesRandomlyAbsolute`) or a relative share of negatives (class `AddNegativesRandomlyShare`). If the gold standard is not known, is also possible to exploit the one-to-one assumption and add random correspondences involving elements that already appear in the positive set of correspondences (class `AddNegativesRandomlyOneOneAssumption`). MELT contains multiple out-of-the box strategies that are already implemented as matching components which can be used within a matching pipeline. All of them implement interface `AddNegatives`. Since multiple flavors can be thought of (e.g. generating type homogeneous or type heterogeneous correspondences), a negatives generator can be easily written from scratch or customized for specific purposes. MELT offers some helper classes to do so such as `RandomSampleOntModel` which can be used to sample elements from ontologies.



# Matching with Transformer Models

## Using a Transformer as Filter
MELT offers the usage of transformer models through a filter class [`TransformersFilter`](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFilter.java). For reasons of performance, it is sensible to use a high-recall matcher first and use the transformer components for the final selection of correspondences. Any transformer model that is locally available or available via the [huggingface repository](https://huggingface.co/) can be used in MELT. 

In a first step, the filter will write a (temporary) CSV file to disk with the string representations of each correspondence. The desired string representations can be defined with a `TextExtractor`. The transformer python code is called via the python server (see above). 

 The `TransformerFilter` is intended to be used in a matching pipeline:

<img src="https://raw.githubusercontent.com/dwslab/melt/gh-pages/media/transformer_pipeline.png" alt="MELT TransformersFilter pipeline">



## Fine-Tuning a Transformer
The default transformer training objectives are not suitable for the task of ontology matching. Therefore, a pre-trained model needs to be fine-tuned. Once a training alignment is available, class [`TransformersFineTuner`](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFineTuner.java) can be used to train and persist a model (which then can be applied in a matching pipeline using `TransformerFilter` as shown in the figure above).


Like the TransformersFilter, the TransformersFineTuner is a matching component that can be used in a matching  pipeline. Note that this pipeline can only be used for training and model serialization. An example for such a pipeline is visualized below:

<img src="https://raw.githubusercontent.com/dwslab/melt/gh-pages/media/transformer_pipeline_finetuning.png" alt="MELT transformer pipeline for fine-tuning.">

