# Example command line application for running an LLM

This example provides a command line application to run a matching task using an LLM.
It is also the implementation for the paper `OLaLa: Ontology Matching with Large Language Models`


## Installation
To run all examples, first MELT needs to be build and then the correct python environment needs to be created.

### Build MELT
In the main directory of MELT execute
```
mvn clean install
```


### Setup python environment

- for pytorch 1.13 (recommended) 
```
conda create -n melt python=3.9
conda activate melt
conda install pytorch==1.13.1 torchvision==0.14.1 pytorch-cuda=11.7 -c pytorch -c nvidia
conda install numpy scikit-learn pandas gensim flask "Werkzeug<=2.2.3" sentencepiece "protobuf==3.20.1"
conda install accelerate -c conda-forge
pip install bitsandbytes transformers sentence-transformers
```


- for pytorch 2
```
conda create -n melt python=3.9
conda activate melt
conda install pytorch torchvision pytorch-cuda=11.8 -c pytorch -c nvidia
conda install numpy scikit-learn pandas gensim flask "Werkzeug<=2.2.3" sentencepiece "protobuf==3.20.1"
conda install accelerate -c conda-forge
pip install bitsandbytes transformers sentence-transformers
```


## Running the default configuration
The default configuration from the paper `OLaLa: Ontology Matching with Large Language Models`
can be executed with the following command:

```
java -jar llm-transformers-1.0-jar-with-dependencies.jar \
    --python {python executable location} \
    --transformerscache {path to transformers cache} \
    --gpu {gpus to use e.g. 1,2} \
    --prompt 7 \
    --includeloadingarguments \
    --textextractor 4 \
    --transformermodels "upstage/Llama-2-70b-instruct-v2" \
    --tracks anatomy \
    > out.txt 2> err.txt
```

Replace the `{python executable location}`by the path to the python exectuable from the created virtual environment above.
To get the path, activate the environment and execute `which python` (linux) or `where python` (windows).

The path to the transformers cache (where all the models are stored) can be changed with `transformerscache` option.
Leave it out completely to used the default (usually in home folder).
The sized for `70B` variants (one models) are usually around 130 GB.
