package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import java.util.ArrayDeque
import java.util.ArrayList

// =================================================================================================

class AssocLeft internal constructor (val g: Grammar, var strict: Boolean): Parser
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

    /** Matches the operator + the right-hand side. */
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    /** Size of the value stack when the parser was invoked. */
    @PublishedApi
    internal var ptr0 = 0

    // ---------------------------------------------------------------------------------------------

    inline fun op_stackless (
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && right!!() && g.perform { effect() } } }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun op_affect (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        op_stackless(syntax) { effect(frame(g.stack.size - ptr0)) }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun op (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        op_affect(syntax) { stack.push(effect(it)) }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun postfix_stackless(
        crossinline syntax: Parser,
        crossinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { syntax() && g.perform { effect() } } }
    }

    // ---------------------------------------------------------------------------------------------

    inline fun postfix_affect(
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Unit)
    {
        postfix_stackless(syntax) { effect(frame(g.stack.size - ptr0)) }
    }

    // ---------------------------------------------------------------------------------------------
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
        ptr0 = g.stack.size
        return  if (strict) invoke_strict()
                else        invoke_lax()
    }
}

// =================================================================================================

fun Grammar.assoc_left (strict: Boolean = false, init: AssocLeft.() -> Unit): Parser
{
    val out = AssocLeft(this, strict)
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

    /** Matches the operator + the right-hand side. */
    @PublishedApi
    internal val operators = ArrayList<Parser>()

    /** A stack of effects pushed while parsing and applied afterwards. */
    @PublishedApi
    internal val effects = ArrayDeque<Grammar.() -> Unit>()

    // ---------------------------------------------------------------------------------------------

    inline fun op_stackless (
        crossinline syntax: Parser,
        noinline effect: Grammar.() -> Unit)
    {
        operators += { g.seq { left!!() && syntax() && g.perform { effects.push(effect) } } }
    }

    // ---------------------------------------------------------------------------------------------

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

    inline fun op (
        crossinline syntax: Parser,
        crossinline effect: Grammar.(Array<Any?>) -> Any?)
    {
        op_affect (syntax) { stack.push(effect(it)) }
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

fun Grammar.assoc_right (strict: Boolean = false, init: AssocRight.() -> Unit): Parser
{
    val out = AssocRight(this, strict)
    out.init()
    if (out.left == null || out.right == null)
        throw Error ("You did not define a higher-precedence parser for a binary operator.")
    return out
}

// =================================================================================================

