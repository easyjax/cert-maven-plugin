<img src="https://images.cooltext.com/5195724.png" align="right">

## cert-maven-plugin<br>![mvn-plugin][mvn-plugin] <a href="https://www.easyjax.org/"><img src="https://img.shields.io/badge/EasyJAX--blue.svg"></a>
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
  <groupId>org.easyjax.cert</groupId>
  <artifactId>cert-maven-plugin</artifactId>
  <version>0.8.9-SNAPSHOT</version>
</plugin>
```

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

<a href="http://cooltext.com" target="_top"><img src="https://cooltext.com/images/ct_pixel.gif" width="80" height="15" alt="Cool Text: Logo and Graphics Generator" border="0" /></a>

[mvn-plugin]: https://img.shields.io/badge/mvn-plugin-lightgrey.svg