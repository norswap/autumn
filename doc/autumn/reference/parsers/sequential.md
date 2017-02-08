#  Matching Sequences and Optionals

### `seq`

    inline fun Grammar.seq (crossinline p: Parser): Boolean

`p` must of the form `p1() && p2() && ...`
e.g. `seq { word("hello") && word("world") }`

If you don't understand why the `&&` are required, check [this FAQ entry][pipes].
 
[pipes]: ../../faq/seq-choice-syntax.md

Matches all the parsers in a sequence.

### `opt`

    inline fun Grammar.opt (crossinline p: Parser): Boolean

Matches `p` if it suceeds, otherwise succeeds without consuming any input.

### `repeat0`

    inline fun Grammar.repeat0 (crossinline p: Parser): Boolean

Matches 0 or more (sequential) repetition of `p`.

### `repeat1`

    inline fun Grammar.repeat1 (crossinline p: Parser): Boolean

Matches 1 or more (sequential) repetition of `p`.

### `repeat`

    inline fun Grammar.repeat (n: Int, crossinline p: Parser): Boolean

Matches exactly `n` (sequential) repetitions of `p`.

### `around0`

    inline fun Grammar.around0 (crossinline around: Parser, crossinline inside: Parser): Boolean

Matches 0 or more repetitions of `around`, separated from one another by input matching `inside`.

### `around1`

    inline fun Grammar.around1 (crossinline around: Parser, crossinline inside: Parser): Boolean

Matches 1 or more repetitions of `around`, separated from one another by input matching `inside`.

### `until0`

    inline fun Grammar.until0 (crossinline repeat: Parser, crossinline terminator: Parser): Boolean

Matches 0 or more repetition of `repeat`, followed by `terminator`.

In case of ambiguity, `terminator` is matched in preference to `repeat`
(this is what makes this different from `seq { repeat0(repeat) && terminator() }`).

### `until1`

    inline fun Grammar.until1 (crossinline repeat: Parser, crossinline terminator: Parser): Boolean

Matches 1 or more repetition of `repeat`, followed by `terminator`.

In case of ambiguity, `terminator` is matched in preference to `repeat`
(this is what makes this different from `seq { repeat1(repeat) && terminator() }`).