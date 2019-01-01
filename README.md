# OpenJAX Support Cert Maven Plugin

**Maven Plugin for SSL Certificate-related goals**

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
  <groupId>org.openjax.support.cert</groupId>
  <artifactId>cert-maven-plugin</artifactId>
  <version>0.8.9-SNAPSHOT</version>
</plugin>
```

### JavaDocs

JavaDocs are available [here](https://support.openjax.org/cert-maven-plugin/apidocs/).

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

### License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.