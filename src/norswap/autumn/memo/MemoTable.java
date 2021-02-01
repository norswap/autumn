package norswap.autumn.memo;

import norswap.autumn.positions.LineMap;
import norswap.autumn.Parser;
import norswap.utils.NArrays;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

import static norswap.utils.Strings.joinArray;

/**
 * A {@link Memoizer} implementation that memoizes every result it is passed.
 *
 * <p>The table has two mode of operations depending on its {@link #matchParser} parameter. If
 * true, it will take into account the parser when storing/retrieving entries — otherwise it will
 * only take into account the input position and the optional context object.
 */
public final class MemoTable implements Memoizer
{
    // ---------------------------------------------------------------------------------------------

    /** Max load factor for the table. */
    private static final double MAX_LOAD = 0.8;

    /** Max displacement from initial position in the table. */
    private long maxDisplacement = 0;

    /** Amount of table slots occupied. */
    private int occupied = 0;

    /**
     * Hashmap storage for the hashes of the stored entries. The value at an index is either 0, or
     * a long whose 32 high-order bits are a displacement, and whose 32 low-order bits is the
     * hash (which must not be 0, so that hash[x] == 0 signifies an empty slot).
     *
     * <p>The memoized entries themselves are stored at the same index in {@link #entries}.
     */
    private long[] hashes = new long[8];

    /** cf. {@link #hashes} */
    private MemoEntry[] entries = new MemoEntry[8];

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether queries to the table should use parser information when storing/retrieving an entry,
     * or just the start position and optional context object.
     */
    public final boolean matchParser;

    // ---------------------------------------------------------------------------------------------

    public MemoTable (boolean matchParser) {
        this.matchParser = matchParser;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Insert the given entry in the table, under the assumption that the table is large enough and
     * does not already contain the entry (if it does, it will be duplicated). Does not update
     * {@link #occupied}.
     */
    private void insert (MemoEntry entry)
    {
        int hash = Memoizer.hash(matchParser, entry);
        int i = (hash & 0x7FFFFFFF) % hashes.length; // non-negative index
        long displacement = 0;

        while (hashes[i] != 0) // as long as we haven't found an empty spot
        {
            long d = hashes[i] >>> 32;

            if (d <= displacement)
            {
                // Found an entry with less displacement than the one we're trying to insert.
                // Insert the later here and carry on trying to insert the former.

                int pos2 = (int) hashes[i];
                MemoEntry entry2 = entries[i];

                hashes[i] = (displacement << 32) + hash;
                entries[i] = entry;

                if (displacement > maxDisplacement)
                    maxDisplacement = displacement;

                hash = pos2;
                entry = entry2;
                displacement = d;
            }

            ++displacement;
            if (++i == hashes.length)
                i = 0;
        }

        if (displacement > maxDisplacement)
            maxDisplacement = displacement;

        hashes[i] = (displacement << 32) + hash;
        entries[i] = entry;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void memoize (MemoEntry entry)
    {
        if (++occupied / (double) hashes.length > MAX_LOAD)
        {
            // rehash
            int len0 = hashes.length;
            MemoEntry[] entries0 = entries;

            hashes = new long        [len0 * 2];
            entries = new MemoEntry  [len0 * 2];

            for (int j = 0; j < len0; ++j)
                if (entries0[j] != null)
                    insert(entries0[j]);
        }

        insert(entry);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public MemoEntry get (Parser parser, int pos, Object ctx)
    {
        int hash = Memoizer.hash(matchParser, parser, pos, ctx);
        int i = (hash & 0x7FFFFFFF) % hashes.length; // non-negative index
        int d = 0; // displacement

        while (true)
        {
            int h = (int) hashes[i]; // stored hash

            if (h == hash && entries[i].matches(matchParser, parser, pos, ctx))
                return entries[i];

            if (h == 0 || d > maxDisplacement)
                return null;

            if (++i == hashes.length) i = 0;
            ++d;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private String string (String sep, Function<MemoEntry, String> f)
    {
        MemoEntry[] entries = NArrays.packed(this.entries);
        Arrays.sort(entries, Comparator.comparingInt(x -> x.startPosition));
        StringBuilder b = new StringBuilder();
        joinArray(b, sep, NArrays.map(entries, new String[0], f));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString (LineMap map)
    {
        return "MemoTable { " + string(", ", e -> e.toString(map)) + "}";
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String listing (LineMap map)
    {
        return string("\n", e -> e.listingString(map, matchParser));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}
