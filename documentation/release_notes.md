# Release Notes


## Changes in 3.5

**New**
- YAAA (Yet Another Alignment Api) is now supporting [SSSOM format](https://mapping-commons.github.io/sssom/)

**Improvements**
- YAAA (Yet Another Alignment Api) extensions are serialized and parsed as json which allows to have same types even after a serialization.
- YAAA (Yet Another Alignment Api) supports [more relations types](https://github.com/dwslab/melt/blob/master/yet-another-alignment-api/src/main/java/de/uni_mannheim/informatik/dws/melt/yet_another_alignment_api/CorrespondenceRelation.java) (e.g. `closeMatch` and `relatedMatch`)
- matching-ml: transformer models can do multi label prediction
- famer clustering allows to set the number of threads used to compute the clustering

**Fixed**
- matching-ml: requires a python environment with `Werkzeug<=2.2.3` because starting from `2.3.0` the webserver do not get the request headers 
- [Wikidata tests are fixed](https://github.com/dwslab/melt/commit/ae824d9#diff-471d5bfa39673c940c5c7c2f7450ffe61545c052efd6ccf85080e0e589cbd7c9) because Wikidata entry for `EU` changed 
- [Testcasevalidation is more memory friendly](https://github.com/dwslab/melt/commit/fe6915287637895c3fee63eec2e28934218ba0bd)
- fixed macro F1 computation (it is **not** the harmonic mean of macro averaged precision and recall but now the macro averaged f1 scores)

**New Tracks**
- [pgx Track](http://oaei.ontologymatching.org/2023/pharmacogenomics/index.html)
- Food track with subsumption relations



## Changes in 3.4

**New**
- Implemented the LogMap reapir as a filter.

**New Tracks**
- CommonKG has a new track YAGO-WIKIDATA. [The whole track moved to own suite id `commonkg`](https://github.com/dwslab/melt/commit/5a270d3ea7d7b1472d184e6a26907d009a0fb83e#diff-ee40cc9813aeee76e725d927b47aafaa850ffaadde575b92d66706a454dbf546)
- Food track (Food Nutritional Composition track)

## Changes in 3.3

**New**
- MELT user guide allows for downloading track data directly via the documentation page.
- `TrainTestSplit` (with stratification) for any Java object and specialized for alignments (`TrainTestSplitAlignment`).
- New alignment repair filter: `AlcomoFilter`.

**Fixed**
- Handling of multiple track versions by `EvaluatorCSV` (fixes [issue #140](https://github.com/dwslab/melt/issues/140)).

**Improvements**
- More intuitive behavior of `ConfidenceFinder`: If no precision is specified, the optimal solution is calculated.
- It is now possible to copy extensions from one alignment to another (multiple methods `copyExtensionsTo...`).
- New mode for `TopXFilter`: `SOURCE_AND_TARGET`
- Python server uses now gensim 4.x and flask 2.x ([issue #67](https://github.com/dwslab/melt/issues/67)).
- Python server can be shut down via `/shutdown`.

**New Tracks**
- Process Matching (2017)

## Changes in 3.2

**New**
- introduced a new matcher: [`SentenceTransformersMatcher`](https://github.com/dwslab/melt/blob/master/matching-ml/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_ml/python/nlptransformers/SentenceTransformersMatcher.java)
- example for track validation (used to analyze biodiv track)
- example for multi source matching called [`multisource experiment`](https://github.com/dwslab/melt/tree/master/examples/multisourceExperiment)
- eval client is build with every commit and the final jar is uploaded as a build artifact
  - a nightly link is generated which always points to the latest version
- eval client now also works with short "built-in" track string names (e.g. `--track conference`)
- all "built-in" tracks can be displayed in the evaluation client using option `--show-tracks`

**Fixed**
- normalization of literals in `BaselineStringMatcher` (fixes issue #44)
- matcher name was not always encoded (because it was used in as file name) (see issue #128)
- use a new local track for a local test case (fixes issue #121)

**Improvements**
- Web frontend for Web/Docker packaged matchers works now with javascript and provides better UX
- Server for Web/Docker based images now more stable
- `MatcherDockerFile` automatically logs the text which is logged in container - this helps to debug errors
  - the function `logAllLinesFromContainer` is thus deprecated
- `MatcherDockerFile` extracts the name for the image from the file content. Thus, the file which contains the image 
  can be named arbitrarily. The file compression is also automatically detected. See issue #123
- documentation about jena interfaces like `LiteralExtractor` and ` TextExtractor`
- welcome message also shows the git commit hash which is used to build the eval client

**New Tracks**
- new biodiv track (version `2021`)

## Changes in 3.1

**New**
- MELT now has an evaluation command line interface (MELT Evaluation CLI, module `matching-eval-cli`)
- three new matchers which uses NLP transformers: `TransformersFilter`, `TransformersFineTuner`, `TransformersFineTunerHpSearch`
- example project which uses NLP transformers
- added [negative sampling methods](https://github.com/dwslab/melt/tree/master/matching-jena-matchers/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_jena_matchers/util/addnegatives) like: `AddNegativesViaMatcher`, `AddNegativesRandomlyShare`, `AddNegativesRandomlyOneOneAssumption`, `AddNegativesRandomlyAbsolute`
- `MatcherHTTPCall` can call remote matchers which implement the web interface defined at the [MELT documentation](https://dwslab.github.io/melt/matcher-packaging/web#swagger-documentation-of-the-web-api)
- `MatcherDockerFile` automatically imports a saved docker image from file into the local repository and starts a fresh container based on this image to call the matcher via `MatcherHTTPCall`
- automatic generating of documentation page of all [matchers](https://dwslab.github.io/melt/matcher-components/full-matcher-list) and [filters](https://dwslab.github.io/melt/matcher-components/full-filter-list) in the MELT repository [MELT documentation](https://dwslab.github.io/melt/matcher-components/full-matcher-list) 
- multisource matching with new interfaces and implementations to reuse one to one matchers for a multisource setting


**Fixed**
- Test Cases of tracks are now ordered in a deterministic way. `getFirstTestCase()`, for instance, will now return
the same test case on every machine.
- MatcherSEALS checks before run if the java executable is really Java 1.8 because this is a strict requirement of SEALS client
- `ConfidenceFinder` in matching-eval returns correct threshold (due to a change of the confidence of false negatives)
- MatcherSEALS works also in parallel mode (when executed with `ExecutorParallel`)

**Improvements**
- Docker Web Packages follow the convention that the file name of the package must carry the name of the image
  (with an optional `-latest` postfix). The constructor of `MatcherDockerFile` has been adapted so that it is 
  sufficient to provide a file.
- Docker Web packaging plugin: Image name now included in name of package.
- Releasing MELT is now easier because gpg plugin can be activated via a maven profile
- MatcherSEALS uses toString method to return a meaningful name which also includes the name of the exeucted matcher
- The SEALS packaging is now also tested in the CI (tests on all operating systems and java versions)
- `ConfidenceFinder` also allows to search for best confidence for F-measure, and F beta-measure
- Caching of python pip packages, OAEI data, and transformers models to improve CI pipeline runtime
- added transformation route from URL to OWLOntology (OWLAPI) - thus OWLAPI also works with IMatcher interface