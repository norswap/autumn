package norswap.autumn.naive

import norswap.autumn.parsers.*

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that match bracketed content and comma-separated content.

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by [left] and [right]. Both brackets are [word]s.
 */
class Brackets (val left: String, val right: String, val p: Parser): Parser()
{
    override fun invoke() = grammar.brackets(left, right, p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by angle brackets. Uses [word] for matching the brackets.
 */
class Angles (val p: Parser): Parser()
{
    override fun invoke() = grammar.angles(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of angles brackets, potentially separated and/or followed by whitespace.
 */
class AnglesEmpty(): Parser()
{
    override fun invoke() = grammar.angles()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by square brackets. Uses [word] for matching the brackets.
 */
class Squares (val p: Parser): Parser()
{
    override fun invoke() = grammar.squares(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of square brackets, potentially separated and/or followed by whitespace.
 */
class SquaresEmpty(): Parser()
{
    override fun invoke() = grammar.squares()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by curly brackets. Uses [word] for matching the brackets.
 */
class Curlies (val p: Parser): Parser()
{
    override fun invoke() = grammar.curlies(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of curly brackets, potentially separated and/or followed by whitespace.
 */
class CurliesEmpty(): Parser()
{
    override fun invoke() = grammar.curlies()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by parens. Uses [word] for matching the parens.
 */
class Parens (val p: Parser): Parser()
{
    override fun invoke() = grammar.parens(p)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of parens, potentially separated and/or followed by whitespace.
 */
class ParensEmpty(): Parser()
{
    override fun invoke() = grammar.parens()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a possibly-empty comma-separated list of [item]. Uses [word] to match the commas.
 */
class CommaList0 (val item: Parser): Parser()
{
    override fun invoke() = grammar.comma_list0(item)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a non-empty comma-separated list of [item]. Uses [word] to match the commas.
 */
class CommaList1 (val item: Parser): Parser()
{
    override fun invoke() = grammar.comma_list1(item)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a possibly-empty comma-separated list of [item]. Uses [word] to match the commas.
 * An additional comma is allowed at the end.
 */
class CommaListTerm0 (val item: Parser): Parser()
{
    override fun invoke() = grammar.comma_list_term0(item)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a non-empty comma-separated list of [item]. Uses [word] to match the commas.
 * An additional comma is allowed at the end.
 */
class CommaListTerm1 (val item: Parser): Parser()
{
    override fun invoke() = grammar.comma_list1(item)
}

// -------------------------------------------------------------------------------------------------