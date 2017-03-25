<img align="right" src="logo.png" />

# The Whimsy Compiler Framework

[![build](https://api.travis-ci.org/norswap/whimsy.svg?branch=master)][travis]
[![jitpack](https://jitpack.io/v/norswap/whimsy.svg)][jitpack]

[travis]: https://travis-ci.org/norswap/whimsy
[jitpack]: https://jitpack.io/#norswap/whimsy

Whimsy is a research project that aims to make writing compilers, transpilers
and source analysis tools easier by supplying easy to use facilities embedded in
a general programming language.

- [Documentation](/doc/README.md)
- [Developer Guide]
- [Installation](#installation)

[Developer Guide]: (/doc/dev-guide.md)

Whimsy currently comprises two parts:

- [Autumn][autumn-doc]: a parsing library.
- [Uranium][uranium-doc]: a middle-end compiler library.  
  Uranium annotates ASTs and computes over them, using a reactive architecture.

## Autumn

Autumn is a [Kotlin] parser combinator library written in with an unmatched feature set:

- Bundles pre-defined parsers and combinators for most common use cases
- Write your own parsers with regular Kotlin/Java code
- Scannerless, but with tokenization support
- Associativity & precedence support for operators
- Left-recursion support
- Context-sensitive parsing **!!**
- Pluggable error-reporting mechanism
- Reasonably fast (3x slower than ANTLR)
- Thoroughly documented
- Small & clean codebase

[Kotlin]: https://kotlinlang.org/

☞ [LEARN MORE][autumn-doc]

## Uranium

Uranium is currently a work in progress.

[autumn-doc]: /doc/autumn/README.md
[uranium-doc]: /doc/uranium/README.md

## Installation

- [Using Maven](#using-maven)
- [Using Gradle](#using-gradle)
- [Other Build Systems](#other-build-systems)
- [Manually](#manually)
- [Build from Sources](#from-sources)

In all cases, you will still need Kotlin installed, either as part of [IntelliJ IDEA] or
[on the command line].

[IntelliJ IDEA]: https://www.jetbrains.com/idea/download/#section=windows
[on the command line]: https://kotlinlang.org/docs/tutorials/command-line.html

### Using Maven

In your `pom.xml`, add the following inside `<project><repositories>`:

    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>

and the following inside `<project><dependencies>`:

	<dependency>
	    <groupId>com.github.norswap</groupId>
	    <artifactId>whimsy</artifactId>
	    <version>-SNAPSHOT</version>
	</dependency>

### Using Gradle

In your `build.gradle`, add the following inside `allprojects { repositories {`:

    maven { url 'https://jitpack.io' }

and the following inside `dependencies {`:

    compile 'com.github.norswap:whimsy:-SNAPSHOT'

### Other Build Systems

See https://jitpack.io/#norswap/whimsy

### Manually

Download the [latest release] `kotlin-fatjar` and add it on your project's classpath.

The release bundles Whimsy's only dependency: the [Apache BCEL] library, but renames its packages to
avoid any possibility of conflict.

The fatjar also includes test fixtures to help you build your own tests. Those depend on the
TestNG library (built with version 6.11 but should be more broadly compatible), which is **not**
bundled in.

[Apache BCEL]: https://commons.apache.org/proper/commons-bcel/

### From Sources

See the [Developer Guide].