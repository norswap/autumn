@file:Suppress("NOTHING_TO_INLINE")
package norswap.autumn.parsers
import norswap.autumn.*

// -------------------------------------------------------------------------------------------------

inline fun Grammar.char_pred (pred: (Char) -> Boolean): Boolean
{
    val success = pred(text[pos])
    if (success) ++pos
    else fail(pos, UnexpectedChar)
    return success
}

// -------------------------------------------------------------------------------------------------

fun Grammar.char_any(): Boolean
    = char_pred { it != '\u0000' }

// -------------------------------------------------------------------------------------------------

fun Grammar.char_range (start: Char, end: Char): Boolean
    = char_pred { start <= it && it <= end }

// -------------------------------------------------------------------------------------------------

fun Grammar.char_set (vararg chars: Char): Boolean
    = char_pred { chars.contains(it) }

// -------------------------------------------------------------------------------------------------

fun Grammar.char_set (chars: String): Boolean
    = char_pred { chars.contains(it) }

// -------------------------------------------------------------------------------------------------

fun Grammar.string (str: String): Boolean
{
    val success = text.regionMatches(pos, str, 0, str.length)
    if (success) pos += str.length
    else fail(pos, NoString(str))
    return success
}

// -------------------------------------------------------------------------------------------------

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

inline fun Grammar.word (p: Parser): Boolean
{
    val success = p()
    if (success) parse_whitespace()
    return success
}

// -------------------------------------------------------------------------------------------------

fun Grammar.alpha(): Boolean
    = char_pred { 'a' <= it && it <= 'z' || 'A' <= it && it <= 'Z' }

// -------------------------------------------------------------------------------------------------

fun Grammar.alphanum(): Boolean
    = char_pred { 'a' <= it && it <= 'z' || 'A' <= it && it <= 'Z' || '0' <= it && it <= '9' }

// -------------------------------------------------------------------------------------------------

fun Grammar.digit(): Boolean
    = char_pred { '0' <= it && it <= '9' }

// -------------------------------------------------------------------------------------------------

fun Grammar.hex_digit(): Boolean
    = char_pred { 'a' <= it && it <= 'f' || 'A' <= it && it <= 'F' || '0' <= it && it <= '9' }

// -------------------------------------------------------------------------------------------------

fun Grammar.octal_digit(): Boolean
    = char_pred { '0' <= it && it <= '7' }

// -------------------------------------------------------------------------------------------------

fun Grammar.space_char(): Boolean
    = char_pred(Char::isWhitespace)

// -------------------------------------------------------------------------------------------------

fun Grammar.java_iden(): Boolean
    = transact_contain(ExpectedIdentifier) b@ {
        if (!text[pos].isJavaIdentifierStart())
            return@b false
        var c: Char
        do { c = text[++pos] } while (c.isJavaIdentifierPart() && c != '\u0000')
        return@b true
    }

// -------------------------------------------------------------------------------------------------

fun Grammar.ascii_java_iden(): Boolean
    = transact_contain(ExpectedIdentifier) b@ {

        var c = text[pos]

        if (! ('a' <= c && c <= 'z'
            || 'A' <= c && c <= 'Z'
            || c == '_'
            || c == '$'))
        {
            return@b false
        }

        do {
            c = text[++pos]
        } while ('a' <= c && c <= 'z'
            || 'A' <= c && c <= 'Z'
            || c == '_'
            || c == '$'
            || '0' <= c && c <= '9')

        true
    }

// -------------------------------------------------------------------------------------------------
