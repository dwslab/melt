# Example command line application for running an LLM

This example provides a command line application to run a matching task using an LLM.
It is also the implementation for the paper `OLaLa: Ontology Matching with Large Language Models`


## Installation
To run all examples, first MELT needs to be build and then the correct python environment needs to be created.

### Build MELT
In the main directory of MELT execute
```
mvn clean install -DskipTests -Dmaven.javadoc.skip=true
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


## OAEI

### Build the docker image
To create the OAEI matcher, uncomment the corresponding build module in the `pom.xml` file.
Then run `mvn install`.

If you want to use `podman` instead of `docker` (only during build!) you need to run 
`podman system service {dashdash}time=0 unix:/run/user/$(id -u)/podman/podman.sock`
in a seperate shell and then before executing maven run `export DOCKER_HOST="unix:/run/user/$(id -u)/podman/podman.sock" `.

### Execute the docker image directly (without MELT)
Running this matcher in a reasonable time requires GPUs (in experiments we used 2 x A100 with 40 GB RAM).

To execute the image run, 

```
docker run --rm --publish 8080:8080 llm-transformers-1.0-web
```
(or replace `docker` by `podman`).

This will download the large language model (~250GB) into the container which means that if you run the container again, it will be downloaded again.
To map the cache folder to a folder on your hard drive (replace `{localPath}` with it) you need to execute:

```
docker run --rm --publish 8080:8080 -v {localPath}:/root/.cache/ llm-transformers-1.0-web
```

With this setup it only downloads the large language model only once even if the container is started multiple times.

If a restriction on specific GPUs is required, set the environemnt variable `CUDA_VISIBLE_DEVICES` like

```
docker run --rm --publish 8080:8080 -e CUDA_VISIBLE_DEVICES=0 llm-transformers-1.0-web
```

Both options can also be combined.

Once the container is started, it will listen on the 8080 port and provides a 
REST API defined by the [MELT Web format](https://dwslab.github.io/melt/matcher-packaging/web).

### Execute the docker image with MELT

[Download the MELT evaluation client](https://dwslab.github.io/melt/matcher-evaluation/client) and run:

```
java -jar matching-eval-client-latest.jar --systems <path to the tar.gz file> --track <location-URI> <collection-name> <version>
```

if the docker container is already running you need to provide the locahost URL:

```
java -jar matching-eval-client-latest.jar --systems http://localhost:8080/match --track <location-URI> <collection-name> <version>
```
