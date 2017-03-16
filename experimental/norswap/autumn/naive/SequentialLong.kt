@file:Suppress("PackageDirectoryMismatch")
package norswap.autumn.naive.long
import norswap.autumn.naive.*
import norswap.autumn.EarlyTermination
import norswap.autumn.parsers.transact

// -------------------------------------------------------------------------------------------------

class Seq (val ps: List<Parser>): Parser()
{
    override fun invoke() = grammar.transact { ps.all(Parser::invoke) }
}

// -------------------------------------------------------------------------------------------------

class Opt (val p: Parser): Parser()
{
    override fun invoke() = p() || true
}

// -------------------------------------------------------------------------------------------------

class Repeat0 (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        while (p()) ;
        return true
    }
}

// -------------------------------------------------------------------------------------------------

class Repeat1 (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        if (!p()) return false
        while (p()) ;
        return true
    }
}

// -------------------------------------------------------------------------------------------------

class Repeat (val n: Int, val p: Parser): Parser()
{
    override fun invoke() = grammar.transact b@{
        for (i in 1..n)
            if (!p()) return@b false
        true
    }
}

// -------------------------------------------------------------------------------------------------

class Around0 (val around: Parser, val inside: Parser): Parser()
{
    override fun invoke(): Boolean {
        var r = around()
        while (r)
            r = grammar.transact { inside() && around() }
        return true
    }
}

// -------------------------------------------------------------------------------------------------

class Around1 (val around: Parser, val inside: Parser): Parser()
{
    override fun invoke(): Boolean {
        var r = around()
        if (!r) return false
        while (r)
            r = grammar.transact { inside() && around() }
        return true
    }
}

// -------------------------------------------------------------------------------------------------

class Until0 (val repeat: Parser, val terminator: Parser): Parser()
{
    override fun invoke() = grammar.transact b@ {
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

class Until1 (val repeat: Parser, val terminator: Parser): Parser()
{
    override fun invoke() = grammar.transact b@ {
        val pos0 = grammar.pos
        var some = false
        while (true) {
            val r1 = terminator()
            if (r1) {
                if (!some) grammar.fail(pos0, EarlyTermination)
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