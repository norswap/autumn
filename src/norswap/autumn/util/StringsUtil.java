package norswap.autumn.util;

import java.awt.event.KeyEvent;

/**
 * Utilities to work with strings in general, used in Autumn, but potentially of interest
 * to the writer of custom parsers too.
 */
public final class StringsUtil
{
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
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);
            switch (c)
            {
                case '\"': b.append("\\\""); break;
                case '\\': b.append("\\\\"); break;
                case '\n': b.append("\\n");  break;
                case '\t': b.append("\\t");  break;
                case '\r': b.append("\\r");  break;
                case '\b': b.append("\\b");  break;
                case '\f': b.append("\\f");  break;
                default:
                    if (StringsUtil.isPrintable(c))
                        b.append(c);
                    else
                        b.append("\\u").append(String.format("%04x", (int) c));
                    break;
            }
        }

        return b.toString();
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
    public static boolean isPrintable (char c)
    {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) &&
            c != KeyEvent.CHAR_UNDEFINED &&
            block != null &&
            block != Character.UnicodeBlock.SPECIALS;
    }

    // ---------------------------------------------------------------------------------------------
}
