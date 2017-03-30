# `Grammar`

The API described in this file is implemented in [Grammar.kt].

[Grammar.kt]: /src/norswap/autumn/Grammar.kt

- [Overview](#overview)
- [API Reference](#api-reference)

----

# Overview

### Usage

Subclass this class to create a new grammar.

Usually, parsers (~ grammar rules) for this new grammar are implemented as boolean-returning
method of the sub-class. Override [`root`] to define which parser is invoked when starting a parse.

You can also override [`whitespace`] to customize what is recognized as whitespace.

To parse some input, instantiate the class and use one of the [`parse`] methods.
You can reuse an instance after a parse: call [`reset`] before calling [`parse`] again.
To perform multiple concurrent parses with the same grammar, create multiple instances.

A parse works over a [`ParseInput`]. You can supply a string instead, and a `ParseInput` will be
automatically constructed.

[`root`]: #root
[`whitespace`]: #whitespace
[`parse`]: #parse
[`reset`]: #reset
[`ParseInput`]: parse-input.md

### Parse State

First, read [Handling Side Effects](/doc/autumn/guide/7-side-effects.md).

All modifications made to parse state during the parse must be mediated by the grammar instance.

These modifications are either the modification of the input position [`pos`]; or a parse state
modifcation encapsulated in a [`Change`] object, which must be applied by passing it to [`apply`].

The result of applying a [`Change`] is the addition of an [`AppliedChange`] object at the top
of the [`log`]. While the log is accessible, it is highly discouraged to access it, excepted
to record its size.

Further primitive parse state handling function are available: [`undo`], [`diff`] and [`merge`].

[`pos`]: #pos
[`Change`]: change.md#change
[`apply`]: #apply
[`AppliedChange`]: changes.md#appliedchange
[`undo`]: #undo
[`diff`]: #diff
[`merge`]: #merge

### Data, Input Position and Value Stack

The grammar gives you access to various properties such as `input` and `text`.

It also enables direct access / modification of the input position ([`pos`]) and the value
stack ([`stack`]).

Additionally, the grammar supplies multiple handling primitives for the value stack, used
by AST-building parsers. These all start by the [`frame`] prefix.

[`stack`]: #stack
[`frame`]: #frame

### Failure Reporting

To report that a parser failed, use the [`fail`] method. Failures should be reported by the
parser that caused them, so you don't need to report the failure of sub-parsers.

Autumn only tracks the failure that occurred furthest in the input. To override the
recorded failure, use [`fail_force`].

[`fail`]: #fail
[`fail_force`]: #fail_force

### Grammar Body DSL

This class also defines a few pieces of syntactic sugar that can be used to define parser
in its body. Those are: [`invoke`], [`list`], [`str`], [`word`], [`set`] and [`unaryPlus`].

[`invoke`]: #invoke
[`list`]: #list
[`str`]: #str
[`word`]: #word
[`set`]: #set
[`unaryPlus`]: #unaryplus

### Tokens

If you need to support some form of tokenization, please use the [`TokenGrammar`] subclass.
Tokenization has quite a few benefits in terms of performance and error reporting, so be sure to
consider it. We handle tokenization during the parse, and the scheme is much less rigid than the
usual lexing - parsing separation.

[`TokenGrammar`]: token-grammar.md

-----

# API Reference

## Usage

### `parse`

    fun parse (input: ParseInput, allow_prefix: Boolean, parser: Parser): Boolean
    
Starts a parse, using the supplied parser as root. `allow_prefix` controls whether the whole input
must match, or if a prefix match is sufficient.

    fun parse (input: ParseInput): Boolean
    fun parse (str: String): Boolean
    
Starts a parse. The parse must match the whole input string or a failure is returned.
    
    fun parse_prefix (input: ParseInput): Boolean
    fun parse_prefix (str: String): Boolean

Starts a parse. The parse may match only a prefix of the input string.

### `reset`

    open fun reset()
    
Resets the grammar for a new parse (or to force releasing unused memory after a parse is complete).
Subclasses may override this to add custom reset logic, but must always call `super.reset()`.

### `root`

    abstract fun root(): Boolean

The root parser for this grammar, which will be invoked by [parse].

[parse]: #parse-api

### `whitespace`

    open fun whitespace(): Boolean
    
The parser used by the [word] parser to skip whitespace.

Failures within this parser will be ignored.

The default implementation matches 0 or more [space_char].

[word]: parsers/chars.md#word-string
[space_char]: parsers/chars.md#space_char

## Side Effect Handling

### `undo`

    fun undo (pos0: Int, ptr0: Int)

Undo all changes that were done after the log was at `ptr0`.
Also restores the input position to `pos0`.

### `diff`

    fun diff (ptr0: Int): List<Change>

Return a list of changes between the current state and the state at `ptr0`.

### `merge`

    fun merge (pos1: Int, changes: List<Change>)

Merge the changes in `changes` into the current state.
Also sets the input position to `pos1`.

### `apply`

    fun apply (change: Change)

Apply `change` to the current state.

## Data, Input Position and Value Stack

### `input`

    var (readonly) input: ParseInput
    
The parse input associated with the current parse.

### `text`

    var (readonly) text: String
    
Null-terminated input text for the current parse. This is a reference to the text of the
[`ParseInput`] for the current parse.

### `pos`

    var pos = 0
    
Input position for the current parse.

### `stack`

    val stack  = UndoList<Any?>(this)

The *value stack*, a backtrack-safe stack available for use, typically to build up AST nodes.
Usually, you should use [stack-manipulation parser combinators][stack] instead of manipulating
this directly.

[stack]: parsers/stack.md

### `log`

    val log = ArrayList<AppliedChange>()
    
This datastructure underpins Autumn's [built-in support for side effects / parse state][side-effects].
Your normally never needs to access this. Most of the time, using [`transact`] instead is the way
to go.

[side-effects]: ../guide/7-side-effects.md
[`transact`]: parsers/misc.md#transact


### `frame`

    fun frame (backlog: Int): Array<Any?>

### `frame_start`

    fun frame_start (backlog: Int = 0): Int

### `frame_end`

    fun frame_end (frame: Int): Array<Any?>

## Grammar Body DSL

### `invoke`

    operator fun <T> Array<Any?>.invoke (i: Int): T

Returns the [i]th element of the array, casted to type [T].

### `list`

    fun <T> Array<Any?>.list(start: Int = 0, end: Int = size - 1): List<T>

Returns a sublist of the list, going from item `start` to `end` (both inclusive)
and casting the result to type `List<T>`.

### `str`

    val String.str: Boolean
    
Sugar for `string(this)`. ([`string`])

[`string`]: parsers/chars.md#string

### `word`

    val String.word: Boolean

Sugar for `word { string(this) }`. ([`word`][word-char], [`string`])
    
[word-char]: parsers/chars.md#word
    
### `set`    

    val String.set: Boolean

Sugar for `char_set(this)`. ([`char_set`])

[`char_set`]: parsers/chars.md#char_set-string

### `unaryPlus`

    operator fun String.unaryPlus(): Boolean
    
Sugar for `word(this)`. ([`word`])