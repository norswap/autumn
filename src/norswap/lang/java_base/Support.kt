package norswap.lang.java_base
import norswap.autumn.Grammar
import norswap.utils.plusAssign

// -------------------------------------------------------------------------------------------------
/*

NOTE(norswap):

Provisions for newer Java features (hex floats, decimals, underscore separators)
are backward compatible with older versions, because these functions have to pass
the grammar check first, and I was careful.

Whenever we throw an exception, it's that the grammar should have precluded that particular
case (hence it denotes a bug in the grammar, and shouldn't happen in practice).

For errors that the grammar cannot/does not catch, we use issues.

 */
// -------------------------------------------------------------------------------------------------

@Strictfp /* float -> double may lose precision if not strictfp */
fun Grammar.parse_float (string: String): Number
{
    val str   = string.replace("_", "")
    val last  = str.last()
    val float = last == 'f' || last == 'F'

    val v: Double =
            if (float)
                str.toFloat().toDouble()
            else
                str.toDouble()

    if (v == Double.POSITIVE_INFINITY)
    {
        if (float)
            persistent_issues.add { "Float literal is too big: rounded to infinity." }
        else
            persistent_issues.add { "Double literal is too big: rounded to infinity." }
    }
    else if (v == 0.0)
    {
        val too_big =
                if (str.length > 2 && str[0] == '0' && str[1] == 'x') // hex literal
                {
                    val i = str.indexOf('p') + str.indexOf('P') + 1 // one of the indices will be -1
                    i > 0 && str.substring(2, i).any { it != '0' }
                }
                else // decimal literal
                {
                    val i = str.indexOf('e') + str.indexOf('E') + 1 // one of the indices will be -1
                    i > 0 && str.substring(0, i).any(('1'..'9')::contains)
                }

        if (too_big)
            if (float)
                persistent_issues.add { "Float literal is too small: rounded to 0." }
            else
                persistent_issues.add { "Double literal is too small: rounded to 0." }
    }

    return if (float) v.toFloat() else v
}

// -------------------------------------------------------------------------------------------------

fun Grammar.parse_int (str: String): Number
{
    return if (str.length == 1 || str[0] != '0')
        parse_int(str, 10)
    else when (str[1]) {
        'b', 'B'    -> parse_int(str.substring(2), 2)
        'x'         -> parse_int(str.substring(2), 16)
        else        -> parse_int(str.substring(1), 8)
    }
}

// -------------------------------------------------------------------------------------------------

fun Grammar.parse_int (str: String, base: Int): Number
{
    var out  = 0L
    val last = str.last()
    val long = last == 'l' || last == 'L'

    for (c in str)
    {
        if (c == '_') continue
        if (c == 'l' || c == 'L') break

        if (long && out != 0L && (Long.MAX_VALUE - (c - '0')) / out < base)
        {
            persistent_issues.add { "Long literal is too big: rounded to max value." }
            out = Long.MAX_VALUE
            break
        }
        else if (!long && out != 0L && (Int.MAX_VALUE - (c - '0')) / out < base)
        {
            persistent_issues.add { "Integer literal is too big: rounded to max value." }
            out = Int.MAX_VALUE.toLong()
            break
        }

        out = out * base + (c - '0')
    }

    return if (long) out else out.toInt()
}

// -------------------------------------------------------------------------------------------------

fun parse_char (str: String): Char
{
    val inner = str.substring(1, str.lastIndex)
    return if (inner.length == 1) inner[0] else inner.unescape()[0]
}

// -------------------------------------------------------------------------------------------------

fun parse_string (str: String)
    =  str.substring(1, str.lastIndex).unescape()

// -------------------------------------------------------------------------------------------------

fun String.escape(): String
{
    val b = StringBuilder()
    this.forEach {
        b += when (it) {
            '\"' -> "\\\""
            '\\' -> "\\\\"
            '\n' -> "\\n"
            '\r' -> "\\r"
            else -> it
        }
    }
    return b.toString()
}

// -------------------------------------------------------------------------------------------------

fun String.unescape(): String
{
    val str = StringBuilder(length)
    val chars = toCharArray()
    var i = 0
    while (i < chars.size) {
        if (chars[i] == '\\') {
            val c = chars[++i]
            when {
                c == 't'  -> str.append("\t")
                c == 'n'  -> str.append("\n")
                c == 'r'  -> str.append("\r")
                c == '\'' -> str.append("\'")
                c == '"'  -> str.append("\"")
                c == '\\' -> str.append("\\")
                c == 'b' -> str.append("\b")
                c == 'f' -> str.append("\u000c")
                c == 'u' -> {
                    var j = ++i
                    while( j < chars.size
                        && j < i + 4
                        && chars[j].isHexDigit())
                        ++j
                    if (j != i + 4)
                        throw IllegalArgumentException("Illegal escape in string.")
                    str.append(Integer.parseInt(substring(i, j), 16).toChar())
                    i = j - 1 // i will be increment at the start of the loop
                }
                c.isOctalDigit() -> {
                    var j = i + 1
                    while( j < chars.size
                        && j < i + 3
                        && chars[j].isOctalDigit())
                        ++j
                    if (j == i + 3 && chars[i] > '3')
                        -- j // the escape only spans two digits
                    str.append(Integer.parseInt(substring(i, j), 8).toChar())
                    i = j - 1 // i will be increment at the start of the loop
                }
                else -> throw IllegalArgumentException("Illegal escape in string.")
            }
        } else str.append(chars[i])
        ++i
    }
    return str.toString()
}

// -------------------------------------------------------------------------------------------------

/**
 * Is the character an octal digit?
 */
fun Char.isOctalDigit()
    = '0' <= this && this <= '7'

// -------------------------------------------------------------------------------------------------

/**
 * Is the character an hexadecimal digit?
 */
fun Char.isHexDigit()
    =  '0' <= this && this <= '9'
    || 'a' <= this && this <= 'f'
    || 'A' <= this && this <= 'F'

// -------------------------------------------------------------------------------------------------
