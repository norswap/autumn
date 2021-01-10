package norswap.lang.java;

import norswap.autumn.util.StringsUtil;
import norswap.utils.exceptions.Exceptional;
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
        public LexProblem (String msg) {
            super(msg, null, true, false); // no stack trace
        }

        @Override public int hashCode() {
            return 31 * getMessage().hashCode();
        }

        @Override public boolean equals (Object obj) {
            return obj instanceof LexProblem
                && ((LexProblem) obj).getMessage().equals(getMessage());
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
        char last           = Strings.lastChar(string);
        boolean is_float    = last == 'f' || last == 'F';

        double value = is_float
            ? Float.parseFloat(str)
            : Double.parseDouble(str);

        if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY)
            return Exceptional.exception(is_float
                ? new LexProblem("Float literal is too big.")
                : new LexProblem("Double literal is too big."));

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
                return Exceptional.exception(is_float
                    ? new LexProblem("Float literal is too small.")
                    : new LexProblem("Double literal is too small."));
        }

        return Exceptional.value(is_float ? Float.valueOf((float) value) : Double.valueOf(value));
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
        if (string.length() == 1 || string.charAt(0) != '0')
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
     * @return {@link LexProblem} if the number is too big or too small to be represented as an
     * integral value of the required type: {@code int} by default, or {@code long} if a trailing
     * 'l' or 'L' indicator is present.
     */
    public static Exceptional<Number> parse_integer (int base, String string)
    {
        long out = 0;
        char last = Strings.lastChar(string);
        boolean is_long = last == 'l' || last == 'L';

        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);
            if (c == '_') continue;
            if (c == 'l' || c == 'L') break;

            int value = digit(c);

            if (out != 0)
            {
                long max = is_long ? Long.MAX_VALUE : Integer.MAX_VALUE;
                long quotient = (max - value) / out;
                if (quotient < base || quotient == base && (max - value) % out > 0)
                    return Exceptional.exception(is_long
                        ? new LexProblem("Long literal is too big.")
                        : new LexProblem("Integer literal is too big."));
            }

            out = out * base + value;
        }

        // The casts are required to prevent inference + boxing to always provide a Long.
        //noinspection UnnecessaryBoxing,RedundantCast
        return Exceptional.value(is_long
            ? (Number) Long.valueOf(out)
            : (Number) Integer.valueOf((int) out));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the integer value of the given decimal or hexadecimal digit.
     */
    public static int digit (int c)
    {
        if ('0' <= c && c <= '9' )
            return c - '0';
        if ('a' <= c && c <= 'f')
            return 10 + c - 'a';
        if ('A' <= c && c <= 'F')
            return 10 + c - 'A';
        else
            throw new RuntimeException("invalid digit: " + c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff {@code c} is a valid hexadecimal digit (for letters, both lower and upper
     * case are accepted).
     */
    public static boolean is_hex_digit (int c)
    {
        return '0' <= c && c <= '9'
            || 'a' <= c && c <= 'f'
            || 'A' <= c && c <= 'F';
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff {@code c} is a valid octal digit.
     */
    public static boolean is_octal_digit (int c)
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
                case 't' : b.append('\t'); break;
                case 'n' : b.append("\n"); break;
                case 'r' : b.append("\r"); break;
                case '\'': b.append("\'"); break;
                case '"' : b.append("\""); break;
                case '\\': b.append("\\"); break;
                case 'b' : b.append("\b"); break;
                case 'f' : b.append("\f"); break;

                case 'u' :
                    j = i + 1;
                    while (j < string.length() && j < i + 5 && is_hex_digit(string.charAt(j))) ++j;
                    if (j != i + 5)
                        return Exceptional.exception(new LexProblem("Illegal hex escape in string."));
                    b.append((char) Integer.parseInt(string.substring(i + 1, j), 16));
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
                    return Exceptional.exception(new LexProblem("Illegal escape in string."));
            }
        }

        return Exceptional.value(b.toString());
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
            ? Exceptional.value(string.charAt(1))
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

    /**
     * Returns an escaped version of the given string, where all non-printable characters have been
     * replaced by a corresponding escape. Some printable and potentially-printable characters, such
     * as the newline (\n), tab (\t), form feed (\f) and carriage return (\r) are also escaped.
     *
     * <p>Specific escapes (e.g. \t) are used preferentially, otherwise unicode escapes (e.g.
     * \u0061) are used.
     *
     * <p>The returned string should be safe to output as the content of a Java string literal.
     * (Hence why we escape \n in particular.)
     */
    public static String escape (String string)
    {
        return StringsUtil.escape(string);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the given character is printable.
     *
     * <p>This hasn't been tested extensively, treat with circumspection. The notion of printable is
     * somewhat vague, and I'm not privy with the Unicode arcane. This pointedly doesn't guarantee
     * that your font handles the given character.
     *
     * <p>Source: https://stackoverflow.com/a/418560
     */
    public static boolean is_printable (char c)
    {
        return StringsUtil.isPrintable(c);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy of the given string, without leading and trailing java whitespace - this
     * includes comments!
     *
     * <p>The passed string should consist of one or more valid Java construction(s) and valid
     * whitespace, otherwise the result might be garbage.
     */
    public static String trim_whitespace (String string)
    {
        string = trim_leading_whitespace(string);
        string = trim_trailing_whitespace(string);
        return string;
    }


    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy of the given string, without leading java whitespace - this includes comments!
     *
     * <p>The passed string should consist of one or more valid Java construction(s) and valid
     * whitespace, otherwise the result might be garbage.
     */
    public static String trim_leading_whitespace (String string)
    {
        int i = 0;
        loop: while (i < string.length())
            switch (string.charAt(i))
            {
                case ' ': case '\t': case '\n': case '\r': case '\f':
                    ++i;
                    continue;

                case '/':
                    if (i + 1 == string.length())
                        break loop;

                    char next = string.charAt(i + 1);

                    if (next == '/') {
                        int j = string.indexOf('\n', i + 1);
                        i = j > 0 ? j + 1 : string.length();
                        continue;
                    }
                    else if (next == '*') {
                        int j = string.indexOf("*/", i + 1);
                        if (j < 0)
                            throw new IllegalArgumentException
                                ("Multi-line comment start without comment ending.");
                        i = j + 2;
                        continue;
                    }
                    break loop;

                default:
                    break loop;
            }

        return string.substring(i);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy of the given string, without trailing java whitespace - this includes
     * comments!
     *
     * <p>The passed string should consist of one or more valid Java construction(s) and valid
     * whitespace, otherwise the result might be garbage.
     */
    public static String trim_trailing_whitespace (String string)
    {
        int i = handle_line_comment(string, string.length());

        loop: while (i > 0)
            switch (string.charAt(i - 1))
            {
                case ' ': case '\t': case '\f':
                    --i;
                    continue;

                case '\n': case '\r':
                    i = handle_line_comment(string, i - 1);
                    continue;

                case '/':
                    if (i - 1 == 0)
                        break loop;

                    if (string.charAt(i - 2) == '*') {
                        // second to last occurence of "*/"
                        int previous = string.lastIndexOf("*/", i - 4);
                        i = string.indexOf("/*", previous > 0 ? previous + 2 : 0);
                        if (i < 0)
                            throw new IllegalArgumentException
                                ("Multi-line comment ending without comment start.");
                        continue;
                    }
                    break loop;

                default:
                    break loop;
            }

        return string.substring(0, i);
    }

    // ---------------------------------------------------------------------------------------------

    private static int handle_line_comment (String string, int end)
    {
        // last newline before end, or -1 if no newline
        int last_newline = string.lastIndexOf('\n', end - 1);

        // first line comment in last line
        int line_comment = string.substring(0, end).indexOf("//", last_newline + 1);

        return line_comment >= 0 ? line_comment : end;
    }

    // ---------------------------------------------------------------------------------------------
}
