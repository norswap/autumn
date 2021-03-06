package norswap.autumn.positions;

import norswap.utils.Vanilla;
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
        (String name, String string, List<? extends Token> tokens, int tabSize, int columnStart)
    {
        this.tokens = tokens;
        this.lineMapString = new LineMapString(name, string, tabSize, columnStart);
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    public LineMapTokens (String name, String string, List<? extends Token> tokens) {
        this(name, string, tokens, 4, 1);
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public String name() {
        return lineMapString.name();
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    private int tokenOffsetToStringOffset (int tokenOffset)
    {
        if (tokenOffset < 0 || tokenOffset > tokens.size())
            throw new IndexOutOfBoundsException("token offset: " + tokenOffset);

        return tokenOffset < tokens.size()
            ? tokens.get(tokenOffset).start()
            : tokens.isEmpty() ? 0 : Vanilla.last(tokens).end();
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    /**
     * Converts a string offset to a token list offset.
     */
    private int stringOffsetToTokenOffset (int stringOffset)
    {
        int result = Collections.binarySearch(tokens, stringOffset, Comparator.comparing(
            it -> (it instanceof Integer) ? (Integer) it : ((Token) it).start()));

        // If the string offset falls within a token, result is -next_token, so we need to adjust.
        return result >= 0 ? result : -result - 2;
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int offsetFor (int line) {
        return stringOffsetToTokenOffset(lineMapString.offsetFor(line));
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int endOffsetFor (int line) {
        return stringOffsetToTokenOffset(lineMapString.endOffsetFor(line));
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int lineFrom (int offset) {
        return lineMapString.lineFrom(tokenOffsetToStringOffset(offset));
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int columnFrom (int offset) {
        return lineMapString.columnFrom(tokenOffsetToStringOffset(offset));
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public Position positionFrom (int offset) {
        return lineMapString.positionFrom(tokenOffsetToStringOffset(offset));
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public int offsetFrom (Position position) {
        return stringOffsetToTokenOffset(lineMapString.offsetFrom(position));
    }

    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------

    @Override public String lineSnippet (Position position) {
        return lineMapString.lineSnippet(position);
    }


    // -----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–-----–---------
}
