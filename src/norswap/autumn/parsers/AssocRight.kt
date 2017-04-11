package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import java.util.ArrayDeque
import java.util.ArrayList

/**
 * A parser that matches applications of a set of right-associative binary operators.
 *
 * This parser must be instantiated through the [assoc_right] function, which takes an
 * initialization function as parameter.
 *
 * Within that function, you must specify how to parse the left- and right-hand side of these
 * operators by assigning the [operands] property.
 *
 * The operators themselves must be defined with one of the [op] functions.
 *
 * All operators explicitly handled by this parser have the same precedence, which is naturally
 * lower than that of the operators (if any) matched by [operands].
 *
 * By default, the parser matches the same thing as its [operands] property if no binary operators
 * are matched.  This is typically the desired behaviour when implementing expressions in a
 * language. This can be controlled through the [strict] property (should be set in the
 * initialization function).
 */
class AssocRight internal constructor (val g: Grammar): Parser
{
    // ---------------------------------------------------------------------------------------------

    /**
     * If false (default), this parser also matches the same thing as its [operands] property if no
     * operators are matched. Set this property in the initialization function.
     */
    var strict: Boolean = false

    // ---------------------------------------------------------------------------------------------

    /**
     * The parser used to match both sides of the operator.
     */
    var operands: Parser? = null

    // ---------------------------------------------------------------------------------------------

    /** Matches the left-hand side + the operator. */
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    /** A stack of effects pushed while parsing and applied afterwards. */
    @PublishedApi
    internal val effects = ArrayDeque<Grammar.() -> Unit>()

    /** Stack frames pushed before parsing each operand. */
    @PublishedApi
    internal val frames = ArrayDeque<Int>()

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a binary operator with the given [syntax] (operator only) and the given [effect] when
     * the operator is matched with its operands.
     */
    inline fun op_stackless (
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)
    {
        operators += { g.transact b@ {
            var result = syntax()
            if (!result) return@b false
            val frame0 = g.frame_start()
            result = operands!!()
            if (!result) return@b false
            frames.pop()
            frames.push(frame0)
            effects.push(effect)
            true
        } }
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
        operators += { g.transact b@ {
            var result = syntax()
            if (!result) return@b false
            val frame0 = g.frame_start()
            result = operands!!()
            if (!result) return@b false
            val frame = frames.pop()
            frames.push(frame0)
            effects.push { effect(frame_end(frame)) }
            true
        } }
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

    private fun Grammar.invoke_strict(): Boolean
        = seq {
            frames.push(frame_start())
            operands!!() && repeat1 { operators.any { it() } }
        }

    private fun Grammar.invoke_lax(): Boolean
        = seq {
            frames.push(frame_start())
            operands!!() && repeat0 { operators.any { it() } }
        }

    override fun invoke(): Boolean
    {
        val effects_size0 = effects.size

        val result =
            if (strict) g.invoke_strict()
            else        g.invoke_lax()

        frames.pop() // the frame for rightmost operand is unused

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
    if (out.operands == null) throw Error ("You did not define the operands parser.")
    return out
}