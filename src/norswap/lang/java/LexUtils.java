package norswap.lang.java;

import norswap.utils.Exceptional;
import norswap.utils.Strings;

/**
 * Utilities related with Java lexing.
 *
 * <p>In particular, include utilities meant to extract values from literal tokens.
 */
public final class LexUtils
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates an issue with a task related to lexical analysis (typically attempting to parse
     * the value of a grammatically-compliant litteral value).
     */
    public static class LexProblem extends RuntimeException
    {
        LexProblem (String msg) {
            super(msg, null, true, false); // no stack trace
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the floating-point value represented by the given literal string.
     *
     * <p>Assumes the string conforms to the grammar rules for floating-point litterals.
     *
     * @return {@link LexProblem} if the number is too big or too small to be represented as floating-point
     * value of the required type: {@code double} by default, or {@code float} if a trailing 'f'
     * or 'F' indicator is present.
     */
    public static Exceptional<Number> parse_floating (String string)
    {
        String str          = string.replace("_", "");
        char last           = Strings.last_char(string);
        boolean is_float    = last == 'f' || last == 'F';

        double value = is_float
            ? Float.parseFloat(str)
            : Double.parseDouble(str);

        if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY)
            return Exceptional.error(is_float
                ? new LexProblem("Float literal too big.")
                : new LexProblem("Double literal too big."));

        if (value == 0.0)
        {
            boolean is_hex = str.length() > 2 && str.startsWith("0x");
            int i = is_hex
                // +1 because at least one of the indices will be -1
                ? str.indexOf('p') + str.indexOf('P') + 1
                : str.indexOf('e') + str.indexOf('E') + 1;

            String sub = i > 0 ? str.substring(0, i) : str;

            // if there are significant digits and we still get 0, the literal is too small
            if ("123456789".chars().anyMatch(c -> sub.indexOf(c) >= 0))
                throw is_float
                    ? new LexProblem("Float literal too small.")
                    : new LexProblem("Double literal too small.");
        }

        return Exceptional.of(is_float ? new Float(value) : new Double(value));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the integer value represented by the given literal string.
     *
     * <p>Assumes the string conforms to the grammar rules for integer litterals (char literals
     * excluded).
     *
     * @return {@link LexProblem} if the number is too big or too small to be represented as an integral
     * value of the required type: {@code int} by default, or {@code long} if a trailing 'l' or
     * 'L' indicator is present.
     */
    public static Exceptional<Number> parse_integer (String string)
    {
        if (string.length() == 1 || string.charAt(0) != 0)
            return parse_integer(10, string);

        switch (string.charAt(1)) {
            case 'b':
            case 'B':
                return parse_integer(2, string.substring(2));
            case 'x':
            case 'X':
                return parse_integer(16, string.substring(2));
            default:
                return parse_integer(8, string.substring(1));
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the integer value represented by the given literal string, using the given base.
     *
     * <p>Assumes the string conforms to the grammar rules for integer litterals BUT that
     * the base indicators "0b" or "0x" have been stripped if present.
     *
     * @throws LexProblem if the number is too big or too small to be represented as an integral
     * value of the required type: {@code int} by default, or {@code long} if a trailing 'l' or
     * 'L' indicator is present.
     */
    public static Exceptional<Number> parse_integer (int base, String string)
    {
        long out = 0;
        char last = Strings.last_char(string);
        boolean is_long = last == 'l' || last == 'L';

        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);
            if (c == '_') continue;
            if (c == 'l' || c == 'L') break;

            // overflows due to c == '0' yield the correct result below!

            if (out != 0 && is_long && (Long.MAX_VALUE - (c - '0') + 1) / out < base)
                return Exceptional.error(new LexProblem("Long literal is too big."));

            if (out != 0 && !is_long && (Integer.MAX_VALUE - (c - '0') + 1) / out < base)
                return Exceptional.error(new LexProblem("Integer literal is too big."));

            out = out * base + (c - '0');
        }

        return Exceptional.of(is_long ? new Long(out): new Integer((int) out));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff {@code c} is a valid hexadecimal digit (for letters, both lower and upper
     * case are accepted).
     */
    public static boolean is_hex_digit (char c)
    {
        return '0' <= c && c <= '9'
            || 'a' <= c && c <= 'f'
            || 'A' <= c && c <= 'F';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff {@code c} is a valid octal digit.
     */
    public static boolean is_octal_digit (char c)
    {
        return '0' <= c && c <= '7';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy of the given string where all Java character escapes have been replaced
     * by the escaped character. Also handles unicode escapes (which are normally handled before
     * lexing).
     *
     * @return {@link LexProblem} if an illegal escape sequence is detected within the string.
     */
    public static Exceptional<String> unescape (String string)
    {
        StringBuilder b = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);

            if (c != '\\') {
                b.append(c);
                continue;
            }

            c = string.charAt(++i);
            int j;
            switch (c)
            {
                case 't' : b.append('\t');
                case 'n' : b.append("\n");
                case 'r' : b.append("\r");
                case '\'': b.append("\'");
                case '"' : b.append("\"");
                case '\\': b.append("\\");
                case 'b' : b.append("\b");
                case 'f' : b.append("\f");

                case 'u' :
                    j = i + 1;
                    while (j < string.length() && j < i + 4 && is_hex_digit(c)) ++j;
                    if (j != i + 4)
                        throw new LexProblem("Illegal hex escape in string.");
                    b.append((char) Integer.parseInt(string.substring(i, j), 16));
                    i = j - 1;
                    break;

                case '0': case '1': case '2': case '3': case '4': case '5': case '6': case '7':
                    j = i + 1;
                    while(j < string.length() && j < i + 3 && is_octal_digit(string.charAt(j))) ++j;
                    if (j == i + 3 && string.charAt(i) > '3')
                        --j; // the escape only spans two digits
                    b.append((char) Integer.parseInt(string.substring(i, j), 8));
                    i = j - 1;
                    break;

                default:
                    throw new LexProblem("Illegal escape in string.");
            }
        }

        return Exceptional.of(b.toString());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the character value represented by the given literal string.
     *
     * <p>Assumes the string conforms to the grammar rules for character literals, including
     * the surrounding single quotes.
     *
     * @return {@link LexProblem} if an illegal escape sequence is detected within the string.
     */
    public static Exceptional<Character> parse_char (String string)
    {
        return string.length() == 3
            ? Exceptional.of(string.charAt(1))
            : unescape(string.substring(1, string.length() - 1)).map(str -> str.charAt(0));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the string value represented by the given literal string.
     *
     * <p>Assumes the string conforms to the grammar rules for string literals, including
     * the surrounding double quotes.
     *
     * @return {@link LexProblem} if an illegal escape sequence is detected within the string.
     */
    public static Exceptional<String> parse_string (String string)
    {
        return unescape(string.substring(1, string.length() - 1));
    }

    // ---------------------------------------------------------------------------------------------
}
