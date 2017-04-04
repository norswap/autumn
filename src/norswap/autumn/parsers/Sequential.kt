package norswap.autumn.parsers
import norswap.autumn.EarlyTermination
import norswap.autumn.Grammar
import norswap.autumn.Parser

// -------------------------------------------------------------------------------------------------

/**
 * [p] must of the form `p1() && p2() && ...`
 * e.g. `seq { word("hello") && word("world") }`
 *
 * Matches all the parsers in a sequence.
 */
inline fun Grammar.seq (crossinline p: Parser): Boolean
{
    return transact(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] if it suceeds, otherwise succeeds without consuming any input.
 */
inline fun Grammar.opt (crossinline p: Parser): Boolean
{
    p()
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches 0 or more (sequential) repetition of [p].
 */
inline fun Grammar.repeat0 (crossinline p: Parser): Boolean
{
    while (p()) ;
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches 1 or more (sequential) repetition of [p].
 */
inline fun Grammar.repeat1 (crossinline p: Parser): Boolean
{
    if (!p()) return false
    while (p()) ;
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches exactly [n] (sequential) repetitions of [p].
 */
inline fun Grammar.repeat (n: Int, crossinline p: Parser): Boolean
{
    return transact b@{
        for (i in 1..n)
            if (!p()) return@b false
        true
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches 0 or more repetitions of [around], separated from one another by input matching [inside].
 */
inline fun Grammar.around0 (crossinline around: Parser, crossinline inside: Parser): Boolean
{
    var r = around()
    while (r)
        r = seq { inside() && around() }
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches 1 or more repetitions of [around], separated from one another by input matching [inside].
 */
inline fun Grammar.around1 (crossinline around: Parser, crossinline inside: Parser): Boolean
{
    var r = around()
    if (!r) return false
    while (r)
        r = seq { inside() && around() }
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches 0 or more repetitions of [around], separated from one another by input matching [inside],
 * optionally followed by input matching [inside].
 */
inline fun Grammar.list_term0 (crossinline around: Parser, crossinline inside: Parser): Boolean
{
    var r = around()
    while (r)
        r = seq { inside() && around() }
    inside()
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches 1 or more repetitions of [around], separated from one another by input matching [inside],
 * optionally followed by input matching [inside].
 */
inline fun Grammar.list_term1 (crossinline around: Parser, crossinline inside: Parser): Boolean
{
    var r = around()
    if (!r) return false
    while (r)
        r = seq { inside() && around() }
    inside()
    return true
}

// -------------------------------------------------------------------------------------------------


/**
 * Matches 0 or more repetition of [repeat], followed by [terminator].
 *
 * In case of ambiguity, [terminator] is matched in preference to [repeat]
 * (this is what makes this different from `seq { repeat0(repeat) && terminator() }`).
 */
inline fun Grammar.until0 (crossinline repeat: Parser, crossinline terminator: Parser): Boolean
{
    return transact b@ {
        while (true) {
            val r1 = terminator()
            if (r1) return@b true
            val r2 = repeat()
            if (!r2) break
        }
        false
    }
}

// -------------------------------------------------------------------------------------------------


/**
 * Matches 1 or more repetition of [repeat], followed by [terminator].
 *
 * In case of ambiguity, [terminator] is matched in preference to [repeat]
 * (this is what makes this different from `seq { repeat1(repeat) && terminator() }`).
 */
inline fun Grammar.until1 (crossinline repeat: Parser, crossinline terminator: Parser): Boolean
{
    return transact b@ {
        val pos0 = pos
        var some = false
        while (true) {
            val r1 = terminator()
            if (r1) {
                if (!some) fail(pos0, EarlyTermination)
                return@b some
            }
            val r2 = repeat()
            if (!r2) break
            some = true
        }
        false
    }
}

// -------------------------------------------------------------------------------------------------