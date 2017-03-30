# Transactionality

In most parsing tools, you cannot modify state in your parsers, or at least observe this
state. Generally, you are limited to advancing the input position and generating AST nodes. Autumn,
on the other hand, lets you modify state and observe these modifications in other parsers, as long
as you respect a few rules.

Parsers that allow the modification and observation of parse are sometimes called
*data-dependent*, *stateful* or *context-sensitive* (no relation with Chomsky's Context Sensitive
Grammars).

In particular, you can perform any modification that you want, as long as you supply a way to roll
back that modification. State modified in that way is the *parse state* and is safe to observe in
other parsers. Note that the input position and the [value stack] that Autumn uses to build the
[AST] are just specialized cases of parse state. We will go over parse state in much details
in the [Handling Side Effects] section.

We call *side-effect* any modification of the parse state. Sometimes I might also use the terms
*environment* or *context* to refer to the parse state.

[value stack]: ../API/grammar.md#stack
[AST]: ast.md
[Handling Side Effects]: side-effects.md

### Parse State and Grammar

All parse state modifications are mediated through a [Grammar] object. It may seem weird to
use the grammar as a container for parse state. The reason is that objects that wish to access
the parse state must have a reference to some kind of context object. And because of how
parsers are created (see [next section][own-parsers]), it is particularly easy to give a
reference to the [Grammar] object.

[Grammar]: ../API/grammar.md
[own-parsers]: own-parsers.md

### Parse State and Parser Failure

The problem with side-effects is that a parser invocation can fail. If a parser invocation fails, we
do not want it to modify the input position, or to alter the value stack, or any other kind of parse
state.

To guard against this, all parsers must uphold the **transactionality rule**.

### The Transactionality Rule

The transactionality rule is very simple: it says that either a parser succeeds (returns `true`), or
it fails (returns `false`) without modifying the parse state.

To be clear, a parser may modify the parse state (enact side-effects), but it must roll back all
changes if it ultimately fails.

### Transactionality and Sub-Parsers

The transactionality rule may be simple but it has one major catch. A parser may call
sub-parsers. For instance, `fun p() = seq { a() && b() }` may call the sub-parsers `a` and `b`.
If `f` calls `a` and it succeeds, but then calls `b` and it fails, `a` might have left changes
in the parse state. But `f` is itself failing (because `b` didn't match), so it must undo all
changes made by `a`.

### Enacting the Transactionality Rule

We will speak about side-effects and how to handle them in depth in the [Handling Side Effects]
section of this guide. In the meantime, there are more important things to explain.

Nevertheless, here is the redux version of how to handle side-effects easily:

- Wrap the body of your parser in a [`transact`] ([explanation]) block.
- If you must have side-effects besides changing the input position or the value stack, use one
  of the classes in the [`norswap.autumn.undoable`] package.

[`transact`]: ../API/misc.md#transact
[explanation]: side-effects.md#TODO
[`norswap.autumn.undoable`]: ../API/undoable/README.md