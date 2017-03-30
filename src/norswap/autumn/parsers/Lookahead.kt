package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser
import norswap.autumn.BadMatch

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds if [p] succeeds, but does not advance the input position (all other side effects of
 * [p] are retained).
 */
inline fun Grammar.ahead (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val result = ignore_errors_if_successful(p)
    pos = pos0
    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds if [p] succeeds, but does produce any side effect (does not even change the input
 * position).
 */
inline fun Grammar.ahead_pure (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    val result = ignore_errors_if_successful(p)
    undo(pos0, ptr0)
    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * Succeeds only if [p] fails.
 */
inline fun Grammar.not (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    val result = ignore_errors(p)
    if (result) {
        undo(pos0, ptr0)
        fail(pos0, BadMatch)
    }
    return !result
}

// -------------------------------------------------------------------------------------------------
