package norswap.autumn.parsers;

import norswap.autumn.LineMap;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.SideEffect;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

    /** The {@link Tokens} instance this cache belongs to. */
    private final Tokens tokens;

    // ---------------------------------------------------------------------------------------------

     TokenCache (Tokens tokens) {
        this.tokens = tokens;
     }

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

                cache[i] = pos + 1;
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

        cache[i] = displacement << 32 + pos + 1;
        results[i] = res;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Inserts the given (pos, result) pair into the cache.
     */
    private void cache (int pos, TokenResult res)
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

        while (p != pos && p >= 0 && d <= max_displacement) {
            if (++i == cache.length) i = 0;
            p = (int) cache[i] - 1;
            ++d;
        }

        return p == pos ? results[i] : null;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Fills the cache with the result for the current position, and return the inserted result.
     *
     * <p>Assumes no result for that position exist yet.
     */
    TokenResult fill_cache (Parse parse)
    {
        int pos0 = parse.pos;
        int log0 = parse.log.size();

        int longest = -1;
        int max_pos = pos0;
        List<SideEffect> delta = null;
        Parser[] parsers = tokens.parsers;

        for (int i = 0; i < parsers.length; ++i)
        {
            boolean success = parsers[i].parse(parse);

            if (success) {
                if (parse.pos > max_pos) {
                    max_pos = parse.pos;
                    delta = parse.delta(log0);
                    longest = i;
                }

                parse.pos = pos0;
                parse.rollback(log0);
            }
        }

        TokenResult result = delta == null
            ? TokenResult.none(pos0)
            : new TokenResult(longest, pos0, max_pos, delta);

        cache(pos0, result);
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    private String result_to_string (TokenResult r, LineMap map)
    {
        String start = LineMap.string(map, r.start_position);
        return !r.matched()
            ? "at " + start + ": no match"
            : "from " + start + " to " + LineMap.string(map, r.end_position)
            + ": " + tokens.parsers[r.parser];
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a textual representation of the content of the token cache, converting
     * the position using {@code map} as appropriate (can be null).
     */
    public String toString (LineMap map)
    {
        TokenResult[] res = results.clone();
        Arrays.sort(res, Comparator.comparingInt(x ->
            x == null ? Integer.MAX_VALUE : x.start_position));

        StringBuilder b = new StringBuilder();

        for (TokenResult r: res) {
            if (r == null) break;
            b.append(result_to_string(r, map)).append('\n');
        }

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString()
    {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}
