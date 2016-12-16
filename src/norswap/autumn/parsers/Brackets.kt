package norswap.autumn.parsers
import norswap.autumn.*

// -------------------------------------------------------------------------------------------------
/*

This file contains constructors for parsers that match bracketed content and comma-separated lists.

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by [left] and [right]. Both brackets are [word]s.
 */
inline fun Grammar.brackets(left: String, right: String, crossinline p: Parser)
    = seq { word(left) && p() && word(right) }

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by angle brackets. Uses [word] for matching the brackets.
 */
inline fun Grammar.angles (crossinline p: Parser)
    = brackets("<", ">", p)

// -------------------------------------------------------------------------------------------------

/**
 *Matches [p] bracketed by square brackets. Uses [word] for matching the brackets.
 */
inline fun Grammar.squares (crossinline p: Parser)
    = brackets("[", "]", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by curly brackets. Uses [word] for matching the brackets.
 */
inline fun Grammar.curlies (crossinline p: Parser)
    = brackets("{", "}", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches [p] bracketed by parens. Uses [word] for matching the parens.
 */
inline fun Grammar.parens (crossinline p: Parser)
    = brackets("(", ")", p)

// -------------------------------------------------------------------------------------------------

/**
 * Matches a possibly-empty comma-separated list of [item]. Uses [word] to match the commas.
 */
inline fun Grammar.comma_list0 (crossinline item: Parser)
    = around0(item) { word(",") }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a non-empty comma-separated list of [item]. Uses [word] to match the commas.
 */
inline fun Grammar.comma_list1 (crossinline item: Parser)
    = around1(item) { word(",") }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a possibly-empty comma-separated list of [item]. Uses [word] to match the commas.
 * An additional comma is allowed at the end.
 */
inline fun Grammar.comma_list_term0 (crossinline item: Parser)
    = seq { around0(item) { word(",") } && opt { word(",") } }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a non-empty comma-separated list of [item]. Uses [word] to match the commas.
 * An additional comma is allowed at the end.
 */
inline fun Grammar.comma_list_term1 (crossinline item: Parser)
    = seq { around1(item) { word(",") } && opt { word(",") } }

// -------------------------------------------------------------------------------------------------