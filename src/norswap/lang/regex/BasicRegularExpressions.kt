package norswap.lang.regex
import norswap.autumn.Grammar
import norswap.autumn.Parser
import norswap.autumn.parsers.*

// =================================================================================================

/**
 * Parses simplified POSIX Basic Regular Expressions:
 * - no collation support or equivalence classes
 * - no multiple character ranges
 *
 * http://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap09.html
 */
class BasicRegularExpressions: Grammar()
{
    fun character_class_name() = build_str {
        choice {
            string("alnum") ||
            string("alpha") ||
            string("blank") ||
            string("cntrl") ||
            string("digit") ||
            string("graph") ||
            string("lower") ||
            string("print") ||
            string("punct") ||
            string("space") ||
            string("upper") ||
            string("xdigit")
    }   }

    fun character_class() = build(
        syntax = { seq { string("[:") && character_class_name() && string(":]") } },
        effect = { Class(it(0)) })

    fun special_char()
        = char_set(".\\[^$*")

    fun regular_char() = build_str (
        syntax = { seq { not { special_char() } && char_any() } },
        value  = { Character(it[0]) })

    fun dot() = build (
        syntax = { string(".") },
        effect = { Dot })

    fun quoted_char() = build_str(
        syntax = { seq { string("\\") && special_char() } },
        value  = { QuotedChar(it[1]) })

    fun number() = build_str (
        syntax = { repeat1 { digit() } },
        value  = { it.toInt() })

    fun back_reference() = build (
        syntax = { seq { string("\\") && number() } },
        effect = { BackReference(it(0)) }
    )

    inline fun bracketed (crossinline p: Parser)
        = seq { string("[") && as_bool { string("^") } && string("]") }

    fun non_closing()
        = seq { not { string("]") } && char_any() }

    fun bracket_list() = build(1,
        syntax = { build_str { seq { opt { string("]") } && repeat0 { non_closing() } } } },
        effect = { CharacterSet(it(0), it(1)) })

    fun bracket_range() = build(1,
        syntax = { build_str { seq { char_any() && string("-") && non_closing() } } },
        effect = { CharacterRange(it(0), it<String>(1)[0], it<String>(2)[2]) })

    fun bracket_content()
        = choice { bracket_range() || bracket_list() }

    fun bracket_expr()
        = bracketed { as_bool { string("^") && bracket_content() } }

    fun paren_group() = build(
        syntax = { seq { string("\\(") && anchoring() && string("\\)") } },
        effect = { SubExpression(it(0)) })

    fun atom(): Boolean
        = choice { regular_char() || dot() || quoted_char() || back_reference() ||
                   character_class() || bracket_expr() || paren_group() }

    inline fun repg (crossinline p: Parser)
        = seq { string("\\{") && p() && string("\\}") }

    fun repetition_group() = build(1,
        syntax = { repg { number() && as_bool { string(",") } && maybe { number() } } },
        effect = { Repetition(it(1), if (it(2)) it[3] as Int? ?: -1 else it(1), it(0)) })

    fun star_suffix() = build (1,
        syntax = { string("*") },
        effect = { StarRepetition(it(0)) })

    fun repetition_suffix()
        = choice { star_suffix() || repetition_group() }

    fun concatenation() = build(
        syntax = { repeat0 { atom() && opt { repetition_suffix() } } },
        effect = { Concatenation(it.list()) })

    fun anchoring() = build(
        syntax = { seq { as_bool { string("^") } && concatenation() && as_bool { string("$") } } },
        effect = { Anchoring(it(0), it(2), it(1)) })

    override fun root() =
        anchoring()
}

// =================================================================================================

interface Node
interface Regex: Node
data class Anchoring (val left: Boolean, val right: Boolean, val concat: Concatenation): Regex
data class Concatenation (val items: List<Node>): Regex
data class Repetition (val min: Int, val max: Int, val item: Node): Regex
data class StarRepetition (val item: Node): Regex
data class SubExpression (val anchoring: Anchoring): Regex
data class Character (val char: Char): Regex
data class QuotedChar (val char: Char): Regex
object Dot: Regex
data class Class (val name: String): Regex
data class CharacterSet (val negated: Boolean, val chars: String): Regex
data class CharacterRange (val negated: Boolean, val start: Char, val stop: Char): Regex
data class BackReference (val num: Int): Regex

// =================================================================================================

fun diagnose (grammar: Grammar, input: String)
{
    if (grammar.parse(input)) {
        println(grammar.stack.peek())
    }
    else
        println("failure: " + grammar.failure?.invoke())

    grammar.reset()
}

fun main (args: Array<String>)
{
    val grammar = BasicRegularExpressions()
    // diagnose(grammar, "a")
    // diagnose(grammar, ".")
    // diagnose(grammar, "[") // fail
    // diagnose(grammar, "]")
    // diagnose(grammar, "^a$")
    // diagnose(grammar, "^")
    // diagnose(grammar, "$")
    // diagnose(grammar, "\\$")
    // diagnose(grammar, "\\^")
    // diagnose(grammar, "[:alpha:]")
    // diagnose(grammar, "\\(abc\\)")
    // diagnose(grammar, "\\(^abc$\\)")
    // diagnose(grammar, "\\(^\\)")
    // diagnose(grammar, "a*")
    // diagnose(grammar, "a\\{2\\}")
    // diagnose(grammar, "a\\{2,\\}")
    // diagnose(grammar, "a\\{2,3\\}")
    // diagnose(grammar, "\\(abc\\)*")
    // diagnose(grammar, "\\1")
    // diagnose(grammar, "ab.c[:alpha:]*d\\{4\\}\\1")
}