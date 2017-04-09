# Choices

Parsers that perform a choice between their sub-parsers.

These parsers are defined in the file [Choice.kt].

[Choice.kt]: /norswap/autumn/parsers/Choice.kt

### `choice`

    inline fun Grammar.choice (crossinline p: Parser): Boolean

`p` must of the form `p1() || p2() || ...`  
 e.g. `choice { string("hello") || string("goodbye") }`
 
 Matches the first parser in the list that matches, or fails if none succeeds.
 
If you don't understand why the `||` are required, check [this FAQ entry][pipes].
 
[pipes]: ../../faq/seq-choice-syntax.md

### `Longest (class)`

    class Longest (val g: Grammar, val ps: Array<Parser>): Parser

Matches the same thing as the parser in `ps` that matches the most input.
 
Side effects are retained only for the parser that is selected.

### `longest`

    fun Grammar.longest(vararg parsers: Parser): Parser

`longest(a, b)` is syntactic sugar for `Longest(this, arrayOf(a, b)`.

### `LongestPure`

    class LongestPure (val g: Grammar, val ps: Array<Parser>): Parser

Matche the same things as the parser in `ps` that matches the most input.
 
The parsers in `ps` should not have side effects besides updating the input position.

### `longest_pure`

    fun Grammar.longest_pure(vararg parsers: Parser): Parser

`longest_pure(a, b)` is syntactic sugar for `LongestPure(this, arrayOf(a, b)`.