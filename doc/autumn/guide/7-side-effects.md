# Handling Side Effects

This article builds on the section on [Transactionality] in order to explain the fine details
of *parse state* and *side effects* handling. In particular, we will discuss how to enforce
[transactionality] and how to implement your own parse state, to complement those we already
discussed (the input position and [ASTs])

[Transactionality]: 2-transactionality.md
[transactionality]: 2-transactionality.md#the-transactionality-principle

[ASTs]: 6-ast.md

## Why Side Effects are a Problem

The problem with *side effects* is that while parsing, we may perform *backtracking*. The reason
is that when faced with a choice in the grammar, we don't know which one to take. The only way to
tell is to try one branch, and go back (*backtrack*) if that fails, to try another branch.

The problem is that while parsing, we may provoke side effects. We want to only retain the
side effects that were produced by parsers that actually suceeded, and whose ancestors all
succeeded. The best way to see this is to think about ASTs: it wouldn't be very useful if we
produced a tree that included nodes for grammatical constructs that didn't match.

For instance, consider the java declaration `int method();`. We may first attempt to match a field
definition. We encounter `int` and so push a node for this on the value stack, idem for the
identifier `method`. However, field declarations may not contain parens. So that rule fails, and
we backtrack. Say we now try to match a method declaration. We will again match `int` and `method`
and create nodes for them: these nodes are now duplicated! The previous ones should have been
discarded.

## Why You Need Side Effects: Stateful Parsing

At this point, a few people are bound to say that, excepted for the AST, you don't actually need
side effects during the parse`*`. You parse the input and produce the AST, then traverse it to
produce the side effects you need, with no concern for backtracking. That is very astute, and if
this can work for you, you should do it like that.

However, sometimes you really need side effects during the parse, because you want some parsers to
depend on (*observe*) some side effects applied by parsers invoked earlier. This is called *stateful
parsing*, and it is very useful in practice. Languages such as Haskell, Standard ML, Common Lisp,
bash, Ruby and Python `**` cannot be described with pure formalisms such as Context Free Grammars
(CFG) or Parsing Expression Grammars (PEG), they need stateful parsing. For other languages, such as
C, it is the AST that depends on side effects, i.e. you need them as a means of disambiguation.

`*` Someone once told me it was "crazy" to do so — I took that as a compliment.  
`**` In the case of Python, the problem is significant identation. However, the problem is
solved by using a custom indentation-aware parser, which is frankly the way to go.

## Transactionality

A quick reminder of the [transactionality] principle:

> The transactionality rule is very simple: it says that either a parser succeeds (returns `true`), or
> it fails (returns `false`) without modifying the parse state.
>
>  To be clear, a parser may modify the parse state (enact side effects), but it must roll back all
>  changes if it ultimately fails.

Remember as well, from the paragraph about [transactionality and sub-parsers], that when
a parser fail it must undo its own side effects, but also the side-effects applied by its
successful sub-parsers.

[transactionality and sub-parsers]: 2-transactionality.md#transactionality-and-sub-parsers

## Enforcing Transactionality: Recording Side Effects

Autumn's strategy to enforce transactionality is to require all side effects to be registered in a
central location (within the [`Grammar`] instance), along with a mean to undo the side effect.

Autumn represents applied side effects by instances of [`AppliedSideEffect`]. An instance of this class
aggregate two other object: an instance of [`SideEffect`] and an instance of [`UndoSideEffect`].

`SideEffect` is an alias for `(Grammar) -> UndoSideEffect`, `UndoSideEffect` is an alias for
`(Grammar) -> Unit`. Essentially, calling a `SideEffect` produces the side effect and returns a
means to undo it.

The reason why we don't just store `UndoSideEffect` instances is that the ability to replay side
effects is also valuable (see later).

[`Grammar`]: ../API/grammar.md
[`SideEffect`]: ../API/side-effects.md#sideeffect
[`AppliedSideEffect`]: ../API/side-effects.md#appliedsideeffect
[`UndoSideEffect`]: ../API/side-effects.md#undosideeffect

## Implementing Side-Effecting Parsers

If you want to write a parser that has side effects, you have to encapsulate any side-effecting
action in a `SideEffect` object: a function that performs the side effect and returns an
`UndoSideEffect` object, which is itself a function that undoes the side effect.

To actually apply the side effect, you need to call [`Grammar#apply`] with the `SideEffect` as
parameter. This will call the `SideEffect` object and register the resulting `AppliedSideEffect`
object.
 
[`Grammar#apply`]: ../API/grammar.md#apply

## Built-in Side-Effecting Data Structure

It can be a chore to wrap all side-effecting actions in instances of `SideEffect` and to supply
the corresponding `UndoSideEffect` objects.

To ease the pain, Autumn bundles a few side-effecting data structures. You instantiate these data
structure with a reference to a `Grammar`, then use them as you would a regular data structure. Any
change made to the data structure will cause a `SideEffect` to be registered with the `Grammar`.

These data structures are available in the [`norswap.autumn.undoable`] package:

- [`UndoList`] implements the immutable [List] interface and provides the side-effecting `push`,
  `pop` and `set` operations.
- [`UndoMap`] implements the immutable [Map] interface and provides the side-effecting
  `put` and `remove` operations.
- [`UndoRef`] represents a reference that can be read and written, given a getter and a setter.
  It must be instantiated with `undo_ref`.
- [`UndoSlot]` represents a reference that can be read and written, and provides the storage for
  that reference. It must be instantiated with `undo_slot`.

[`UndoList`]: ../API/undoable/undo-list.md
[`UndoMap`]: ../API/undoable/undo-map.md
[`UndoRef`]: ../API/undoable/undo-ref.md#undoref
[`UndoSlot`]: ../API/undoable/undo-ref.md#undoslot
[List]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/
[Map]: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/
[`norswap.autumn.undoable`]: ../API/undoable/README.md
 
## Implementing Safe Parser Combinators

If you recall the key principle outlined above, if a parser calls other parsers, it must be able
to undo their side effects. There are two ways to achieve this.

The first way is to  wrap your logic in a [`transact`] combinator. The combinator will ensure that
if your parser fails, all side effects are properly undone (assuming they were properly registered
with [`Grammar#apply`]).

The second way is to explicitly call the state handling primitive [`undo`]. This function takes two
parameters: an input position and a *pointer*, which is a previous size of [`Grammar#log`]. The log
is the record of all applied side effects. In practice what you should do is record the input
position and the size of the log before calling any sub-parsers, and call `undo` with these
parameters in case of failure. Of course, this is what `transact` does for you already, without any
loss of efficiency, so you should prefer `transact`. The real reason the primitive is available
is to implement unusual behaviour. For instance, it is used by the [`Longest`] parser.

[`transact`]: ../API/parsers/misc.md#transact
[`undo`]: ../API/grammar.md#undo
[`Grammar#log`]: ../API/grammar.md#log
[`Longest`]: ../API/parsers/choice.md#Longest