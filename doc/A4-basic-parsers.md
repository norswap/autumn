# A4. Basic Parsers: Whirlwind Tour

In this section, we'll give an overview of the basic parsers and combinators so that you know
what is available to you.

Quick reminder of things established in previous sections:

Parsers are instances of (subclasses of) [`Parser`] while combinators can refer
either these parsers that have subparsers or the methods used to create them (typically from the
classes [`DSL`] and [`rule`]).

A `rule` is a wrapper for a `Parser` that enables easy parser construction via the [builder
pattern]. In general, however, we'll reserve the word "rule" for those that are assigned to a
field (i.e. `public rule my_rule = ... ;`).

[`Parser`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/Parser.html
[`DSL`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html
[`rule`]:  https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html
[builder pattern]: https://dzone.com/articles/design-patterns-the-builder-pattern

In general, if you want to find about Autumn's built-in parsers, there are two places to look at:

- The [`norswap.autumn.parsers`] package, which contains all bundled subclasses of `Parser`.
- The [`DSL`] and [`rule`] classes, which contains all builder methods to construct instances
  of those classes.
  
In general, the behaviour of the parser will be specified in the documentation of its `Parser`
subclass. Builder methods sometimes provide important options, so their documentation is important
too.

[`norswap.autumn.parsers`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/package-summary.html

Let's jump right in. We won't review every built-in parser here, only the basic ones (which is a
majority of them). Details on advanced parsers will follow in further sections.

## Sequences and Choices

We already talked about the [`Sequence`] and [`Choice`] parsers in the [the previous section].

Construct with [`seq`] and [`choice`], respectively.

Basic examples:
- `seq(str("a"), str("b"), str("c"))`
- `choice(str("a"), str("b"), str("c"))`

[`Sequence`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Sequence.html
[`Choice`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Choice.html
[the previous section]: A3-how-autumn-works.md
[`seq`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#seq-java.lang.Object...-
[`choice`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#choice-java.lang.Object...-

## Longest

The [`Longest`] parser is a slight variation on the choice parser. Instead of matching the same
thing as its first successful child, `Longest` tries to match every one of its children, then
succeeds by matching the same thing as the one matching the most input. In case of a tie, matches
like the earliest longest matching child.

Construct with [`longest`].

Basic example: `longest(str("ab"), str("abb"), str("aba"))`

[`Longest`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Longest.html
[`longest`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#longest-java.lang.Object...-

## Repetitions

There are two classes that implement repetitions: [`Repeat`] and [`Around`].

`Repeat` supports specifying a number of repetitions to match, and whether that is a minimum or an
exact number. Those variants can be constructed with [`rule#at_least`] and [`rule#repeat`],
respectively.

Basic examples:
- `str("a").at_least(0)`
- `str("b").at_least(1)`
- `str("c").repeat(6)`

`Around` matches a series of repetition of a subparser (the *around* parser), separated by another
parser (the *inside* parser). The canonical example is parsing comma-separated lists. It also
enables specifying whether a trailing repetition of the inside* parser should be allowed, as some
language allow trailing commas in comma-separated lists, for instance. Just like `Repeat`, a minimum
or exact number of repetition (of the *around* parser) can also be specified.

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

[`Repeat`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Repeat.html
[`Around`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Around.html
[`rule#at_least`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#at_least-int-
[`rule#repeat`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#repeat-int- 
[`rule#sep`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#sep-int-java.lang.Object-
[`rule#sep_trailing`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#sep_trailing-int-java.lang.Object-
[`rule#sep_exact`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#sep_exact-int-java.lang.Object-
 
## Optional

The [`Optional`] parser allows you to specify some optional bit of syntax. Construct with
[`rule#opt`].

Basic example: `str("a").opt()`, which matches both "" and "a".

[`Optional`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Optional.html
[`opt`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#opt--  

## Lookahead

The two lookahead parsers, [`Lookahead`] and [`Not`], are able to match input without acutally
consuming it (meaning they leave the input position untouched even when they succeed).

`Ahead` behaves exactly like its child parser, except for the restoration of the initial input
position. Construct with [`rule#ahead()`].

Basic example: `seq(str("="), digit.at_least(1)).ahead()`

`Not` succeeds only if its child parser fails, which can be useful to check for a delimiter or
reserved workds. Construct with [`rule#not()`].

Example: `seq(str("{", seq(str("}").not(), any).at_least(0), str("}"))`

Advanced note: in the classical [PEG] semantics, lookahead can be defined in terms of a double
negation (i.e. `something.not().not()`). This does not work in Autumn (and most other frameworks),
because negation discards any parse tree that may have been built when it fails (i.e. when its child
succeeds).

[`Lookahead`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Lookahead.html
[`Not`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Not.html
[`rule#ahead()`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#ahead--
[`rule#not()`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#not--
[PEG]: https://en.wikipedia.org/wiki/Parsing_expression_grammar

## Primitive Parsers

These are parsers which are not combinators, i.e. who don't have subparsers and whose success
generally depends on a direct check against the input.

First, there are the [`Empty`] and [`Fail`] parsers (build with [`empty`] and [`fail`]), which
always succeeds without consuming any input, and always fails (respectively).

Instances of [`CharPredicate`] match a single character when the input is a string. You can define
your own predicate using [`cpred`], or use one of the pre-defined ones:

- `any` — matches any character
- `digit` — matches a decimal digit (from '0' to '9')
- `octal_digit` — matches an octal digit (from '0' to '7')
- `hex_digit` — matches an hexadecimal digit (from '0' to '9', 'a' to 'f' and 'A' to 'F')
- `alpha` — matches a single ASCII alphabetic character
- `alphanum` — matches a single ASCII alpha-numeric character

(I didn't put individual links, these are all fields in [`DSL`]).

It's also possible a single character ([`character`]) (redundant with [`str`]), as well as ranges
([`range`]) and sets ([`set(char...)`] and [`set(String)`]) of characters.

Basic examples, a couple of parsers matching 'a', 'b', 'c' or 'd':

- `range('a', 'd')`
- `set('a', 'b', 'c', 'd')`
- `set("abcd")`
- `cpred(c -> 'a' <= c && c <= 'd')`

In the same way, it's possible to match single objects with [`ObjectPredicate`] when the input is
a list of objects. Construct with [`opred`].

Finally, it's possible to match whole strings (when the input is a string) with [`str`].

[`Empty`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Empty.html
[`Fail`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Fail.html
[`CharPredicate`]:  https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/CharPredicate.html
[`ObjectPredicate`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/ObjectPredicate.html
[`StringMatch`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/StringMatch.html
[`empty`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#empty
[`fail`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#fail
[`cpred`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#cpred-java.util.function.IntPredicate-
[`character`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#character-char-
[`str`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#str-java.lang.String-
[`range`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#range-char-char-
[`set(char...)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#set-char...-
[`set(String)`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#str-java.lang.String-
[`opred`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#opred-java.util.function.Predicate-

## Matching Whitespace

We've discussed matching whitespace in the corresponding section of [A2. Your First Grammar].

`DSL` defines a [`ws`] field, which you can freely assign in order to define what consistutes
whitespace in your language. It should always succeed, and match as much whitespace as possible.

This field is reused by the [`word`] and [`rule#word`] methods. The first matches its string
parameter followed by `ws`. The second matches the receiver followed by `ws`.

Note that these methods capture the value of `ws` at the moment when they are called. As such, it is
best to define the whitespace as one of the first thing you do in a grammar definition (as indeed we
do in [A2]).

Also remember that passing a string literal directly to a combinator implicitly calls `word(String)`
on it!

[A2]: A2-first-grammar.md
[A2. Your First Grammar]: A2-first-grammar.md
[`ws`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#ws
[`word`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#word-java.lang.String-
[`rule#word`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.rule.html#word--

## Lazy Parsing and Recursion

Because we define grammars as a collection of `rule`- or `Parser`-valued fields that refer to one
another, we run into a fundamental limitation: in the definition of a field A, we can't refer to a
field B that is declared after A. But we need to do this if your grammar includes any kind of
recursion!

A solution to this problem is the [`lazy`] parser that is showcased in [A2. Your First Grammar].
Like we explained there:

> `lazy` returns a parser that will be initialized when first used, based on the lambda that it was
> passed.

This lazily initialized parser is an instance of [`LazyParser`].

Here's a basic example that shows a recursive grammar where both rules match alternations
of "a" and "b" (`A` must start with an "b", `B` with a "b")

```
rule A = choice(
    seq(str("a"), lazy(() -> this.B)),
    str("a"));
    
rule B = choice(
    seq(str("b"), A),
    str("b"));
```

If the rule is self-recursive, you can use [`recursive`] instead:

```
rule AB = recursive(self -> 
    choice(
        seq(set("ab"), self), 
        set("ab"));
```

(Of course, a much better way to write this rule is `set("ab").at_least(1)`.)

**Beware:** Autumn doesn't support naive left-recursion, so make sure that you don't use either
`lazy` or `recursive` to cause a parser to (direclty or indirectly) call itself at the same
input position: this would cause an infinite loop!

We'll explain how to tackle this issue easily in Section TODO.

[`LazyParser`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/LazyParser.html
[`lazy`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#lazy-java.util.function.Supplier-
[`recursive`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/DSL.html#recursive-java.util.function.Function-

## Advanced

Here is a small map of where you can find information on the advanced parsers / combinators we
didn't talk about in this section.

(TODO SOON)