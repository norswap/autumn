package norswap.lang.examples.trivial
import norswap.autumn.Grammar
import norswap.autumn.parsers.*

class RegexGrammar: Grammar()
{
    fun meta_char()
        = char_set("|*+?()\\")

    fun regular_char()
        = seq { not { meta_char() } && char_any() }

    fun quoted_char()
        = seq { string("\\") && char_any() }

    fun character()
        = choice { quoted_char() || regular_char() }

    fun paren_group(): Boolean
        = seq { string("(") && alternation() && string(")") }

    fun atom()
        = choice { paren_group() || character() }

    fun repetition_char()
        = char_set("*+?")

    fun repetition()
        = seq { atom() && repeat0 { repetition_char() } }

    fun concatenation()
        = repeat1 { repetition() }

    fun alternation()
        = around1 ({ concatenation() }, { string("|") })

    override fun root() =
        alternation()
}

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
    val grammar = RegexGrammar()
    diagnose(grammar, "a(bb)+a|b(cc)*b") // success
    diagnose(grammar, "(xx") // failure: unexpected character
}