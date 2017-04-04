package norswap.autumn.parsers
import norswap.autumn.*

// ---------------------------------------------------------------------------------------------

inline fun Grammar.transact (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    val out = p()
    if (!out) undo(pos0, ptr0)
    return out
}

// ---------------------------------------------------------------------------------------------

inline fun Grammar.ignore_errors (crossinline p: Parser): Boolean
{
    val pos0 = fail_pos
    val failure0 = failure
    val result = p()
    fail_pos = pos0
    failure = failure0
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.ignore_errors_if_successful (crossinline p: Parser): Boolean
{
    val pos0 = fail_pos
    val failure0 = failure
    val result = p()
    if (result) {
        fail_pos = pos0
        failure = failure0
    }
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.perform (f: Grammar.() -> Unit): Boolean
{
    f()
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.contain (noinline failure: () -> String, crossinline p: Parser): Boolean
{
    val fail_pos0 = fail_pos
    val result = p()
    if (!result && fail_pos0 < pos) fail_force(pos, failure)
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.transact_contain (noinline failure: () -> String, crossinline p: Parser): Boolean
{
    return contain(failure) { transact { p() } }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.catch (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    try {
        return p()
    }
    catch (e: Throwable) {
        undo(pos0, ptr0)
        if (e is AutumnLogicException) fail(pos, e.failure)
        else fail(pos, CaughtException(e))
        return false
    }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.catch_contain (crossinline p: Parser): Boolean
{
    val pos0 = pos
    val ptr0 = log.size
    val fail_pos0 = fail_pos
    try {
        return p()
    }
    catch (e: Throwable) {
        undo(pos0, ptr0)
        if (fail_pos0 < pos)
            if (e is AutumnLogicException)
                fail_force(pos, e.failure)
            else
                fail_force(pos, CaughtException(e))
        return false
    }
}

// -------------------------------------------------------------------------------------------------

fun Grammar.log (str: String): Boolean
{
    println(input.string(pos) + ": " + str)
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches all characters until [terminator] (also matched).
 * All characters matched in this manner (excluding [terminator]) are collected in a string
 * which is pushed on the value stack.
 */
inline fun Grammar.gobble (crossinline terminator: Parser): Boolean
{
    val pos0 = pos
    return transact b@ {
        while (true) {
            val pos1 = pos
            val r1 = terminator()
            if (r1) {
                stack.push(text.substring(pos0, pos1))
                return@b true
            }
            val r2 = char_any()
            if (!r2) break
        }
        false
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches input using [outer] then, if successful, calls [inner] with the matching input
 * and use the result as the result of the parse.
 */
inline fun Grammar.inner (crossinline outer: Parser, crossinline inner: (String) -> Boolean): Boolean
{
    return transact {
        val pos0 = pos
        val result = outer()
        if (!result) false
        else inner(text.substring(pos0, pos))
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches all characters until [terminator] (also matched).
 *
 * Then, if successful, all characters matched in this manner (excluding [terminator]) are collected
 * in a string, which is passed to [inner], whose result is the result of the parse.
 *
 * The exclusion of the terminator is what makes this different from [inner] and closer
 * to [gobble].
 */
inline fun Grammar.until_inner (crossinline terminator: Parser, crossinline inner: (String) -> Boolean): Boolean
{
    return transact {
        val result = gobble(terminator)
        if (!result) false
        else inner(stack.pop() as String)
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * A parser that matches the same thing as parsing [sub_grammar] with the remainder of the input
 * would. If successful, the [completion] function is called, passing it [sub_grammar].
 *
 * The default action for the completion function is to push the top of the value stack of the
 * sub-grammar on top on the value stack of the current grammar.
 */
class SubGrammar (
    val grammar: Grammar,
    val sub_grammar: Grammar,
    val completion: Grammar.(Grammar) -> Unit)
    : Parser
{
    override fun invoke(): Boolean
    {
        val pos0 = grammar.pos
        val text = grammar.text

        val remaining_input = object: CharSequence
        {
            override val length = text.length - pos0 - 1 // null terminator

            override fun get (index: Int) = text[pos0 + index]

            override fun subSequence (startIndex: Int, endIndex: Int)
                = text.subSequence(pos0 + startIndex, pos0 + endIndex)
        }

        val parse_input = ParseInput(remaining_input)
        val result = sub_grammar.parse(parse_input)
        if (result) {
            grammar.pos += sub_grammar.pos
            grammar.completion(sub_grammar)
        }
        sub_grammar.reset()
        return result
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * (Syntactic sugar for [SubGrammar])
 *
 * A parser that matches the same thing as parsing [sub_grammar] with the remainder of the input
 * would. If successful, the [completion] function is called, passing it [sub_grammar].
 *
 * The default action for the completion function is to push the top of the value stack of the
 * sub-grammar on top on the value stack of the current grammar.
 */
fun Grammar.sub_grammar (
    sub_grammar: Grammar,
    completion: Grammar.(Grammar) -> Unit = { stack.push(it.stack[0]) })
    : Parser
{
    return SubGrammar(this, sub_grammar, completion)
}

// -------------------------------------------------------------------------------------------------