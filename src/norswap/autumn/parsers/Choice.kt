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