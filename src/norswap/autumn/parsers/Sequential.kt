package norswap.autumn.parsers
import norswap.autumn.Grammar
import norswap.autumn.Parser

// -------------------------------------------------------------------------------------------------

/**
 * seq { p1() && p2() }
 */
inline fun Grammar.seq (crossinline p: Parser): Boolean
{
    return transact(p)
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.opt (crossinline p: Parser): Boolean
{
    p()
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.repeat0 (crossinline p: Parser): Boolean
{
    while (p()) ;
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.repeat1 (crossinline p: Parser): Boolean
{
    if (!p()) return false
    while (p()) ;
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.repeat (n: Int, crossinline p: Parser): Boolean
{
    return transact b@{
        for (i in 1..n)
            if (!p()) return@b false
        true
    }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.around0 (crossinline around: Parser, crossinline inside: Parser): Boolean
{
    var r = around()
    while (r)
        r = seq { inside() && around() }
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.around1 (crossinline around: Parser, crossinline inside: Parser): Boolean
{
    var r = around()
    if (!r) return false
    while (r)
        r = seq { inside() && around() }
    return true
}

// -------------------------------------------------------------------------------------------------