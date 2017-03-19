# FAQ: Why use Autumn?

Autumn is a [Kotlin] parser combinator library written in with an unmatched feature set:

- Bundles pre-defined parsers and combinators for most common use cases
- Write your own parsers with regular Kotlin/Java code
- Scannerless, but with tokenization support
- Associativity & precedence support for operators
- Left-recursion support
- Context-sensitive parsing **!!!**
- Pluggable error-reporting mechanism
- Reasonably fast (3x slower than ANTLR)
- Thoroughly documented
- Small & clean codebase

[Kotlin]: https://kotlinlang.org/

## Goal & Philosophy

The goal of Autumn is to [combine the advantages][handtool] of hand-written parsers and parsing
tools, without incurring the downsides. Read [Hand-Written Parsers vs Parsing Tools][handtool] to
learn more.

[handtool]: faq/hand-vs-tool.md

Autumn also exists to fix shortcomings with existing parsing tools. It was the [first parsing
tool](/doc/autumn/publications/sle2015.md) to support left-recursion with associativity selection, and is
[one of the few](/doc/autumn/publications/sle2016.md) parsing tool to support context-sensitive parsings.

Philosophically, Autumn is firmly in the camp of *recognition-based* parsing (like PEG), as opposed to
parsing using *generative* grammar formalisms (like CFG). You can read about my opinions on the topic
in [this note about PEGs](../notes/peg.md).

Last, but not least, Autumn is also a [research project](publications.md).

## Classification

Autumn is a parsing-combinator library. It supports a superset of the [PEG](peg.md) formalism, but
since you can define your own parsers, it is actually closer to "functional-style" parser-combinator
libraries like [parsec](https://wiki.haskell.org/Parsec).

Through its use of inline functions, Autumn is also much closer to parser generators
than most parser-combinator libraries.

## Autumn vs ...

I initially thought to write a detailed comparison of Autumn with some other parsing tools, but
there are [too many][tools] of them and I just can't give them all justice.

This page should answer your questions about Whimsy's specificities. If a doubt remains, please
ask in an [issue](https://github.com/norswap/whimsy/issues).

You can also peruse a [list of parsing tools][tools] I find interesting and/or relevant.

[tools]: parsing-tools.md