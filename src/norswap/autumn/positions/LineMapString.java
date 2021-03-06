package norswap.autumn.positions;

import norswap.utils.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See {@link LineMap}.
 */
public final class LineMapString implements LineMap
{
    // ---------------------------------------------------------------------------------------------

    private final String name;

    // ---------------------------------------------------------------------------------------------

    /**
     * The string over which the lines are mapped.
     */
    public final String string;

    // ---------------------------------------------------------------------------------------------

    /**
     * Array containing the offset of the first character of each line.
     */
    public final int[] linePositions;

    // ---------------------------------------------------------------------------------------------

    /**
     * The size of tab characters (4 by default).
     *
     * <p>Might be a good idea to set to 1 when using IntelliJ, see {@link LineMap}.
     */
    public final int tabSize;

    // ---------------------------------------------------------------------------------------------

    /**
     * The start index for columns numbers. One by default.<br>
     * Zero is the other useful value, for editors like Emacs.
     */
    public final int columnStart;

    // ---------------------------------------------------------------------------------------------

    private static final int lineStart = 1;

    // ---------------------------------------------------------------------------------------------

    public LineMapString (String name, String string, int tabSize, int columnStart)
    {
        this.name        = name;
        this.string      = string;
        this.tabSize     = tabSize;
        this.columnStart = columnStart;

        List<Integer> positions = new ArrayList<>();
        positions.add(0);

        for (int i = 0; i < string.length(); ++i)
            if (string.charAt(i) == '\n')
                positions.add(i + 1);

        linePositions = positions.stream().mapToInt(i -> i).toArray();
    }

    // ---------------------------------------------------------------------------------------------

    public LineMapString (String name, String string) {
        this(name, string, 4, 1);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String name() {
        return name;
    }

    // ---------------------------------------------------------------------------------------------

    private void checkLine (int line) {
        if (line < lineStart || line - lineStart >= linePositions.length)
            throw new IndexOutOfBoundsException("line " + line);
    }

    // ---------------------------------------------------------------------------------------------

    private void checkOffset (int offset) {
        if (offset < 0 || string.length() < offset)
            throw new IndexOutOfBoundsException("string offset " + offset);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int offsetFor (int line) {
        checkLine(line);
        return linePositions[line - lineStart];
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int endOffsetFor (int line) {
        checkLine(line);
        return line - lineStart == linePositions.length - 1
            ? string.length()
            : offsetFor(line + 1) - 1;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int lineFrom (int offset)
    {
        checkOffset(offset);
        final int index = Arrays.binarySearch(linePositions, offset);

        // if (`offset` points to a char right after a newline)
        //      `line` is the 0-based line index
        //  else
        //      `line` is -`nextLine` - 1
        //       where `nextLine` is the 0-based index of the first line starting after `offset`

        return index >= 0
            ? index + lineStart
            : -index - 2 + lineStart;
    }

    // ---------------------------------------------------------------------------------------------

    private int columnFrom (int line, int offset)
    {
        final int lineOffset = offsetFor(line);
        int col = 0;
        for (int i = lineOffset; i < offset; ++i)
            col += (string.charAt(i) == '\t') ? (tabSize - col % tabSize) : 1;
        return col + columnStart;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int columnFrom (int offset)
    {
        int line = lineFrom(offset);
        return columnFrom(line, offset);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Position positionFrom (int offset)
    {
        int line = lineFrom(offset);
        int column = columnFrom(line, offset);
        return new Position(line, column);
    }

    // ---------------------------------------------------------------------------------------------

    private RuntimeException noColumn (int line, int column) {
        return new IndexOutOfBoundsException("no column " + column + " in line " + line);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int offsetFrom (Position position)
    {
        final int line   = position.line;
        final int column = position.column;

        checkLine(line);
        final int lineOffset = offsetFor(line);

        if (column < columnStart)
            throw noColumn(line, column);

        int columnOffset = 0;
        int columnIndex  = 0;

        while (columnIndex + columnStart < column)
        {
            char c = string.charAt(lineOffset + columnOffset);
            if (c == '\n') throw noColumn(line, column);
            columnIndex += (c == '\t') ? (tabSize - columnIndex % tabSize) : 1;
            ++columnOffset;
        }

        if (columnIndex + columnStart != column)
            throw new IllegalArgumentException("column " + column + " happens inside a tab");

        return lineOffset + columnOffset;
    }

    // ---------------------------------------------------------------------------------------------

    private String getLine (int line) {
        return string.substring(offsetFor(line), endOffsetFor(line));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String lineSnippet (Position position)
    {
        final int line = position.line;
        final int column = position.column;
        checkLine(line);

        String lineString = getLine(line).replaceAll("\t", Strings.repeat(' ', tabSize));
        int start = 0;
        int end = lineString.length();

        if (end > MAX_SNIPPET_LENGTH) {
            int snippetLength = MAX_SNIPPET_LENGTH / 2 * 2; // for idiots who use odd numbers
            start = Math.max(0, column - snippetLength / 2);
            end  = Math.min(lineString.length(), column + snippetLength / 2);
            if (end - start < snippetLength)
                if (start == 0)
                    end += snippetLength - (end - start);
                else // end == lineString.length()
                    start -= snippetLength - (end - start);
        }

        if (column < columnStart || columnStart + lineString.length() < column)
            throw noColumn(line, column);
        String spaces = Strings.repeat(' ', column - columnStart);

        // note: substring is optimized not to copy when it spans the whole string
        return String.format("%s\n%s^\n", lineString.substring(start, end), spaces);
    }

    // ---------------------------------------------------------------------------------------------
}
