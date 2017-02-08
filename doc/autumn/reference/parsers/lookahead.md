# Lookahead

### `ahead`

    inline fun Grammar.ahead (crossinline p: Parser): Boolean

Succeeds if `p` succeeds, but does not advance the input position
(all other side-effects of `p` are retained).

### `not`

    inline fun Grammar.not (crossinline p: Parser): Boolean

Succeeds only if `p` fails.