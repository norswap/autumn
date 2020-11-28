package norswap.autumn.memo;

import norswap.autumn.positions.LineMap;
import norswap.autumn.Parser;

/**
 * A memoizer that doesn't actually memoize anything.
 *
 * <p>Notably useful to measure the performance improvement brought by memoization.
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
