package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import java.util.ArrayDeque
import java.util.ArrayList

/**
 * A parser that matches applications of a set of left-associative binary operators and
 * postfix operators.
 *
 * This parser must be instantiated through the [assoc_left] function, which
 * takes an initialization function as parameter.
 *
 * Within that function, you must specify how to parse the left-hand side and right-hand side of
 * these operators by assigning the [left] and [right] properties. If both sides are recognized by
 * the same parser, assign [operands] instead. For postfix operators, only the left-hand side
 * is relevant.
 *
 * The operators themselves must be defined with one of the [op] or [postfix] functions.
 *
 * All operators explicitly handled by this parser have the same precedence, which is naturally
 * lower than that of the operators (if any) matched by [left] and [right].
 *
 * By default, the parser matches the same thing as its [left] property if no binary or postfix
 * operators are matched.  This is typically the desired behaviour when implementing expressions in
 * a language. This can be controlled through the [strict] property (should be set in the
 * initialization function).
 */
class AssocLeft internal constructor (val g: Grammar): Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * If false (default), this parser also matches the same thing as its [left] property if no
     * binary or postfix operators are matched. Set this property in the initialization function.
     */
    var strict: Boolean = false

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used to match the left-hand side of the operators.
     * Must be set (or [operands]) in [assoc_left]'s initialization function.
     */
    var left: Parser? = null

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used to match the right-hand side of the operators.
     * Must be set (or [operands]) [assoc_left]'s initialization function.
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

    /** Matches the operator + the right-hand side. */
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    /** Stack frame when the parser was invoked. */
    @PublishedApi
    internal var frame = 0

    /** Previous values of [frame] (to support recursive invocation of AssocLeft). */
    private val frames = ArrayDeque<Int>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a binary operator with the given [syntax] (operator only) and the given [effect] when
     * the operator is matched with its operands.
     */
    inline fun op_stackless (
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && right!!() && g.perform { effect() } } }
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
        op_stackless(syntax) { effect(frame_end(frame)) }
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
        op_affect(syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a postfix operator (no right operand) with the given [syntax] (operator only) and the
     * given [effect] when the operator is matched with its operand.
     */
    inline fun postfix_stackless(
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && g.perform { effect() } } }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a postfix operator (no right operand) with the given [syntax] (operator only) and the
     * given [effect] when the operator is matched with its operand.
     *
     * The [effect] function is passed the stack frame of the operator and its operand.
     */
    inline fun postfix_affect(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        postfix_stackless(syntax) { effect(frame_end(frame)) }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a postfix operator (no right operand) with the given [syntax] (operator only) and the
     * given [effect] when the operator is matched with its operand.
     *
     * The [effect] function is passed the stack frame of the operator and its operand,
     * and its result is pushed on the value stack.
     */
    inline fun postfix(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        postfix_affect(syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    private fun invoke_strict(): Boolean
        = g.seq { left!!() && g.repeat1 { operators.any { it() } } }

    private fun invoke_lax(): Boolean
        = g.seq { left!!() && g.repeat0 { operators.any { it() } } }

    override fun invoke(): Boolean
    {
        frames.push(frame)
        frame = g.frame_start()

        val out =   if (strict) invoke_strict()
                    else        invoke_lax()

        frame = frames.pop()
        return out
    }
}

// =================================================================================================

/**
 * Constructor for [AssocLeft]. See the class documentation for details, notably
 * on the content of [init].
 */
fun Grammar.assoc_left (init: AssocLeft.() -> Unit): Parser
{
    val out = AssocLeft(this)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}
