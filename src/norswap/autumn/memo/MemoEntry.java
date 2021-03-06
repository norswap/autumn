package norswap.autumn.memo;

import norswap.autumn.positions.LineMap;
import norswap.autumn.Parser;
import norswap.autumn.SideEffect;
import norswap.autumn.parsers.Memo;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Memoizer} entry, indicating a match over a range of the input, or a failure to match a
 * token at a given position.
 *
 * <p>Such entries are generated by a {@link Memo} parser or by a user-defined custom parser.
 *
 * <p>A failure to match is a valid entry, characterized by a -1 {@link #endPosition} and an empty
 * {@link #delta}.
 */
public final class MemoEntry
{
    // ---------------------------------------------------------------------------------------------

    /** The parser that generated this result. */
    public final Parser parser;

    /** The start position of the match. */
    public final int startPosition;

    /** The end position of the match. */
    public final int endPosition;

    /** List of side-effects generated by the match. */
    public final List<SideEffect> delta;

    /** User-defined contextual information. */
    public final Object ctx;

    // ---------------------------------------------------------------------------------------------

    /**
     * Builds a new memo entry with the given parameters. {@code success} indicates whether the
     * parser succeeded. If false, the end position is overwritten to -1 and the delta is
     * overwritten to an empty list.
     */
    public MemoEntry (
        boolean success, Parser parser, int startPosition, int endPosition,
        List<SideEffect> delta, Object ctx)
    {
        this.parser = parser;
        this.startPosition = startPosition;
        this.endPosition = success ? endPosition : -1;
        this.delta = success ? delta : Collections.emptyList();
        this.ctx = ctx;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff the entry indicates a successful parser invocation.
     */
    public boolean succeeded()
    {
        return endPosition >= 0;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether this entry matches the passed parameters: same starting position, same
     * parser if {@code matcherParser} is true and same context (may be null).
     */
    public boolean matches (boolean matchParser, Parser parser, int startPosition, Object ctx)
    {
        return this.startPosition == startPosition
            && (!matchParser || this.parser == parser)
            && Objects.equals(this.ctx, ctx);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the entry, converting the position using {@code map} (can
     * be null, in which case plain offsets will be used).
     *
     * <p>Compared to {@link #toString(LineMap)}, this generates entries that look good in a dump of
     * a memoization table. This omits the class name, the hash; and the parser names if {@code
     * parserName} is false.
     */
    public String listingString (LineMap map, boolean parserName)
    {
        String start = LineMap.string(map, startPosition);

        if (!succeeded())
            return "at " + start + ": no match";

        StringBuilder b = new StringBuilder(128);

        b   .append("from ")    .append(start)
            .append(" to ")     .append(LineMap.string(map, endPosition));

        if (parserName)
            b.append(": ").append(parser);

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the entry, converting the position using {@code map} (can
     * be null, in which case plain offsets will be used).
     */
    public String toString (LineMap map)
    {
        StringBuilder b = new StringBuilder(128);

        b   .append("MemoEntry {")
            .append("{ parser = ") .append(parser)
            .append(", ");

        if (succeeded())
            b   .append("range = [")
                .append(LineMap.string(map, startPosition))
                .append(" - ")
                .append(LineMap.string(map, endPosition))
                .append("]");
        else
            b   .append("no match");

        b.append(" }");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}
