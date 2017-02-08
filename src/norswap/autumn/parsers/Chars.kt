@file:Suppress("NOTHING_TO_INLINE")
package norswap.autumn.parsers
import norswap.autumn.*

// -------------------------------------------------------------------------------------------------
/*

This file contains parsers that match at the character level.

 */
// -------------------------------------------------------------------------------------------------

/**
 * Matches any character that satisfied [pred].
 */
inline fun Grammar.char_pred (pred: (Char) -> Boolean): Boolean
{
    val success = pred(text[pos])
    if (success) ++pos
    else fail(pos, UnexpectedChar)
    return success
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches any character.
 * Only fails when the end of the input (represented by the null byte) is reached.
 */
fun Grammar.char_any(): Boolean
    = char_pred { it != '\u0000' }

// -------------------------------------------------------------------------------------------------

/**
 * Matches any character in the range between [start] and [end].
 */
fun Grammar.char_range (start: Char, end: Char): Boolean
    = char_pred { it in start..end }

// -------------------------------------------------------------------------------------------------

/**
 * Matches any of the character in [chars].
 */
fun Grammar.char_set (vararg chars: Char): Boolean
    = char_pred { chars.contains(it) }

// -------------------------------------------------------------------------------------------------

/**
 * Matches any of the characters in [chars].
 */
fun Grammar.char_set (chars: String): Boolean
    = char_pred { chars.contains(it) }

// -------------------------------------------------------------------------------------------------

/**
 * Matches [str].
 */
fun Grammar.string (str: String): Boolean
{
    val success = text.regionMatches(pos, str, 0, str.length)
    if (success) pos += str.length
    else fail(pos, NoString(str))
    return success
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches [str], and any trailing whitespace (as defined by [Grammar.whitespace]).
 */
fun Grammar.word (str: String): Boolean
{
    val success = text.regionMatches(pos, str, 0, str.length)
    if (success) {
        pos += str.length
        parse_whitespace()
    }
    else fail(pos, NoString(str))
    return success
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches the same thing as [p], and any trailing whitespace (as defined by [Grammar.whitespace]).
 */
inline fun Grammar.word (p: Parser): Boolean
{
    val success = p()
    if (success) parse_whitespace()
    return success
}

// -------------------------------------------------------------------------------------------------

/**
 * Matches an alphabetic character (the ranges a-z and A-Z).
 */
fun Grammar.alpha(): Boolean
    = char_pred { it in 'a'..'z' || it in 'A'..'Z' }

// -------------------------------------------------------------------------------------------------

/**
 * Matches an alphanumeric character (the ranges a-z, A-Z and 0-9).
 */
fun Grammar.alphanum(): Boolean
    = char_pred { it in 'a'..'z' || it in 'A'..'Z' || it in '0'..'9' }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a digit (the range 0-9).
 */
fun Grammar.digit(): Boolean
    = char_pred { it in '0'..'9' }

// -------------------------------------------------------------------------------------------------

/**
 * Matches an hexadecimal digit (the ranges a-f, A-F and 0-9).
 */
fun Grammar.hex_digit(): Boolean
    = char_pred { it in 'a'..'f' || it in 'A'..'F' || it in '0'..'9' }

// -------------------------------------------------------------------------------------------------

/**
 * Matches an octal digit (the range 0-7).
 */
fun Grammar.octal_digit(): Boolean
    = char_pred { it in '0'..'7' }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a whitespace character, as defined by [Char.isWhitespace].
 */
fun Grammar.space_char(): Boolean
    = char_pred(Char::isWhitespace)

// -------------------------------------------------------------------------------------------------

/**
 * Matches a java identifier (as defined by JLS 3.8).
 */
fun Grammar.java_iden(): Boolean
    = transact_contain(ExpectedIdentifier) b@ {
        if (!text[pos].isJavaIdentifierStart())
            return@b false
        var c: Char
        do { c = text[++pos] } while (c.isJavaIdentifierPart() && c != '\u0000')
        return@b true
    }

// -------------------------------------------------------------------------------------------------

/**
 * Matches a java identifier that consists (as defined by JLS 3.8) that consists only of
 * ASCII characters.
 */
fun Grammar.ascii_java_iden(): Boolean
    = transact_contain(ExpectedIdentifier) b@ {

        var c = text[pos]

        if (! (c in 'a'..'z'
            || c in 'A'..'Z'
            || c == '_'
            || c == '$'))
        {
            return@b false
        }

        do {
            c = text[++pos]
        } while (c in 'a'..'z'
            || c in 'A'..'Z'
            || c == '_'
            || c == '$'
            || c in '0'..'9')

        true
    }

// -------------------------------------------------------------------------------------------------
