package norswap.autumn.parsers;

import norswap.autumn.LineMap;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.utils.NArrays;
import norswap.utils.Strings;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A {@link Memoizer} implementation that memoizes every result it is passed.
 *
 * <p>The table has two mode of operations for entry retrieval ({@link #get}: if {@link
 * #check_parser} is true, it will only retrieve an entry if the parser is the same as the one
 * specified — otherwise it will return any entry at the specified position that matches
 * the predicate (if supplied).
 *
 * <p>The second mode of operation is useful for memoizing the result of disjunctions.
 * It's notably needed for token memoization via {@link Tokens}.
 */
public final class MemoTable implements Memoizer
{
    // ---------------------------------------------------------------------------------------------

    /** Max load factor for the table. */
    private static final double MAX_LOAD = 0.8;

    /** Max displacement from initial position in the table. */
    private long max_displacement = 0;

    /** Amount of cache slots occupied. */
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
     * Whether queries to the table should check the parser when returning an entry, or just
     * the start position.
     */
    public final boolean check_parser;

    // ---------------------------------------------------------------------------------------------

    public MemoTable (boolean check_parser) {
        this.check_parser = check_parser;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Insert the given entry in the table, under the assumption that the table is large enough and
     * does not already contain the entry (if it does, it will be duplicated). Does not update
     * {@link #occupied}.
     */
    private void insert (MemoEntry entry)
    {
        int hash = entry.hash;
        int i = hash % hashes.length;
        long displacement = 0;

        while (hashes[i] != 0)
        {
            long d = hashes[i] >>> 32;

            if (d <= displacement)
            {
                int pos2 = (int) hashes[i];
                MemoEntry entry2 = entries[i];

                hashes[i] = (displacement << 32) + hash;
                entries[i] = entry;

                if (displacement > max_displacement)
                    max_displacement = displacement;

                hash = pos2;
                entry = entry2;
                displacement = d;
            }

            ++displacement;
            if (++i == hashes.length)
                i = 0;
        }

        if (displacement > max_displacement)
            max_displacement = displacement;

        hashes[i] = (displacement << 32) + hash;
        entries[i] = entry;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void memoize (MemoEntry entry)
    {
        if (++occupied / (double) hashes.length > MAX_LOAD)
        {
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

    @Override
    public MemoEntry get (int hash, Parser parser, int pos, Parse parse, Predicate<Parse> predicate)
    {
        if (hash == 0)
            throw new IllegalArgumentException("Memo entry hashes may not be 0");

        int i = hash % hashes.length;
        int h = (int) hashes[i]; // stored hash
        int d = 0; // displacement

        while (true)
        {
            if (h == hash) {
                MemoEntry e = entries[i];
                if (e.start_position == pos
                && (!check_parser || e.parser == parser)
                && (predicate == null || predicate.test(parse)))
                    return e;
            }

            if (h == 0 || d > max_displacement)
                return null;

            if (++i == hashes.length) i = 0;
            h = (int) hashes[i];
            ++d;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private String string (String sep, Function<MemoEntry, String> f)
    {
        MemoEntry[] entries = NArrays.packed(this.entries);
        Arrays.sort(entries, Comparator.comparingInt(x -> x.start_position));
        StringBuilder b = new StringBuilder();
        Strings.separated(b, sep, NArrays.map(entries, new String[0], f));
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
        return string("\n", e -> e.listing_string(map, check_parser));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString() {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}
