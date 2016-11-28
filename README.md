<img src="http://safris.org/logo.png" align="right"/>
## cert-maven-plugin<br>[![JavaCommons](https://img.shields.io/badge/mvn-plugin-lightgrey.svg)](https://cohesionfirst.com/) [![CohesionFirst](https://img.shields.io/badge/CohesionFirst%E2%84%A2--blue.svg)](https://cohesionfirst.com/)
> Maven Plugin for SSL Certificate-related goals

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
  <groupId>org.safris.maven.plugin</groupId>
  <artifactId>cert-maven-plugin</artifactId>
  <version>1.0.4</version>
</plugin>
```

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.