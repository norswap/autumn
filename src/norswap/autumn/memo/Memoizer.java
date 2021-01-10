package norswap.autumn.memo;

import norswap.autumn.positions.LineMap;
import norswap.autumn.Parser;
import norswap.autumn.parsers.Memo;
import java.util.Objects;

/**
 * An interface for classes that can memoize (or cache) parse results (in the guise of a {@link
 * MemoEntry}). For use by {@link Memo} or custom parsers.
 *
 * <p>Implementations of this interface will typically want to memoize based on the input position
 * and on an optional "context" object that encapsulates the context on which the result is
 * dependent. It's possible to memoize the parser, in case the {@code Memoizer} instance is shared
 * between multiple parsers (or memoizes the result of multiple sub-parsers).
 *
 * <p>The supplied {@link #hash(boolean, Parser, int, Object)} and {@link #hash(boolean, MemoEntry)}
 * methods help deriving hash codes for both of these scenarios.
 *
 * @see MemoTable
 * @see MemoCache
 * @see NullMemoizer
 */
public interface Memoizer
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a hash value for the given parser (if {@code matchParser} is true), position and
     * context (can be null).
     *
     * <p>Guaranteed never to be 0.
     *
     * <p>These hash values can be used to speed up lookups (when the memoizer takes the parser into
     * account), but a full comparison via {@link MemoEntry#matches} is still required.
     */
    static int hash (boolean matchParser, Parser parser, int pos, Object ctx)
    {
        int h = pos + 1;
        if (matchParser) h = 31*h + Objects.hashCode(parser);
        if (ctx != null)  h = 31*h + ctx.hashCode();
        if (h == 0) h = 1;
        return h;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * {@code return hash(matchParser, e.parser, e.startPosition, e.ctx);}
     * @see #hash(boolean, Parser, int, Object)
     */
    static int hash (boolean matchParser, MemoEntry e) {
        return hash(matchParser, e.parser, e.startPosition, e.ctx);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Insert the given entry, under the assumption that the table doesn't contain it yet (if it
     * does, the entry might end up duplicated).
     */
    void memoize (MemoEntry entry);

    // ---------------------------------------------------------------------------------------------

    /**
     * Retrieve an entry from the memoizer, or null if an entry can't be found.
     *
     * @param parser the parser of the entry to recover — memoizer are free to ignore this
     *               depending on their mode of operation.
     * @param pos starting input position of the entry.
     * @param ctx object representing the context to be compared again the context stored in
     *            candidate entries. May be null.
     */
    MemoEntry get (Parser parser, int pos, Object ctx);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the content of the memoizer (on a single line),
     * converting the input positions using {@code map} (can be null, in which case plain offsets
     * will be used).
     */
    String toString (LineMap map);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a listing of the content of the memoizer (includes newlines), converting the input
     * positions using {@code map} (can be null, in which case plain offsets will be used).
     */
    String listing (LineMap map);

    // ---------------------------------------------------------------------------------------------
}
