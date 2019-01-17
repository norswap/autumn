package norswap.autumn;

import java.util.function.Supplier;

import static norswap.utils.Util.cast;

/**
 * Instances of this class defines a kind of parse state — stored in an instance of {@link State} —
 * and handles the retrieval of the {@link State} instance linked to a particular {@link Parse}. A
 * single instance of this class can be used to access mutliple instances of {@link State} linked to
 * multiple different {@link Parse}s.
 *
 * <p>Usually, changes to the parse state will need to be undone upon backtracking. If that is the
 * case, any change to the state object (type {@link State}) must be done through a {@link
 * SideEffect}.
 *
 * <p>This class does not actually store the parse state. Instead it is stored in the {@link
 * Parse#states} map. This class also includes a cache to speed up lookups.
 *
 * <p>Each instance of this class designates his own {@link State} instances in the {@link
 * Parse#states} maps using an object key. Using a unique object ({@code new Object()}) is a good
 * way to create a key that is guaranteed to be unique.
 *
 * <p>Instances of this class are meant to be stored in parsers. Storing the parse state itself in
 * the {@link Parse} object is necessary because parsers are not tied to a particular parse and can
 * be reused.
 *
 * <p>This class caches a (parse, thread) pair. It's possible for multiple parse on different
 * threads to use this kind of parse state (with different instances of {@link State} and {@link
 * Parse}!), but this class will cache the state of a single thread, while the other thread will
 * fall back on querying {@link Parse#states} on access. The cached thread is selected
 * non-deterministically (it's a race). After the parse that owns the cache completes, the cache is
 * evicted, enabling other threads, or another parse on the same thread, to take ownership of the
 * cache.
 *
 * <p>If for performance reasons you really require parse state caching for every thread, give each
 * his own copy of the parser.
 */
public class ParseState<State>
{
    // ---------------------------------------------------------------------------------------------

    private class Cached
    {
        final Parse parse;
        final State state;

        Cached (Parse parse) {
            this.parse = parse;
            this.state = get_or_init_state(parse);
        }
    }

    // ---------------------------------------------------------------------------------------------

    private Cached cached;

    // ---------------------------------------------------------------------------------------------

    /**
     * The key used to access the state in {@link Parse#states}.
     */
    public final Object key;

    // ---------------------------------------------------------------------------------------------

    /**
     * Used to initialize the parse state. Must not return null!
     */
    public final Supplier<State> init;

    // ---------------------------------------------------------------------------------------------

    /**
     * @param key The key used to access the state in {@link Parse#states}.
     * @param init Used to initialize the parse state. Must not return null!
     */
    public ParseState (Object key, Supplier<State> init)
    {
        this.key = key;
        this.init = init;
    }

    // ---------------------------------------------------------------------------------------------

    private State get_or_init_state (Parse parse)
    {
        State state = cast(parse.states.get(key));
        if (state == null) {
            state = init.get();
            if (state == null) throw new Error("state initialized to null");
            parse.states.put(key, state);
            parse.parse_state_kinds.add(this);
        }
        return state;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Return the parse state for the given parse.
     */
    public State state (Parse parse)
    {
        // There are race conditions on cached, but ultimately a single cache entry will
        // prevail, with other threads forced to the slow path. The semantics of the function
        // is preserved during races.

        Cached c = cached;
        if (c == null)
            cached = c = new Cached(parse);
        return c.parse == parse
            ? c.state
            : get_or_init_state(parse); // slow path
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Discard the cached parse state. Automatically called after a parse in order to enable another
     * thread to cache this state.
     */
    void discard_cache (Parse parse)
    {
        if (cached != null && cached.parse == parse)
            cached = null;
    }

    // ---------------------------------------------------------------------------------------------
}
