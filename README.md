# liboscal-java

A Java library to support processing OSCAL content.

This open-source, Metaschema Java library offers a programmatic means to work with [OSCAL](https://pages.nist.gov/OSCAL/) models defined by the [Metaschema modeling language](https://github.com/metaschema-framework/metaschema). This framework also supports programmatically creating, modifying, parsing, and writing XML, JSON, and YAML OSCAL instance data. This work is intended to make it easier for Java software developers to incorporate OSCAL-based capabilities into their applications.

The following features are supported by this library:
- Reading and writing OSCAL documents in XML, JSON, and YAML formats into a common Java object model.
- Resolution of OSCAL profiles to [produce resolved catalogs](https://pages.nist.gov/OSCAL/concepts/processing/profile-resolution/).
- Validation of OSCAL content [well-formedness and validation](https://pages.nist.gov/OSCAL/concepts/validation/) of OSCAL syntax using XML and JSON schemas.
- (Experimental) Validation of OSCAL content using [Metaschema](https://metaschema.dev/) constraints to enforce allowed values, cross-references, and some conditionally required data elements.
- Builders for programmatically creating common OSCAL data elements.

This library is based on the [Metaschema Java Tools](https://metaschema-java.metaschema.dev/) project.

## Contributing to this code base

Thank you for interest in contributing to the Metaschema Java framework. For complete instructions on how to contribute code, please read through our [CONTRIBUTING.md](CONTRIBUTING.md) documentation.

## Public domain

This project is in the worldwide [public domain](LICENSE.md) and as stated in [CONTRIBUTING.md](CONTRIBUTING.md).

## Using as a Maven dependency

This project's modules are published to [Maven Central](https://central.sonatype.com/artifact/dev.metaschema.oscal/liboscal-java). We recommend you use the latest stable release on the Maven Central repository.

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

The [full code](src/test/java/gov/nist/secauto/oscal/java/ExamplesTest.java) for this example is also available.

## Relationship to prior work

The contents of this repository is based on work from the [Metaschema Java repository](https://github.com/usnistgov/liboscal-java/) maintained by the National Institute of Standards and Technology (NIST), the [contents of which have been dedicated in the worldwide public domain](https://github.com/usnistgov/liboscal-java/blob/a56c130fa8d35dff9590065c942ccd5ee7f25ae3/LICENSE.md) using the [CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/) public domain dedication. This repository builds on this prior work, maintaining the [CCO license](https://github.com/metaschema-framework/liboscal-java/blob/main/LICENSE.md) on any new works in this repository.
