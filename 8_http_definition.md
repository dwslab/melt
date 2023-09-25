---
layout: default
title: HTTP/WEB Matcher Definition 
nav_order: 8
permalink: /http-definition
---

# Web Interface (HTTP Matching Interface)

In this section, the REST API interface for a matcher is described.
It is used for matchers running on servers and not locally.
When using a docker based matcher, the communication interface is exactly the same.

There are two possible `content types` (below: drop-down menu for "Request body"): (1) An URL encoded form and (2) multipart upload. For the first option, URLs are provided pointing to the data files. In this case, the matching server will access the URL and download the data. For the second option, the files are directly contained in the HTTP POST request. 

`source` and  `target` are plain RDF files (RDF/XML, Turtle, N3). 

`inputAlignment` is an alignment in the [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html).

`parameters` are used to provide task-specific information (for example "match only classes"). Some keys are already defined by [MELT](https://github.com/dwslab/melt/blob/master/matching-base/src/main/java/de/uni_mannheim/informatik/dws/melt/matching_base/ParameterConfigKeys.java). The parameters can be transferred as JSON (preferred) or YAML.

The system directly returns the resulting alignment in the [alignment format](https://moex.gitlabpages.inria.fr/alignapi/format.html).

See also a more complete documentation in [MELT Web packaging](https://dwslab.github.io/melt/matcher-packaging/web).

## Swagger documentation of the Web API
The swagger documentation is displayed below / 
[[open full screen documentation in a new tab]](https://dwslab.github.io/melt/7_matcher_packaging/swagger_ui_melt.html){:target="_blank"}

<iframe src="https://dwslab.github.io/melt/7_matcher_packaging/swagger_ui_melt.html" scrolling="no"
    style="border:0; width:100%; height:2750px; overflow:hidden;">
</iframe>