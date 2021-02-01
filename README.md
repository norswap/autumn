# Autumn

- [Install](/doc/INSTALL.md)
- [Javadoc] (updates daily) / [Mirror] (might need loading)
- [User Guide](/doc/README.md)

[Javadoc]: https://javadoc.io/doc/com.norswap/autumn/
[Mirror]: https://jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/

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
- [Java Grammar](/examples/norswap/lang/java/JavaGrammar.java)

The latest version of this document is available online at
https://github.com/norswap/autumn/blob/master/README.md

## Versioning

Versions are `M.m.p`

- Major (`M`) is incremented when significant changes are made to the library. It might take
  non-trivial time to migrate.
- Minor (`m`) is incremented when new features are added, or existing features are modified.
  The main contract here is that migration should be quick, and a clear migration path exists.
- Patch (`p`) is incremented for hotfixes, or tiny / quality-of-life improvements. Patch **never**
  introduce breaking changes, excepted under the guise of bug-fixes.

## Legacy

If you were looking for older Autumn releases (such as those described in one of my [papers]), see
the [autumn_archive repository][archive].

If you were looking for the Whimsy compiler framework, see [here][whimsy]. The Uranium
semantic analysis library lives on [in this repository][uranium].

[papers]: https://norswap.com/publications/
[archive]: https://github.com/ncellar/autumn_archive
[whimsy]: https://github.com/ncellar/whimsy
[uranium]: https://github.com/norswap/uranium
