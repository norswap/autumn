package norswap.autumn;

import norswap.autumn.visitors.WellFormednessChecker;
import java.util.function.Supplier;

/**
 * This class represents a set of options that can be passed to one of the {@link Autumn} {@code
 * .run} methods.
 *
 * <p>To create an instance of this class, call any of its static methods and chain further calls
 * from {@link ParseOptionsBuilder} to select the option you desires. End with {@link
 * ParseOptionsBuilder#get()} to create the option set.
 *
 * <p>Instances may usually be reused, but beware if {@link #metrics} and {@link
 * #well_formedness_checker} return objects that are shared accross parses. For one, these objects
 * are not thread-safe, and for two, sharing them might not be what you want.
 *
 * <p>The canonical documentation for an option is the field through which it is accessible in
 * {@link ParseOptions}.
 *
 * <p>It is advised to disable {@link #well_formedness_check} in production to avoid its overhead.
 * This is a static check intended to catch problems while constructing a grammar.
 *
 * <hr>
 *
 * <p><b>Default configuration:</b>
 *
 * <ul>
 *     <li>{@link #trace} = {@code false}</li>
 *     <li>{@link #record_call_stack} = {@code false}</li>
 *     <li>{@link #well_formedness_check} = {@code true}</li>
 *     <li>{@link #metrics} = {@code null}</li>
 *     <li>{@link #well_formedness_checker} = {@link WellFormednessChecker#WellFormednessChecker()}</li>
 * </ul>
 *
 * <p>The code ensures that if {@link #trace} or {@link #well_formedness_check} is true/false, its
 * corresponding object ({@link #metrics} or {@link #well_formedness_checker}, respectively) is
 * non-null/null (this works both ways).
 *
 * <p>If {@link #trace} or {@link #well_formedness_check} is set to true while the corresponding
 * object is null, it will be assigned a default value ({@link ParseMetrics}'s default constructor
 * or {@link WellFormednessChecker#WellFormednessChecker()}, respectively).
 *
 * <p>If multiple conflicting builder method calls occur, the last call always takes precedence!
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
    public final boolean well_formedness_check;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specifies a function returning a {@link ParseMetrics} object that will receive
     * the trace measurements made during the parse. You can aggregate measurements over multiple
     * parses by returning the same {@link ParseMetrics}.
     */
    public final Supplier<ParseMetrics> metrics;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specified a function returning the instance of {@link WellFormednessChecker}
     * that should be used to check well-formedness in the grammar.
     *
     * <p>A custom checker is (mostl likely) required if your use custom parsers in your grammar.
     *
     * <p>By default, set to {@link WellFormednessChecker::new} (which handles all built-in
     * parsers).
     */
    public final Supplier<WellFormednessChecker> well_formedness_checker;

    // ---------------------------------------------------------------------------------------------

    private ParseOptions
        (boolean trace, boolean record_call_stack, boolean well_formedness_check,
         Supplier<ParseMetrics> metrics, Supplier<WellFormednessChecker> well_formedness_checker)
    {
        this.trace = trace;
        this.record_call_stack = record_call_stack;
        this.well_formedness_check = well_formedness_check;
        this.metrics = metrics;
        this.well_formedness_checker = well_formedness_checker;
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
     * Enables/disables the {@link ParseOptions#record_call_stack} option.
     */
    public static ParseOptionsBuilder record_call_stack (boolean enabled) {
        return new ParseOptionsBuilder().record_call_stack (enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Enables/disables the {@link ParseOptions#well_formedness_check} option.
     *
     * <p>May affect {@link ParseOptions#well_formedness_checker}, see {@link ParseOptions}.
     */
    public static ParseOptionsBuilder well_formedness_check (boolean enabled) {
        return new ParseOptionsBuilder().well_formedness_check(enabled);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the {@link ParseOptions#metrics} option and sets {@link ParseOptions#trace}
     * to {@code metrics != null}.
     */
    public static ParseOptionsBuilder metrics (Supplier<ParseMetrics> metrics) {
        return new ParseOptionsBuilder().metrics(metrics);
    }

    // ---------------------------------------------------------------------------------------------´

    /**
     * Sets the {@link ParseOptions#well_formedness_checker} option and sets {@link
     * ParseOptions#well_formedness_check} to {@code checker != null}.
     */
    public static ParseOptionsBuilder well_formedness_checker (Supplier<WellFormednessChecker> checker) {
        return new ParseOptionsBuilder().well_formedness_checker(checker);
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

    // ---------------------------------------------------------------------------------------------

    /**
     * See {@link ParseOptions}.
     */
    public final static class ParseOptionsBuilder
    {
        private boolean trace = false;
        private boolean record_call_stack = false;
        private boolean well_formedness_check = true;
        private Supplier<ParseMetrics> metrics = null;
        private Supplier<WellFormednessChecker> well_formedness_checker = WellFormednessChecker::new;

        private ParseOptionsBuilder() {}

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
         * Enables/disables the {@link ParseOptions#record_call_stack} option.
         */
        public ParseOptionsBuilder record_call_stack (boolean enabled)
        {
            record_call_stack = enabled;
            return this;
        }

        /**
         * Enables/disables the {@link ParseOptions#well_formedness_check} option.
         *
         * <p>May affect {@link ParseOptions#well_formedness_checker}, see {@link ParseOptions}.
         */
        public ParseOptionsBuilder well_formedness_check (boolean enabled)
        {
            well_formedness_check = enabled;
            if (!enabled) well_formedness_checker = null;
            else if (well_formedness_checker == null)
                well_formedness_checker = WellFormednessChecker::new;
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
         * Sets the {@link ParseOptions#well_formedness_checker} option and sets {@link
         * ParseOptions#well_formedness_check} to {@code checker != null}.
         */
        public ParseOptionsBuilder well_formedness_checker (Supplier<WellFormednessChecker> checker)
        {
            well_formedness_check = checker != null;
            well_formedness_checker = checker;
            return this;
        }

        /**
         * Builds the set of options.
         */
        public ParseOptions get()
        {
            return new ParseOptions(trace, record_call_stack, well_formedness_check,
                metrics, well_formedness_checker);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
