# liboscal-java

A Java library to support processing OSCAL content.

This open-source, Metaschema Java library offers a programatic means to work with [OSCAL](https://pages.nist.gov/OSCAL/) models defined by the [Metaschema modeling language](https://github.com/usnistgov/metaschema). This framework also supports programatically creating, modifying, parsing, and writing XML, JSON, and YAML OSCAL instance data. This work is intended to make it easier for Java software developers to incorporate OSCAL-based capabilities into their applications.

This library is based on the [Metaschema Java Tools](https://pages.nist.gov/metaschema-java/) project.

This effort is part of the National Institute of Standards and Technology (NIST) OSCAL Program.

## Contributing to this code base

Thank you for interest in contributing to the Metaschema Java framework. For complete instructions on how to contribute code, please read through our [CONTRIBUTING.md](CONTRIBUTING.md) documentation.

## Public domain

This project is in the worldwide [public domain](LICENSE.md). As stated in [CONTRIBUTING.md](CONTRIBUTING.md).

## Using as a Maven dependency

This project's modules are published to [Maven Central](https://search.maven.org/search?q=g:gov.nist.secauto.oscal.liboscal-java).

You can include these artifacts in your Maven POM as a dependency.

## Building

This project can be built with [Apache Maven](https://maven.apache.org/) version 3.8.4 or greater.

The following instructions can be used to clone and build this project.

1. Clone the GitHub repository.

```bash
git clone --recurse-submodules https://github.com/usnistgov/liboscal-java.git 
```

2. Build the project with Maven

```bash
mvn install
```

## Using

The following is a simple example of how to load and write OSCAL content using this API.

```
// Initialize the Metaschema framework
OscalBindingContext bindingContext = OscalBindingContext.instance(); // manages the Metaschema model
IBoundLoader loader = bindingContext.newBoundLoader(); // supports loading OSCAL documents

// load an OSCAL catalog
Catalog catalog = loader.load(Paths.get("src/test/resources/content/test-catalog.xml")); // load the catalog

// Create a serializer which can be used to write multiple catalogs
ISerializer<Catalog> serializer = bindingContext.newSerializer(Format.YAML, Catalog.class);

// create the output directory
Path outDir = Paths.get("target/generated-test-resources");
Files.createDirectories(outDir);

// serialize the catalog as yaml
serializer.serialize(catalog, outDir.resolve("test-catalog.yaml"));
```

tree/ddb1a4667b8c09b08300cd7ad9a2161ff4dc4d23#readme

The [full code](src/test/java/gov/nist/secauto/oscal/java/ExamplesTest.javasrc/test/java/gov/nist/secauto/oscal/java/ExamplesTest.java) for this example is also available.

## Contact us

Maintainer: [David Waltermire](https://www.nist.gov/people/david-waltermire) - [@david-waltermire-nist](https://github.com/david-waltermire-nist), [NIST](https://www.nist.gov/) [Information Technology Labratory](https://www.nist.gov/itl), [Computer Security Division](https://www.nist.gov/itl/csd), [Security Components and Mechanisms Group](https://www.nist.gov/itl/csd/security-components-and-mechanisms)

Email us: [oscal@nist.gov](mailto:oscal@nist.gov)

Chat with us: [Gitter usnistgov-OSCAL/Lobby](https://gitter.im/usnistgov-OSCAL/Lobby)

