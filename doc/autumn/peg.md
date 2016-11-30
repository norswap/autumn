# Parsing Expression Grammars

Parsing Expression Grammars (PEG) is a grammar formalism introduced in a [2004 paper].

[2004 paper]: http://bford.info/pub/lang/peg

PEGs have a few characteristics that made them popular:

- Is it very easy to implement a parser that recognizes PEG grammar.

- PEG parsers can be memoized, allowing them to run in O(1) time with respect to the input, albeit
  at the cost of large memory overheads.
  
- They are eminently composable. Given some PEG parsers, it is trivial to combine them to
  create another PEG parser, for instance to recognize a sequence of items, or a choice between
  multiple possible items.
  
PEGs are overwhemingly used in parser combinator libraries. In parser combinator libraries,
one composes objects or functions to create a PEG parser. Each object or function
represents a PEG parser, and those can be composed (as per the third bullet above) to create
more complex parsers.

TODO
- explain that PEG is a formalism, not an implem, but tied to implem
- explain that packrat is not killer
    - link redzie paper
    - say that that paper is still doubtful, and that I want to try it someday
- link to peg operations