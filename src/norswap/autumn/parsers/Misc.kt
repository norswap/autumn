package norswap.autumn.parsers
import norswap.autumn.*
import norswap.autumn.undoable.UndoList

// ---------------------------------------------------------------------------------------------

inline fun Grammar.transact (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    val out = p()
    if (!out) undo(pos0, ptr0)
    return out
}

// ---------------------------------------------------------------------------------------------

inline fun Grammar.ignore_errors (crossinline p: Parser): Boolean
{
    val pos0 = fail_pos
    val failure0 = failure
    val result = p()
    fail_pos = pos0
    failure = failure0
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.ignore_errors_if_successful (crossinline p: Parser): Boolean
{
    val pos0 = fail_pos
    val failure0 = failure
    val result = p()
    if (result) {
        fail_pos = pos0
        failure = failure0
    }
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.perform (f: Grammar.() -> Unit): Boolean
{
    f()
    return true
}

// -------------------------------------------------------------------------------------------------

data class Invocation (
    val invocation_pos: Int,
    var max_pos: Int,
    var max_delta: List<Change>)
{
    fun is_at (pos: Int): Boolean
        = invocation_pos == pos

    fun has_delta(): Boolean
        = invocation_pos != max_pos

    fun grow (g: Grammar, pos: Int, ptr0: Int): Boolean
    {
        if (pos <= max_pos) return false
        max_pos = pos
        max_delta = g.diff(ptr0)
        return true
    }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.leftrec (crossinline p: Grammar.(self: Parser) -> Boolean): Parser
{
    return object: (Grammar) -> Boolean
    {
        val invocations = UndoList<Invocation>(this@leftrec)

        init {
            // avoid checking if stack is empty
            invocations._push(Invocation(-1, -1, emptyList<Change>()))
        }

        override fun invoke (g: Grammar): Boolean
        {
            var invoc = invocations.peek()

            if (invoc.is_at(pos)) {
                if (!invoc.has_delta())
                    return false
                merge(invoc.max_pos, invoc.max_delta)
                return true
            }

            // This is the initial invocation at this position.

            invoc = Invocation(pos, pos, emptyList<Change>())
            invocations.push(invoc)

            val ptr0 = log.size
            val pos0 = pos

            while (true)
            {
                p { invoke(g) }
                val grew = invoc.grow(g, pos, ptr0)
                undo(pos0, ptr0)
                if (!grew) break
            }

            merge(invoc.max_pos, invoc.max_delta)
            return invoc.has_delta()
        }
    }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.affect (backlog: Int, syntax: Parser, effect: Grammar.(Array<Any?>) -> Unit): Boolean
{
    val frame = frame_start(backlog)
    val result = syntax()
    if (result) {
        effect(frame_end(frame))
    }
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.affect (syntax: Parser, effect: Grammar.(Array<Any?>) -> Unit): Boolean
{
    return affect(0, syntax, effect)
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build (backlog: Int, syntax: Parser, effect: Grammar.(Array<Any?>) -> Any): Boolean
{
    return affect(backlog, syntax) { stack.push(effect(it)) }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build (syntax: Parser, effect: Grammar.(Array<Any?>) -> Any): Boolean
{
    return build(0, syntax, effect)
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build_str (syntax: Parser, value: Grammar.(String) -> Any): Boolean
{
    val pos0 = pos
    val result = syntax()
    if (result) {
        stack.push(value(text.substring(pos0, pos)))
    }
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build_str (syntax: Parser): Boolean
{
    return build_str (syntax) { it }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.contain (noinline failure: () -> String, crossinline p: Parser): Boolean
{
    val fail_pos0 = fail_pos
    val result = p()
    if (!result && fail_pos0 < pos) fail_force(pos, failure)
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.transact_contain (noinline failure: () -> String, crossinline p: Parser): Boolean
{
    return contain(failure) { transact { p() } }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.catch (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    try {
        return p()
    }
    catch (e: Throwable) {
        undo(pos0, ptr0)
        if (e is AutumnLogicException) fail(pos, e.failure)
        else fail(pos, CaughtException(e))
        return false
    }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.catch_contain (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    val fail_pos0 = fail_pos
    try {
        return p()
    }
    catch (e: Throwable) {
        undo(pos0, ptr0)
        if (fail_pos0 < pos)
            if (e is AutumnLogicException)
                fail_force(pos, e.failure)
            else
                fail_force(pos, CaughtException(e))
        return false
    }
}

// -------------------------------------------------------------------------------------------------

fun Grammar.log (str: String): Boolean
{
    println(offsetToString(pos) + ": " + str)
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.maybe (crossinline p: Parser): Boolean
{
    if (!p()) stack.push(null)
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.as_bool (crossinline p: Parser): Boolean
{
    val frame = frame_start()
    val result = p()
    frame_end(frame)
    stack.push(result)
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.as_val (value: Any?, crossinline p: Parser): Boolean
{
    val result = p()
    if (result) stack.push(value)
    return result
}

// -------------------------------------------------------------------------------------------------