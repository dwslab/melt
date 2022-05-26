---
layout: default
title: MELT Track Repository
nav_order: 5
permalink: /track-repository
---


# MELT Track Repository
The [`TrackRepository`](https://github.com/dwslab/melt/blob/master/matching-data/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_data/TrackRepository.java) 
checks whether the required ontologies and alignments are available in the cache folder (`~/oaei_track_cache`); if data is missing, it is automatically downloading and 
caching it for the next access. The MELT Track Repository is used by the OAEI since 2020.

Exemplary call using the `TrackRepository`:
```java
// access the Anatomy track
TrackRepository.Anatomy.Default;

// access all Conference test cases
TrackRepository.Conference.V1.getTestCases();
```

The resulting instances can be directly used by the Executor or any other MELT functionality that requires tracks or
test cases.

## Available Tracks
MELT also provides a server with default datasets used by the OAEI. 
They are identified by three components: 
1. `Repository` (location-URI): `http://oaei.webdatacommons.org/tdrs/`	 
2. `Track/Suite - ID` (collection-name): *\<see table below\>*
3. `Version ID`: *\<see table below\>*

The [MELT evaluation client](https://dwslab.github.io/melt/matcher-evaluation/client) uses these track identifiers.
The MELT repository is built so that it is fully compatible with the former SEALS client. For example, you can still use the SEALS client JAR to fetch the data from the MELT repository.

You can also download a ZIP file of the data by clicking on the download icon (<i class="fa fa-arrow-circle-down"></i>) next to the Version ID in the table below.

Name | Suite-ID | Version-ID 
---- | -------- | ---------- 
[anatomy](http://oaei.ontologymatching.org/2019/anatomy/index.html) | `anatomy_track` | `anatomy_track-default` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/anatomy_track/anatomy_track-default/anatomy_track_anatomy_track-default.zip)
[biodiv](http://oaei.ontologymatching.org/2018/biodiv/index.html) | `biodiv` | - `2018` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/biodiv/2018/biodiv_2018.zip) <br/> - `2021` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/biodiv/2021/biodiv_2021.zip) <br/> - `2021owl` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/biodiv/2021owl/biodiv_2021owl.zip)
[complex](http://oaei.ontologymatching.org/2019/complex/index.html) | - `geolink`<br>- `hydrography`<br>- `popgeolink`<br>- `popenslaved`<br>- `popconference`|  - `geolink-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/geolink/geolink-v1/geolink_geolink-v1.zip)<br>- `hydrography-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/hydrography/hydrography-v1/hydrography_hydrography-v1.zip)<br>- `popgeolink-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popgeolink/popgeolink-v1/popgeolink_popgeolink-v1.zip)<br>- `popenslaved-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popenslaved/popenslaved-v1/popenslaved_popenslaved-v1.zip)<br>- `popconference-0-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popconference/popconference-0-v1/popconference_popconference-0-v1.zip)<br>- `popconference-20-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popconference/popconference-20-v1/popconference_popconference-20-v1.zip)<br>- `popconference-40-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popconference/popconference-40-v1/popconference_popconference-40-v1.zip)<br>- `popconference-60-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popconference/popconference-60-v1/popconference_popconference-60-v1.zip)<br>- `popconference-80-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popconference/popconference-80-v1/popconference_popconference-80-v1.zip)<br>- `popconference-100-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/popconference/popconference-100-v1/popconference_popconference-100-v1.zip)<br>
[conference](http://oaei.ontologymatching.org/2019/conference/index.html) | `conference` | - `conference-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/conference/conference-v1/conference_conference-v1.zip)<br> - `conference-v1-all` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/conference/conference-v1-all/conference_conference-v1-all.zip) (also testcases without reference) <br> - `conference-dbpedia` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/conference/conference-dbpedia/conference_conference-dbpedia.zip)
[knowledgegraph](http://oaei.ontologymatching.org/2019/knowledgegraph/index.html) | `knowledgegraph` | - `v3` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/knowledgegraph/v3/knowledgegraph_v3.zip)<br/> - `v4` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/knowledgegraph/v4/knowledgegraph_v4.zip)<br/> - `commonkg` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/knowledgegraph/commonkg/knowledgegraph_commonkg.zip) ([paper](https://github.com/OmaimaFallatah/KG_GoldeStandard)) 
[GeoLinkCruise](http://oaei.ontologymatching.org/2021/geolinkcruise/index.html) | `geolinkcruise`| `geolinkcruise-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/geolinkcruise/geolinkcruise-v1/geolinkcruise_geolinkcruise-v1.zip)
Laboratory | `laboratory`| `laboratory-v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/laboratory/laboratory-v1/laboratory_laboratory-v1.zip)
[iimb](https://web.archive.org/web/20210518201656/https://islab.di.unimi.it/content/im_oaei/2018/ ) | `iimb` | `v1` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/iimb/v1/iimb_v1.zip) | 
[largebio](http://www.cs.ox.ac.uk/isg/projects/SEALS/oaei/) | `largebio` |  -`largebio-all_tasks_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-all_tasks_2016/largebio_largebio-all_tasks_2016.zip)<br>- `largebio-fma_nci_small_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-fma_nci_small_2016/largebio_largebio-fma_nci_small_2016.zip)<br>- `largebio-fma_nci_whole_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-fma_nci_whole_2016/largebio_largebio-fma_nci_whole_2016.zip)<br>- `largebio-fma_snomed_small_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-fma_snomed_small_2016/largebio_largebio-fma_snomed_small_2016.zip)<br>- `largebio-fma_snomed_whole_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-fma_snomed_whole_2016/largebio_largebio-fma_snomed_whole_2016.zip)<br>- `largebio-snomed_nci_small_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-snomed_nci_small_2016/largebio_largebio-snomed_nci_small_2016.zip)<br> - `largebio-snomed_nci_whole_2016` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/largebio/largebio-snomed_nci_whole_2016/largebio_largebio-snomed_nci_whole_2016.zip)
[link](https://project-hobbit.eu/challenges/om2019/) | `link` | `2017` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/link/2017/link_2017.zip)
[multifarm](http://oaei.ontologymatching.org/2018/multifarm/index.html) | `<language_pair>`<br> `multifarm`  |  `<language_pair>-v2` (see below table) <br> `all-v2` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/multifarm/all-v2/multifarm_all-v2.zip)
[phenotype](https://sws.ifi.uio.no/oaei/phenotype/) | `phenotype` | - `phenotype-hp-mp-2017-bioportal` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/phenotype/phenotype-hp-mp-2017-bioportal/phenotype_phenotype-hp-mp-2017-bioportal.zip)<br/>- `phenotype-doid-ordo-2017-bioportal` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/phenotype/phenotype-doid-ordo-2017-bioportal/phenotype_phenotype-doid-ordo-2017-bioportal.zip)
[pm](https://web.informatik.uni-mannheim.de/oaei/pm17/) | `pm` | - `2017-all` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/pm/2017-all/pm-2017-all.zip)<br/>- `2017-br` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/pm/2017-br/pm-2017-br.zip)<br/>- `2017-ua` [<i class="fa fa-arrow-circle-down"></i>](https://oaei.webdatacommons.org/tdrs/testdata/persistent/pm/2017-ua/pm-2017-ua.zip) | 




Available multifarm language pairs:<br/>
`ar-cn`, `ar-cz`, `ar-de`, `ar-en`, `ar-es`, `ar-fr`, `ar-nl`, `ar-pt`, `ar-ru`, `cn-cz`, `cn-de`, `cn-en`, `cn-es`, 
`cn-fr`, `cn-nl`, `cn-pt`, `cn-ru`, `cz-de`, `cz-en`, `cz-es`, `cz-fr`, `cz-nl`, `cz-pt`, `cz-ru`, `de-en`, `de-es`, 
`de-fr`, `de-nl`, `de-pt`, `de-ru`, `en-es`, `en-fr`, `en-nl`, `en-pt`, `en-ru`, `es-fr`, `es-nl`, `es-pt`, `es-ru`, 
`fr-nl`, `fr-pt`, `fr-ru`, `nl-pt`, `nl-ru`, `pt-ru`


## Further Services

### TestCase/Track Validation Service
Creating new tracks and test case can be very cumbersome. The MELT validation service allows you to check whether your 
test cases:
1. Contain parseable ontologies.
2. Contain a parseable reference alignment.
3. Mention only URIs in the reference alignment that also appear in the corresponding source and target ontologies.

Exemplary call using the [`TestCaseValidationService`](https://github.com/dwslab/melt/blob/master/matching-validation/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_validation/TestCaseValidationService.java):
```java
URI sourceUri = new File("<path to source ontology file>").toURI();
URI targetUri = new File("<path to target ontology file>").toURI();
URI referenceUri = new File("<path to reference alignment file>").toURI();
TestCase testCase = new TestCase("FSDM", sourceUri, targetUri, referenceUri, null);
TestCaseValidationService validator = new TestCaseValidationService(testCase)
System.out.println(validator);
```
You can also test your track on different versions of Jena and the OWL API automatically
by adapting the [`TestLocalFile`](https://github.com/dwslab/melt/blob/master/matching-validation/src/test/java/de/uni_mannheim/informatik/dws/melt/matching_validation/local/TestLocalFile.java) 
and running `runAll.cmd` in the Windows shell. The release versions to be tested can be edited in the corresponding
[`pom.xml`](https://github.com/dwslab/melt/blob/master/matching-validation/pom.xml).
