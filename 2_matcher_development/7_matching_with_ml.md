---
layout: default
title: Matching with ML
parent: Matcher Development
nav_order: 7
permalink: /matcher-development/with-ml

---

# Matching with Machine Learning


## Python Integration
The MELT-ML module exposes some machine learning functionality that is implemented in Python. This is achieved
through the start of a python process within Java. The communication is performed through local HTTP calls. This
is also shown in the following figure. 

<img src="https://github.com/dwslab/melt/blob/gh-pages/media/melt_ml_architecture.png">

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