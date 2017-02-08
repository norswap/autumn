package norswap.autumn.parsers
import norswap.autumn.*

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
    println(input.offsetToString(pos) + ": " + str)
    return true
}

// -------------------------------------------------------------------------------------------------