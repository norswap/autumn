# Parsing Expression Grammars

Parsing Expression Grammars (PEG) is a grammar formalism introduced in a [2004 paper].
I will use ther term PEG both as "a PEG" (a grammar) and "PEG" (the formalism).
A "PEG parser" is a parser derived from a PEG grammar.

[2004 paper]: http://bford.info/pub/lang/peg

PEG is a grammar formalism. This means that a PEG is a formal (mathematical)
description of a language (a language is just a set of strings), it it doesn't really matter
how you recognize the language described by the grammar.

Nevertheless, PEG is strongly linked with a certain type of parser. In fact, PEG is an attempt to
formalize top-down recursive-descent parsers. These are the kind of parsers you would naturally
write by hand: you would write a function to parse each kind of program element. For instance
you would have function that parses statements, and a function that parses expressions. Since
there are many kind of statements, the statement parsing function would probably delegate to
a separate function for each kind of statement. Since statements may contains expressions, these
functions would call the expression parsing function. Etc, etc.

If you are not familiar with parsing this leaves you deeply perplexed, I recommend learning
more about PEGs before continuing. I recommend this [introductory paper] by Roman Redziejowski,
or to peruse the [PEG wikipedia page].

[introductory paper]: http://www.romanredz.se/papers/FI2007.pdf
[PEG wikipedia page]: https://en.wikipedia.org/wiki/Parsing_expression_grammar

The [PEG Operators] page details every "standard" PEG operator.

[PEG Operators]: /doc/autumn/peg-ops.md

## PEG vs CFG

Top-down recursive descent parsers work well and are pretty intuitive, but they are at odds with the
most popular grammar formalism: Context Free Grammars (CFG). Explaining CFGs is out of scope for
this document (the [wikipedia article] is pretty good). Howver we will say that CFGs are
*generative*: where PEG formalize a way to recognize the strings in a language, PEGs formalize a way
to generate all the strings in the language.

[wikipedia article]: https://en.wikipedia.org/wiki/Context-free_grammar

Obviously, when parsing, you are recognizing an input. This explains why it is easy to
implement a PEG grammar (or to write a basic parsing tool working with PEGs). Conversely,
there is generally a large complexity gap between a CFG and its implementation.

On the other hand, it can be argued that CFGs do a better job than PEG in actually *describing* a
language. The generative approch makes it easier to visualize valid strings in the language.
 My own personal, and somewhat controversial, opinion is that in practice this difference is small,
while the difference in implementation complexity is **vast**.

If you are using a parsing tool, how does the choice of PEG vs CFG impact you?

- CFG grammars can be ambiguous, while PEG cannot

- PEG grammars can have "language hiding" where an alternate in a choice will prevent another
  alternate from ever being matched. For instance: `a / ab` will never match `ab` (it will match `a`
  then be left with `b` it can't consume).
  
That is for sure. However there are some aspects where PEG-based or CFG-based tools *tend* to be
better, but not general rule can be made.

- Most PEG parsing tools can't handle left-recursion. If you think about the function analogy, this
  make sense: you're just re-entering the same function without making any progress.
  *Psst: Autumn [handles left-recursion].*
  
[handles left-recursion]: /docs/autumn/left-recursion.md

- It is often possible to write custom parser combinators in PEG parsing tools. If you map this to
  the formalism level, it essentially means you can extend the formalism with new operators. I've
  never seen a CFG parsing tool where that was possible, although in some tools you can influence
  the parse through semantic actions and filters.
  
- PEG parsers are generally simpler to understand. That can be handy when debugging, or if you
  attach value to using simple tools.
  
 What shouldn't bother you:
 
- Expressivity. There are languages that can be described in PEG but not in CFG, but it is not known
  whether the reverse is true. Even should it be the case, the fact we have no example means it
  doesn't matter in practice. The additional expressivity from PEG isn't useful either.
  
  Beware, however, of parsing tools that use subsets of CFG: LL(k), LR(k), LALR, ... These
  impose real constraints on the languages that can be recognized, and how the grammars can be
  written. The latter is usually the source of much more pain. (If you ever heard of "shift-reduce
  conflicts", this is what this is about.) I recommend sticking to general* CFG parsing.
  
  
## Packrat Parsing

Initially, one of the selling points of PEG parsers was that they could be parsed in O(n) time
with respect to the input text, using a technique called *packrat parsing*.

The idea is relatively simple: since a PEG expression invoked at a given input position will always
yield the same result, they can be memoized, so that each expression will only be invoked once.

On paper, this sounds great, but in practice it involves large memory and runtime overhead.
[One paper][packrat-flop] found that packrat was slower on a Java grammar.
However, that result should be taken with a grain of salt. It only applies to a single
grammar. My intuition is that things are not that simple, and that performance greatly depends
of the way in which the grammar is written. A grammar that backtracks a lot will benefit from
memoization much more. Similarly, a parser with a lot of overhead will also more readily benefit
from packratting (I heard this about [ohm]). My own experiences with this (a *long* time ago) were
also that indiscriminate memoization slows things down. I may do some more experiments about this
in the future.

Selective memoization (only memoizing the result of some expressions) is a potent technique. But
knowing what to memoize is not trivial, one has to instrument a parser and measure its execution to
know exactly what would benefit from memoization.

A note about Autumn: since grammars can be context-sensitive, great care must be taken with
memoization. A memoized result should only be recalled if it was acquired under a context that
is compatible with the current context. The definition of compatibility is up to the user, but
the requirement is simple: parsing or without memoization should yield the same result.

[packrat-flop]: https://www.mercurylang.org/documentation/papers/packrat.pdf
[ohm]: https://github.com/harc/ohm

## Parser Combinators

PEG parser are eminently composable. Given some PEG parsers, it is trivial to combine them to create
another PEG parser, for instance to recognize a sequence of items, or a choice between multiple
possible items. There is no table or automata to build, one just calls the other parsers
directly.

For this reason, PEGs are overwhemingly used in parser combinator libraries. In parser combinator
libraries, a parser is represented by an object or a function, and those can be composed to create
more complex parsers.

Here is a list of [interesting parsing tools], which includes a lot of parser combinators and
PEG-based tools.

[interesting parsing tools]: /doc/autumn/parsing-tools.md

TODO
- explain ordered choice
- explain single parse rule
- gather other things I wrote on the subject