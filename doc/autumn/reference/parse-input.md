# `ParseInput` API Reference

Container for a null-terminated, tab-expanded input string, and for the positions
of its newlines.

The class allows conversion between file offsets (0-based indices into the input string) and
line/column indices (also 0-based).

It also offers string representations of line/columns, offset indices and offset ranges,
display line/column information.

### `ParseInput` (constructor)
 
    class ParseInput (
        str: String,
        val tab_size: Int = TAB_SIZE,
        val line_start: Int = LINE_START,
        val column_start: Int = COLUMN_START)
        
Creates a new parse input from the given string. The parse input will append a null character to the
string (which must not contain any such characters to begin with), and will be tab-expanded
(see [`tab_size`] below for details).

See below for a description of the parameters.

The default parameter values are global constants defined in [Conf.kt].

[`tab_size`]: #tab_size
[Conf.kt]: /src/norswap/autumn/Conf.kt

### `tab_size`

    val tab_size: Int
    
The size of tab for tab expansion.

If 0, specifies that tab should not be expanded.
Otherwise, all tab characters will be replaced by space characters so that the tab brings
the line position to the next multiple of `tab_size`.

### `line_start`

    val line_start: Int

Index of the first line (only impacts string representations).
Usually 1, which is the default.

Controls the reported index of the first line. Usually this will be 1.

### `column_start`

    val column_Start: Int
    
Index of the first character in a line (only impacts string representations).
Usually 0 (e.g. Emacs) or 1 (e.g. IntelliJ IDEA). The default is 1.

### `line_from` (offset)

    fun line_from (offset: Int): Int

Returns the line index of the given file offset.

### `column_from` (offset, line)

    fun column_from (offset: Int, line: Int): Int

Returns the column index of the given file offset,
using the line as a hint to speedup the computation.

### `column_from` (offset)

    fun column (offset: Int): Int

Returns the column index of the given file offset.

### `column_line_from`

    fun column_line_from (offset: Int): Pair<Int, Int>

Return a (line, column) pair from the given file offset.

### `offset_from`

    fun offset_from (line: Int): Int

Returns the file offset of the start of the given line.

### `string` (line, column)

    fun string (line: Int, column: Int): String

Returns a string representation of the given line/column pair.

### `string` (offset)

    fun string (offset: Int): String

Returns a string representation (with line/column info) of the given file offset.

### `range_string`

    fun range_string (start: Int, end: Int): String

Returns a string representation (with line/column info) of the given offset range.