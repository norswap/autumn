# Transactionality

Autumn is a parser capable of handling side-effects. This means that when invoked, parsers can
modify their environment (otherwise they'd simply be functions returning booleans, which is not very
useful).

The most basic form of side-effect is to update the input position, which is held within a [Grammar]
object. Another well-known form of side-effect is to push or pop nodes from the [value stack] used
to build the [AST].

[Grammar]: ../API/grammar.md
[stack]: ../API/grammar.md#stack
[AST]: ast.md

Of course, all parsing tools can do that. The parsers that, like Autumn, go beyond and allow you to
manipulate arbitrary parse state are sometimes called data-dependent, stateful, or context-sensitive
(even thought that's not the original definition of "context-sensitive parsing").

We will use the terms *environment*, *parse state* and *side-effects* fairly interchangeably, but
they all refer to the same idea: the ability to define and change some state, and to have these
changes exposed to other parsers.

As we will see [later][Side Effects], you can really change what you want, but you will need to
supply a way to rollback any chanage you make.

### Parse State and Parser Failure

The problem with side-effects is that a parser invocation can fail. If a parser invocation fails,
we do not want it to modify the input position, or to alter the value stack. The same is true
for almost anything in the environment.

To guard against this, all parsers must uphold the **transactionality rule**.

### The Transactionality Rule

The transactionality rule is very simple: it says that either a parser succeeds (returns `true`), or
it fails (returns `false`) without modifying the environment.

To be clear, a parser may modify the environment (enact side-effects), but it must roll back all
changes if it ultimately fails.

### Transactionality and Sub-Parsers

The transactionality rule may be simple but it has one major catch. A parser may call
sub-parsers. For instance, `fun p() = seq { a() && b() }` may call the sub-parsers `a` and `b`.
If `f` calls `a` and it succeeds, but then calls `b` and it fails, `a` might have left changes
in the environment. But `f` is itself failing (because `b` didn't match), so it must undo all
changes made by `a`.

### Enacting the Transactionality Rule

We will speak about side-effects and how to handle them in depth in the [Side Effects] section
of this guide. In the meantime, there are more important things to explain.

Nevertheless, here is the redux version of how to handle side-effects easily:

- Wrap the body of your parser in a [`transact`] ([explanation]) block.
- If you must have side-effects besides changing the input position or the value stack, use one
  of the classes in the [`norswap.autumn.undoable`] package.

[Side Effects]: side-effects.md
[`transact`]: ../API/misc.md#transact
[explanation]: side-effects.md#TODO
[`norswap.autumn.undoable`]: ../API/undoable/README.md