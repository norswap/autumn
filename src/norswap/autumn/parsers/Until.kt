package norswap.autumn.parsers
import norswap.autumn.EarlyTermination
import norswap.autumn.Grammar
import norswap.autumn.Parser

// -------------------------------------------------------------------------------------------------

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
