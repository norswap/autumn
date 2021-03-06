# B4. Debugging & Tracing a Parse

## Checklist for Debugging

Here are a few easy ways to debug and/or improve the performance of your parser:

- Write unit tests using [`TestFixture`]. See [an example] for the [Java grammar].

- Make sure assertions are enabled by passing the `-ea` argument to the Java virtual machine (`java`
command).

- Call [`ParseResult#toString`] to get detailed information on the failure, pass it a
  [`LineMapString`] or [`LineMapTokens`] with the appropriate file name in order to get nicely
  formatted line:column file positions that will even be hyperlinked in IntelliJ (and possibly other
  editors/IDEs).

- If you are using IntelliJ IDEA, make sure to define the environment variable
 `AUTUMN_USE_CHAR_COLUMN` for more accurate hyperlinked file locations. The same applies if your
 editor supports hyperlinked file locations with columns expressed as a character offset (tabs count
 for 1) instead of width (tabs go to next multiple of the tab size).

- Use a `PEEK_ONLY` collect parser to print or set a breakpoint during the parse:
```
public rule myParser = seq(a, b, whatever)
    // add this line:
    .collect($ -> System.out.println("N was here!"), PEEK_ONLY);
```

- By default, parsers get assigned a rule name ([`Parser#ruleName`]) that is the field name they are
  assigned to (in subclasses of [`Grammar`] only). Sub-parsers that are not assigned to field have
  a `null` rule name. All parsers can be printed in full with [`Parser#toStringFull()`]. Beware of this method
  however - it is only safe to use if one of these condition hold:

  - The parser is not recursive.
  - The parser (and its sub-parsers) were defined in a [`Grammar`] subclass, and (a) parse has already
    been run using that grammar or (b) [`Grammar#makRuleNames(Class)`] has been called explicitly.

  Finally, note you can assign custom names to parsers with [`Parser#setRule`].

- Be aware that by default ([`Grammar#excludeWhitespaceErrors`]), errors occurring in whitespace
  are not recorded. This is generally what you want - otherwise you'd risk reporting a failure to
  match some whitespace instead of an error at the start of some syntax that follows whitespace.

  But if your whitespace is bugged, this could trip you up - so be aware of it.

  Be similarly wary if you've set [`Parse#excludeErrors`] on any parser that you are using.

Debugging an issue that involves whitespace?

## Checklist for Performance Tuning

- **!!!** Don't forget to disable call stack recording **and** well-formedness checking in production with
  `ParseOptions.recordCallStack(false).wellFormednessCheck(false).get()`

  These options are on by default to help you debug your grammar. (They're on by default because I
  don't trust people to think of them otherwise, and I'd rather they have a slow working parser than
  quite in frustration while making the parser.)

- If you run benchmarks, call [`Grammar#makeRuleNames()`] in advance, to avoid measuring the
  overhead to assigning rule names to parsers the first you use the grammar.

- Specify that the parse should be traced via the options (and don't forget to disable this in
production):
```
ParseOptions options = ParseOptions.trace(true).get();
ParseResult result = Autumn.parse(grammar, input, options);
if (!result.fullMatch)
    System.err.println(result.toString(new LineMapString("my-input", input), false));
else
    System.out.println(options.metrics);
```

- How are your infix expression (e.g. arithmetic) implemented? If you're not using
  [`LeftExpression`] and [`RightExpression`], it's very likely that your expressions are causing a
  performance bug. It's also good to extend your scrutiny to other recursive constructs.

- Using reserved words in your language (words that identifiers are not allowed to match)? Make
  sure to use the [`reserved`] and [`identifier`] combinators to define them, as discussed in
  section [A7. Reserved Words And Identifiers][A7].

[`TestFixture`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/TestFixture.html
[an example]: /test/lang/java/TestGrammar.java
[Java grammar]: /examples/norswap/lang/java/JavaGrammar.java
[`LeftExpression`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/LeftExpression.html
[`RightExpression`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/RightExpression.html
[A6]: A6-left-recursion-associativity.md
[`reserved`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#reserved-String-
[`identifier`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#identifier-Object-
[A7]: A7-reserved-words-and-identifiers.md
[`ParseResult#toString`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseResult.html#toString-norswap.autumn.positions.LineMap-boolean
[`LineMapString`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/positions/LineMapString.html
[`LineMapTokens`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/positions/LineMapTokens.html
[`Grammar#excludeWhitespaceErrors`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#excludeWhitespaceErrors
[`Parser#excludeErrors`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#excludeErrors
[`Parser#ruleName`]:  https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#rule--
[`Grammar`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html
[`Parser#toStringFull()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#toStringFull--
[`Grammar#makRuleNames()`]: https://javadoc.io/static/com.norswap/autumn/1.0.6/norswap/autumn/Grammar.html#makeRuleNames--
[`Parser#setRule`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html#setRule-java.lang.String-

## Memoization

One solution to performance woes is to use memoization, as described in section [B2.
Memoization][B2].

A lot of PEG parsers are actually memoizing (but by default, not Autumn). However, experiments have
shown that full memoization is more often than not slower than no memoization at all!  See my [PhD
thesis (pdf)], Section 6.1.3, for a full discussion.

[PhD thesis (pdf)]: https://norswap.com/pubs/thesis.pdf
[B2]: B2-memoization.md

Memoization is useful in cases where the parser often tries the same rule at the same input
position. To some extent this is normal, and memoization may unlock modest gains. In particular,
selective memoization, guided by tracing (see the checklist above).

If memoization enables order-of-magnitude gains in your grammar, I would kindly suggest that
it might have some fundamental inefficiens — make sure you gave the checklist above due
consideration.

The [example grammar for Java][javagram] grammar is a perfect example. After experimenting
carefully, I ended up adding memoization to 5 rules, yielding a 12% parse time reduction. These
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
