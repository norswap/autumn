package norswap.autumn.parsers;

import norswap.autumn.LineMap;
import norswap.utils.Strings;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Implementation of a cache that maps input positions to tokens.
 *
 * <p>See {@link Tokens}.
 */
final class TokenCache
{
    // ---------------------------------------------------------------------------------------------

    /** Max load factor for the cache. */
    private static final double MAX_LOAD = 0.8;

    /** Max displacement from initial position in the cache. */
    private long max_displacement = 0;

    /** Amount of cache slots occupied. */
    private int occupied = 0;

    /**
     * Hashmap storage for position indices. The value at an index is either 0, or
     * a long whose 32 high-order bits are a displacement, and whose 32 low-order bits are
     * the input position itself + 1 (to avoid collision with 0 when the displacement is 0 too).
     *
     * <p>The tokenization results themselves are stored at the same index in {@link #results}.
     */
    private long[] cache = new long[1024];

    /** cf. {@link #cache} */
    private TokenResult[] results = new TokenResult[1024];

     // ---------------------------------------------------------------------------------------------

    /**
     * Insert the given (position, result) pair in the cache, under the assumption that the cache is
     * large enough. Does not update {@link #occupied}.
     */
    private void insert (int pos, TokenResult res)
    {
        int i = pos % cache.length;
        long displacement = 0;

        while (cache[i] != 0)
        {
            long d = cache[i] >>> 32;

            if (d <= displacement)
            {
                int pos2 = (int) cache[i] - 1;
                TokenResult res2 = results[i];

                cache[i] = (displacement << 32) + pos + 1;
                results[i] = res;

                if (displacement > max_displacement)
                    max_displacement = displacement;

                pos = pos2;
                res = res2;
                displacement = d;
            }

            ++displacement;
            if (++i == cache.length)
                i = 0;
        }

        if (displacement > max_displacement)
            max_displacement = displacement;

        cache[i] = (displacement << 32) + pos + 1;
        results[i] = res;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Inserts the given (pos, result) pair into the cache.
     */
    void put (int pos, TokenResult res)
    {
        if (++occupied / (double) cache.length > MAX_LOAD)
        {
            long[] old_cache = cache;
            TokenResult[] old_results = results;

            cache   = new long        [cache.length * 2];
            results = new TokenResult [cache.length * 2];

            for (int j = 0; j < old_cache.length; ++j)
                if (old_cache[j] != 0)
                    insert((int) old_cache[j] - 1, old_results[j]);
        }

        insert(pos, res);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Retrieve a result from the cache or null if the position is not in the cache.
     */
    TokenResult get (int pos)
    {
        int i = pos % cache.length;
        int p = (int) cache[i] - 1; // position
        int d = 0; // displacement

        while (p != pos && p >= 0 && d < max_displacement) {
            if (++i == cache.length) i = 0;
            p = (int) cache[i] - 1;
            ++d;
        }

        return p == pos ? results[i] : null;
    }

    // ---------------------------------------------------------------------------------------------

    private String result_to_string (Tokens tokens, TokenResult r, LineMap map)
    {
        String start = LineMap.string(map, r.start_position);
        return !r.matched()
            ? "at " + start + ": no match"
            : "from " + start + " to " + LineMap.string(map, r.end_position)
                + ": " + (tokens != null ? tokens.parsers[r.parser] : r.parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the content of the token cache, converting
     * the position using {@code map}, and {@code tokens} (to print parser names) as appropriate
     * (both can be null).
     */
    String toString (Tokens tokens, LineMap map)
    {
        TokenResult[] res = results.clone();
        Arrays.sort(res, Comparator.comparingInt(x ->
            x == null ? Integer.MAX_VALUE : x.start_position));

        StringBuilder b = new StringBuilder();

        for (TokenResult r: res) {
            if (r == null) break;
            b.append(result_to_string(tokens, r, map)).append('\n');
        }

        if (res.length > 0 && res[0] != null)
            Strings.pop(b, 1); // remove trailing newline

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString()
    {
        return toString(null, null);
    }

    // ---------------------------------------------------------------------------------------------
}
