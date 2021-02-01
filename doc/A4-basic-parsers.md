# A4. Basic Parsers: Whirlwind Tour

In this section, we'll give an overview of the basic parsers and combinators of Autumn, so that you
know what is available to you.

Quick reminder of things established in previous sections:

Parsers are instances of (subclasses of) [`Parser`] while combinators can refer
either these parsers that have sub-parsers, or the methods used to create them (typically from the
classes [`Grammar`] and [`rule`]).

A `rule` is a wrapper for a `Parser` that enables easy parser construction via the [builder
pattern]. In general, however, we'll reserve the word "rule" for those that are assigned to a
field (i.e. `public rule my_rule = ... ;`).

[`Parser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Parser.html
[`Grammar`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html
[`rule`]:  https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html
[builder pattern]: https://dzone.com/articles/design-patterns-the-builder-pattern

In general, if you want to find out more about Autumn's built-in parsers, there are two places to
look at:

- The [`norswap.autumn.parsers`] package, which contains all bundled subclasses of `Parser`.
- The [`Grammar`] and [`rule`] classes, which contain all builder methods to construct instances
  of those classes.
  
In general, the behaviour of the parser will be specified in the documentation of its `Parser`
subclass. Builder methods sometimes provide important options, so their documentation is important
too.

[`norswap.autumn.parsers`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/package-summary.html

Let's jump right in. We won't review every built-in parser here, only the basic ones (which will
still be the majority of them). Details on advanced parsers will follow in further sections.

## Sequences and Choices

We already talked about the [`Sequence`] and [`Choice`] parsers in [the previous section]
(sub-section "Vertical Backtracking"), so I won't repeat the explanation here.

Construct them with [`seq`][seqb] and [`choice`][choiceb], respectively.

Basic examples:
- `seq(str("a"), str("b"), str("c"))`
- `choice(str("a"), str("b"), str("c"))`

[`Sequence`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Sequence.html
[`Choice`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Choice.html
[the previous section]: A3-how-autumn-works.md#vertical-backtracking
[seqb]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#seq-java.lang.Object...-
[choiceb]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#choice-java.lang.Object...-

## Longest

The [`Longest`] parser is a slight variation on the choice parser. Instead of matching the same
thing as its first successful child, `Longest` tries to match every one of its children, then
succeeds by matching the same thing as the one matching the most input. In case of a tie, it matches
the earliest longest matching child.

Construct with [`longest`][longestb].

Basic example: `longest(str("ab"), str("abb"), str("aba"))`

[`Longest`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Longest.html
[longestb]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#longest-java.lang.Object...-

## Repetitions

There are two classes that implement repetitions: [`Repeat`] and [`Around`].

`Repeat` supports specifying a number of repetitions to match, and whether that is a minimum or an
exact number. Those variants can be constructed with [`rule#at_least`] and [`rule#repeat`],
respectively.

Basic examples:
- `str("a").at_least(0)`
- `str("b").at_least(1)`
- `str("c").repeat(6)`

`Around` matches a series of repetitions of a subparser (the *around* parser), separated by another
parser (the *inside* parser). The canonical example is parsing comma-separated lists. It also
enables specifying whether a trailing repetition of the *inside* parser should be allowed, as some
languages allow trailing commas in comma-separated lists, for instance. Just like `Repeat`, a
minimum or exact number of repetitions (of the *around* parser) can also be specified.

Construct with [`rule#sep`], [`rule#sep_trailing`] and [`rule#sep_exact`]. 

Basic examples:
- `str("a").sep(0, "/")`  
   matches "", "a", "a/a", ...

- `str("a").sep_trailing(0, "/")`  
   matches "", "a", "a/", "a/a", "a/a/", ...

- `str("a").sep(1, "/")`
   matches "a", "a/a", ...
   
- `str("a").sep_trailing(1, "/")`
   matches "a", "a/", "a/a", "a/a/", ...
   
- `str("a").sep_exact(3, "/")`
   matches "a/a/a" only

[`Repeat`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Repeat.html
[`Around`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Around.html
[`rule#at_least`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#at_least-int-
[`rule#repeat`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#repeat-int- 
[`rule#sep`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#sep-int-java.lang.Object-
[`rule#sep_trailing`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#sep_trailing-int-java.lang.Object-
[`rule#sep_exact`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#sep_exact-int-java.lang.Object-
 
## Optional

The [`Optional`] parser allows you to specify some optional bit of syntax. Construct with
[`rule#opt`].

Basic example: `str("a").opt()`, which matches both "" and "a".

[`Optional`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Optional.html
[`rule#opt`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#opt--  

## Lookahead

The two lookahead parsers, [`Lookahead`] and [`Not`], are able to match input without actually
consuming it (meaning they leave the input position untouched even when they succeed).

`Lookahead` behaves exactly like its child parser, except for the restoration of the initial input
position. Construct with [`rule#ahead()`].

Basic example: `seq(str("="), digit.at_least(1)).ahead()`

`Not` succeeds only if its child parser fails, which can be useful to check for a delimiter or
reserved workds. Construct with [`rule#not()`].

Basic example: `seq(str("{", seq(str("}").not(), any).at_least(0), str("}"))`, which matches a pair
of matching curly braces and everything in between (e.g. "{abc}" or "{{}", but not "{}}").

Advanced note: in the classical [PEG] semantics, lookahead can be defined in terms of a double
negation (i.e. `something.not().not()`). This does not work in Autumn (and most other frameworks),
because negation discards any parse tree that may have been built when it fails (i.e. when its child
succeeds).

[`Lookahead`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Lookahead.html
[`Not`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Not.html
[`rule#ahead()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#ahead--
[`rule#not()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#not--
[PEG]: https://en.wikipedia.org/wiki/Parsing_expression_grammar

## Primitive Parsers

These are parsers which are not combinators, i.e. who don't have subparsers and whose success
generally depends on a direct check against the input.

First, there are the [`Empty`] and [`Fail`] parsers (build with [`empty`][emptyb] and
[`fail`][failb]), which always succeeds without consuming any input, and always fails
(respectively).

Instances of [`CharPredicate`] match a single character when the input is a string. You can define
your own predicate using [`cpred`], or use one of the pre-defined ones:

- `any` — matches any character
- `digit` — matches a decimal digit (from '0' to '9')
- `octal_digit` — matches an octal digit (from '0' to '7')
- `hex_digit` — matches an hexadecimal digit (from '0' to '9', 'a' to 'f' and 'A' to 'F')
- `alpha` — matches a single ASCII alphabetic character
- `alphanum` — matches a single ASCII alpha-numeric character

(I didn't put individual links, these are all fields in [`Grammar`]).

It's also possible to match a single character ([`character`]) (can also be done with [`str`]), as
well as ranges ([`range`]) and sets ([`set(char...)`] and [`set(String)`]) of characters.

Basic examples, a couple of parsers matching 'a', 'b', 'c' or 'd':

- `range('a', 'd')`
- `set('a', 'b', 'c', 'd')`
- `set("abcd")`
- `cpred(c -> 'a' <= c && c <= 'd')`

In the same way, it's possible to match single objects with [`ObjectPredicate`] when the input is
a list of objects. Construct with [`opred`].

Finally, it's possible to match whole strings (when the input is a string) with [`str`].

[`Empty`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Empty.html
[`Fail`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Fail.html
[`CharPredicate`]:  https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/CharPredicate.html
[`ObjectPredicate`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/ObjectPredicate.html
[`StringMatch`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/StringMatch.html
[emptyb]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#empty
[failb]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#fail
[`cpred`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#cpred-java.util.function.IntPredicate-
[`character`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#character-char-
[`str`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#str-java.lang.String-
[`range`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#range-char-char-
[`set(char...)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#set-char...-
[`set(String)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#str-java.lang.String-
[`opred`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#opred-java.util.function.Predicate-

## Matching Whitespace

We've discussed matching whitespace in [the corresponding section of section A2][A2ws].

`Grammar` defines a [`ws`] field, which you can freely assign in order to define what consistutes
whitespace in your language. It should always succeed, and match as much whitespace as possible.

This field is reused by the [`word`] and [`rule#word`] methods. The first matches its string
parameter followed by `ws`. The second matches the receiver followed by `ws`.

Note that these methods capture the value of `ws` at the moment when they are called. As such, it is
best to define the whitespace as one of the first things you do in a grammar definition (as indeed
we do in [A2 (Your First Grammar)][A2]).

[A2]: A2-first-grammar.md
[A2ws]: A2-first-grammar.md#whitespace-handling--string-literals
[`ws`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#ws
[`word`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#word-java.lang.String-
[`rule#word`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#word--

## Lazy Parsing and Recursion

Because we define grammars as a collection of `rule`- or `Parser`-valued fields that refer to one
another, we run into a fundamental limitation: in the definition of a field A, we can't refer to a
field B that is declared after A. However, we need to do this if our grammar includes any kind of
recursion!

A solution to this problem is the [`lazy`] parser that is showcased in [the corresponding section of
A2][A2]. Like we explained there:

> `lazy` returns a parser that will be initialized when first used, based on the lambda that it was
> passed.

This lazily initialized parser is an instance of [`LazyParser`].

Here's a basic example that shows a recursive grammar where rule `A` matches one or more
repetition of the string "ab":

```
rule A = 
    seq(str("a"), lazy(() -> this.B));
    
rule B = choice(
    seq(str("b"), A),
    str("b"));
```

If the rule is self-recursive, you can use [`recursive`] instead:

```
rule A = recursive(self -> 
    choice(
        seq(str("ab"), self), 
        str("ab"));
```

(Of course, a much better way to write this rule is `str("ab").at_least(1)`.)

**Beware:** Autumn doesn't support naive left-recursion, so make sure that you don't use either
`lazy` or `recursive` to cause a parser to (directly or indirectly) call itself at the same input
position: this would cause an infinite loop (or, in practice, a stack overflow)! For instance, don't
do this:

```
rule A = recursive(self -> 
    choice(
        seq(self, str("ab")), // left-recursion using "recursive" - STACK OVERFLOW!
        str("ab"));
```


We'll explain how to tackle this issue easily in [A6. Left-Recursion and
Associativity](A6-left-recursion-associativity.md).

[A2lazy]: A2-first-grammar.md#lazy-and-sep
[`LazyParser`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/LazyParser.html
[`lazy`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#lazy-java.util.function.Supplier-
[`recursive`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.html#recursive-java.util.function.Function-

## Advanced

Here is a small map of where you can find information on the advanced parsers / combinators we
didn't talk about in this section.

(TODO)