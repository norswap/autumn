package norswap.autumn.parsers
import norswap.autumn.*

// -------------------------------------------------------------------------------------------------
/*

This file contains parser combinators that act on [Grammar.stack].

 */
// -------------------------------------------------------------------------------------------------

inline fun Grammar.affect (backlog: Int, syntax: Parser, effect: Grammar.(Array<Any?>) -> Unit): Boolean
{
    val frame = frame_start(backlog)
    val result = syntax()
    if (result) {
        effect(frame_end(frame))
    }
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.affect (syntax: Parser, effect: Grammar.(Array<Any?>) -> Unit): Boolean
{
    return affect(0, syntax, effect)
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build (backlog: Int, syntax: Parser, effect: Grammar.(Array<Any?>) -> Any): Boolean
{
    return affect(backlog, syntax) { stack.push(effect(it)) }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build (syntax: Parser, effect: Grammar.(Array<Any?>) -> Any): Boolean
{
    return build(0, syntax, effect)
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build_str (syntax: Parser, value: Grammar.(String) -> Any): Boolean
{
    val pos0 = pos
    val result = syntax()
    if (result) {
        stack.push(value(text.substring(pos0, pos)))
    }
    return result
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.build_str (syntax: Parser): Boolean
{
    return build_str (syntax) { it }
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.maybe (crossinline p: Parser): Boolean
{
    if (!p()) stack.push(null)
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.as_bool (crossinline p: Parser): Boolean
{
    val frame = frame_start()
    val result = p()
    frame_end(frame)
    stack.push(result)
    return true
}

// -------------------------------------------------------------------------------------------------

inline fun Grammar.as_val (value: Any?, crossinline p: Parser): Boolean
{
    val result = p()
    if (result) stack.push(value)
    return result
}

// -------------------------------------------------------------------------------------------------