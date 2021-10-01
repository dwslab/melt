---
layout: default
title: SEALS
parent: MELT Project Management
nav_order: 1
permalink: /contribute/project-management
---

# MELT Project Management

## Creating a new MELT Release

### Prerequisits
- make sure you have the latest commit
- make sure that the unit tests run on your local machine and that there are no javadoc errors (`mvn clean install`)

### Process

Run:
`mvn release:prepare -P melt-release,default`

Run:
`mvn release:perform -P melt-release,default`