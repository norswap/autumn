# Autumn Documentation

Welcome to the Autumn documentation!

The latest version of this document is available online at  
https://github.com/norswap/whimsy/tree/master/doc/autumn/README.md

This document acts both as a user manual, and as a wiki of linked notes that I sometimes use
to structure my research.

## Table of Contents

### [Why use Autumn?](faq/why.md)

### Guide

1. [Your First Grammar](tutorial/first-grammar.md)
1. [Using Basic Parsers](tutorial/basic-parsers.md)
1. [Writing Your Own Parsers](tutorial/own-parsers.md)
1. [Handling Left-Recursion](tutorial/left-recursion.md)
1. [Building an AST](tutorial/ast.md)
1. [Handling Side-Effects](tutorial/side-effects.md)

### Advanced

- [Optimizing Your Grammars](advanced/optimize.md)
- [Testing Your Grammars](advanced/test.md)
- [Debugging Your Grammars](advanced/debug.md)

### Concepts

- [Parsers](parsers.md)

### Reference

- [Bundled Parsers Reference](bundled-parsers.md)

### Internals

- [Developer Guide](dev-guide.md)

### FAQ

- [Why use Autumn?](faq/why.md)
- [Hand-Written Parsers vs Parsing Tools](faq/hand-vs-tool.md)
- [What is the relationship between Autumn and PEGs?](faq/autumn-peg.md)
- [Is Autumn scannerless?](faq/scannerless.md)
- [Can I feed tokens to Autumn instead of text?](faq/feed-tokens.md)
- [Why is the DSL syntax so ugly/verbose?](faq/why-ugly.md)
- [Why write Autumn in Kotlin?](faq/kotlin.md)
- [Is Autumn compatible with Java?](faq/java-compat.md)
- [Is Autumn compatible with Kotlin's Javascript backend?](faq/js-compat.md)
- [Why are items in sequences / choices separated by `||` / `&&` ?](faq/seq-choice-syntax.md)

### Notes

- [Publications about Autumn](publications.md)
- [List of Interesting Parsing Tools](parsing-tools.md)
- [Parsing Expression Grammars](peg.md)