package norswap.autumn.positions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * See {@link LineMap}.
 */
public final class LineMapString implements LineMap
{
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

    public LineMapString (String string, int tabSize, int columnStart)
    {
        this.string       = string;
        this.tabSize = tabSize;
        this.columnStart = columnStart;

        List<Integer> positions = new ArrayList<>();
        positions.add(0);

        for (int i = 0; i < string.length(); ++i)
            if (string.charAt(i) == '\n')
                positions.add(i + 1);

        linePositions = positions.stream().mapToInt(i -> i).toArray();
    }

    // ---------------------------------------------------------------------------------------------

    public LineMapString (String string) {
        this(string, LineMap.tabSizeInit(), 1);
    }

    // ---------------------------------------------------------------------------------------------

    private int lineOffset (int line)
    {
        return linePositions[line - lineStart];
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int lineFrom (int offset)
    {
        if (offset < 0 || string.length() < offset)
            throw new IndexOutOfBoundsException("offset " + offset);

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
        final int lineOffset = lineOffset(line);

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

    private RuntimeException noColumn (int line, int column)
    {
        return new IndexOutOfBoundsException("no column " + column + " in line " + line);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public int offsetFrom (Position position)
    {
        final int line   = position.line;
        final int column = position.column;

        if (line < lineStart || linePositions.length + lineStart <= line)
            throw new IndexOutOfBoundsException("line " + line);

        final int lineOffset = lineOffset(line);

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
}
