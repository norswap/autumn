# `AssocRight`


This parser is defined in the file [AssocRight.kt].

[AssocRight.kt]: /norswap/autumn/parsers/AssocRight.kt

A parser that matches applications of a set of right-associative binary operators and
prefix operators.

This parser must be instantiated through the [`assoc_right`] function, which
takes an initialization function as parameter.

Within that function, you must specify how to parse the left-hand side and right-hand side of
these operators by assigning the [`left`] and [`right`] properties. If both sides are recognized by
the same parser, assign [`operands`] instead. For prefix operators, only the right-hand side
is relevant.

The operators themselves must be defined with one of the [`op`] or [`prefix`] functions.

All operators explicitly handled by this parser have the same precedence, which is naturally
lower than that of the operators (if any) matched by [`left`] and [`right`].

By default, the parser matches the same thing as its [`right`] property if no binary or prefix
operators are matched.  This is typically the desired behaviour when implementing expressions in
a language. This can be controlled through the [`strict`] property (should be set in the
initialization function).

[`assoc_right`]: #assoc_right
[`left`]: #left
[`right`]: #right
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

If false (default), this parser also matches the same thing as its [`right`] property if no
binary or prefix operators are matched. Set this property in the initialization function.

### `left`

    var left: Parser? = null

The parser used to match the left-hand side of the operators.
Must be set (or [`operands`]) in [`assoc_right`]'s initialization function.

### `right`

    var right: Parser? = null

The parser used to match the right-hand side of the operators.
Must be set (or [`operands`]) [`assoc_right`]'s initialization function.

### `operands`

    var operands: Parser?

The parser used to match both sides of the operator.
Setting this property automatically sets both [`left`] and [`right`].

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

### `prefix_stackless`

    inline fun prefix_stackless(
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)

Adds a prefix operator (no left operand) with the given `syntax` (operator only) and the
given `effect` when the operator is matched with its operand.

### `prefix_affect`

    inline fun prefix_affect(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)

Adds a prefix operator (no left operand) with the given `syntax` (operator only) and the
given `effect` when the operator is matched with its operand.

The `effect` function is passed the stack frame of the operator and its operand.

### `prefix`

    inline fun prefix(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)

Adds a prefix operator (no left operand) with the given `syntax` (operator only) and the
given `effect` when the operator is matched with its operand.

The `effect` function is passed the stack frame of the operator and its operand,
and its result is pushed on the value stack.