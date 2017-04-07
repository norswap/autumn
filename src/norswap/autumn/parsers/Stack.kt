package norswap.autumn.parsers
import norswap.autumn.*

// -------------------------------------------------------------------------------------------------
/*

This file contains parser combinators that act on [Grammar.stack].

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches [syntax], then call [effect], passing it an array containing everything pushed on the
 * stack since the parser's invocation, to which [backlog] items of backlog have been prepended.
 * All these items are removed from the stack.
 *
 * Insufficient items to satisfy the backlog requirement will the cause the parser to fail with
 * an execption.
 */
inline fun Grammar.affect (
    backlog: Int,
    crossinline syntax: Parser,
    crossinline effect: Grammar.(Array<Any?>) -> Unit)
    : Boolean
{
    val frame = frame_start(backlog)
    val result = syntax()
    if (result) {
        effect(frame_end(frame))
    }
    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * Like [affect], with no backlog.
 */
inline fun Grammar.affect (
    crossinline syntax: Parser,
    crossinline effect: Grammar.(Array<Any?>) -> Unit)
    : Boolean
{
    return affect(0, syntax, effect)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [syntax], then calls [effect], passing it a string containing the matched text.
 */
inline fun Grammar.affect_str (
    crossinline syntax: Parser,
    crossinline effect: Grammar.(String) -> Unit)
    : Boolean
{
    val pos0 = pos
    val result = syntax()
    if (result) effect(text.substring(pos0, pos))
    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [syntax], then calls [effect], passing it an array containing everything pushed on the
 * stack since the parser's invocation, to which [backlog] items of backlog have been prepended.
 * All these items are removed from the stack. The return value of [effect] is itself pushed on the
 * stack.
 *
 * Insufficient items to satisfy the backlog requirement will the cause the parser to fail with
 * an execption.
 */
inline fun Grammar.build (
    backlog: Int,
    crossinline syntax: Parser,
    crossinline effect: Grammar.(Array<Any?>) -> Any)
    : Boolean
{
    return affect(backlog, syntax) { stack.push(effect(it)) }
}

// -------------------------------------------------------------------------------------------------

/**
 * Like [build], with no backlog.
 */
inline fun Grammar.build (
    crossinline syntax: Parser,
    crossinline effect: Grammar.(Array<Any?>) -> Any): Boolean
{
    return build(0, syntax, effect)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [syntax], then calls [value], passing it a string containing the matched text.
 * The return value of [value] is pushed on the stack.
 */
inline fun Grammar.build_str (
    crossinline syntax: Parser,
    crossinline value: Grammar.(String) -> Any)
    : Boolean
{
    return affect_str(syntax) { stack.push(value(it)) }
}

// -------------------------------------------------------------------------------------------------

/**
 * Like [build_str], but the string is directly pushed on the stack instead of being passed to
 * a function.
 */
inline fun Grammar.build_str (crossinline syntax: Parser): Boolean
{
    return build_str (syntax) { it }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] or, if [p] fails, pushes `null` on the stack.
 * Always succeeds.
 */
inline fun Grammar.maybe (crossinline p: Parser): Boolean
{
    if (!p()) stack.push(null)
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Attempts to match [p], then pushes `true` on the stack if successful, `false` otherwise.
 * Also discards its stack frame.
 * Always suceeds.
 */
inline fun Grammar.as_bool (crossinline p: Parser): Boolean
{
    val frame = frame_start()
    val result = p()
    frame_end(frame)
    stack.push(result)
    return true
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] then pushes [value] on the stack if successful.
 */
inline fun Grammar.as_val (value: Any?, crossinline p: Parser): Boolean
{
    val result = p()
    if (result) stack.push(value)
    return result
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches all characters until [terminator] (also matched).
 *
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