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
 * It also offers string representations of line/columns, offset indices and offset ranges.
 * These representations show line/column information and are modified
 * by [lineStart] and [columnStart].
 */
class ParseInput (

    str: String,

    /**
     * See [TAB_SIZE] (used as default).
     */
    val tabSize: Int = TAB_SIZE,

    /**
     * See [LINE_START] (used as default).
     */
    val lineStart: Int = LINE_START,

    /**
     * See [COLUMN_START] (used as default).
     */
    val columnStart: Int = COLUMN_START)
{
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
     * `linePosition[i]` returns the index of the first character on line `i`.
     */
    private val linePositions: IntArray

    init {
        val positions = mutableListOf(0)
        text.forEachIndexed { i, c -> if (c == '\n') positions.add(i + 1) }
        linePositions = positions.toIntArray()
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the line index of the given file offset.
     */
    fun lineFromOffset (offset: Int): Int
    {
        assert(offset >= 0 && offset < text.length)
        val line = linePositions.binarySearch(offset)

        // Either `offset` is the first char of the line,
        // or `-line - 1` == number of first line starting after `offset`
        return if (line >= 0) line else -line - 2
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index of the given file offset,
     * using the line as a hint to speedup the computation.
     */
    fun columnFromOffsetAndLine (offset: Int, line: Int): Int
    {
        assert(offset >= 0 && offset < text.length)
        assert(line < linePositions.size)
        assert(linePositions[line] <= offset)
        assert(line == linePositions.size - 1 && offset < text.length
            || offset < linePositions[line + 1])

        return offset - linePositions[line]
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index of the given file offset.
     */
    fun columnFromOffset (offset: Int): Int
    {
        assert(offset >= 0 && offset < text.length)
        val line = lineFromOffset(offset)
        return offset - linePositions[line]
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return a (line, column) pair from the given file offset.
     */
    fun columnAndLineFromOffset (offset: Int): Pair<Int, Int>
    {
        assert(offset >= 0 && offset < text.length)

        val line   = lineFromOffset(offset)
        val column = columnFromOffsetAndLine(offset, line)

        return Pair(line, column)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the file offset of the start of the given line.
     */
    fun offsetFromLine (line: Int): Int
        = linePositions[line]

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the given line/column pair.
     */
    fun lineAndColumnToString (line: Int, column: Int): String
    {
        return "line ${line + lineStart} column ${column + columnStart}"
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation (with line/column info) of the given file offset.
     */
    fun offsetToString (offset: Int): String
    {
        val line   = lineFromOffset(offset)
        val column = columnFromOffsetAndLine(offset, line)

        return lineAndColumnToString(line, column)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation (with line/column info) of the given offset range.
     */
    fun offsetRangeToString (start: Int, end: Int): String
    {
        val l1 = lineFromOffset(start)
        val l2 = lineFromOffset(end)
        val c1 = columnFromOffsetAndLine(start, l1)
        val c2 = columnFromOffsetAndLine(end,   l2)

        if (l1 != l2)
            return "${lineAndColumnToString(l1, c1)} to ${lineAndColumnToString(l2, c2)}"
        else
            return "line ${l1 + lineStart} columns ${c1 + columnStart} to ${c2 + columnStart}"
    }

    // ---------------------------------------------------------------------------------------------
}
