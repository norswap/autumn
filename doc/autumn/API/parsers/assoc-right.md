# `AssocRight`


This parser is defined in the file [AssocRight.kt].

[AssocRight.kt]: /norswap/autumn/parsers/AssocRight.kt

A parser that matches applications of a set of right-associative binary operators.

This parser must be instantiated through the [`assoc_right`] function, which
takes an initialization function as parameter.

Within that function, you must specify how to parse the left- and right-hand side of these
operators by assigning the [`operands`] property.

The operators themselves must be defined with one of the [`op`] functions.

All operators explicitly handled by this parser have the same precedence, which is naturally
lower than that of the operators (if any) matched by [`operands`].

By default, the parser matches the same thing as its [`operands`] property if no binary operators
are matched.  This is typically the desired behaviour when implementing expressions in a
language. This can be controlled through the [`strict`] property (should be set in the
initialization function).

[`assoc_right`]: #assoc_right
[`operands`]: #operands
[`op`]: #op_stackless
[`prefix`]: #prefix_stackless
[`strict`]: #strict

## `assoc_right` (top-level function)

    fun Grammar.assoc_right (init: AssocRight.() -> Unit): Parser
    
Constructor for `AssocRight` (this is not an instance method).
See above for details on the content of `init`.

### `strict`

    val strict: Boolean = false

If false (default), this parser also matches the same thing as its [`operands`] property if no
operators are matched. Set this property in the initialization function.

### `operands`

    var operands: Parser?

The parser used to match both sides of the operator.

### `op_stackless`

    inline fun op_stackless (
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)

Adds a binary operator with the given `syntax` (operator only) and the given `effect` when
the operator is matched with its operands.

### `op_affect`

    inline fun op_affect (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)

Adds a binary operator with the given `syntax` (operator only) and the given `effect` when
the operator is matched with its operands.

The `effect` function is passed the stack frame of the operator and its operands.

### `op`

    inline fun op (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)

Adds a binary operator with the given `syntax` (operator only) and the given `effect` when
the operator is matched with its operands.

The `effect` function is passed the stack frame of the operator and its operands,
and its result is pushed on the value stack.