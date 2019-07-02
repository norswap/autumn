# B1. Lexing / Lexical Analysis / Tokenization

*Lexing*, *Lexical Analysis* and *Tokenization* all refer to the same thing. A common way to do
parsing is to split it into two separate steps. Lexing (& synonyms) is the first of these steps,
which consists of going from a sequence of characters (typically) to a sequence of *tokens*.

For instance, languages typically have tokens for keywords, identifiers, number literals, string
literals, braces, arithmetic operators, ...

The second step of parsing then uses the sequence of tokens as its input, and looks like what
we've described in [Autumn Basics](README.md#a-autumn-basics).

While lexing *is* a form of parsing, one generally calls the first step *lexing*/*lexical
analysis*/*tokenization* while *parsing* is reserved for the second step.

Such a division has many potential advantages:

- You can use different parsing techniques for both steps, in particular it is useful to do certain
  things during lexing that formalisms like CFG or PEG can't do, or can't do efficiently.
  
- It generally improves the performance, especially when the parsing algorithm may backtrack.
  
- It simplifies error reporting, as the errors can point to tokens instead of single characters
  (you can do better anyway, but with lexing this comes "for free").
  
Let's take Java as a very representative example. Java [specifies] that the input string must
translate to a sequence of tokens using *longest-match*: basically, if two distinct tokens can
be matched at the start of the remainder of the input, the algorithm selects the token that matches
the most input.

[specifies]: https://docs.oracle.com/javase/specs/jls/se8/html/jls-3.html#jls-3.2

This scenario comes up in the case of keywords. Consider the input `do_procedure = true;`. `do` is a
keyword in Java, but `do_procedure` is a legal identifier, and is longer, so the Java lexer matches
an identifier. Java also specifies that identifiers can't be keywords, so `do` on its own is matched
as a keyword.

Surprisingly, there is no way to encode this rule (longest-match) in a CFG! ([*1])
In Autumn, this can simple be encoded with the [`longest`] combinator.

[`longest`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#longest-java.lang.Object...-

The PEG formalism (which inspired Autumn) doesn't have something analogous to `longest` but can
nevertheless encode most lexing constraints. For keywords and identifiers, we'd do something like
this:

```
Keyword     ::= if | while | do | ...
Identifier  ::= !Keyword [a-z]+ 
``` 

(Supposing identifiers are comprised of only lowercase letters, which is not the case in Java, of
course.)

This capability is often sold as a benefit of PEG over CFG, but I think the argument can be
disingenuous. If your parser isn't memoizing (saving the result of every parser invocation) ([*2]),
then walking the whole list of keywords each time you want to match an identifier is very expensive
— because given backtracking you might have to do it many times for a single identifier! Note that
the `longest` combinator suffers from exactly the same issue.

This is where Autumn's solution for lexing enters the scene. The idea is to let you specify the
syntax of tokens using all the usual parsers and combinators, but to memoize the result of token
selection in order to greatly improve performance.

We actually do more than just "memoize the result of an attempt to match a specific token". We
try to obtain the longest-match of any token at the current position, and memoize the result
of that query. So the next time we try to match **any** token at the same position, the result will
already be ready.

It bears pointing out that (if you use the supplied facilities), Autumn **assumes longest-match
lexing**. In practice, I do not know of any language that does not do this, and it's been done this
way since the time of the venerable `lex` tool. But even if your language doesn't follow suit, you
can always use parsers (especially [lookahead parsers](A4-basic-parsers.md#lookahead) or even
[custom parsers]) to disambiguate.  

Also worth pointing out: we do not actually split the parse in two steps: this occurs during the
normal parse. This lets us keep a single conceptual framework (no need to define separate lex-time
and parse-time parsers!), and Autumn is flexible enough to accomodate it, so why not do it? As an
added benefit, you're also still free to match on the underlying input sequence if you want to.

Autumn also supports a more general form of memoization, which will be described in [B3.
Memoization][B3].

<!-- TODO: speak about error reporting: how tokens improve it little (cf. last sub-section)
     but we have other means of improving it -->

[custom parsers]: B4-custom-parsers.md

## Tokenization in Practice

The `DSL` class (which you should extend in the class that defines your grammar, as in
[A2](A2-first-grammar.md)), holds a field of type [`Tokens`] that will be used to build up the
token set.

To define a kind of token, simply define the parser as usual then wrap using the [`rule#token()`]
combinator. Invoking the combinator will add the wrapped parser as a "base parser" to the `Tokens`
instance, defining it as new kind of token.

The parsers returned by `token()` hold a reference to the `Tokens` object. When invocated, they
delegate to `Tokens`, which tries to find the longest-matching token amongst those we defined (in
the same way that [`Longest`] does), and memoizes that result if it wasn't already. Then, it
compares the token kind that was actually matched to the kind that we tried to match, and succeeds
only if they are similar.

We also offer the [`token_choice`] combinator as an optimized form of choice between token parsers.
We could just wrap token parsers inside a regular [`choice`], but this would repeat the token lookup
for each attempted alternative. Instead, using `token_choice`, we only perform the lookup once and
then check if the matched token kind is one of those we were trying to match.

So, imagining we wanted to add tokens to our [JSON example] and we want to group to push
a new `Literal` node for number and string literals.

```java
public final class JSON extends DSL
{
    ...
    
    public rule number =
        seq(character('-').opt(), integer, fractional.opt(), exponent.opt())
        .collect()
        .push_with_string((p,xs,str) -> Double.parseDouble(str))
        .word().token();
    
    ...
    
    public rule string =
        seq(character('"'), string_char.at_least(0), character('"'))
        .collect()
        .push_with_string((p,xs,str) -> str.substring(1, str.length() - 1))
        .word().token();
    
    ...
    
    public rule number =
        seq(character('-').opt(), integer, fractional.opt(), exponent.opt())
        .word().token();
    
    ...
    
    public rule string =
        seq(character('"'), string_char.at_least(0), character('"'))
        .word().token();
        
    public rule literal =
        token_choice(number, string)
        .push((p,xs) -> new Literal(xs[0]));
        
    ...
    
    public rule value = lazy(() -> choice(
        literal,
        ...));
        
    ...
}
```

We added `.token()` at the end of the `number` and `string` rules, added the `literal` rule, and
redefined the `value` rule to use `literal` instead of `number` and `string`.

Of course, in reality, we'd have added much more tokens (for identifiers, operators, separators,
..).

[`Tokens`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Tokens.html
[`rule#token()`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#token--
[`token_choice`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#token_choice-java.lang.Object...-
[`build_tokenizer()`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#build_tokenizer--
[`Longest`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Longest.html
[`choice`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#choice-java.lang.Object...-
[JSON example]: A5-creating-an-ast.md

## Further Precisions: Error-Reporting, Context/State, Memoization

Tokens are submitted to a special error-handling regime: we never report errors that occur *inside*
a token parser. However, we may report that we failed to match the token!

This is actually a customization that can be made on any parser, by setting the
[`Parser#exclude_errors`] flag of a Parser, but it is done automatically for token parsers.

We'll discuss error reporting further in section TODO.

Token parsers may modify the parse state (in fact, they have to if they are to push items onto the
[value stack]). However, since they are indiscriminately memoized, they can't be context-sensitive
(we'll discuss context-sensitivity in section [B2. Context-Sensititive (Stateful) Parsing][b2]).

<!-- TODO propose alternative when context-sensitive tokens would be welcome + rationale for not
     including them -->
     
Finally, we note it's possible to change the memoization strategy used by `Tokens`. To do this, one
should explicitly call the [`DSL(Supplier<Memoizer>)`] super-constructor when extending `DSL`.
The purpose of a [`Memoizer`] is covered in section [B3. Memoization][B3].

[`Parser#exclude_errors`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/Parser.html#exclude_errors
[value stack]: A5-creating-an-ast.md#basic-principles--changes-explained
[b2]: B2-context-sensitive-parsing.md 
[`DSL(Supplier<Memoizer>)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#DSL-java.util.function.Supplier-
[`Memoizer`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/memo/Memoizer.html
[B3]: B3-memoization.md 

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) To be strictly correct, it is possible, but only if you encode a whole trie into your grammar.
If you even consider doing this, I'm afraid your soul has already been lost.

[*2]: #footnote2
<h6 id="footnote2" display=none;></h6>

(*2) A lot of PEG parsers are actually memoizing (but by default, not Autumn). However, experiments
have shown that full memoization is more often than not slower than no memoization at all! For those
parsers that claim that memoization is faster, I conjoncture that memoizing only the tokens would
actually be faster than full memoization. See my PhD thesis (soon) for a full discussion.

Memoization is discussed more in depth in [B3. Memoization][B3].

<!-- TODO (Kim): indicate whether the experiment are/hold for Autumn -->