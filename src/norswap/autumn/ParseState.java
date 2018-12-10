package norswap.autumn;

import java.util.function.Supplier;

import static norswap.utils.Util.cast;

/**
 * Use this class to store parse state - mutable state accessed by parsers during the parse.
 *
 * <p>Usually, changes to such state will need to be undone upon backtracking. If that is the case,
 * any change to the state object (type {@link State}) must be done through a {@link SideEffect}.
 *
 * <p>This class does not actually store the parse state. Instead it is stored in the {@link
 * Parse#states} map and cached in this class for fast retrieval, thus avoiding the overheads of a
 * map lookup.
 *
 * <p>Each instance of this class designates his own parse state in this {@link Parse#states} using
 * an object key. Using a unique object ({@code new Object()}) is a good solution.
 *
 * <p>Instances of this class are meant to be stored in parsers. Storing the parse state itself in
 * the {@link Parse} object is necessary because parsers are not tied to a particular parse and can
 * be reused.
 *
 * <p>This class caches a (state, parse, thread) triplet. If the parser to which it is attached
 * is reused (different parse) on the same thread, the cached triplet is updated.
 *
 * <p>A single parser can also be shared accross threads. If this is the case, this class
 * will cache the state for a single thread, while the other thread will fall back on
 * querying {@link Parse#states} on access. Note that the parser will remain tied to that thread,
 * even after the parse finishes. A way to fix this is to call {@link #discard_cache()} explicitly.
 *
 * <p>The cached thread is selected non-deterministically, however you can force the selection
 * by ensuring a call to {@link #state(Parse)} completes on your chosen thread before being called
 * on the other threads.
 *
 * <p>If for performance reasons you really require parse state caching for every thread, give each
 * his own copy of the parser.
 *
 * <p>If you're holding on to the parse, but want the parse state to garbage collect when the parse
 * is over, you will need to call {@link #discard_cache()} manually after the parse.
 */
public class ParseState<State>
{
    // ---------------------------------------------------------------------------------------------

    private class Cached
    {
        final Parse parse;
        final Thread thread;
        final State state;

        Cached (Parse parse, Thread thread)
        {
            this.parse = parse;
            this.thread = thread;
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
            cached = c = new Cached(parse, Thread.currentThread());
        else if (c.parse != parse)
            if (c.thread == Thread.currentThread()) // new parse on same thread
                cached = c = new Cached(parse, c.thread);
            else
                return get_or_init_state(parse); // slow path

        return c.state;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Discard the cached parse state. You can call this after a parse is complete to enable the
     * parse state to collect, or to enable another thread to cache this state. It's safe
     * to call this method even if the attached parse might be executing.
     */
    public void discard_cache()
    {
        cached = null;
    }

    // ---------------------------------------------------------------------------------------------
}
