package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import java.util.ArrayDeque
import java.util.ArrayList

// =================================================================================================

class AssocLeft internal constructor (val g: Grammar, var strict: Boolean = false): Parser
{
    // ---------------------------------------------------------------------------------------------

    var left  : Parser? = null
    var right : Parser? = null

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

    // Matches the operator + the right-hand side.
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    // ---------------------------------------------------------------------------------------------

    inline fun op_stackless (
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && right!!() && g.perform { effect() } } }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun op_affect (
        n_operands: Int,
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        op_stackless(syntax) { effect(frame(n_operands)) }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun op (
        n_operands: Int,
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        op_affect(n_operands, syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun postfix(
        n_operands: Int,
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        operators += { g.seq { syntax() && g.perform { stack.push(effect(frame(n_operands))) } } }
    }

    // ---------------------------------------------------------------------------------------------

    override fun invoke(): Boolean
    {
        return g.seq { left!!() && g.repeat0 { operators.any { it() } } }
    }
}

// =================================================================================================

fun Grammar.assoc_left (init: AssocLeft.() -> Unit): Parser
{
    val out = AssocLeft(this)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}

// =================================================================================================

class AssocRight internal constructor (val g: Grammar, val strict: Boolean = false): Parser
{
    // ---------------------------------------------------------------------------------------------

    var left  : Parser? = null
    var right : Parser? = null

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

    // Matches the operator + the right-hand side.
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    // A stack of effects pushed while parsing and applied afterwards.
    @PublishedApi
    internal val effects = ArrayDeque<Grammar.() -> Unit>()

    // ---------------------------------------------------------------------------------------------

    inline fun op_stackless (
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && right!!() && g.perform { effects.push(effect) } } }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun op_affect (
        n_operands: Int,
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        op_stackless(syntax) { effect(frame(n_operands)) }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun op (
        n_operands: Int,
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        op_affect(n_operands, syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun postfix(
        n_operands: Int,
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        operators += { g.seq { syntax()
                        && g.perform { effects.push { stack.push(effect(frame(n_operands))) } } } }
    }

    // ---------------------------------------------------------------------------------------------

    override fun invoke(): Boolean
    {
        val effects_size0 = effects.size

        val result = g.seq { left!!() && g.repeat0 { operators.any { it() } } }

        while (effects.size > effects_size0)
            effects.pop()(g)

        return result
    }
}

// =================================================================================================

fun Grammar.assoc_right (init: AssocRight.() -> Unit): Parser
{
    val out = AssocRight(this)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}

// =================================================================================================

