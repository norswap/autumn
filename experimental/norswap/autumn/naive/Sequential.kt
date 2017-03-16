package norswap.autumn.naive
import norswap.autumn.EarlyTermination
import norswap.autumn.parsers.*

// -------------------------------------------------------------------------------------------------

class Seq (val ps: List<Parser>): Parser()
{
    override fun invoke() = grammar.seq { ps.all(Parser::invoke) }
}

// -------------------------------------------------------------------------------------------------

class Opt (val p: Parser): Parser()
{
    override fun invoke() = grammar.opt { p() }
}

// -------------------------------------------------------------------------------------------------

class Repeat0 (val p: Parser): Parser()
{
    override fun invoke() = grammar.repeat0 { p() }
}

// -------------------------------------------------------------------------------------------------

class Repeat1 (val p: Parser): Parser()
{
    override fun invoke() = grammar.repeat1 { p() }
}

// -------------------------------------------------------------------------------------------------

class Repeat (val n: Int, val p: Parser): Parser()
{
    override fun invoke() = grammar.repeat(n) { p() }
}

// -------------------------------------------------------------------------------------------------

class Around0 (val around: Parser, val inside: Parser): Parser()
{
    override fun invoke() = grammar.around0( {around()} , {inside()} )
}

// -------------------------------------------------------------------------------------------------

class Around1 (val around: Parser, val inside: Parser): Parser()
{
    override fun invoke() = grammar.around1( {around()} , {inside()} )
}

// -------------------------------------------------------------------------------------------------

class Until0 (val repeat: Parser, val terminator: Parser): Parser()
{
    override fun invoke() = grammar.until0( {repeat()} , {terminator()} )
}

// -------------------------------------------------------------------------------------------------

class Until1 (val repeat: Parser, val terminator: Parser): Parser()
{
    override fun invoke() = grammar.until1( {repeat()} , {terminator()} )
}

// -------------------------------------------------------------------------------------------------