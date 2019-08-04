# Autumn

- [Maven Dependency][jitpack]
- [Javadoc][snapdoc]
- [User Guide](/doc/README.md)

[jitpack]: https://jitpack.io/#norswap/autumn
[snapdoc]: https://jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/

Autumn is a Java (8+) parser combinator library written with an unmatched feature
set:

- Bundles pre-defined parsers and combinators for most common use cases
- Write your own parsers with regular Java code
- Support for both scannerless parsing and a separate lexing/tokenization step
- Support for parsing both text strings and lists of objects
- Associativity support for operators
- Left-recursion support
- Context-sensitive parsing **(exclusive !!)**
- Reasonably fast (5x slower than (the very fast) ANTLR)
- Thoroughly documented
- Small & clean codebase

Examples:

- [JSON Grammar](/examples/norswap/lang/json/JSON.java)
- [Java Grammar](/examples/norswap/lang/java/Grammar.java)

The latest version of this document is available online at  
https://github.com/norswap/autumn/blob/master/README.md

## Installation 

If you are using Maven (or another popular JVM build tool), [see here][jitpack].

## Legacy

If you were looking for older Autumn releases (such as those described in one of my [papers]), see
the [autumn_archive repository][archive].

If you were looking for the Whimsy compiler framework, see [here][whimsy]. Whimsy was Autumn +
Uranium (a middle-end compiler library), and Uranium didn't pan out, so everything that was useful
in Whimsy now lives here.

[papers]: https://norswap.com/publications/
[archive]: https://github.com/ncellar/autumn_archive
[whimsy]: https://github.com/ncellar/whimsy

<!-- (for first release)

A self-contained JAR file is also available [here][jar] as part of [a release] that also
includes sources and javadoc.

[jar]: https://github.com/norswap/autumn/releases/download/1.0.0/autumn-1.0.0-fatjar.jar
[a release]: https://github.com/norswap/autumn/releases

-->
