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


# Matching with Transformer Models

## Using a Transformer as Filter
MELT offers the usage of transformer models through a filter class [`TransformersFilter`](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/TransformersFilter.java). For reasons of performance, it is sensible to use a high-recall matcher first and use the transformer components for the final selection of correspondences. Any transformer model that is locally available or available via the [huggingface repository](https://huggingface.co/) can be used in MELT. 

In a first step, the filter will write a (temporary) CSV file to disk with the string representations of each correspondence. The desired string representations can be defined with a `TextExtractor`. The transformer python code is called via the python server (see above). 

 The `TransformerFilter` is intended to be used in a matching pipeline:

<img src="https://raw.githubusercontent.com/dwslab/melt/gh-pages/media/transformer_pipeline.png" alt="MELT TransformersFilter pipeline">


