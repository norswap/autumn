package norswap.autumn.naive

import norswap.autumn.parsers.*
import norswap.autumn.Grammar

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that match at the character level.

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches any character that satisfied [pred].
 */
class CharPred (val pred: (Char) -> Boolean): Parser()
{
    override fun invoke() = grammar.char_pred(pred)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any character.
 * Only fails when the end of the input (represented by the null byte) is reached.
 */
class CharAny(): Parser()
{
    override fun invoke() = grammar.char_any()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any character in the range between [start] and [end].
 */
class CharRange (val start: Char, val end: Char): Parser()
{
    override fun invoke() = grammar.char_range(start, end)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any of the characters in [chars].
 */
class CharSet (val chars: String): Parser()
{
    constructor (vararg c: Char): this(String(c))
    override fun invoke() = grammar.char_set(chars)
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [str].
 */
class Str (val str: String): Parser()
{
    override fun invoke() = grammar.string(str)
}
// -------------------------------------------------------------------------------------------------

/**
 * Matches [str], and any trailing whitespace (as defined by [Grammar.whitespace]).
 */
class WordString (val str: String): Parser()
{
    override fun invoke() = grammar.word(str)
}

class WordParser (val p: Parser): Parser()
{
    override fun invoke() = grammar.word { p() }
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an alphabetic character (the ranges a-z and A-Z).
 */
class Alpha(): Parser()
{
    override fun invoke() = grammar.alpha()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an alphanumeric character (the ranges a-z, A-Z and 0-9).
 */
class Alphanum(): Parser()
{
    override fun invoke() = grammar.alphanum()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a digit (the range 0-9).
 */
class Digit(): Parser()
{
    override fun invoke() = grammar.digit()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an hexadecimal digit (the ranges a-f, A-F and 0-9).
 */
class HexDigit(): Parser()
{
    override fun invoke() = grammar.hex_digit()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an octal digit (the range 0-7).
 */
class OctalDigit(): Parser()
{
    override fun invoke() = grammar.octal_digit()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a whitespace character, as defined by [Char.isWhitespace].
 */
class SpaceChar(): Parser()
{
    override fun invoke() = grammar.space_char()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a java identifier (as defined by JLS 3.8).
 */
class JavaIden(): Parser()
{
    override fun invoke() = grammar.java_iden()
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches a java identifier that consists (as defined by JLS 3.8) that consists only of
 * ASCII characters.
 */
class AsciiJavaIden(): Parser()
{
    override fun invoke() = grammar.ascii_java_iden()
}

// -------------------------------------------------------------------------------------------------
