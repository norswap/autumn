package norswap.autumn;

import norswap.autumn.positions.LineMap;
import norswap.autumn.util.ArrayStack;
import norswap.utils.Strings;

/**
 * A stack of {@link ParserCallFrame} representing parser invocations at a certain position.
 */
public final class ParserCallStack extends ArrayStack<ParserCallFrame>
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Pushes a new call frame onto the stack.
     */
    public void push (Parser parser, int position)
    {
        push(new ParserCallFrame(parser, position));
    }

    // ---------------------------------------------------------------------------------------------

    private static final int MIN_LINE_WIDTH = 4;
    private static final int MIN_COLUMN_WIDTH = 3;

    /**
     * Appends a nicely formatted string representation of the parser call stack to {@code b},
     * indented with {@code indent} tabs. The appended content never ends with a newline.
     *
     * @param map If non-null, used to translate input positions in terms of lines and columns.
     *
     * @param onlyRules If true, only parsers which are are grammar rules (i.e. have a non-null
     * {@link Parser#rule()}) will be included in the representation.
     *
     * @param filePath If non-null, appended in front of the input positions position in order for
     * them to be become clickable in IntelliJ (and potentially other editors). This is only useful
     * if a {@code map} is also supplied.  Note that in IntelliJ, only absolute paths enable linking
     * to colums in addition to lines.
     */
    public void appendTo (
            StringBuilder b, int indent, LineMap map, boolean onlyRules, String filePath)
    {
        // Use spaces and not tabs, as tabs inhibit hyperlinking of the file path in IntelliJ.
        String tabs = Strings.repeat(' ', indent * 4);

        for (ParserCallFrame frame: this)
            if (!onlyRules || frame.parser.rule() != null) {
                b.append(tabs);
                b.append("at ");
                if (filePath != null) b
                    .append(filePath).append(":")
                    .append(LineMap.string(map, frame.position));
                else
                    b.append(LineMap.string(map, frame.position, MIN_LINE_WIDTH, MIN_COLUMN_WIDTH));
                b.append(" in ");
                b.append(frame.parser);
                b.append("\n");
            }

        if (!isEmpty())
            Strings.pop(b, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of this call stack, as per {@link #appendTo(StringBuilder,
     * int, LineMap, boolean, String)} (with no indentation).
     *
     * @param map If non-null, used to translate input positions in terms of lines and columns.
     *
     * @param onlyRules If true, only parsers which are are grammar rules (i.e. have a non-null
     * {@link Parser#rule()}) will be included in the representation.
     *
     * @param filePath If non-null, appended in front of the input positions position in order for
     * them to be become clickable in IntelliJ (and potentially other editors). This is only useful
     * if a {@code map} is also supplied. Note that in IntelliJ, only absolute paths enable linking
     * to colums in addition to lines.
     */
    public String toString (LineMap map, boolean onlyRules, String filePath)
    {
        StringBuilder b = new StringBuilder();
        appendTo(b, 0, map, onlyRules, filePath);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of this call stack, as per {@link #appendTo(StringBuilder,
     * int, LineMap, boolean, String)} (with no identation, and no line map conversion).
     */
    @Override public String toString()
    {
        StringBuilder b = new StringBuilder();
        appendTo(b, 0, null, false, null);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public ParserCallStack clone()
    {
        return (ParserCallStack) super.clone();
    }

    // ---------------------------------------------------------------------------------------------
}

