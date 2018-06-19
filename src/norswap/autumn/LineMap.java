package norswap.autumn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Enables converting between line/column indices and absolute offsets in a given input string.
 * <p>
 * A line map is bound to a given string, kept as a reference. For that string, it enables
 * translating between absolute character indices starting at 0, and line/column indices, which
 * can also be paired in a {@link Position} object.
 * <p>
 * Line indices start at 1, as they do in all text editors. The start for column indices is
 * customizable, but the only two useful values are 1 (most editors, and the default here) and 0
 * (Emacs-like editors).
 * <p>
 * Column indices have one additional sophistication: tab characters can span multiple indices,
 * in order to bring the column index in line with the next multiple of the tab size. The tab size
 * is also customizable (defaulting to 4).
 * <p>
 * The valid offset range is [0 - string.length]
 * <p>
 * There are as many line indices as the number of newline character in the files + 1
 * (the first line).
 * <p>
 * Given a valid line index, the valid column indices for that line are those that can successfully
 * be mapped to a valid offset that is not located on another line. Note that newline characters are
 * considered to be part of the line they follow. The range of valid column indices is potentially
 * discontinuous, because some of these indices can map to "the inside" of a tab character, and as
 * such cannot be mapped to a valid offset.
 */
public final class LineMap
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
    public final int[] line_positions;

    // ---------------------------------------------------------------------------------------------

    /**
     * The size of tab characters (4 by default).
     */
    public final int tab_size;

    // ---------------------------------------------------------------------------------------------

    /**
     * The start index for columns numbers. One by default.<br>
     * Zero is the other useful value, for editors like Emacs.
     */
    public final int column_start;

    // ---------------------------------------------------------------------------------------------

    private static final int line_start = 1;

    // ---------------------------------------------------------------------------------------------

    public LineMap (String string, int tab_size, int column_start)
    {
        this.string       = string;
        this.tab_size     = tab_size;
        this.column_start = column_start;

        List<Integer> positions = new ArrayList<>();
        positions.add(0);

        for (int i = 0; i < string.length(); ++i)
            if (string.charAt(i) == '\n')
                positions.add(i + 1);

        line_positions = positions.stream().mapToInt(i -> i).toArray();
    }

    // ---------------------------------------------------------------------------------------------

    public LineMap (String string) {
        this(string, 4, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private int line_offset (int line)
    {
        return line_positions[line - line_start];
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the line index for the given string offset.
     */
    public int line_from (int offset)
    {
        if (offset < 0 || string.length() < offset)
            throw new IndexOutOfBoundsException("offset " + offset);

        final int index = Arrays.binarySearch(line_positions, offset);

        // if (`offset` points to a char right after a newline)
        //      `line` is the 0-based line index
        //  else
        //      `line` is -`next_line` - 1
        //       where `next_line` is the 0-based index of the first line starting after `offset`

        return index >= 0
            ? index + line_start
            : -index - 2 + line_start;
    }

    // ---------------------------------------------------------------------------------------------

    private int column_from (int line, int offset)
    {
        final int line_offset = line_offset(line);

        int col = 0;

        for (int i = line_offset; i < offset; ++i)
            col += (string.charAt(i) == '\t') ? (tab_size - col % tab_size) : 1;

        return col + column_start;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index from the given string offset.
     */
    public int column_from (int offset)
    {
        int line = line_from(offset);
        return column_from(line, offset);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a line/column position pair from the given string offset.
     */
    public Position position_from (int offset)
    {
        int line = line_from(offset);
        int column = column_from(line, offset);
        return new Position(line, column);
    }

    // ---------------------------------------------------------------------------------------------

    private RuntimeException no_column(int line, int column)
    {
        return new IndexOutOfBoundsException("no column " + column + " in line " + line);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string offset from the given line/column position pair.
     * @throws RuntimeException if the position does not match a valid string offset.
     */
    public int offset_from (Position position)
    {
        final int line   = position.line;
        final int column = position.column;

        if (line < line_start || line_positions.length + line_start <= line)
            throw new IndexOutOfBoundsException("line " + line);

        final int line_offset = line_offset(line);

        if (column < column_start)
            throw no_column(line, column);

        int column_offset = 0;
        int column_index  = 0;

        while (column_index + column_start < column)
        {
            char c = string.charAt(line_offset + column_offset);
            if (c == '\n') throw no_column(line, column);
            column_index += (c == '\t') ? (tab_size - column_index % tab_size) : 1;
            ++column_offset;
        }

        if (column_index + column_start != column)
            throw new IllegalArgumentException("column " + column + " happens inside a tab");

        return line_offset + column_offset;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Represents a file position as a line/column pair.
     */
    public static class Position
    {
        public int line;
        public int column;

        public Position (int line, int column)
        {
            this.line = line;
            this.column = column;
        }

        @Override public int hashCode()
        {
            return line * 31 + column;
        }

        @Override public boolean equals (Object other)
        {
            if (!(other instanceof Position))
                return false;
            Position p = (Position) other;
            return line == p.line && column == p.column;
        }

        @Override public String toString()
        {
            return line + ":" + column;
        }
    }

    // ---------------------------------------------------------------------------------------------
}
