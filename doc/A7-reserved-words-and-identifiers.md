# A7. Reserved Words and Identifiers

Autumn, as most PEG parsers, does not separate the lexical layer (concerned with matching characters
into fundamental units of meaning — tokens) from the syntactic layer built on top of it.

In systems that separate lexing from parsing, emitting tokens is the role of the *lexer*. A lexer
will typically emit tokens for keywords, identifiers, number literals, string literals, braces,
arithmetic operators, ...

When using Context Free Grammars (CFGs), lexing is a must, as CFGs can't express some language
restricton that are very useful at the lexical layer.

Let's take Java as a very representative example. Java [specifies] that the input string must
translate to a sequence of tokens using *longest-match*: basically, if two distinct tokens can
be matched at the start of the remainder of the input, the algorithm selects the token that matches
the most input.

[specifies]: https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.2

This scenario comes up in the case of keywords. Consider the input `doProcedure = true;`. `do` is a
keyword in Java, but `doProcedure` is a legal identifier, and is longer, so the Java lexer matches
an identifier. Java also specifies that identifiers can't be keywords, so `do` on its own is matched
as a keyword.

Surprisingly, there is no way to encode this rule (longest-match) in a CFG! ([*1])
In Autumn, this can simple be encoded with the [`longest`] combinator.

[`longest`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#longest-java.lang.Object...-

The PEG formalism (which inspired Autumn) doesn't have something analogous to `longest` but can
nevertheless encode most lexing constraints. For keywords and lowercase-letter identifiers, we'd do
something like this:

```
Keyword     ::= if | while | do | ...
Identifier  ::= !Keyword [a-z]+
```

This pattern is quite wasteful: each time you want to parse an identifier, you now need to trudge
through the whole list of keywords. Depending on how much your parser backtracks, this can happen
quite often.

## Reserved Word and Identifiers in Autumn

Many languages have reserved words, and since the setup described above is always the same,
Autumn offers to automate it away. Here is how to do it:

1. Set [`Grammar#id_part`] to a parser that can match any single character that can appear within
   an identifier.
   
2. Use the [`reserved(String)`] combinator to define your reserved words (these are keywords like `if` or
  `while`, but in some language also reserved type names like `int` or special values liek `true`).
   
  `reserved("hello")` is equivalent to `seq("hello", id_part.not()).word()`, but it also registers
  `"hello"` so that it will be excluded when parsing identifiers.

3. Use the [`identifier`] combinator to define your identifier rule. The combinator uses the passed
   parser to recognize identifiers, but excludes any identifier that would also match a reserved
   word.
   
It's as simple as that.

[`Grammar#id_part`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#id_part
[`reserved(String)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#reserved-String-
[`identifier`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#identifier-Object-

## Performance Considerations

It can seem slow to trudge through every the rules reserved word when we want to match an
identifier. To alleviate this problem, Autumn use sthe optimized [`StringChoice`] parser, whose
implementation uses a [trie] to cut down on the match time.

[`StringChoice`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/StringChoice.html
[trie]: https://en.wikipedia.org/wiki/Trie

If identifiers are often reparsed at the same input position (**and you have experimentally
determined that they cause a performance issue**), consider using memoization to speed up the parse
— see section [B2. Memoization][B2].

[B2]: B2-memoization.md