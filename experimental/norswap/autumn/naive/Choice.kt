package norswap.autumn.naive
import norswap.autumn.parsers.*
import norswap.autumn.Grammar
import norswap.autumn.Parser

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
class Choice (val p: Parser): norswap.autumn.naive.Parser()
{
    override fun invoke() = grammar.choice { p() }
}

// -------------------------------------------------------------------------------------------------

/**
 * `longest(a, b)` is syntactic sugar for `Longest(this, arrayOf(a, b)`.
 */
@Suppress("UNCHECKED_CAST")
class Longest(vararg val parsers: Parser): norswap.autumn.naive.Parser()
{
    override fun invoke() = grammar.longest(*parsers).invoke()
}

// -------------------------------------------------------------------------------------------------

/**
 * `longest_pure(a, b)` is syntactic sugar for `LongestPure(this, arrayOf(a, b)`.
 */
@Suppress("UNCHECKED_CAST")
class Longest_pure(vararg val parsers: Parser): norswap.autumn.naive.Parser()
{
    override fun invoke() = grammar.longest_pure(*parsers).invoke()
}
// -------------------------------------------------------------------------------------------------