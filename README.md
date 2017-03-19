# The Whimsy Compiler Framework

Whimsy is a research project that aims to make writing compilers, transpilers
and source analysis tools easier by supplying easy to use facilities embedded in
a general programming language.

- [Documentation](/doc/README.md)
- [Developer Guide (source layout)](/doc/dev-guide.md)

Whimsy currently comprises two parts:

- [Autumn][autumn-doc]: a parsing library
- [Uranium][uranium-doc]: a library to annotate and compute (over) ASTs, using a reactive
  architecture

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