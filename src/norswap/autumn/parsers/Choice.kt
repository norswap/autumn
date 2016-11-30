package norswap.autumn.parsers
import norswap.autumn.Change
import norswap.autumn.Grammar
import norswap.autumn.Parser

// -------------------------------------------------------------------------------------------------

/**
 * choice { p1() || p2() }
 */
inline fun Grammar.choice (crossinline p: Parser): Boolean
{
    return transact(p)
}

// -------------------------------------------------------------------------------------------------

fun Grammar.longest (vararg ps: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size

    var max_pos = -1
    var max_delta = emptyList<Change>()

    for (p in ps) {
        val result = p()
        if (result) {
            if (pos > max_pos) {
                max_pos = pos
                max_delta = diff(ptr0)
            }
            undo(pos0, ptr0)
        }
    }

    return if (max_pos > -1) {
        merge(max_pos, max_delta)
        true
    }
    else false
}

// -------------------------------------------------------------------------------------------------