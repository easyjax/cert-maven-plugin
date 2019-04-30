# OpenJAX Std Cert Maven Plugin

> Maven Plugin for SSL Certificate-related goals

[![Build Status](https://travis-ci.org/openjax/cert-maven-plugin.png)](https://travis-ci.org/openjax/cert-maven-plugin)
[![Coverage Status](https://coveralls.io/repos/github/openjax/cert-maven-plugin/badge.svg)](https://coveralls.io/github/openjax/cert-maven-plugin)
[![Javadocs](https://www.javadoc.io/badge/org.openjax.std/cert-maven-plugin.svg)](https://www.javadoc.io/doc/org.openjax.std/cert-maven-plugin)
[![Released Version](https://img.shields.io/maven-central/v/org.openjax.std/cert-maven-plugin.svg)](https://mvnrepository.com/artifact/org.openjax.std/cert-maven-plugin)

### Introduction

The `cert-maven-plugin` plugin is used for general SSL Certificate-related goals.

### Goals Overview

* [`cert:import`](#certimport) imports HTTPS server certificates of the `project.repositories` to the keystore.

### Usage

#### `cert:import`

The `cert:import` goal is bound to the `compile` phase, and is used to import HTTPS server certificates of the `project.repositories` to the keystore.

##### Example 1

```xml
<plugin>
  <groupId>org.openjax.std.cert</groupId>
  <artifactId>cert-maven-plugin</artifactId>
  <version>0.8.9-SNAPSHOT</version>
</plugin>
```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.