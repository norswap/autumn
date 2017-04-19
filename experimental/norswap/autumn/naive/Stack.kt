package norswap.autumn.naive

import norswap.autumn.Grammar

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
class Affect (
        backlog: Int,
        val syntax: Parser,
        val effect: Grammar.(Array<Any?>) -> Unit)
    : Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Like [affect], with no backlog.
 */
class Affect (
        val syntax: Parser,
        val effect: Grammar.(Array<Any?>) -> Unit)
    : Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [syntax], then calls [effect], passing it a string containing the matched text.
 */
class Affect_str (
        val syntax: Parser,
        val effect: Grammar.(String) -> Unit)
    : Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
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
class Build (
        backlog: Int,
        val syntax: Parser,
        val effect: Grammar.(Array<Any?>) -> Any)
    : Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Like [build], with no backlog.
 */
class Build (
        val syntax: Parser,
        val effect: Grammar.(Array<Any?>) -> Any): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [syntax], then calls [value], passing it a string containing the matched text.
 * The return value of [value] is pushed on the stack.
 */
class Build_str (
        val syntax: Parser,
        val value: Grammar.(String) -> Any)
    : Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Like [build_str], but the string is directly pushed on the stack instead of being passed to
 * a function.
 */
class Build_str (val syntax: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] or, if [p] fails, pushes `null` on the stack.
 * Always succeeds.
 */
class Maybe (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Attempts to match [p], then pushes `true` on the stack if successful, `false` otherwise.
 * Also discards its stack frame.
 * Always suceeds.
 */
class As_bool (val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] then pushes [value] on the stack if successful.
 */
class As_val (value: Any?, val p: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches all characters until [terminator] (also matched).
 *
 * All characters matched in this manner (excluding [terminator]) are collected in a string
 * which is pushed on the value stack.
 */
class Gobble (val terminator: Parser): Parser()
{
    override fun invoke(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

// -------------------------------------------------------------------------------------------------