package norswap.autumn.naive

/**
 * A parser that matches applications of a set of right-associative binary operators and
 * prefix operators.
 *
 * This parser must be instantiated through the [assoc_right] function, which
 * takes an initialization function as parameter.
 *
 * Within that function, you must specify how to parse the left-hand side and right-hand side of
 * these operators by assigning the [left] and [right] properties. If both sides are recognized by
 * the same parser, assign [operands] instead. For prefix operators, only the right-hand side
 * is relevant.
 *
 * The operators themselves must be defined with one of the [op] or [prefix] functions.
 *
 * All operators explicitly handled by this parser have the same precedence, which is naturally
 * lower than that of the operators (if any) matched by [left] and [right].
 *
 * By default, the parser matches the same thing as its [right] property if no binary or prefix
 * operators are matched.  This is typically the desired behaviour when implementing expressions in
 * a language. This can be controlled through the [strict] property (should be set in the
 * initialization function).
 */
// class AssocRight internal constructor (val g: Grammar): Parser

// =================================================================================================

/**
 * Constructor for [AssocRight]. See the class documentation for details, notably
 * on the content of [init].
 */
// fun Grammar.assoc_right (init: AssocRight.() -> Unit): Parser