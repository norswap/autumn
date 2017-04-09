package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import java.util.ArrayDeque
import java.util.ArrayList

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
class AssocRight internal constructor (val g: Grammar): Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * If false (default), this parser also matches the same thing as its [right] property if no
     * binary or prefix operators are matched. Set this property in the initialization function.
     */
    var strict: Boolean = false

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used to match the left-hand side of the operators.
     * Must be set (or [operands]) in [assoc_right]'s initialization function.
     */
    var left: Parser? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used to match the right-hand side of the operators.
     * Must be set (or [operands]) [assoc_right]'s initialization function.
     */
    var right: Parser? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used to match both sides of the operator.
     * Setting this property automatically sets both [left] and [right].
     */
    var operands: Parser?
        get() {
            if (left != right) throw IllegalStateException("Left and right operands are different.")
            return left
        }
        set (p) {
            left  = p
            right = p
        }

    // ---------------------------------------------------------------------------------------------

    /** Matches the left-hand side + the operator. */
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    /** A stack of effects pushed while parsing and applied afterwards. */
    @PublishedApi
    internal val effects = ArrayDeque<Grammar.() -> Unit>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a binary operator with the given [syntax] (operator only) and the given [effect] when
     * the operator is matched with its operands.
     */
    inline fun op_stackless (
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { left!!() && syntax() && g.perform { effects.push(effect) } } }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a binary operator with the given [syntax] (operator only) and the given [effect] when
     * the operator is matched with its operands.
     *
     * The [effect] function is passed the stack frame of the operator and its operands.
     */
    inline fun op_affect (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        operators += b@ {
            val ptr = g.frame_start()
            val result = g.seq { left!!() && syntax() }
            if (!result) return@b false
            effects.push { effect(frame_end(ptr)) }
            true
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a binary operator with the given [syntax] (operator only) and the given [effect] when
     * the operator is matched with its operands.
     *
     * The [effect] function is passed the stack frame of the operator and its operands,
     * and its result is pushed on the value stack.
     */
    inline fun op (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        op_affect (syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a prefix operator (no left operand) with the given [syntax] (operator only) and the
     * given [effect] when the operator is matched with its operand.
     */
    inline fun prefix_stackless(
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && g.perform { effects.push(effect) } } }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a prefix operator (no left operand) with the given [syntax] (operator only) and the
     * given [effect] when the operator is matched with its operand.
     *
     * The [effect] function is passed the stack frame of the operator and its operand.
     */
    inline fun prefix_affect(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        operators += b@ {
            val ptr = g.frame_start()
            if (!syntax()) return@b false
            effects.push { effect(frame_end(ptr)) }
            true
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a prefix operator (no left operand) with the given [syntax] (operator only) and the
     * given [effect] when the operator is matched with its operand.
     *
     * The [effect] function is passed the stack frame of the operator and its operand,
     * and its result is pushed on the value stack.
     */
    inline fun prefix(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        prefix_affect(syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    private fun invoke_strict(): Boolean
        = g.seq { g.repeat1 { operators.any { it() } } && right!!() }

    private fun invoke_lax(): Boolean
        = g.seq { g.repeat0 { operators.any { it() } } && right!!() }

    override fun invoke(): Boolean
    {
        val effects_size0 = effects.size

        val result =
            if (strict) invoke_strict()
            else        invoke_lax()

        while (effects.size > effects_size0)
            if (result) effects.pop()(g)
            else        effects.pop()

        return result
    }
}

// =================================================================================================

/**
 * Constructor for [AssocRight]. See the class documentation for details, notably
 * on the content of [init].
 */
fun Grammar.assoc_right (init: AssocRight.() -> Unit): Parser
{
    val out = AssocRight(this)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}