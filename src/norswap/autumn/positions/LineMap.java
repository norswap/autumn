package norswap.autumn.positions;

/**
 * Enables converting between line/column indices and absolute offsets in either an input string
 * ({@link LineMapString} implementation) or a {@link Token} list ({@link LineMapTokens}
 * implementation).
 * <p>
 * A line map is bound to a given string, kept as a reference. For token maps, it is also bound to a
 * token list. For that string, it enables translating between offsets (absolute character indices
 * starting at 0), and line/column indices, which can also be paired in a {@link Position} object.
 * <p>
 * Line indices start at 1, as they do in all text editors. The start for column indices is
 * customizable, but the only two useful values are 1 (most editors, and the default here) and 0
 * (Emacs-like editors).
 * <p>
 * Column indices have one additional sophistication: tab characters can span multiple indices,
 * in order to bring the column index in line with the next multiple of the tab size. The tab size
 * is also customizable (defaulting to 4).
 * <p>
 * The valid offset range is [0, string.length] for {@link LineMapString} and [0, tokens.length]
 * for {@link LineMapTokens}.
 * <p>
 * There are as many line indices as the number of newline character in the files + 1
 * (the first line).
 * <p>
 * Given a valid line index, the valid column indices for that line are those that can successfully
 * be mapped to a valid offset that is not located on another line. Note that newline characters are
 * considered to be part of the line they follow. The range of valid column indices is potentially
 * discontinuous, because some of these indices can map to "the inside" of a tab character, and as
 * such cannot be mapped to a valid offset.
 * <p>
 * Getting a position from an offset is O(log(number_of_lines) + column_length) for both strings
 * and tokens. For strings, getting an offset from a position is O(column_length), while for
 * tokens it's O(log(tokens_length) + column_length).
 */
public interface LineMap
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the line index for the given string offset.
     */
    int line_from (int offset);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index from the given string offset.
     */
    int column_from (int offset);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a line/column position pair from the given string offset.
     */
    Position position_from (int offset);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string offset from the given line/column position pair.
     * @throws IndexOutOfBoundsException if the position does not match a valid string offset.
     */
    int offset_from (Position position);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representing the given offset, using the given line map.
     *
     * <p>If {@code map} is null, simply return the offset. Otherwise, returns "line:column"
     * according to the line map. If the offset is out-of-bounds in the line map, returns
     * the offset followed by " (out of bounds)".
     */
    static String string (LineMap map, int offset)
    {
        try {
            return map == null
                ? "" + offset
                : "" + map.position_from(offset);
        }
        catch (IndexOutOfBoundsException e) {
            return "" + offset + " (out of bounds)";
        }
    }

    // ---------------------------------------------------------------------------------------------
}
