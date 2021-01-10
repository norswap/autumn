package norswap.autumn.util;

import norswap.autumn.Parser;
import java.util.ArrayList;
import java.util.List;

import static norswap.utils.Strings.indent;

/**
 * Utilities to work with the string representation of Autumn parsers.
 */
public final class ParserStringsUtil
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Pretty prints the string representation of the parser (via {@link Parser#toString()}.
     *
     * <p>The output string has one parser per line, with indentation to show nesting. When there is
     * only a single level of nesting, both the parent and the child are shown on the same line.
     *
     * <p>For this method to work properly, the string representation of parsers should
     * respect the following conditions:
     *
     * <ul>
     * <li>The string representation of every parser in the graph must is either of
     * the form {@code HEADER(ARG, ...)} (with 0 or more args) or a free-form string.</li>
     *
     * <li>In free-form strings, parens ('(', ')') and commas (',') may not appear, excepted between
     * square brackets ('[', ']'), which is a form of quotation.</li>
     *
     * <li>To enable square brackets themselves to be quoted, one should call {@link
     * #escapeQuotedSection(String)} on string sections that are include inside square brackets.
     * This also escapes unusual characters.</li>
     *
     * <li>Newlines are tolerated but they mess up the output, so avoid them; except between square
     * brackets, where they get escaped to \n.</li>
     * </ul>
     */
    public static String prettyPrint (Parser parser) {
        return prettyPrint(parser.toString());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pretty prints the full string representation of the parser (via {@link Parser#toStringFull()}.
     *
     * <p>For details, see {@link #prettyPrint(Parser)}
     */
    public static String prettyPrintFull (Parser parser) {
        return prettyPrint(parser.toStringFull());
    }

    // ---------------------------------------------------------------------------------------------

    private static String prettyPrint (String string)
    {
        if (!isParameterized(string))
            return string;

        List<String> components = components(string);
        if (components.size() == 1)
            return string;

        StringBuilder b = new StringBuilder();
        b.append(components.get(0));

        if (components.size() == 2 && !isParameterized(components.get(1))) {
            b.append('(').append(components.get(1)).append(')');
        } else for (int i = 1; i < components.size(); ++i) {
            b.append("\n");
            b.append(indent(prettyPrint(components.get(i)), "    "));
        }

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    // foo      --> false
    // foo()    --> true
    // foo(bar) --> true
    private static boolean isParameterized (String string) {
        return string.charAt(string.length() - 1) == ')';
    }

    // ---------------------------------------------------------------------------------------------

    // foo(bar, baz) --> [foo, bar, baz]
    private static List<String> components (String string)
    {
        final int open = string.indexOf('(');
        final String head = string.substring(0, open);
        final List<String> components = new ArrayList<>();
        components.add(head);

        int start = open + 1;
        final int end = string.length() - 1;

        while (start < end)
        {
            String remaining = string.substring(start, end);
            int argEnd = nextArgEnd(remaining);
            start = argEnd + 2; // skip comma + space
        }

        return components;
    }

    // ---------------------------------------------------------------------------------------------

    // "foo, bar"      --> 3
    // "foo"           --> 3
    // "foo(bar), baz" --> 8
    private static int nextArgEnd (String string)
    {
        int sqbraCount = 0;
        int parenCount = 0;

        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);

            switch (c)
            {
                case '[':
                    ++ sqbraCount;break;
                case ']':
                    -- sqbraCount;break;
                case '(':
                    if (sqbraCount == 0) ++ parenCount; break;
                case ')':
                    if (sqbraCount == 0) -- parenCount; break;
                case ',':
                    if (parenCount == 0 && sqbraCount == 0)
                        return i;
                    break;
            }
        }

        return string.length();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Escapes a quoted section of a parser string representation. Such quoted sections are surround
     * by square brackets ([]). This enables pretty-printing via {@link #prettyPrint(Parser)} while
     * allowing control characters to be represented.
     *
     * <p>This does two things: escape the string with {@link StringsUtil#escape(String)}, and
     * replaces ']' by '⦎' (unicode 0x298) — enabling closing square brackets to be quoted too.
     *
     * <p>The later does make this a lossy operation, but since the goal of such representations
     * is display and debug, that's quite okay. Also nobody uses 0x298.
     */
    public static String escapeQuotedSection (String string) {
        return StringsUtil.escape(string).replace(']', '⦎');
    }

    // ---------------------------------------------------------------------------------------------
}
