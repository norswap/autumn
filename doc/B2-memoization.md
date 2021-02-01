# B2. Memoization

**NOTE: This section is outdated and needs to be rewritten/improved.**

[Memoization] is a technique that consists of saving the result of a function so that it does not
need to be recomputed if it is needed again.

Indeed, parsers can be seen as functions that will always return the same result if invoked at the
same input position, and with the same relevant context. This equivalence is explained in section
[A3. How Autumn Works] (sub-section on "Vertical Backtracking"). The role and mechanisms of context
are explained in [B1. Context-Sensititive (Stateful) Parsing].

Parsers may be invoked multiple times at the same position and with the same context because Autumn
*backtracks* (see [section A3] if this is unclear). In some cases, these repeated invocations might
add up to significant wasted time during the parse.

Many parser frameworks based on [PEG] systematically memoize all the intermediate parse results.
This is called *packrat parsing*. Some studies were made that suggest that this is in general
inefficient because PEG grammars tend to be "mostly deterministic", i.e. they don't backtrack enough
to justify the overhead. Instead, it is suggested that memoizing just a couple of parsers is what
yields the best results ([*1]). See my thesis (SOON) for a full discussion and references.

Another issue with packrat parsing is that even in the cases where it is beneficial, it trades
execution time for memory use, which might become consequent: on the order of gigabyte(s) (depending
on the implementation) even for inputs less than a couple thousands lines long.

In light of these findings, which match our own experimental observations, Autumn doesn't
automatically memoize intermediate parser results. However, since there is something to be gained by
memoizing some parsers, we enable selective parser memoization through a few parser combinators.

The next sub-section will explain this mechanism. As to *when* to memoize — this should generally
be decided after making performance measurements on meaningful input. Autumn includes facilities
to facilitate such measurement, which will be covered in section [B4. Debugging & Tracing a Parse].

[Memoization]: https://en.wikipedia.org/wiki/Memoization
[section A3]: A3-how-autumn-works.md
[A3. How Autumn Works]: A3-how-autumn-works.md#vertical-backtracking
[B1. Context-Sensititive (Stateful) Parsing]: B1-context-sensitive-parsing.md
[PEG]: https://en.wikipedia.org/wiki/Parsing_expression_grammar
[B4. Debugging & Tracing a Parse]: B4-debugging-tracing.md

## Memoization in Autumn

Memoizing a parser in Autumn is achieved by wrapping the parser in a [`Memo`] parser.

The `Memo` parser has two additional parameters: a [`ParseState`] for an implementation of the
[`Memoizer`] interface, and an optional (may be null) function from a `Parse` to an object that
represent the relevant context for the underlying parser (the *context object*).

([`ParseState`] was explained in [B1. Context-Sensititive (Stateful) Parsing][B1-parse].)

The `Memoizer` interface defines the operations that a memoization strategy must support (namely
handling a new parse result, and attempting to retrieve an existing result).

Autumn supplies two implementations of `Memoizer`, but users can define their own. The first
strategy is [`MemoTable`], which memoizes every result it is passed. This strategy ensures the same
result is never computed twice but may have large memory requirements. The second strategy is
[`MemoCache`], which reserves a limited number of slots for memoizing results. A new result will
cause the oldest stored result to be evicted from the cache if it is full. With this strategy,
results could potentially be computed multiple times, but the memory requirement is bounded.

Both strategies can be further parameterized by deciding whether results are memoized based on their
position and optionally the context object, or whether the particular parser used to produce the
result should also be taken into account.

Taking the parser into account allows sharing the same memoizer between multiple `Memo` parsers.

Regarding the context object (if available), its `hashCode()` method is used by memoizers to
store/retrieve the result and its `equals()` method is used to determine whether a memoized result
is applicable in the current context (by comparing the context object stored in the result and the
context object extracted in the current context). 

[`Memo`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/parsers/Memo.html
[`Memoizer`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/memo/Memoizer.html
[`MemoTable`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/memo/MemoTable.html
[`MemoCache`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/memo/MemoCache.html
[`ParseState`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/ParseState.html
[B1-parse]: B1-context-sensitive-parsing.md#parse-state

## Memoization Combinators

Instances of [`Memo`] can be constructed using a family of combinators:

- [`rule#memo()`]: builds a memo parser using a [`MemoTable`].
- [`rule#memo(Function<Parse, Object>)`]: builds a context-sensitive memo parser using a [`MemoTable`].
- [`rule#memo(int)`]: builds a memo parser using a [`MemoCache`] with the given number of slots.
- [`rule#memo(int, Function<Parse, Object>)`]:
  builds a context-sensitive memo parser using a [`MemoCache`] with the given number of slots.
- [`rule#memo(ParseState<memo parser>)`]: builds a memo parser using the supplied memoizer.
- [`rule#memo(ParseState<Memoizer>, Function<Parse, Object>)`]: builds a context-sensitive memo
  parser using the supplied memoizer.

A special note on those combinators that take a `ParseState<Memoizer>`: recall that (from [B1,
sub-section on ParseState][B1-parse]) you can declare a `ParseState` inside your grammar and pass it
to the combinator without fear that multiple parses will write to the same `Memoizer` (`ParseState`
maintains separate states for each parse).
  
[`rule#memo()`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#memo--
[`rule#memo(Function<Parse, Object>)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#memo-java.util.function.Function-
[`rule#memo(int)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#memo-int-
[`rule#memo(int, Function<Parse, Object>)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#memo-int-java.util.function.Function-
[`rule#memo(ParseState<memo parser>)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#memo-norswap.autumn.ParseState-
[`rule#memo(ParseState<Memoizer>, Function<Parse, Object>)`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/Grammar.rule.html#memo-norswap.autumn.ParseState-
[B1-parse]: B1-context-sensitive-parsing.md#parse-state

## Custom Memoizers & Memoizing Parsers

It's possible for users to implement their own [`Memoizer`]. This is mostly straightforward, just
refer to [the Javadoc][`Memoizer`] for information. One thing to note however is that if you need
to obtain a hashcode to store a [`MemoEntry`] (which stores a parse result), you should use the
the [`Memoizer.hash`] functions.

It's also possible to use a `Memoizer` from a parser that isn't a [`Memo`]. Here again, it's just
a matter of using the [`Memoizer`] API. The important thing that you need to enforce is *the single
parse rule*, which (from [A3. How Autumn Works]) says:

> A parser, when called at the same input position (and in context-sensitive parses, with the same
> context) should should always yield the same (singular) result.

[`Memoizer.hash`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/memo/Memoizer.html#hash-boolean-norswap.autumn.parsers.MemoEntry-
[`MemoEntry`]: https://javadoc.io/doc/com.norswap/autumn/latest/norswap/autumn/memo/MemoEntry.html
[A3. How Autumn Works]: A3-how-autumn-works.md

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) On the other hand, I've heard reported that some parser frameworks fare miserably in terms of
performance if memoization is disabled. I have no trouble believing it, and I strongly suspect that
the culprits are the performance pitfalls that can easily occur when defining the syntax of
left-associative binary expressions with PEG.

If you use the facilities supplied by Autumn to handle left-recursion/left-association (section [A6.
Left-Recursion and Associativity]), you won't run into performance issues due to these particular
issues however!

See my thesis (SOON) for a full discussion of these potential performance issues in other tools.

[A6. Left-Recursion and Associativity]: A6-left-recursion-associativity.md