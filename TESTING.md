# Testing Standards

## Summary

This documentation describes approaches to meeting requirements around governance, quality checks, and best practices for technical staff before, during, and after open-source release of software.

The following requirements, and their implied goals, are directly and indirectly inspired by [NIST policies and their requirements mentioned in the appendix](#Appendix).

## Why do have these requirements?

The developers provide rationale below for [requirements](#What-are-our-requirements) and [how to meet their criteria](#Distilled-Requirements). Those details are primarily focused on technical leads and developers. Other stakeholders may not be familiar with the methodologies and their strategic benefit to software development projects like Metaschema. We describe the methodologies and their benefits in this section.

### Continuous Builds and Testing

Continuous builds and testing are part of the continuous integration methodology of software development. Continuous integration involves developers frequently merging code changes into a central code repository, automatically building software from changed code, testing the code and/or the resulting software. This methodology creates a fast feedback loop to test changes and validate the desired behavior of the software from code changes. Continuous builds and testing increase developer productivity and reduce the risk of infrequently aggregating large changes in the code with a higher likelihood of releasing software with undesired behavior. Reducing the likelihood of degradations or failures as early as possible reduces the reputational risk of end users using poor quality software after the fact.

This project uses [GitHub Actions](https://github.com/metaschema-framework/liboscal-java/actions) (GHA) as a Continuous Integration and Continuous Deployment (CI/CD) environment to build, [test](#unit-testing), and [release](https://github.com/metaschema-framework/liboscal-java/releases) code. The CI is used for all commits against the `develop` and `main` branches, and for all [Pull Requests](https://github.com/metaschema-framework/liboscal-java/pulls). CD is used for all [tagged releases](https://github.com/metaschema-framework/liboscal-java/tags). Specialized [GHA workflows](https://github.com/metaschema-framework/liboscal-java/tree/main/.github/workflows) are used to drive this automation.

References:

- [NIST Special Publication 800-204C: Implementation of DevSecOps for a Microservices-based Application with Service Mesh](https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-204C.pdf)
- [USGS Testing and Automation Guidance](https://www.usgs.gov/software-management/testing-and-automation)

### Unit Testing

Developers can formally or informally identify logically discrete portions of the code during the development process as self-contained units. These units have distinct purposes or functionality that contributes to the complete software as whole. Discretely testing the expected functionality, inputs, and outputs of each unit, not just the resulting software as a whole, has important benefits. It can detect bugs in newly created or modified units of code more efficiently and effectively than debugging all the software at once. Operating all the software to contextually exercise specific parts of its code to locate or fix a bug can consume significant resources. Unit testing reduces the risk of releasing software with erroneous or undesired behavior. It ensures errors are found quickly and efficiently as early as possible in the development cycle.

This project uses the Maven [Surefire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/) to automatically execute [JUnit 5](https://junit.org/junit5/) unit tests during all CI builds.

References:

- [Agile Alliance Glossary: Unit Testing](https://www.agilealliance.org/glossary/unit-test)
- [USGS Testing and Automation Guidance](https://www.usgs.gov/software-management/testing-and-automation)

### Code Coverage

Developers can use write tests that exercise discrete units of code and check functionality. Code coverage, and code coverage analysis tools, analyze how tests are structured and what code paths are exercised. This analysis can measure how many lines of code will be executed by one or more tests. If a line of code is not tested, it is not "covered." Tests measure the correctness of code's behavior, and code coverage measures the completeness and thoroughness of the tests.

This project uses [Jacoco](https://github.com/jacoco/jacoco) and the Maven [Jacoco plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html) to automate code coverage analysis during builds.

The project strives for a 60% test code coverage ratio to ensure adequate test coverage. Work in this area is focused on enhancing unit testing of critical code paths to achieve this goal and to maximize [unit testing](#unit-testing) benefits.

References:

- [NIST Cybersecurity White Paper: Combinatorial Coverage Difference Measurement](https://nvlpubs.nist.gov/nistpubs/CSWP/NIST.CSWP.06222021-draft.pdf)
- [USGS Testing and Automation Guidance](https://www.usgs.gov/software-management/testing-and-automation)

### Static analysis

As developers write code incrementally, it is not uncommon that undesirable (e.g. insecure code, underperforming) behavior of the code is not immediately apparent to the developer as they add or modify it. There are many factors a developer must continuously track across the codebase to prevent any undesirable behavior caused by how the code is written. Static analyzers are tools that augment the development process by analyzing the code "statically" (i.e. only reading the code as written and not executing it as bundled, finalized software). These tools perform analysis to spot potentially undesired behavior and report it to the developer. Often, these tools will provide recommendations on how to rewrite the code to potentially mitigate the issue. These tools help increase the ongoing awareness and inside into undesired behaviors while reducing the time and cognitive load needed for one or more developers to review the code.

Static analysis in this project is provided by multiple solutions.

- [PMD](https://pmd.github.io/) is used to identify common programming errors. PMD is [configured](https://github.com/metaschema-framework/oss-maven/blob/main/oss-build-support/src/main/resources/pmd/category/java/custom.xml) to enforce Java programming best practices; to look for flaws in the code style, design, and documentation; and to identify error prone code and potential performance problems.
- [SpotBugs](https://spotbugs.github.io/) identifies potential bugs using over 400 bug patterns. SpotBugs performs analysis during builds using the Maven [SpotBugs plugin](https://spotbugs.github.io/spotbugs-maven-plugin/).
- [Checkstyle](https://checkstyle.sourceforge.io/) to enforce a [coding style](https://github.com/metaschema-framework/oss-maven/blob/main/oss-build-support/src/main/resources/checkstyle/checkstyle.xml). Checkstyle is run during builds using the Maven [checkstyle plugin](https://maven.apache.org/plugins/maven-checkstyle-plugin/).

References:

- [NIST Computer Security Resource Center Glossary: What is a static code analyzer?](https://csrc.nist.gov/glossary/term/static_code_analyzer)
- [USGS Testing and Automation Guidance](https://www.usgs.gov/software-management/testing-and-automation)

### Dynamic Analysis

As developers write code incrementally, it is common that undesirable behavior (e.g. not secure, not performant) is unnoticed until software is executed. In those cases it is not always apparent to the developer as they write or execute the software. Dynamic analysis tools, unlike static analysis tools, run the software in various conditions and with various data inputs to test for undesirable behavior. Dynamic analysis will find bugs that static analysis will often not find and vice versa, so performing both analyses on the code base has benefits. Dynamic analysis tools can recommend configuration and deployment changes to staff to potentially mitigate the undesired behavior. Using such a tool can reduce risk and improve the software's behavior with less developer effort. Detecting and fixing the behavior before an end user observes it also reduces reputational risk for the software.

This project does not use a dynamic analysis solution at this time.

References:

- [NIST Computer Security Resource Center Glossary: What is a dynamic code analyzer?](https://csrc.nist.gov/glossary/term/dynamic_code_analyzer)
- [USGS Testing and Automation Guidance](https://www.usgs.gov/software-management/testing-and-automation)


### Supply Chain Analysis

Modern software must often provide complex functionality combining a variety of media, protocols, and data exchange mechanisms. It is often too resource intensive to implement the prerequisite functionality for these capabilities in before completing the features of the software the the end user wants. Developers use third-party libraries and tools to efficiently develop software and not expend resources on common prerequisites. Supply chain analysis identifies these third-party tools and software libraries and subsequently flag potentially undesired behavior from code changes, discovered security vulnerabilities, and/or performance degradation. When developers perform this analysis, they significantly reduce the risk of supply chain attacks. Not doing any supply chain analysis significantly increases the risk and likelihood of being an unknowing target of this increasingly popular attack vector.

Supply chain analysis is supported in this project by GitHub [Dependabot](https://docs.github.com/en/code-security/dependabot). Dependabot is [configured](https://github.com/metaschema-framework/liboscal-java/blob/main/.github/dependabot.yml) to automatically identify vulnerable and out-of-date Maven dependencies and GitHub actions plugins, and create pull requests to update to newer versions against the `develop` branch.

References:

- [NIST Special Publication 800-161 Revision 1: Cybersecurity Supply Chain Risk Management Practices for Systems and Organizations](https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-161r1.pdf)

### Documentation for Common Use Cases

It is good practice for developers to document how to change code or build the software from its code. This documentation facilitates knowledge transfer and limits the risk of little or no maintenance for the software after replacement or reduction of staff. Additionally, it is good for developers to provide documentation for end users to onboard them and explain common uses of the software. This documentation benefits the end user by increasing familiarity with the software and reducing ambiguity in how the software operates or the resulting impact on their work.

This aspect is supported in multiple ways.

- Building instructions are included in the [README.md](../../README.md#building).
- Code documenation is provided using Javadocs in the [project website](). This is accessable for each module under "Project Reports".
- Some modules provide basic usage examples, e.g. [Metaschema XML](https://liboscal-java.metaschema.dev/).

Improving documentation is a current work focus for the project.

### Code Review by Peer Developers

It is good practice for one developer to add or modify code for software and have an independent developer review the change for correctness and quality. A code review by a developer with "a fresh set of eyes" will use that perspective to analyze the code for a variety of factors, provide feedback, and have the developer who authored the code potentially make changes. Code review reduces the risk of releasing software with undesirable behavior or incorrectly addressing the business case of a particular feature or business case overall. Like other methods above, it similarly increases the ease of knowledge transfer and limits the institutional risk of one singular developer being the sole staff member understanding the software.

All changes to the project's source are made through [pull requests](https://github.com/metaschema-framework/liboscal-java/pulls), with required reviews. This ensures that all changes are reviewed by another set of eyes. The GitHub [CODEOWNERS](https://github.com/metaschema-framework/liboscal-java/blob/main/CODEOWNERS) configuration is used to direct reviews to the people responsible for the changed code.

#### References

- [NIST Special Publication 800-95: Guide to Secure Web Services](https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-95.pdf)
- [NIST Information Quality Standards](https://www.nist.gov/director/nist-information-quality-standards)
- [Examples of Software Development Standards from the Department of State](https://github.com/CA-CST-SII/Software-Standards/)