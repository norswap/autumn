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

    public final LineMapString lineMapString;

    // ---------------------------------------------------------------------------------------------

    public LineMapTokens
        (String string, List<? extends Token> tokens, int tabSize, int columnStart)
    {
        this.tokens = tokens;
        this.lineMapString = new LineMapString(string, tabSize, columnStart);
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    public LineMapTokens (String string, List<? extends Token> tokens) {
        this(string, tokens, LineMap.tabSizeInit(), 1);
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int lineFrom (int offset) {
        return lineMapString.lineFrom(tokens.get(offset).start());
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int columnFrom (int offset) {
        return lineMapString.columnFrom(tokens.get(offset).start());
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public Position positionFrom (int offset) {
        return lineMapString.positionFrom(tokens.get(offset).start());
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int offsetFrom (Position position) {
        int stringOffset = lineMapString.offsetFrom(position);
        int result = Collections.binarySearch(tokens, stringOffset, Comparator.comparing(
            it -> (it instanceof Integer) ? (Integer) it : ((Token) it).start()));
        return result >= 0 ? result : -result - 2;
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------
}
