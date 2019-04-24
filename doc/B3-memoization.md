# B3. Memoization

[Memoization] is a technique that consists of saving the result of a function so that it does not
need to be recomputed if it is needed again.

Indeed, parsers can be seen as functions that will always return the same result if invoked at the
same input position, and with the same relevant context. This equivalence is explained in section
[A3. How Autumn Works] (sub-section on "Vertical Backtracking"). The role and mechanisms of context
are explained in [B2. Context-Sensititive (Stateful) Parsing].

Parsers may be invoked multiple times at the same position because Autumn *backtracks* (see [section
A3] if this is unclear). In some cases, these repeated invocations might add up to significant
wasted time during the parse.

Many parser frameworks based on [PEG] systematically memoize all the intermediate parse results.
This is called *packrat parsing*. Some studies were made that suggest that this is in general
inefficient because PEG grammars tend to be "mostly deterministic", i.e. they don't backtrack enough
to justify the overhead. Instead, it is suggested that memoizing just a couple of parsers is what
yields the best results ([*1]). See my thesis (SOON) for a full discussion and references.

Another issue with packrat parsing is that even in the cases where it is beneficial, it trades
execution time for memory use, which might become consequent: on the order of gigabyte(s) (depending
on the implementation) even for inputs less than a couple thousands lines long.

In light of these findings, which match our own experiments, Autumn doesn't automatically memoize
intermediate performance results. However, since there is something to be gained by memoizing some
parsers, we enable selective parser memoization through a few parser combinators.

The next sub-section will explain this mechanism. As to *when* to memoize — this should generally
be decided after making performance measurements on meaningful input. Autumn includes facilities
to facilitate such measurement, which will be covered in section [B5. Debugging & Tracing a Parse].

[Memoization]: https://en.wikipedia.org/wiki/Memoization
[section A3]: A3-how-autumn-works.md
[A3. How Autumn Works]: A3-how-autumn-works.md#vertical-backtracking
[B2. Context-Sensititive (Stateful) Parsing]: B2-context-sensitive-parsing.md
[PEG]: https://en.wikipedia.org/wiki/Parsing_expression_grammar
[B5. Debugging & Tracing a Parse]: B5-debugging-tracing.md

## Memoization in Autumn

Memoizing a parser in Autumn is achieved by wrapping the parser in a [`Memo`] parser.

The `Memo` parser has two additional parameters: a supplier (a factory) for implementations of the
[`Memoizer`] interface, and an optional (may be null) function from a `Parse` to an object that
represent the relevant context for the underlying parser.

The `Memoizer` interface defines the operations that a memoization strategy must support (namely
handling a new parse result, and attempting to retrieve an existing result).

Autumn supplies two implementations of `Memoizer`, but users can define their own. The first
strategy is [`MemoTable`], which memoizes every result it is passed (which must differ in the
`(position, parser, context)` triplet). This strategy ensures the same result is never computed
twice but may have large memory requirements. The second strategy is [`MemoCache`], which reserves a
limited number of slots for memoizing results. A new result will cause the oldest stored result to
be evicted from the cache if it is full. With this strategy, results could potentially be computed
multiple times, but the memory requirement is bounded.

[`Memo`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Memo.html
[`Memoizer`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/Memoizer.html
[`MemoTable`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/MemoTable.html
[`MemoCache`]: https://javadoc.jitpack.io/com/github/norswap/autumn4/-SNAPSHOT/javadoc/norswap/autumn/parsers/MemoCache.html

TODO

----
**Footnotes**

[*1]: #footnote1 
<h6 id="footnote1" display=none;></h6>

(*1) On the other hand, I've heard reported that some parser frameworks fare miserably in terms of
performance if memoization is disabled. I have no trouble believing it, and I strongly suspect that
the culprits are the performance pitfalls that can easily occur when defining the syntax of
left-associative binary expressions with PEG.

If you use the facilities supplied by Autumn to handle left-recursion/left-association (section [A6.
Left-Recursion and Associativity]), you won't run into these issues however!

See my thesis (SOON) for a full discussion of these potential performance issues in other tools.

[A6. Left-Recursion and Associativity]: A6-left-recursion-associativity.md