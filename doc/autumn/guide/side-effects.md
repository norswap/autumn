# Handling Side Effects

We already saw how to handle at least two kind of side effects: changing the input position, and
pushing values on the [value stack].

The principles that govern these side effects can be generalized. This section discusses
the notion of *side effect* and *parse state* in details, and explains how you can implement
your own side effects.

[value stack]: ast.md

## Why Side Effects are a Problem

The problem with *side effects* is that while parsing, we may perform *backtracking*. The reason
is that when faced with a choice in the grammar, we don't know which one to take. The only way to
tell is to try one branch, and go back (*backtrack*) if that fails, to try another branch.

The problem is that while parsing, we may provoke side effects. We want to only retain the
side-effects that were produced by parsers that actually suceeded, and whose ancestors all
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
depend on some side-effects applied by parsers invoked earlier. This is called *stateful parsing*,
and it is very useful in practice. Languages such as Haskell, Standard ML, Common Lisp, bash, Ruby
and Python `**` cannot be described with pure formalisms such as Context Free Grammars (CFG) or
Parsing Expression Grammars (PEG), they need stateful parsing. For other languages, such as C, it is
the AST that depends on side effects, i.e. you need them as a means of disambiguation.

`*` Someone once told me it was "crazy" to do so — I took that as a compliment.  
`**` In the case of Python, the problem is significant identation. However, the problem is
solved by using a custom indentation-aware parser, which is frankly the way to go.

## Key Principle

The key principle when dealing with side effects is this:

> Each parser either suceeds, or fails leaving the state as though no side-effects had ever been
> applieds.

This is harder than it seems at first, because you have to care about side effects that you may not
even know about. If a [`seq`] combinators chains three parser together, and the third fails, `seq`
has to ensure that the side-effects applied by the two first parsers are somehow undone. The parsers
themselves won't take care of it because they suceeded!

[`seq`]: ../API/parsers/sequential.md#seq

## Autumn's Solution

Autumn's solution is actually quite simple: it requires all side effects to be registered in a
central location (within the `Grammar` instance), along with a mean to undo the side effect.

Autumn represents applied side effects by instances of `AppliedChange`. An instance of this class
aggregate two other object: an instance of `Change` and an instance of `UndoChange`.

`Change` is an alias for `(Grammar) -> UndoChange`, `UndoChange` is an alias for `(Grammar) ->
Unit`. Essentially, calling a `Change` produces the side-effect and returns a means to undo it.

The reason why we don't just store `UndoChange` instances is that the ability to replay changes
is also valuable (see later).

**TODO**: links  
**TODO**: replay changes

## Implementing Side-Effecting Parsers

If you want to write a parser that has side-effects, you have to encapsulate any side-effecting
action in a `Change` object: a function that performs the side effect and returns an `UndoChange`
object, which is itself a function that undoes the side-effect.

To actually apply the side-effect, you need to call [`Grammar#apply`] with the `Change` as
parameter. This will call the `Change` object and register and `AppliedChange` object.
 
[`Grammar#apply`]: ../API/grammar.md#apply
 
## Implementing Safe Parser Combinators

If you recall the key principle outlined above, if a parser calls other parsers, it must be able
to undo their side-effects. There are two ways to achieve this.

The first way is to  wrap your logic in a [`transact`] combinator. The combinator will ensure that
if your parser fails, all side-effects are properly undone (assuming they were properly registered
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