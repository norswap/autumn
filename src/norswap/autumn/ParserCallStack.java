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
     * <p>If {@code map} is non-null, it is used to translate the input position in terms of lines
     * and columns.
     *
     * <p>If {@code only_rules} is true, only parsers which are are grammar rules (i.e. have a
     * non-null {@link Parser#rule()}) will be included in the representation.
     */
    public void append_to (StringBuilder b, int indent, LineMap map, boolean only_rules)
    {
        String tabs = Strings.repeat('\t', indent);

        for (ParserCallFrame frame: this)
            if (!only_rules || frame.parser.rule() != null) {
                b.append(tabs);
                b.append("at ");
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
     * Returns a string representation of this call stack, as per {@link #append_to(StringBuilder,
     * int, LineMap, boolean)} (with no identation).
     *
     * <p>If {@code only_rules} is true, only parsers which are are grammar rules (i.e. have a
     * non-null {@link Parser#rule()}) will be included in the representation.
     */
    public String toString (LineMap map, boolean only_rules)
    {
        StringBuilder b = new StringBuilder();
        append_to(b, 0, map, only_rules);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of this call stack, as per {@link #append_to(StringBuilder,
     * int, LineMap, boolean)} (with no identation, and no line map conversion).
     */
    @Override public String toString()
    {
        StringBuilder b = new StringBuilder();
        append_to(b, 0, null, false);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public ParserCallStack clone()
    {
        return (ParserCallStack) super.clone();
    }

    // ---------------------------------------------------------------------------------------------
}

