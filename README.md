# Autumn

- [Install](/doc/INSTALL.md)
- [Javadoc](https://jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/)
- [User Guide](/doc/README.md)

---

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

## Legacy

If you were looking for older Autumn releases (such as those described in one of my [papers]), see
the [autumn_archive repository][archive].

If you were looking for the Whimsy compiler framework, see [here][whimsy]. The Uranium
semantic analysis library lives on [in this repository][uranium].

[papers]: https://norswap.com/publications/
[archive]: https://github.com/ncellar/autumn_archive
[whimsy]: https://github.com/ncellar/whimsy
[uranium]: https://github.com/norswap/uranium
