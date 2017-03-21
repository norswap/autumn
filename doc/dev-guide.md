# Developer Guide

## Source Layout

The repository has the following top-level directories:

- `doc/` contains the documentation of all projects. 
- `src/` contains the source code of all projects.
- `experimental/` contains the source of the experimental features of all projects.
- `test/` contains the the tests for all projects.

To learn more about how the source of specific projects is structured, follow the following links:

- [Autumn Developer Guide](/doc/autumn/dev-guide.md)
- [Uranium Developer Guide](/doc/uranium/dev-guide.md)

## Building

There are two options to build from sources:

1) Import the project in IntelliJ IDEA, using a Kotlin plugin with version 1.1.1.

2) Build on the command line. You will need:

    - The [Kotlin CLI Compiler], version 1.1.1 
    - GNU Make
    - curl
    - unix utilities: mkdir, touch, rm, find, grep, xargs
    
    [Kotlin CLI Compiler]: https://kotlinlang.org/docs/tutorials/command-line.html