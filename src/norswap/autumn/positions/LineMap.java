package norswap.autumn.positions;

/**
 * Enables converting between line/column indices and absolute offsets in either an input string
 * ({@link LineMapString} implementation) or a {@link Token} list ({@link LineMapTokens}
 * implementation).
 * <p>
 * It can also optionally carry a name. This is typically a file path, which enables hyperlinkable
 * positions in some IDEs - see below. Note that in IntelliJ for instance, only absolute file paths
 * enable hyperlinking.
 *
 * <h2>Description</h2>
 * <p>
 * A line map is bound to a given string, kept as a reference. For token maps, it is also bound to a
 * token list. For that string, it enables translating between offsets (absolute character or token
 * indices starting at 0), and line/column indices, which can also be paired in a {@link Position}
 * object. Please note that the columns in a {@link Position} object are expressed in characters,
 * even for {@link LineMapTokens}.
 * <p>
 * Line indices start at 1, as they do in all text editors. The start for column indices is
 * customizable, but the only two useful values are 1 (most editors, and the default here) and 0
 * (Emacs-like editors).
 * <p>
 * Column indices have one additional sophistication: tab characters can span multiple indices, in
 * order to bring the column index in line with the next multiple of the tab size. The tab size is
 * also customizable (via the constructor of the implementations of this interafce), defaulting to
 * 4.
 * <p>
 * In addition to any documented exception, each method is liable to throw a {@link
 * IndexOutOfBoundsException} if passed an out of bounds column, line, or offset.
 *
 * <h2>Hyperlinkable Positions</h2>
 * <p>
 * Some editors (such as IntelliJ, which is really the only one we tried) support linking to
 * intra-file positions when using the {@code <file>:<line>:<column>} format.
 * <p>
 * However, in this case it interprets the column as a character offset, not a character width. In
 * those cases, Autumn is able to produce the correct hyperlinks (through the {@link
 * #string(LineMap, int)} ()} and {@link #string(LineMap, int, int, int)} methods), if the {@code
 * AUTUMN_USE_CHAR_COLUMN} environment variable is set (to any value).
 *
 * <h2>Technicalities</h2>
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
     * Max length of the snippet part of the string returned by {@link #lineSnippet}.
     */
    int MAX_SNIPPET_LENGTH = 100;

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the name of the source, if provided, otherwise {@code null}. If this is a file
     * path, this can enable the printing of hyperlinkable positions in some IDEs like IntelliJ.
     */
    String name();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the line index for the given offset.
     */
    int lineFrom (int offset);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the column index from the given offset.
     */
    int columnFrom (int offset);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a line/column position pair from the given offset.
     */
    Position positionFrom (int offset);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the offset where the given line starts.
     */
    int offsetFor (int line);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the offset where the given line ends (this is the offset at which a newline
     * can be found, or one past the last character of the input).
     */
    int endOffsetFor (int line);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a offset from the given line/column position pair.
     *
     * @throws IllegalArgumentException if the column points inside tab character
     */
    int offsetFrom (Position position);

    // ---------------------------------------------------------------------------------------------

    /**
     * Return an excerpt of the input around the position. This excerpt will be the whole line
     * where the position appears, unless that line's size exceed {@link #MAX_SNIPPET_LENGTH}. In
     * that case the snippet is capped to the max length.
     *
     * <p>The input will also contain a second line consisting only of white space and a caret (^)
     * that points up to the position in the snippet.
     *
     * @throws IllegalArgumentException if the column points inside tab character
     */
    String lineSnippet (Position position);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representing the given offset as "line:column". If the offset is
     * out-of-bounds in the line map, returns the offset followed by " (out of bounds)".
     *
     * <p>Will honor the {@code AUTUMN_USE_CHAR_COLUMN} environment variable, see {@link LineMap}
     * for details.
     */
    default String string (int offset) {
        try {
            return "" + LineMapUtils.positionForDisplay(this, offset);
        }
        catch (IndexOutOfBoundsException e) {
            return "" + offset + " (out of bounds)";
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #string(int)}, but returns a string version of the offset if the {@code map}
     * is {@code null}.
     */
    static String string (LineMap map, int offset) {
        return map == null
            ? "" + offset
            : map.string(offset);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #string(int)}, but will pad the line and the column with spaces so that they
     * reach the minimum indicated width. Spaces are added before the line, and after the column.
     */
    default String string (int offset, int minLineWidth, int minColumnWidth) {
        try {
            Position position = LineMapUtils.positionForDisplay(this, offset);
            String format = "%" + minLineWidth +  "d:%-" + minColumnWidth + "d";
            return String.format(format, position.line, position.column);
        }
        catch (IndexOutOfBoundsException e) {
            return "" + offset + " (out of bounds)";
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #string(int, int, int)}, but returns a string version of the offset if the
     * {@code map} is {@code null}.
     */
    static String string (LineMap map, int offset, int minLineWidth, int minColumnWidth)
    {
        return map == null
            ? "" + offset
            : map.string(offset, minLineWidth, minColumnWidth);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the result of {@link #string(int)} prefixed with the result of {@link #name()} and
     * a colon (":"), yielding and output like "name:line:column".
     *
     * <p>If the offset is out of bounds, the output will look like "(name) offset (out of bounds)"
     * instead.
     */
    default String stringWithName (int offset) {
        String string = string(offset);
        return string.endsWith("(out of bounds)")
            ? String.format("(%s) %s", name(), string)
            : name() + ":" + string;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Same as {@link #stringWithName(int)}, but returns a string version of the offset if the
     * {@code map} is {@code null}.
     */
    static String stringWithName (LineMap map, int offset) {
        return map == null
            ? "" + offset
            : map.stringWithName(offset);
    }

    // ---------------------------------------------------------------------------------------------
}
