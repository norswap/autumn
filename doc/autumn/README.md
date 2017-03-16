# Autumn Documentation

Welcome to the Autumn documentation!

The latest version of this document is available online at  
https://github.com/norswap/whimsy/tree/master/doc/autumn/README.md

## Table of Contents

### [Why use Autumn?](faq/why.md)

### Guide

1. [Your First Grammar](guide/first-grammar.md)
1. [Using Basic Parsers](guide/basic-parsers.md)
1. [Writing Your Own Parsers](guide/own-parsers.md)
1. [Handling Left-Recursion](guide/left-recursion.md)
1. [Building an AST](guide/ast.md)
1. [Handling Side-Effects](guide/side-effects.md)

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

### Misc Notes

- [Publications about Autumn](publications/README.md)
- [List of Interesting Parsing Tools](notes/parsing-tools.md)
- [Parsing Expression Grammars](notes/peg.md)
- [List of Standard PEG Operations](notes/peg-ops.md)