package norswap.autumn;

/**
 * This class represents a set of options that can be passed to one of the {@link Autumn} {@code
 * .run} methods.
 *
 * <p>To create an instance of this class, call any of its static methods and chain further calls
 * from {@link ParseOptionsBuilder} to select the option you desires. End with {@link
 * ParseOptionsBuilder#get()} to create the option set.
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
     * If non-null, specifies a {@link ParseMetrics} object that will receive the trace measurements
     * made during the parse. This can be used to aggregate measurements over multiple parses.
     *
     * <p>Implies {@link #trace}.
     */
    public final ParseMetrics metrics;

    // ---------------------------------------------------------------------------------------------

    private ParseOptions (boolean trace, boolean record_call_stack, ParseMetrics metrics)
    {
        this.trace = trace;
        this.record_call_stack = record_call_stack;
        this.metrics = metrics;
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

    /** Sets the {@link #metrics} option and enables {@link #trace}. */
    public static ParseOptionsBuilder metrics (ParseMetrics metrics) {
        return new ParseOptionsBuilder().metrics(metrics);
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
        private ParseMetrics metrics = null;

        private ParseOptionsBuilder() {}

        /** Enables the {@link #trace} option. */
        public ParseOptionsBuilder trace() {
            this.trace = true;
            return this;
        }

        /** Enables the {@link #record_call_stack} option. */
        public ParseOptionsBuilder record_call_stack() {
            this.trace = true;
            return this;
        }

        /** Sets the {@link #metrics} option and enables {@link #trace}. */
        public ParseOptionsBuilder metrics (ParseMetrics metrics) {
            this.trace = true;
            this.metrics = metrics;
            return this;
        }

        /** Build the set of options. */
        public ParseOptions get() {
            return new ParseOptions(trace, record_call_stack, metrics);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
