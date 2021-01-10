package norswap.autumn;

import norswap.autumn.positions.Span;
import java.util.HashMap;
import java.util.function.Supplier;

import static norswap.utils.Util.cast;

/**
 * This class represents a set of options that can be passed to one of the {@link Autumn} {@code
 * .run} methods.
 *
 * <p>It is also possible for users to define their own custom options (associated with a unique
 * key).
 *
 * <p>To create an instance of this class, call any of its static methods and chain further calls
 * from {@link ParseOptionsBuilder} to select the option you desires. End with {@link
 * ParseOptionsBuilder#get()} to create the option set.
 *
 * <p>Instances may usually be reused, but beware that {@link #metrics} return an object that is
 * shared accross parses. For one, this object is not thread-safe, and for two, sharing it might not
 * be what you want.
 *
 * <p>The canonical documentation for an option is the field through which it is accessible in
 * {@link ParseOptions}.
 *
 * <p>It is advised to disable {@link #wellFormednessCheck} in production to avoid its overhead.
 * This is a static check intended to catch problems while constructing a grammar.
 *
 * <hr>
 *
 * <p><b>Default configuration:</b>
 *
 * <ul>
 *     <li>{@link #trace} = {@code false}</li>
 *     <li>{@link #recordCallStack} = {@code false}</li>
 *     <li>{@link #wellFormednessCheck} = {@code true}</li>
 *     <li>{@link #metrics} = {@code null}</li>
 * </ul>
 *
 * <p>The code ensures that if {@link #trace} is true/false, its corresponding {@link #metrics}
 * object is non-null/null (this works both ways).
 *
 * <p>If {@link #trace} is set to true while the corresponding {@link #metrics} object is null, it
 * will be assigned a default value ({@link ParseMetrics}'s default constructor).
 *
 * <p>If multiple conflicting builder method calls occur, the last call always takes precedence!
 */
public final class ParseOptions
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the parse traces its execution. This records performance metrics for each
     * parser (see {@link ParserMetrics}) into {@link Parse#parseMetrics}. Enabling this flag does
     * slow down the execution considerably (around x2 in our initial tests).
     */
    public final boolean trace;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the parse records the stack of parser invocations, made available to
     * parsers via  {@link Parse#callStack}); as well as the call stack snapshot for the furthest
     * error location ({@link Parse#error}), made available to parsers via {@link
     * Parse#errorCallStack} and passed on to the {@link ParseResult}.
     */
    public final boolean recordCallStack;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates if Autumn should check that the grammar is well-formed (i.e. does not exhibit
     * unprotected left-recursion nor repetition over nullable parsers) before starting the parse.
     *
     * <p>True by default.
     */
    public final boolean wellFormednessCheck;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether we should track whitespace positions in order to generate better
     * {@link Span} objects. This tracking comes with a very slight runtime overhead.
     *
     * <p>True by default.
     */
    public final boolean trackWhitespace;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specifies a function returning a {@link ParseMetrics} object that will receive
     * the trace measurements made during the parse. You can aggregate measurements over multiple
     * parses by returning the same {@link ParseMetrics}.
     */
    public final Supplier<ParseMetrics> metrics;

    // ---------------------------------------------------------------------------------------------

    /**
     * A map contain user-defined options.
     */
    private final HashMap<Object, Object> customOptions;

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the custom option for the given key, automatically casting it to the required type.
     */
    public <T> T get (Object key) {
        return cast(customOptions.get(key));
    }

    // ---------------------------------------------------------------------------------------------

    private ParseOptions
        (boolean trace, boolean recordCallStack, boolean wellFormednessCheck,
         boolean trackWhitespace,
         Supplier<ParseMetrics> metrics, HashMap<Object, Object> customOptions)
    {
        this.trace = trace;
        this.recordCallStack = recordCallStack;
        this.wellFormednessCheck = wellFormednessCheck;
        this.trackWhitespace = trackWhitespace;
        this.metrics = metrics;
        this.customOptions = customOptions;
    }

    // =============================================================================================

    /**
     * Defines a new custom option with the given key. The key should be chosen such as to not
     * clash with other keys: use a {@code Class} object, not a {@code String}!
     */
    public static ParseOptionsBuilder custom (Object key, Object value) {
        return new ParseOptionsBuilder().custom(key, value);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disabled the {@link ParseOptions#trace} option.
     *
     * <p>May affect {@link ParseOptions#metrics}, see {@link ParseOptions}.
     */
    public static ParseOptionsBuilder trace (boolean enabled) {
        return new ParseOptionsBuilder().trace(enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disables the {@link ParseOptions#recordCallStack} option.
     */
    public static ParseOptionsBuilder recordCallStack (boolean enabled) {
        return new ParseOptionsBuilder().recordCallStack(enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disables the {@link ParseOptions#wellFormednessCheck} option.
     */
    public static ParseOptionsBuilder wellFormednessCheck (boolean enabled) {
        return new ParseOptionsBuilder().wellFormednessCheck(enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the {@link ParseOptions#metrics} option and sets {@link ParseOptions#trace}
     * to {@code metrics != null}.
     */
    public static ParseOptionsBuilder metrics (Supplier<ParseMetrics> metrics) {
        return new ParseOptionsBuilder().metrics(metrics);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a parse options builder with the default options (see {@link ParseOptions}).
     */
    public static ParseOptionsBuilder builder() {
        return new ParseOptionsBuilder();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Builds a default option set (see {@link ParseOptions}).
     */
    public static ParseOptions get() {
        return new ParseOptionsBuilder().get();
    }

    // =============================================================================================

    /**
     * See {@link ParseOptions}.
     */
    public final static class ParseOptionsBuilder
    {
        private boolean trace = false;
        private boolean recordCallStack = false;
        private boolean wellFormednessCheck = true;
        private boolean trackWhitespace = true;
        private Supplier<ParseMetrics> metrics = null;
        private final HashMap<Object, Object> customOptions = new HashMap<>();

        private ParseOptionsBuilder() {}

        /**
         * Defines a new custom option with the given key. The key should be chosen such as to not
         * clash with other keys: use a {@code Class} object, not a {@code String}!
         */
        public ParseOptionsBuilder custom (Object key, Object value) {
            customOptions.put(key, value);
            return this;
        }

        /**
         * Enables/disabled the {@link ParseOptions#trace} option.
         *
         * <p>May affect {@link ParseOptions#metrics}, see {@link ParseOptions}.
         */
        public ParseOptionsBuilder trace (boolean enabled)
        {
            trace = enabled;
            if (!enabled) metrics = null;
            else if (metrics == null) metrics = ParseMetrics::new;
            return this;
        }

        /**
         * Enables/disables the {@link ParseOptions#recordCallStack} option.
         */
        public ParseOptionsBuilder recordCallStack (boolean enabled)
        {
            recordCallStack = enabled;
            return this;
        }

        /**
         * Enables/disables the {@link ParseOptions#wellFormednessCheck} option.
         */
        public ParseOptionsBuilder wellFormednessCheck (boolean enabled)
        {
            wellFormednessCheck = enabled;
            return this;
        }

        /**
         * Enables/disables the {@link ParseOptions#trackWhitespace} option.
         */
        public ParseOptionsBuilder trackWhitespace (boolean enabled)
        {
            trackWhitespace = enabled;
            return this;
        }

        /**
         * Sets the {@link ParseOptions#metrics} option and sets {@link ParseOptions#trace}
         * to {@code metrics != null}.
         */
        public ParseOptionsBuilder metrics (Supplier<ParseMetrics> metrics)
        {
            this.trace = metrics != null;
            this.metrics = metrics;
            return this;
        }

        /**
         * Builds the set of options.
         */
        public ParseOptions get()
        {
            return new ParseOptions(trace, recordCallStack, wellFormednessCheck,
                trackWhitespace, metrics, customOptions);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
