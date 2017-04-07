# Manipulating the Value Stack

### `affect (backlog)`

    inline fun Grammar.affect (backlog: Int, syntax: Parser, effect: Grammar.(Array<Any?>) -> Unit): Boolean

Matches `syntax`, then call `effect`, passing it an array containing everything pushed on the stack
since the parser's invocation, to which `backlog` items of backlog have been prepended. All these
items are removed from the stack.

Insufficient items to satisfy the backlog requirement will the cause the parser to fail with an
execption.

### `affect`

    inline fun Grammar.affect (syntax: Parser, effect: Grammar.(Array<Any?>) -> Unit): Boolean

Like `affect (backlog)`, but with no backlog.


### `affect_str`

    inline fun Grammar.affect_str (syntax: Parser, effect: Grammar.(String) -> Unit): Boolean

Matches `syntax`, then calls `effect`, passing it a string containing the matched text.

### `build (backlog)`

    inline fun Grammar.build (backlog: Int, syntax: Parser, effect: Grammar.(Array<Any?>) -> Any): Boolean

Matches `syntax`, then calls `effect`, passing it an array containing everything pushed on the stack
since the parser's invocation, to which `backlog` items of backlog have been prepended. All these
items are removed from the stack. The return value of `effect` is itself pushed on the stack.

Insufficient items to satisfy the backlog requirement will the cause the parser to fail with an
exception.

### `build`

    inline fun Grammar.build (syntax: Parser, effect: Grammar.(Array<Any?>) -> Any): Boolean

Like `build (backlog)`, with no backlog.

### `build_str (value)`

    inline fun Grammar.build_str (syntax: Parser, value: Grammar.(String) -> Any): Boolean

Matches `syntax`, then calls `value`, passing it a string containing the matched text.
The return value of `value` is pushed on the stack.

### `build_str`

    inline fun Grammar.build_str (syntax: Parser): Boolean

Like `build_str`, but the string is directly pushed on the stack
instead of being passed to a function.

### `maybe`

    inline fun Grammar.maybe (crossinline p: Parser): Boolean

Matches `p` or, if `p` fails, pushes `null` on the stack.  
Always succeeds.

### `as_bool`

    inline fun Grammar.as_bool (crossinline p: Parser): Boolean

Attempts to match `p`, then pushes `true` on the stack if successful, `false` otherwise.
Also discards its stack frame.
Always suceeds.

### `as_val`

    inline fun Grammar.as_val (value: Any?, crossinline p: Parser): Boolean

Matches `p` then pushes `value` on the stack if successful.

### `gobble`

    inline fun Grammar.gobble (crossinline terminator: Parser): Boolean
    
Matches all characters until `terminator` (also matched).

All characters matched in this manner (excluding `terminator`) are collected in a string
which is pushed on the value stack.