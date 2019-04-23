package norswap.autumn.parsers;

import norswap.autumn.LineMap;
import norswap.autumn.Parser;
import norswap.utils.NArrays;
import norswap.utils.Strings;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;

/**
 * A {@link Memoizer} implementation that memoizes the last {@code n} results it is passed.
 *
 * <p>The cache has two mode of operations for entry retrieval ({@link Memoizer#get}: if {@link
 * #check_parser} is true, it will only retrieve an entry if the parser is the same as the one
 * specified — otherwise it will return any entry at the specified position that matches the
 * predicate (if supplied).
 *
 * <p>The second mode of operation is useful for memoizing the result of disjunctions.
 */
public final class MemoCache implements Memoizer
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Whether queries to the table should check the parser when returning an entry, or just
     * the start position.
     */
    public final boolean check_parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * The number of entries in this cache.
     */
    public final int num_entries;

    // ---------------------------------------------------------------------------------------------

    private MemoEntry[] entries;

    // ---------------------------------------------------------------------------------------------

    private int next = 0;

    // ---------------------------------------------------------------------------------------------

    public MemoCache (int num_entries, boolean check_parser)
    {
        this.num_entries = num_entries;
        this.check_parser = check_parser;
        this.entries = new MemoEntry[num_entries];
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void memoize (MemoEntry entry)
    {
        entries[next] = entry;
        if (++next == num_entries) next = 0;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public MemoEntry get (int hash, Parser parser, int pos, Object ctx)
    {
        for (int i = 0; i < num_entries; ++i)
        {
            int j = next - 1 - i;
            if (j < 0) j += num_entries;
            if (entries[j] == null)
                return null;
            if (entries[j].matches(hash, pos, check_parser, parser, ctx))
                return entries[j];
        }
        return null;
    }

    // ---------------------------------------------------------------------------------------------

    private String string (String sep, Function<MemoEntry, String> f)
    {
        MemoEntry[] entries = this.entries.clone();
        Arrays.sort(entries, Comparator.comparingInt(x -> x.start_position));
        StringBuilder b = new StringBuilder();
        Strings.separated(b, sep, NArrays.map(entries, new String[0], f));
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toString (LineMap map)
    {
        return "MemoCache { " + string(", ", e -> e.toString(map)) + "}";
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
