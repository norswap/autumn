# B4. Debugging & Tracing a Parse

## Checklist

Here are a few easy ways to debug and/or improve the performance of your parser:

- Write unit tests using [`TestFixture`]. See [an example] for the [Java grammar].
  
- Make sure assertions are enabled by passing the `-ea` argument to the Java virtual machine (`java`
command).
  
- If you are using IntelliJ IDEA, make sure to define the environment variable
 `AUTUMN_USE_CHAR_COLUMN` for more accurate hyperlinked file locations. The same applies if your
 editor supports hyperlinked file locations with columns expressed as a character offset (tabs count
 for 1) instead of width (tabs go to next multiple of the tab size).

- **!!!** Don't forget to disable call stack recording in production with
  `ParseOptions.recordCallStack(false).get()`

- Specify that the parse should be traced via the options:
```
ParseOptions options = ParseOptions.trace(true).get();
ParseResult result = Autumn.parse(grammar, input, options);
if (!result.fullMatch)
    //
else
    System.out.println(options.metrics);
```

- Use a `PEEK_ONLY` collect parser to print or set a breakpoint during the parse:
```
public rule myParser = seq(a, b, whatever)
    // add this line:
    .collect($ -> System.out.println("N was here!"), PEEK_ONLY); 
```

- How are your infix expression (e.g. arithmetic) implemented? If you're not using
[`LeftExpression`] and [`RightExpression`], it's very likely that your expressions are causing a
performance bug. It's also good to extend your scrutiny to other recursive constructs.

[`TestFixture`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/TestFixture.html
[an example]: /test/lang/java/TestGrammar.java
[Java grammar]: /examples/norswap/lang/java/JavaGrammar.java
[`LeftExpression`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/parsers/LeftExpression.html
[`RightExpression`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/parsers/RightExpression.html
[A6]: A6-left-recursion-associativity.md

## Problem Area: Lexical Layer

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

[`longest`]: https://javadoc.jitpack.io/com/github/norswap/autumn/-SNAPSHOT/javadoc/norswap/autumn/Grammar.html#longest-java.lang.Object...-

The PEG formalism (which inspired Autumn) doesn't have something analogous to `longest` but can
nevertheless encode most lexing constraints. For keywords and lowercase-letter identifiers, we'd do
something like this:

```
Keyword     ::= if | while | do | ...
Identifier  ::= !Keyword [a-z]+ 
```

This pattern is quite wasteful: each time you want to parser an identifier, you now need to trudge
through the whole list of keywords. Depending on how much your parser backtracks, this can happen
quite often.

## Solution: Memoization

One solution to this problem is to use memoization, as described in section [B2. Memoization][B2].

A lot of PEG parsers are actually memoizing (but by default, not Autumn). However, experiments
have shown that full memoization is more often than not slower than no memoization at all! For those
parsers that claim that memoization is faster, I conjoncture that memoizing only the tokens would
actually be faster than full memoization. See my [PhD thesis (pdf)], Section 6.1.3, for a full
discussion.

[PhD thesis (pdf)]: https://norswap.com/pubs/thesis.pdf
[B2]: B2-memoization.md

However, selective memoization, guided by tracing (see the checklist above) can be used to
improve performance — in particular at the lexical level.

The [example grammar for Java][javagram] grammar is a perfect example. After experimenting
carefully, I ended up adding memoization to 5 rules, yielding a 16% parse time reduction. These
rules fell in two categories.

The first category were choices of items that are typically used as tokens: identifiers (remember
the list of keyword!), literals and primitive types.

The second categories included things that were parsed multiple times at the same location: list of
modifiers (`private`, `final`, ...) and list of annotations. For instance, modifiers can precede
both variable and method declarations.

## Left-Factoring

For my second category from the last section (things that were parsed multiple times at the same
location), there is another possible fix: left-factoring. To give you a trivial example, you could
rewrite:

```
public rule declaration = choice(
    seq(modifiers, method_declaration),
    seq(modifiers, field_declaration));
```

into:

```
public rule declaration =
    seq(modifiers, choice(method_declaration, field_declaration));
```

The issue with left-factoring is that outside trivial examples, it tends to make your grammars
uglier. It also requires some gymnastic to build the AST (often requiring the use of [`LOOKBACK`]).
That being said, the Java grammar also uses some left-factoring (try looking up `class_body_decl`
for an example).

Note that a big benefit of using [`LeftExpression`] and [`RightExpression`] (see section [A6.
Left-Recursion and Associativity][A6]) is that they essentially perform left-factoring for you
under the wraps.

[`LOOKBACK`]: A5-creating-an-ast.md#customizing-collect-parsers
[javagram]: /examples/norswap/lang/java/JavaGrammar.java

----
**Footnotes**

[*1]: #footnote1
<h6 id="footnote1" display=none;></h6>

(*1) To be strictly correct, it is possible, but only if you encode a whole trie into your grammar.
Ain't nobody got time for that.

