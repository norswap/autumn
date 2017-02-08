# Grammars

## Two Roles

In Autumn, grammars fill two distinct but important roles:

- They are the place where parsers are defined. A parser is somewhat analogous to a grammar
  rule, hence the name.

- They encapsulate all the parse state. This includes the input text, the input position,
  as well as any additional state the grammar writer wishes to define.
  
These roles have very little to do with one another. In fact, a better name for the second
role would have been *Context*. However, it turned it was incredibly convenient to merge the two
roles, and so the name `Grammar` stuck.