package norswap.autumn;

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

    /**
     * Appends a nicely formatted string representation of the parser call stack to {@code b},
     * indented with {@code indent} tabs. The appended content never ends with a newline.
     *
     * <p>If {@code map} is non-null, it is used to translate the input position in terms of lines
     * and columns.
     */
    public void append_to (StringBuilder b, int indent, LineMap map)
    {
        String tabs = Strings.repeat('\t', indent);
        for (ParserCallFrame frame: this)
            b   .append(tabs)
                .append("at ")
                .append(LineMap.string(map, frame.position))
                .append(" in ")
                .append(frame.parser)
                .append("\n");

        if (!isEmpty())
            Strings.pop(b, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of this call stack, as per {@link #append_to(StringBuilder,
     * int, LineMap)} (with no identation).
     */
    public String toString (LineMap map)
    {
        StringBuilder b = new StringBuilder();
        append_to(b, 0, map);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of this call stack, as per {@link #append_to(StringBuilder,
     * int, LineMap)} (with no identation, and no line map conversion).
     */
    @Override public String toString()
    {
        StringBuilder b = new StringBuilder();
        append_to(b, 0, null);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public ParserCallStack clone()
    {
        return (ParserCallStack) super.clone();
    }

    // ---------------------------------------------------------------------------------------------
}

