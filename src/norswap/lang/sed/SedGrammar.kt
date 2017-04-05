package norswap.lang.sed
import norswap.autumn.Grammar
import norswap.autumn.ParseInput
import norswap.autumn.Parser
import norswap.autumn.parsers.*
import norswap.lang.regex.BasicRegularExpressions
import norswap.lang.regex.Regex

// =================================================================================================

/**
 * Grammar for POSIX sed scripts.
 * (Incomplete: only includes the s command.)
 *
 * http://pubs.opengroup.org/onlinepubs/009695399/utilities/sed.html
 */
class SedGrammar: Grammar()
{
    fun blanks()
        = repeat1 { char_set(" \t") }

    fun number() = build_str (
        syntax = { repeat1 { digit() } },
        value  = { it.toInt() })

    fun number_address() = build(
        syntax = { number() },
        effect = { NumberAddress(it(0)) })

    fun last_address() = build(
        syntax = { string("$") },
        effect = { LastAddress })

    var delimiter: Char = '/'

    fun read_delim() = affect_str(
        syntax = { char_any() },
        effect = { delimiter = it[0] })

    fun delim()
        = char_pred { it == delimiter }

    inline fun delimited (crossinline f: (String) -> Boolean)
        = seq { read_delim() && until_inner ({ delim() } , f) }

    val REGEX_GRAMMAR
        = BasicRegularExpressions()

    val regex
        = sub_grammar_inner(REGEX_GRAMMAR)

    fun delimited_regex()
        = delimited { regex(it) }

    fun regex_address() = build(
        syntax = { seq { ahead { string("/") } && delimited_regex() } },
        effect = { RegexAddress(it(0)) })

    fun command_leading()
        = repeat0 { char_set(" \t;") }

    fun address()
        = choice { number_address() || last_address() || regex_address() }

    fun second_address_suffix() = build(1,
        syntax = { seq { string(",") && address () } },
        effect = { TwoAddresses(it(0), it(1)) })

    fun two_addr()
        = maybe { address() && opt { second_address_suffix() } }

    inline fun cmd0 (crossinline p: Parser)
        = seq { command_leading() && p() }

    inline fun cmd1 (crossinline p: Parser)
        = seq { command_leading() && maybe { address() } && p() }

    inline fun cmd2 (crossinline p: Parser)
        = seq { command_leading() && two_addr() && p() }

    fun s_num_flag() = build(
        syntax = { number() },
        effect = { SNumFlag(it(0)) })

    fun s_letter_flag() = build_str(
        syntax = { char_set("gp")},
        value  = { SLetterFlag(it[0]) })

    fun file()
        = false

    fun s_file_flag() = build(
        syntax = { string("w") && blanks() && file() },
        effect = { SFileFlag(it(0)) })

    fun s_flags() = build(
        syntax = { seq { repeat0 { choice { s_num_flag() || s_letter_flag() } } && opt { s_file_flag() } } },
        effect = { it.list<Flag>() })

    fun s_cmd() = build(
        syntax = { cmd2 { string("s") && delimited_regex() && gobble { delim() } && s_flags() } },
        effect = { SCommand(it(0), it(1), it(2), it(3)) })

    fun curly_cmd()
        = false

    fun b_cmd()
        = false

    fun c_cmd()
        = false

    fun d_cmd()
        = false

    fun D_cmd()
        = false

    fun i_cmd()
        = false

    fun I_cmd()
        = false

    fun n_cmd()
        = false

    fun N_cmd()
        = false

    fun p_cmd()
        = false

    fun P_cmd()
        = false

    fun q_cmd()
        = false

    fun r_cmd()
        = false

    fun t_cmd()
        = false

    fun w_cmd()
        = false

    fun x_cmd()
        = false

    fun y_cmd()
        = false

    fun pound_cmd()
        = true

    fun colon_cmd()
        = false

    fun command() = choice {
        curly_cmd() ||
        b_cmd() ||
        c_cmd() ||
        d_cmd() ||
        D_cmd() ||
        i_cmd() ||
        I_cmd() ||
        n_cmd() ||
        N_cmd() ||
        p_cmd() ||
        P_cmd() ||
        q_cmd() ||
        r_cmd() ||
        s_cmd() ||
        t_cmd() ||
        w_cmd() ||
        x_cmd() ||
        y_cmd() ||
        pound_cmd() ||
        colon_cmd()
    }

    fun separation()
        = repeat1 { choice { string("\n") || string(";") } }

    override fun root() = build(
        syntax = { list_term0 ({ command() } , { separation() }) },
        effect = { it.list<Command>() })
}

// =================================================================================================

interface Node
interface Address: Node
interface Command: Node

data class NumberAddress (val num: Int): Address
data class RegexAddress (val regex: Regex): Address
object LastAddress: Address
data class TwoAddresses (val a: Address, val b: Address): Address
interface Flag: Node
data class SNumFlag (val num: Int): Flag
data class SLetterFlag (val letter: Char): Flag
data class SFileFlag (val file: String): Flag

data class SCommand (
    val addr: Address?,
    val regex: Regex,
    val replacement: String,
    val flags: List<Flag>)
    : Command

// =================================================================================================

fun diagnose (grammar: Grammar, input: String, parser: Parser = { grammar.root() })
{
    if (grammar.parse(ParseInput(input), false, parser)) {
        println(grammar.stack.peek())
    }
    else
        println("failure: " + grammar.failure?.invoke())

    grammar.reset()
}

fun main (args: Array<String>)
{
    val grammar = SedGrammar()
    diagnose(grammar, "/hello/") { grammar.delimited_regex() }
    diagnose(grammar, "s/hello/test/") { grammar.s_cmd() }
}