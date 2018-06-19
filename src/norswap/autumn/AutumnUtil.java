package norswap.autumn;

import java.util.ArrayList;
import java.util.List;

import static norswap.utils.Strings.indent;

/**
 * Utilities to work with Autumn parsers, e.g. for debugging.
 */
public final class AutumnUtil
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Within {@code string}, replaces ']' by '⦎' (unicode 0x298).
     *
     * <p>This should be called by all parsers whose {@link Parser#toStringFull()} method outputs
     * contains bracketed characters ([xyz]), in order to facilitate pretty-printing by {@link
     * #pretty_print(Parser)}. An opening square bracket should be matched with a single closing
     * square bracket with no intermediate closing square bracket.
     */
    public static String replace_closing_square_brackets (String string)
    {
        return string.replace(']', '¤');
    }

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
     * square brackets ('[', ']').</li>
     *
     * <li>Each opening square bracket should be matched to a single closing square bracket (there
     * should be no intervening closing square bracket). To ensure this, one should call {@link
     * #replace_closing_square_brackets(String)} on strings that are included inside square
     * brackets.</li>
     * </ul>
     */
    public static String pretty_print (Parser parser)
    {
        return pretty_print(parser.toString());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Pretty prints the full string representation of the parser (via {@link Parser#toStringFull()}.
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
     * square brackets ('[', ']').</li>
     *
     * <li>Each opening square bracket should be matched to a single closing square bracket (there
     * should be no intervening closing square bracket). To ensure this, one should call {@link
     * #replace_closing_square_brackets(String)} on strings that are included inside square
     * brackets.</li>
     * </ul>
     */
    public static String pretty_print_full (Parser parser)
    {
        return pretty_print(parser.toString());
    }

    // ---------------------------------------------------------------------------------------------

    private static String pretty_print (String string)
    {
        if (!is_parameterized(string))
            return string;

        List<String> components = components(string);
        if (components.size() == 1)
            return string;

        StringBuilder b = new StringBuilder();
        b.append(components.get(0));

        if (components.size() == 1 && !is_parameterized(components.get(1)))
            b.append('(').append(components.get(1)).append(')');

        else for (int i = 1; i < components.size(); ++i)
            b.append("\n")
             .append(indent(pretty_print(components.get(i)), "    "));

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    // <foo>    --> false
    // foo()    --> true
    // foo(bar) --> true
    private static boolean is_parameterized (String string)
    {
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
            int arg_end = next_arg_end(remaining);
            start = arg_end + 2; // skip comma + space
        }

        return components;
    }

    // ---------------------------------------------------------------------------------------------

    // "foo, bar"      --> 3
    // "foo"           --> 3
    // "foo(bar), baz" --> 8
    private static int next_arg_end (String string)
    {
        int sqbra_count = 0;
        int paren_count = 0;

        for (int i = 0; i < string.length(); ++i)
        {
            char c = string.charAt(i);

            switch (c)
            {
                case '[':
                    ++ sqbra_count;break;
                case ']':
                    -- sqbra_count;break;
                case '(':
                    if (sqbra_count == 0) ++ paren_count; break;
                case ')':
                    if (sqbra_count == 0) -- paren_count; break;
                case ',':
                    if (paren_count == 0 && sqbra_count == 0)
                        return i;
                    break;
            }
        }

        return string.length();
    }

    // ---------------------------------------------------------------------------------------------
}
