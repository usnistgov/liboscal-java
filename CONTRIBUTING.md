# Contributing to this Project

This page is for potential contributors to this project. It provides basic information on the project, describes the main ways people can make contributions, explains how to report issues relating to the project and project artifacts, and lists pointers to additional sources of information.

## Project approach

This project uses an agile approach for development, where we focus on implementing the 20% of the functionality that solves 80% of the problem. We’re trying to focus on the core capabilities that are needed to provide the greatest amount of benefit. Because we’re working on a small set of capabilities, this allows us to make very fast progress. We’re building the features that we believe solve the biggest problems to provide the most value. We provide extension points that allow uncovered cases to be supported by others.

We track our current work items using GitHub [project cards](../../projects).

## Making Contributions

Contributions are welcome to this project repository.

For more information on the project's current needs and priorities, see the project's GitHub issue tracker (discussed below). Please refer to the [guide on how to contribute to open source](https://opensource.guide/how-to-contribute/) for general information on contributing to an open source project.

## Issue reporting and handling

All requests for changes and enhancements to the repository are initiated through the project's [GitHub issue tracker](../../issues). To initiate a request, please [create a new issue](https://help.github.com/articles/creating-an-issue/). The following issue templates exist for creating a new issue:
* [User Story](../../issues/new?assignees=&labels=User+Story%2Cenhancement&projects=&template=1-feature_request.yml): Use to describe a new feature or capability to be added to the project.
* [Defect Report](../../issues/new?assignees=&labels=bug&projects=&template=2-bug_report.yml): Use to report a problem with an existing feature or capability.

The project team regularly reviews the open issues, prioritizes their handling, and updates the issue statuses, proving comments on the current status as needed.

## Contributing to this GitHub repository

This project uses a typical GitHub fork and pull request [workflow](https://guides.github.com/introduction/flow/). To establish a development environment for contributing to the project, you must do the following:

1. Fork the repository to your personal workspace. Please refer to the Github [guide on forking a repository](https://help.github.com/articles/fork-a-repo/) for more details.
1. Create a feature branch from the main branch for making changes. You can [create a branch in your personal repository](https://help.github.com/articles/creating-and-deleting-branches-within-your-repository/) directly on GitHub or create the branch using a Git client. For example, the ```git branch working``` command can be used to create a branch named *working*.
1. You will need to make your modifications by adding, removing, and changing the content in the branch, then staging your changes using the ```git add``` and ```git rm``` commands.
1. Once you have staged your changes, you will need to commit them. When committing, you will need to include a commit message. The commit message should describe the nature of your changes (e.g., added new feature X which supports Y). You can also reference an issue from the project repository by using the hash symbol. For example, to reference issue #34, you would include the text "#34". The full command would be: ```git commit -m "added new feature X which supports Y addressing issue #34"```.
1. Next, you must push your changes to your personal repo. You can do this with the command: ```git push```.
1. Finally, you can [create a pull request](https://help.github.com/articles/creating-a-pull-request-from-a-fork/).

### Repository structure

This repository consists of the following directories and files pertaining to the project:

- [.github](.github): Contains GitHub issue and pull request templates for the project.
- [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md): This file contains a code of conduct for all project contributors.
- [CONTRIBUTING.md](CONTRIBUTING.md): This file is for potential contributors to the project. It provides basic information on the project, describes the main ways people can make contributions, explains how to report issues, and lists pointers to additional sources of information. It also has instructions on establishing a development environment for contributing to the project and using GitHub project cards to track development sprints.
- [LICENSE.md](LICENSE.md): This file contains license information for the files in this GitHub repository.
- [USERS.md](USERS.md): This file explains which types of users are most likely to benefit from use of this project and its artifacts.

## Contributing to ongoing Development

This project is using the GitHub [project cards](../../projects) feature to track development using a [Kanban](https://en.wikipedia.org/wiki/Kanban_\(development\)) approach.

### User Stories

Project cards are used to represent a set of [user stories](../../issues?q=is%3Aopen+is%3Aissue+label%3A%22User+Story%22), that describe features, actions, or enhancements that are intended to be developed. Each user story is based on a [template](../../issues/new?template=feature_request.md&labels=enhancement%2C+User+Story) and describes the basic problem or need to be addressed, a set of detailed goals to accomplish, any dependencies that must be addressed to start or complete the user story, and the criteria for acceptance of the contribution.

The goals in a user story will be bulleted, indicating that each goal can be worked on in parallel, or numbered, indicating that each goal must be worked on sequentially. Each goal will be assigned to one or more individuals to accomplish.

The user story issue discussion will be used for periodic updates, questions, and comments related to designs and requirements.

Note: A user story that is not selected as an active work item can still be worked on at any time by any project contributor and will still be considered as a possible contribution to the project.

### Project Status

Each project card will be in one of the following states:

- "To do" - The user story has been queued for work, but the work has not started.
- "In progress" - Work has started on the user story, but development has not been completed.
- "Review in Progress" - All goals for the user story have been completed and one or more pull requests have been submitted for all associated work. Associated PRs require review by code owners and community reviewers to ensure that all goals and acceptance criteria have been met and that any identified concerns have been addressed.
- "Reviewer Approved" - All required reviews of a pull request related to a user story have been completed. The pull request still needs to be merged.
- "Done" - Once the contributed work has been reviewed and the pull request has been merged, the user story will be marked as "Done".

Note: One or more pull requests must be submitted addressing all user story goals before the issue will be moved to the "under review" status. If any goals or acceptance criteria have not been met, then the user story will be commented on to provide feedback, and the issue will be returned to the "In progress" state.

## Communications mechanisms

You can contact the maintainers of this project at [maintainers@metaschema.dev](mailto:maintainers@metaschema.dev) if you are interested in contributing to the development of this project or exchanging ideas.

## Developer information

### Core metaschema functions and custom project functions

The Metaschema [specification](https://pages.nist.gov/metaschema/specification/syntax/metapath/) and the conformant [metaschema-java](https://github.com/metaschema-framework/metaschema-java/tree/main/CONTRIBUTING.md#core-metaschema-functions-functions) library implement the Metapath functions required by the specification. However, other libraries, such as this [this project's library](https://github.com/metaschema-framework/liboscal-java) can extend the inventory of available functionality with custom functions that are not required in the core specification. These are OSCAL-specific functions, not core Metaschema functions. At the the of the v4.1.0 release, there are only two custom OSCAL-specific functions in this library.

- The [`HasOscalNamespace` class](https://github.com/metaschema-framework/liboscal-java/blob/v4.1.0/src/main/java/gov/nist/secauto/oscal/lib/metapath/function/library/HasOscalNamespace.java) implements the `has-oscal-namespace` function.
- The [`ResolveProfile` class](https://github.com/metaschema-framework/liboscal-java/blob/v4.1.0/src/main/java/gov/nist/secauto/oscal/lib/metapath/function/library/HasOscalNamespace.java) implements the `resolve-profile()` function.

See the library's [registry class with an inventory of these functions](https://github.com/metaschema-framework/liboscal-java/blob/main/src/main/java/gov/nist/secauto/oscal/lib/metapath/function/library/OscalFunctionLibrary.java) for a current list of custom, OSCAL-specific functions after the v4.1.0 release.

# Licenses and attribution

## This project is in the public domain

This project is in the worldwide public domain.

This project is in the public domain within the United States, and copyright and related rights in the work worldwide are waived through the [CC0 1.0 Universal public domain](https://creativecommons.org/publicdomain/zero/1.0/) dedication.

## Contributions will be released into the public domain

All contributions to this project will be released under the CC0 dedication. By submitting a pull request, you are agreeing to comply with this waiver of copyright interest.
