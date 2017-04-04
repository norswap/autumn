package norswap.autumn.parsers
import norswap.autumn.*

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that match bracketed content and comma-separated content.

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by [left] and [right]. Both brackets are [word]s.
 */
inline fun Grammar.brackets(left: String, right: String, crossinline p: Parser): Boolean
    = seq { word(left) && p() && word(right) }

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by angle brackets. Uses [word] for matching the brackets.
 */
inline fun Grammar.angles (crossinline p: Parser): Boolean
    = brackets("<", ">", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of angles brackets, potentially separated and/or followed by whitespace.
 */
fun Grammar.angles(): Boolean
    = angles { true }

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by square brackets. Uses [word] for matching the brackets.
 */
inline fun Grammar.squares (crossinline p: Parser): Boolean
    = brackets("[", "]", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of square brackets, potentially separated and/or followed by whitespace.
 */
fun Grammar.squares(): Boolean
    = squares { true }

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by curly brackets. Uses [word] for matching the brackets.
 */
inline fun Grammar.curlies (crossinline p: Parser): Boolean
    = brackets("{", "}", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of curly brackets, potentially separated and/or followed by whitespace.
 */
fun Grammar.curlies(): Boolean
    = curlies { true }

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by parens. Uses [word] for matching the parens.
 */
inline fun Grammar.parens (crossinline p: Parser): Boolean
    = brackets("(", ")", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches an empty set of parens, potentially separated and/or followed by whitespace.
 */
fun Grammar.parens(): Boolean
    = parens { true }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a possibly-empty comma-separated list of [item]. Uses [word] to match the commas.
 */
inline fun Grammar.comma_list0 (crossinline item: Parser): Boolean
    = around0(item) { word(",") }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a non-empty comma-separated list of [item]. Uses [word] to match the commas.
 */
inline fun Grammar.comma_list1 (crossinline item: Parser): Boolean
    = around1(item) { word(",") }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a possibly-empty comma-separated list of [item]. Uses [word] to match the commas.
 * An additional comma is allowed at the end.
 */
inline fun Grammar.comma_list_term0 (crossinline item: Parser): Boolean
    = list_term0 (item) { word(",") }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a non-empty comma-separated list of [item]. Uses [word] to match the commas.
 * An additional comma is allowed at the end.
 */
inline fun Grammar.comma_list_term1 (crossinline item: Parser): Boolean
    = list_term1 (item) { word(",") }

// -------------------------------------------------------------------------------------------------