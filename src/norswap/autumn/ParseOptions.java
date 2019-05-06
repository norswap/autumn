package norswap.autumn;

import norswap.autumn.visitors.WellFormednessChecker;

/**
 * This class represents a set of options that can be passed to one of the {@link Autumn} {@code
 * .run} methods.
 *
 * <p>To create an instance of this class, call any of its static methods and chain further calls
 * from {@link ParseOptionsBuilder} to select the option you desires. End with {@link
 * ParseOptionsBuilder#get()} to create the option set.
 *
 * <p>An instance may sometimes be reused, but this can not be expected in general.
 * In particular, the non-flag options {@link #metrics} and {@link #well_formed_checker} are
 * stateful!
 *
 * <p>Default configuration:</p>
 * <ul>
 *     <li>{@link #trace} = {@code false}</li>
 *     <li>{@link #record_call_stack} = {@code false}</li>
 *     <li>{@link #well_formed_check} = {@code true}</li>
 *     <li>{@link #metrics} = {@code null}</li>
 *     <li>{@link #well_formed_checker} = {@code null}</li>
 * </ul>
 *
 * <p>It is advised to disable {@link #well_formed_check} in production to avoid its overhead.
 * This is a static check intended to catch problems while constructing a grammar.
 *
 * <p>Setting {@link #metrics} or {@link #well_formed_checker} to null/non-null values sets {@link
 * #trace} or {@link #well_formed_check} (respectively) to {@code false}/{@code true}. Disabling
 * {@link #trace} or {@link #well_formed_check} resets {@link #metrics} or {@link
 * #well_formed_check} (respectively) to null. It's the last call that wins!
 *
 * <p>The canonical documentation for an option is the field through which it is accessible in
 * {@link ParseOptions}.
 */
public final class ParseOptions
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the parse traces its execution. This records performance metrics for each
     * parser (see {@link ParserMetrics}) into {@link Parse#parse_metrics}. Enabling this flag does
     * slow down the execution considerably (around x2 in our initial tests).
     */
    public final boolean trace;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the parse records the stack of parser invocations, made available to
     * parsers via  {@link Parse#call_stack}); as well as the call stack snapshot for the furthest
     * error location ({@link Parse#error)}), made available to parsers via {@link
     * Parse#error_call_stack} and passed on to the {@link ParseResult}.
     */
    public final boolean record_call_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates if Autumn should check that the grammar is well-formed (i.e. does not exhibit
     * unprotected left-recursion nor repetition over nullable parsers) before starting the parse.
     *
     * <p>True by default.
     */
    public final boolean well_formed_check;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specifies a {@link ParseMetrics} object that will receive the trace measurements
     * made during the parse. This can be used to aggregate measurements over multiple parses.
     */
    public final ParseMetrics metrics;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specified the instance of {@link WellFormednessChecker} that should be used
     * to check well-formedness in the grammar.
     *
     * <p>A custom checker is (mostl likely) required if your use custom parsers in your grammar.
     *
     * <p>By default, will be set to a default checker ({@link WellFormednessChecker::new}) if
     * {@link #well_formed_check} is true.
     */
    public final WellFormednessChecker well_formed_checker;

    // ---------------------------------------------------------------------------------------------

    private ParseOptions
        (boolean trace, boolean record_call_stack, boolean well_formed_check,
         ParseMetrics metrics, WellFormednessChecker well_formed_checker)
    {
        this.trace = trace;
        this.record_call_stack = record_call_stack;
        this.well_formed_check = well_formed_check;
        this.metrics = metrics;
        this.well_formed_checker = well_formed_checker;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disabled the {@link ParseOptions#trace} option.
     *
     * <p>If disabling, resets {@link ParseOptions#metrics} to null.
     */
    public static ParseOptionsBuilder trace (boolean enabled) {
        return new ParseOptionsBuilder().trace(enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disables the {@link ParseOptions#record_call_stack} option.
     */
    public static ParseOptionsBuilder record_call_stack (boolean enabled) {
        return new ParseOptionsBuilder().record_call_stack (enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disables the {@link ParseOptions#well_formed_check} option.
     *
     * <p>If disabling, resets the {@link ParseOptions#well_formed_checker} to null.
     */
    public static ParseOptionsBuilder well_formed_check (boolean enabled) {
        return new ParseOptionsBuilder().well_formed_check(enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the {@link ParseOptions#metrics} option and sets {@link ParseOptions#trace}
     * to {@code metrics != null}.
     */
    public static ParseOptionsBuilder metrics (ParseMetrics metrics) {
        return new ParseOptionsBuilder().metrics(metrics);
    }

    // ---------------------------------------------------------------------------------------------´

    /**
     * Sets the {@link ParseOptions#well_formed_checker} option and sets {@link
     * ParseOptions#well_formed_check} to {@code checker != null}.
     */
    public static ParseOptionsBuilder well_formed_checker (WellFormednessChecker checker) {
        return new ParseOptionsBuilder().well_formed_checker(checker);
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
     * Builds a default option set.
     */
    public static ParseOptions get() {
        return new ParseOptionsBuilder().get();
    }

    // ---------------------------------------------------------------------------------------------

    /** See {@link ParseOptions}. */
    public final static class ParseOptionsBuilder
    {
        private boolean trace = false;
        private boolean record_call_stack = false;
        private boolean well_formed_check = true;
        private ParseMetrics metrics = null;
        private WellFormednessChecker well_formed_checker = null;

        private ParseOptionsBuilder() {}

        /**
         * Enables/disabled the {@link ParseOptions#trace} option.
         *
         * <p>If disabling, resets {@link ParseOptions#metrics} to null.
         */
        public ParseOptionsBuilder trace (boolean enabled)
        {
            this.trace = enabled;
            if (!enabled) this.metrics = null;
            return this;
        }

        /**
         * Enables/disables the {@link ParseOptions#record_call_stack} option.
         */
        public ParseOptionsBuilder record_call_stack (boolean enabled)
        {
            this.record_call_stack = enabled;
            return this;
        }

        /**
         * Enables/disables the {@link ParseOptions#well_formed_check} option.
         *
         * <p>If disabling, resets the {@link ParseOptions#well_formed_checker} to null.
         */
        public ParseOptionsBuilder well_formed_check (boolean enabled)
        {
            this.well_formed_check = enabled;
            if (!enabled) this.well_formed_checker = null;
            return this;
        }

        /**
         * Sets the {@link ParseOptions#metrics} option and sets {@link ParseOptions#trace}
         * to {@code metrics != null}.
         */
        public ParseOptionsBuilder metrics (ParseMetrics metrics)
        {
            this.trace = metrics != null;
            this.metrics = metrics;
            return this;
        }

        /**
         * Sets the {@link ParseOptions#well_formed_checker} option and sets {@link
         * ParseOptions#well_formed_check} to {@code checker != null}.
         */
        public ParseOptionsBuilder well_formed_checker (WellFormednessChecker checker)
        {
            this.well_formed_check = checker != null;
            this.well_formed_checker = checker;
            return this;
        }

        /**
         * Builds the set of options.
         */
        public ParseOptions get()
        {
            if (well_formed_check && well_formed_checker == null)
                well_formed_checker = new WellFormednessChecker();
            return new ParseOptions(trace, record_call_stack, well_formed_check,
                metrics, well_formed_checker);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
