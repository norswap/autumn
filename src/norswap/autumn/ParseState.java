package norswap.autumn;

import java.util.function.Supplier;

import static norswap.utils.Util.cast;

/**
 * Instances of this class defines a kind of parse state, whose data for a specific {@link Parse} is
 * stored in an instance of {@code Data}.
 *
 * <p>This class handles the retrieval of the {@code Data} instance linked to a particular {@link
 * Parse}. A single instance of this class can be used to access mutliple instances of {@code Data}
 * linked to multiple different {@link Parse}s.
 *
 * <p>Usually, changes to the parse state will need to be undone upon backtracking. If that is the
 * case, any change to the data object ({@code Data}) must be done through a {@link SideEffect}.
 *
 * <p>This class does not actually store the parse state. Instead it is stored in the {@link
 * Parse#stateData} map. This class also includes a cache to speed up lookups.
 *
 * <p>Each instance of this class designates his own {@code Data} instances in the {@link
 * Parse#stateData} maps using a <b>unique</b> object key. The convention is to use a {@link Class}
 * instance whenever it makes sense. Using a unique object ({@code new Object()}) is also a good way
 * to create a key that is guaranteed to be unique.
 *
 * <p>Note that because this class does not store the data, it is fine to have multiple instance
 * of it with the same key — for instance one per parser, if that is more convenient. However you
 * must make SURE that all the instances are constructed with the same {@code Supplier<Data>} (cf.
 * {@link #ParseState(Object, Supplier)}).
 *
 * <p>Instances of this class are meant to be stored in parsers. Storing the parse state data itself
 * in the {@link Parse} object is necessary because parsers are not tied to a particular parse and
 * can be reused.
 *
 * <p>This class caches a (parse, thread) pair. It's possible for multiple parse on different
 * threads to use this kind of parse state (with different instances of {@code Data} and {@link
 * Parse}!), but this class will cache the state of a single thread, while the other thread will
 * fall back on querying {@link Parse#stateData} on access. The cached thread is selected
 * non-deterministically (it's a race). After the parse that owns the cache completes, the cache is
 * evicted, enabling other threads, or another parse on the same thread, to take ownership of the
 * cache.
 *
 * <p>If for performance reasons you really require parse state caching for every thread, give each
 * thread his own copy of the parser.
 */
public class ParseState<Data>
{
    // ---------------------------------------------------------------------------------------------

    private class Cached
    {
        final Parse parse;
        final Data data;

        Cached (Parse parse) {
            this.parse = parse;
            this.data = getOrInitData(parse);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Cached cached;

    // ---------------------------------------------------------------------------------------------

    /**
     * The key used to access the state in {@link Parse#stateData}.
     */
    public final Object key;

    // ---------------------------------------------------------------------------------------------

    /**
     * Used to initialize the parse state data. Must not return null!
     */
    public final Supplier<Data> init;

    // ---------------------------------------------------------------------------------------------

    /**
     * @param key The key used to access the state in {@link Parse#stateData}.
     * @param init Used to initialize the parse state data. Must not return null!
     */
    public ParseState (Object key, Supplier<Data> init)
    {
        this.key = key;
        this.init = init;
    }

    // ---------------------------------------------------------------------------------------------

    private Data getOrInitData (Parse parse)
    {
        Data data = cast(parse.stateData.get(key));
        if (data == null) {
            data = init.get();
            if (data == null) throw new Error("state initialized to null");
            parse.stateData.put(key, data);
            parse.parseStates.add(this);
        }
        return data;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the parse state data for the given parse.
     */
    public Data data (Parse parse)
    {
        // There are race conditions on cached, but ultimately a single cache entry will
        // prevail, with other threads forced to the slow path. The semantics of the function
        // is preserved during races.

        Cached c = cached;
        if (c == null)
            cached = c = new Cached(parse);
        return c.parse == parse
            ? c.data
            : getOrInitData(parse); // slow path
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Discard the cached parse state data. Automatically called after a parse in order to enable
     * another thread to cache his data.
     */
    void discardCache (Parse parse)
    {
        if (cached != null && cached.parse == parse)
            cached = null;
    }

    // ---------------------------------------------------------------------------------------------
}
