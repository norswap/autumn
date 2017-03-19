package norswap.lang.examples.simple
import norswap.autumn.Grammar
import norswap.autumn.parsers.*

class SimpleGrammar: Grammar()
{
    fun integer() = word { build_str(
        syntax = { repeat1 { digit() } },
        value  = { Integer.valueOf(it) }) }

    fun identifier() = word { build_str(
        syntax = { repeat1 { alpha() } },
        value  = { it }) }

    fun parenthesized()
        = parens { add_expr() }

    fun atom(): Boolean
        = choice { parenthesized() || integer() || identifier() }

    val mult_expr = PrecedenceLeft {
        higher { atom() }
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
        syntax = { repeat0 { seq { statement() && +";" } } },
        effect = { it.list<Any>() })
}

// =================================================================================================

fun diagnose (grammar: Grammar, input: String)
{
    if (grammar.parse(input))
        println("success")
    else
        println("failure: " + grammar.failure?.invoke())

    grammar.reset()
}

fun main (args: Array<String>)
{
    val grammar = SimpleGrammar()
    //diagnose(grammar, "x;")
    diagnose(grammar, "x = 2;")                 // success
    diagnose(grammar, "y = 3;")                 // success
    diagnose(grammar, "z = (x + y) * (x + y);") // success
    diagnose(grammar, "print(z - 4);")          // success
}