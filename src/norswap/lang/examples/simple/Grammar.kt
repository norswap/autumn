package norswap.lang.examples.simple
import norswap.autumn.Grammar
import norswap.autumn.Parser
import norswap.autumn.parsers.*

class SimpleGrammar: Grammar()
{
    fun integer() = build_str(
        syntax = { repeat1 { digit() } },
        value  = { Integer.valueOf(it) })

    fun identifier() = build_str(
        syntax = { repeat1 { alpha() } },
        value  = { it })

    inline fun parens (crossinline p: Parser)
        = seq { +"(" && p() && +")" }

    fun parenthesized()
        = parens { add_expr() }

    fun atom()
        = choice { parenthesized() || integer() || identifier() }

    val mult_expr = PrecedenceLeft {
        higher { integer() }
        op(2, { +"*" }) { Product  (it(0), it(1)) }
        op(2, { +"/" }) { Division (it(0), it(1)) }
    }

    val add_expr = PrecedenceLeft {
        higher (mult_expr)
        op(2, { +"+" },  { Sum  (it(0), it(1)) })
        op(2, { +"-" },  { Diff (it(0), it(1)) })
    }

    fun print() = build(
        syntax = { seq { +"print" && parens { add_expr() } } },
        effect = { Print(it(0)) })

    fun assignment() = build(
        syntax = { seq { identifier() && +"=" && add_expr() } },
        effect = { Assignment(it(0), it(1)) })

    fun statement()
        = choice { print() || assignment() }

    override fun root() = build (
        syntax = { around0 ( { statement() } , { +";" } ) },
        effect = { it.list<Any>() })
}