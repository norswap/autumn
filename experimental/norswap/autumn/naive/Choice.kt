package norswap.autumn.naive

import norswap.autumn.parsers.*
import norswap.autumn.Grammar

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that perform a choice between their sub-parsers.

*/
// -------------------------------------------------------------------------------------------------

/**
 * [p] must of the form `p1() || p2() || ...`
 * e.g. `choice { string("hello") || string("goodbye") }`
 *
 * Matches the same things as the first parser in the list that matches, or fails if none succeeds.
 */
class Choice (val ps: List<Parser>): Parser()
{
    override fun invoke() = grammar.choice { ps.any(Parser::invoke) }
}

// -------------------------------------------------------------------------------------------------

/**
 * `longest(a, b)` is syntactic sugar for `Longest(this, arrayOf(a, b)`.
 */
@Suppress("UNCHECKED_CAST")
class Longest(vararg val parsers: Parser): Parser()
{
    val longest = grammar.longest(*parsers)
    override fun invoke() = longest.invoke()
}

// -------------------------------------------------------------------------------------------------

/**
 * `longest_pure(a, b)` is syntactic sugar for `LongestPure(this, arrayOf(a, b)`.
 */
@Suppress("UNCHECKED_CAST")
class LongestPure(vararg val parsers: Parser): Parser()
{
    val longest_pure = grammar.longest_pure(*parsers)
    override fun invoke() = longest_pure.invoke()
}
// -------------------------------------------------------------------------------------------------