package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import java.util.ArrayDeque
import java.util.ArrayList

// =================================================================================================

class PrecedenceLeft internal constructor (val g: Grammar): Parser
{
    // ---------------------------------------------------------------------------------------------

    var left  : Parser? = null
    var right : Parser? = null

    // ---------------------------------------------------------------------------------------------

    fun higher (p: Parser)
    {
        if (left != null || right != null)
            throw IllegalStateException("You already defined a higher-precedence parser.")
        left  = p
        right = p
    }

    // ---------------------------------------------------------------------------------------------

    val operators = ArrayList<Parser>()

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

    inline fun op_suffix (
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

fun Grammar.PrecedenceLeft (init: PrecedenceLeft.() -> Unit): Parser
{
    val out = PrecedenceLeft(this)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}

// =================================================================================================

class PrecedenceRight internal constructor (val g: Grammar): Parser
{
    // ---------------------------------------------------------------------------------------------

    var left  : Parser? = null
    var right : Parser? = null

    // ---------------------------------------------------------------------------------------------

    fun higher (p: Parser)
    {
        if (left != null || right != null)
            throw IllegalStateException("You already defined a higher-precedence parser.")
        left  = p
        right = p
    }

    // ---------------------------------------------------------------------------------------------

    val operators = ArrayList<Parser>()

    val effects = ArrayDeque<Grammar.() -> Unit>()

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

    inline fun op_suffix (
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

fun Grammar.PrecedenceRight (init: PrecedenceRight.() -> Unit): Parser
{
    val out = PrecedenceRight(this)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}

// =================================================================================================

