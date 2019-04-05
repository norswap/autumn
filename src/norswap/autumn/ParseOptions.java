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
     */
    public final boolean well_formed_check;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specifies a {@link ParseMetrics} object that will receive the trace measurements
     * made during the parse. This can be used to aggregate measurements over multiple parses.
     *
     * <p>Implies {@link #trace}.
     */
    public final ParseMetrics metrics;

    // ---------------------------------------------------------------------------------------------

    /**
     * If non-null, specified the instance of {@link WellFormednessChecker} that should be used
     * to check well-formedness in the grammar.
     *
     * <p>A custom checker is (mostl likely) required if your use custom parsers in your grammar.
     *
     * <p>Implies {@link #well_formed_check}
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

    /** Enables the {@link #trace} option. */
    public static ParseOptionsBuilder trace() {
        return new ParseOptionsBuilder().trace();
    }

    // ---------------------------------------------------------------------------------------------

    /** Enables the {@link #record_call_stack} option. */
    public static ParseOptionsBuilder record_call_stack() {
        return new ParseOptionsBuilder().record_call_stack();
    }

    // ---------------------------------------------------------------------------------------------

    /** Enables the {@link #well_formed_check} option. */
    public static ParseOptionsBuilder well_formed_check() {
        return new ParseOptionsBuilder().well_formed_check();
    }

    // ---------------------------------------------------------------------------------------------

    /** Sets the {@link #metrics} option and enables {@link #trace}. */
    public static ParseOptionsBuilder metrics (ParseMetrics metrics) {
        return new ParseOptionsBuilder().metrics(metrics);
    }

    // ---------------------------------------------------------------------------------------------´

    /** Sets the {@link #well_formed_checker} option and enables {@link #well_formed_check}. */
    public static ParseOptionsBuilder well_formed_checker (WellFormednessChecker checker) {
        return new ParseOptionsBuilder().well_formed_checker(checker);
    }

    // ---------------------------------------------------------------------------------------------

    /** Returns a parse options builder with the default options. */
    public static ParseOptionsBuilder builder() {
        return new ParseOptionsBuilder();
    }

    // ---------------------------------------------------------------------------------------------

    /** Builds a default option set. */
    public static ParseOptions get() {
        return new ParseOptionsBuilder().get();
    }

    // ---------------------------------------------------------------------------------------------

    /** See {@link ParseOptions}. */
    public final static class ParseOptionsBuilder
    {
        private boolean trace = false;
        private boolean record_call_stack = false;
        private boolean well_formed_check = false;
        private ParseMetrics metrics = null;
        private WellFormednessChecker well_formed_checker = null;

        private ParseOptionsBuilder() {}

        /** Enables the {@link ParseOptions#trace} option. */
        public ParseOptionsBuilder trace() {
            this.trace = true;
            return this;
        }

        /** Enables the {@link ParseOptions#record_call_stack} option. */
        public ParseOptionsBuilder record_call_stack() {
            this.record_call_stack = true;
            return this;
        }

        /** Enables the {@link ParseOptions#well_formed_check} option. */
        public ParseOptionsBuilder well_formed_check() {
            this.well_formed_check = true;
            return this;
        }

        /** Sets the {@link ParseOptions#metrics} option and enables {@link ParseOptions#trace}. */
        public ParseOptionsBuilder metrics (ParseMetrics metrics) {
            this.trace = true;
            this.metrics = metrics;
            return this;
        }

        public ParseOptionsBuilder well_formed_checker (WellFormednessChecker checker) {
            this.well_formed_check = true;
            this.well_formed_checker = checker;
            return this;
        }

        /** Build the set of options. */
        public ParseOptions get() {
            return new ParseOptions(trace, record_call_stack, well_formed_check,
                metrics, well_formed_checker);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
