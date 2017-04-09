# `AssocLeft`

This parser is defined in the file [AssocLeft.kt].

[AssocLeft.kt]: /norswap/autumn/parsers/AssocLeft.kt

A parser that matches applications of a set of left-associative binary operators and
postfix operators.

This parser must be instantiated through the [`assoc_left`] function, which
takes an initialization function as parameter.

Within that function, you must specify how to parse the left-hand side and right-hand side of
these operators by assigning the [`left`] and [`right`] properties. If both sides are recognized by
the same parser, assign [`operands`] instead. For postfix operators, only the left-hand side
is relevant.

The operators themselves must be defined with one of the [`op`] or [`postfix`] functions.

All operators explicitly handled by this parser have the same precedence, which is naturally
lower than that of the operators (if any) matched by [`left`] and [`right`].

By default, the parser matches the same thing as its [`left`] property if no binary or postfix
operators are matched.  This is typically the desired behaviour when implementing expressions in
a language. This can be controlled through the [`strict`] property (should be set in the
initialization function).

[`assoc_left`]: #assoc_left
[`left`]: #left
[`right`]: #right
[`operands`]: #operands
[`op`]: #op_stackless
[`postfix`]: #postfix_stackless
[`strict`]: #strict

### `assoc_left` (top-level function)
 
    fun Grammar.assoc_left (init: AssocLeft.() -> Unit): Parser

Constructor for `AssocLeft` (this is not an instance method).
See above for details on the content of `init`.

### `strict`
    
    var strict: Boolean = false

If false (default), this parser also matches the same thing as its [`left`] property if no
binary or postfix operators are matched. Set this property in the initialization function.
    
### `left`
 
    var left: Parser? = null

The parser used to match the left-hand side of the operators.
Must be set (or [`operands`]) in [`assoc_left`]'s initialization function.

### `right`

    var right: Parser? = null

The parser used to match the right-hand side of the operators.
Must be set (or [`operands`]) [`assoc_left`]'s initialization function.

### `operands`

    var operands: Parser?
    
The parser used to match both sides of the operator.
Setting this property automatically sets both [`left`] and [`right`].

### `op_stackless`

    inline fun op_stackless (
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)

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

### `postfix_stackless`

    inline fun postfix_stackless(
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)

Adds a postfix operator (no right operand) with the given `syntax` (operator only) and the
given `effect` when the operator is matched with its operand.

### `postfix_affect`

    inline fun postfix_affect(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)

Adds a postfix operator (no right operand) with the given `syntax` (operator only) and the
given `effect` when the operator is matched with its operand.

The `effect` function is passed the stack frame of the operator and its operand.

### `postfix`

    inline fun postfix(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)

Adds a postfix operator (no right operand) with the given `syntax` (operator only) and the
given `effect` when the operator is matched with its operand.

The `effect` function is passed the stack frame of the operator and its operand,
and its result is pushed on the value stack.