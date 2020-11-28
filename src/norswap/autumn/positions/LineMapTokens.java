package norswap.autumn.positions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * See {@link LineMap}.
 * <p>Built on top of {@link LineMapString}.
 */
public final class LineMapTokens implements LineMap
{
    // ---------------------------------------------------------------------------------------------

    public final List<? extends Token> tokens;

    // ---------------------------------------------------------------------------------------------

    public final LineMapString line_map_string;

    // ---------------------------------------------------------------------------------------------

    public LineMapTokens
        (String string, List<? extends Token> tokens, int tab_size, int column_start)
    {
        this.tokens = tokens;
        this.line_map_string = new LineMapString(string, tab_size, column_start);
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    public LineMapTokens (String string, List<? extends Token> tokens) {
        this(string, tokens, 4, 1);
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int line_from (int offset) {
        return line_map_string.line_from(tokens.get(offset).start());
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int column_from (int offset) {
        return line_map_string.column_from(tokens.get(offset).start());
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public Position position_from (int offset) {
        return line_map_string.position_from(tokens.get(offset).start());
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int offset_from (Position position) {
        int string_offset = line_map_string.offset_from(position);
        int result = Collections.binarySearch(tokens, string_offset, Comparator.comparing(
            it -> (it instanceof Integer) ? (Integer) it : ((Token) it).start()));
        return result >= 0 ? result : -result - 2;
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------
}
