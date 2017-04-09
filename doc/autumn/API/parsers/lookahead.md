# Lookahead

These parsers are defined in the file [Lookahead.kt].

[Lookahead.kt]: /norswap/autumn/parsers/Lookahead.kt

### `ahead`

    inline fun Grammar.ahead (crossinline p: Parser): Boolean

Succeeds if `p` succeeds, but does not advance the input position
(all other side effects of `p` are retained).

### `ahead_pure`

    inline fun Grammar.ahead_pure (crossinline p: Parser): Boolean

Succeeds if [p] succeeds, but does produce any side effect (does not even change the input
position).

### `not`

    inline fun Grammar.not (crossinline p: Parser): Boolean

Succeeds only if `p` fails.