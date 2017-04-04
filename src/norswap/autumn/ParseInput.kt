package norswap.autumn
import norswap.autumn.conf.*
import norswap.utils.expandTabsToBuilder
import norswap.utils.plusAssign

/**
 * Container for a null-terminated, tab-expanded input string, and for the positions
 * of its newlines.
 *
 * The class allows conversion between file offsets (0-based indices into the input string)
 * and line/column indices (also 0-based).
 *
 * It also offers string representations of line/columns, offset indices and offset ranges,
 * display line/column information.
 */
class ParseInput (

    str: CharSequence,

    /**
     * The size of tab for tab expansion.
     *
     * If 0, specifies that tab should not be expanded.
     * Otherwise, all tab characters will be replaced by space characters so that the tab brings
     * the line position to the next multiple of `tab_size`.
     */
    val tab_size: Int = TAB_SIZE,

    /**
     * Index of the first line (only impacts string representations).
     * Usually 1, which is the default.
     */
    val line_start: Int = LINE_START,

    /**
     * Index of the first character in a line (only impacts string representations).
     * Usually 0 (e.g. Emacs) or 1 (e.g. IntelliJ IDEA). The default is 1.
     */
    val column_start: Int = COLUMN_START)
{
    // ---------------------------------------------------------------------------------------------

    companion object {
        val DUMMY = ParseInput("")
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A null-terminated string with tabs expanded to spaces.
     */
    val text: String
        init {
            val b = str.expandTabsToBuilder(TAB_SIZE)
            b += '\u0000'
            text = b.toString()
        }

    // ---------------------------------------------------------------------------------------------

    /**
     * Size of the tab-expanded input-text, null-termination excluded.
     */
    val size = text.length - 1

    // ---------------------------------------------------------------------------------------------

    /**
     * `line_position.get(i)` returns the index of the first character on line `i`.
     */
    private val line_position: IntArray

    init {
        val positions = mutableListOf(0)
        text.forEachIndexed { i, c -> if (c == '\n') positions.add(i + 1) }
        line_position = positions.toIntArray()
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the line index of the given file offset.
     */
    fun line_from (offset: Int): Int
    {
        assert(offset >= 0 && offset < text.length)
        val line = line_position.binarySearch(offset)

        // Either `offset` is the first char of the line,
        // or `-line - 1` == number of first line starting after `offset`
        return if (line >= 0) line else -line - 2
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index of the given file offset,
     * using the line as a hint to speedup the computation.
     */
    fun column_from (offset: Int, line: Int): Int
    {
        assert(offset >= 0 && offset < text.length)
        assert(line < line_position.size)
        assert(line_position[line] <= offset)
        assert(line == line_position.size - 1 && offset < text.length
            || offset < line_position[line + 1])

        return offset - line_position[line]
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index of the given file offset.
     */
    fun column_from (offset: Int): Int
    {
        assert(offset >= 0 && offset < text.length)
        val line = line_from(offset)
        return offset - line_position[line]
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return a (line, column) pair from the given file offset.
     */
    fun line_column_from (offset: Int): Pair<Int, Int>
    {
        assert(offset >= 0 && offset < text.length)

        val line   = line_from(offset)
        val column = column_from(offset, line)

        return Pair(line, column)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the file offset of the start of the given line.
     */
    fun offset_from (line: Int): Int
        = line_position[line]

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the given line/column pair.
     */
    fun string (line: Int, column: Int): String
    {
        return "line ${line + line_start} column ${column + column_start}"
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation (with line/column info) of the given file offset.
     */
    fun string (offset: Int): String
    {
        val line   = line_from(offset)
        val column = column_from(offset, line)

        return string(line, column)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation (with line/column info) of the given offset range.
     */
    fun range_string (start: Int, end: Int): String
    {
        val l1 = line_from(start)
        val l2 = line_from(end)
        val c1 = column_from(start, l1)
        val c2 = column_from(end,   l2)

        if (l1 != l2)
            return "${string(l1, c1)} to ${string(l2, c2)}"
        else
            return "line ${l1 + line_start} columns ${c1 + column_start} to ${c2 + column_start}"
    }

    // ---------------------------------------------------------------------------------------------
}
