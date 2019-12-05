package norswap.lang.java;

import norswap.autumn.LineMap;
import norswap.autumn.Parser;
import norswap.autumn.memo.MemoEntry;
import norswap.autumn.memo.Memoizer;

/**
 * A memoizer that doesn't actually memoize anything.
 *
 * <p>Used for measuring the performance improvement of memoization.
 */
public final class NullMemoizer implements Memoizer
{
    @Override public void memoize (MemoEntry entry) {
        // do nothing
    }

    @Override public MemoEntry get (Parser parser, int pos, Object ctx) {
        return null; // we have nothing
    }

    @Override public String toString (LineMap map) {
        return super.toString();
    }

    @Override public String listing (LineMap map) {
        return super.toString();
    }
}
