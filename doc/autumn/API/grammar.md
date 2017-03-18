# `Grammar` API Reference

The API described in this file is implemented in [Grammar.kt].

[Grammar.kt]: /src/norswap/autumn/Grammar.kt

## Instantiating and Starting a Parse

You create a grammar by subclassing this class. 
Refer to the [user guide] for more details.

[User Guide]: ../guide/README.md

After instantiating the grammar, you can start a parse with one of the `parse` or `parse_prefix`
methods.

Each grammar instance can only handle one parse at a time. `Grammar` is not
thread-safe. If you want multiple parallel parses of the same grammar, create multiple instances.

You can reuse a single instance for multiple (non-concurrent) parses, by calling the [`reset`]
method before issuing a new `parse` call.

A parse needs a [`ParseInput`], which tab-expands the input string, adds a null terminator and wraps
it with line/column information. You can supply a string instead, and a `ParseInput` will be
automatically constructed.

[`ParseInput`]: parse-input.md
    
### Parse API
    
    fun parse (input: ParseInput, allow_prefix: Boolean, parser: Parser): Boolean
    
Starts a parse, using the supplied parser as root. `allow_prefix` controls whether the whole input
must match, or if a prefix match is sufficient.

    fun parse (input: ParseInput): Boolean
    fun parse (str: String): Boolean
    
Starts a parse. The parse must match the whole input string or a failure is returned.
    
    fun parse_prefix(input: ParseInput): Boolean
    fun parse_prefix(str: String): Boolean

Starts a parse. The parse may match only a prefix of the input string.

    open fun reset()
    
Resets the grammar for a new parse (or to force releasing unused memory after a parse is complete).
Subclasses may override this to add custom reset logic, but must always call `super.reset()`.

## Overridable Members

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

## Data Accessible From Parsers

This is data that can be accessed from parsers that have a reference to the `Grammar`.
In cases where the data is mutable, remember that your modifications must obey the
[Transactionality Rule].

[Transactionality Rule]: ../guide/transactionality.md

    var (readonly) input: ParseInput
    
The parse input associated with the current parse.

    var (readonly) text: String
    
Null-terminated input text for the current parse. This is a reference to the text of the
[`ParseInput`] for the current parse.

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
    
This datastructure underpins Autumn's [built-in support for side-effects / parse state][side].
Your normally never needs to access this. Most of the time, using [`transact`] instead is the way
to go.

[side]: ../guide/side-effects.md
[`transact`]: parsers/misc.md#transact

## Side-Effects / State Manipulation

TODO: some explanations

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

    