package norswap.autumn.parsers
import norswap.autumn.Change
import norswap.autumn.Grammar
import norswap.autumn.Parser

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that perform a choice between their sub-parsers.

*/
// -------------------------------------------------------------------------------------------------

/**
 * [p] must of the form `p1() || p2() || ...`
 * e.g. `choice { string("hello") || string("goodbye") }`
 *
 * Matches the same things as the first parser in the list that matches, or fails if none succeeds.
 */
inline fun Grammar.choice (crossinline p: Parser): Boolean
{
    return transact(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches the same thing as the parser in [ps] that matches the most input.
 *
 * Side-effects are retained only for the parser that is selected.
 */
class Longest (val g: Grammar, val ps: Array<Parser>): Parser
{
    fun select(): Int
    {
        val pos0 = g.pos
        val ptr0 = g.log.size

        var max_pos = pos0
        var max_delta = emptyList<Change>()
        var max_i = -1

        for (i in ps.indices ) {
            val result = ps[i]()
            if (result) {
                if (g.pos > max_pos) {
                    max_pos = g.pos
                    max_i = i
                    max_delta = g.diff(ptr0)
                }
                g.undo(pos0, ptr0)
            }
        }

        if (max_i >= 0)
            g.merge(max_pos, max_delta)

        return max_i
    }

    override fun invoke(): Boolean
    {
        return select() >= 0
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * `longest(a, b)` is syntactic sugar for `Longest(this, arrayOf(a, b)`.
 */
@Suppress("UNCHECKED_CAST")
fun Grammar.longest(vararg parsers: Parser): Parser
    = Longest(this, parsers as Array<Parser>)

// -------------------------------------------------------------------------------------------------

/**
 * Matche the same things as the parser in [ps] that matches the most input.
 *
 * The parsers in [ps] should not have side-effects besides updating the input position.
 */
class LongestPure (val g: Grammar, val ps: Array<Parser>): Parser
{
    fun select(): Int
    {
        val pos0 = g.pos
        var max_pos = pos0
        var max_i = -1

        for (i in ps.indices ) {
            val result = ps[i]()
            if (result) {
                if (g.pos > max_pos) {
                    max_pos = g.pos
                    max_i = i
                }
                g.pos = pos0
            }
        }

        g.pos = max_pos
        return max_i
    }

    override fun invoke(): Boolean
    {
        return select() >= 0
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * `longest_pure(a, b)` is syntactic sugar for `LongestPure(this, arrayOf(a, b)`.
 */
@Suppress("UNCHECKED_CAST")
fun Grammar.longest_pure(vararg parsers: Parser): Parser
    = LongestPure(this, parsers as Array<Parser>)

// -------------------------------------------------------------------------------------------------