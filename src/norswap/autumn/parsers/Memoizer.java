package norswap.autumn.parsers;

import norswap.autumn.LineMap;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import java.util.function.Predicate;

/**
 * An interface for classes that can memoize (or cache) parse results (in the guise of a {@link
 * MemoEntry}). For use by {@link Memo} or custom parsers.
 */
public interface Memoizer
{
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
     * @param hash computed hash of the entry to recover.
     * @param parser the parser of the entry to recover — memoizer are free to ignore this
     *               depeding on their mode of operation.
     * @param pos starting input position of the entry.
     * @param predicate an additional predicate to check the validity of an entry or discriminate
     *                  between entries. May be null in which case the parameter is ignored.
     */
    MemoEntry get (int hash, Parser parser, int pos, Parse parse, Predicate<Parse> predicate);

    // ---------------------------------------------------------------------------------------------n

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
