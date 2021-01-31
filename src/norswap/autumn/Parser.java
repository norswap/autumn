package norswap.autumn;

/**
 * The parent class for all parsers.
 *
 * <p>A parser is at core a function that, given the remaining input, succeeds or fails at matching
 * a prefix of this remaining input.
 *
 * <p>In particular, parsers are invoked via the {@link #parse(Parse)} function. The remaining input
 * is delineated via the input ({@link Parse#string} or {@link Parse#list}) and the {@link
 * Parse#pos} fields. This method returns a boolean to indicate success or failure, and, in case of
 * success, updates {@link Parse#pos} to reflect the amount of input that was matched (otherwise
 * the position remains unchanged).
 *
 * <p>However, to implement the parser, you must actually implement the {@link #doparse(Parse)}
 * method. The reason is that {@link #parse(Parse)} wraps {@code doparse} with some bookkeeping
 * logic. In particular, it automatically restores {@link Parse#pos} and {@link Parse#log} in
 * case of error ({@code doparse} returns false), as well as update {@link Parse#error} (or not,
 * depending on {@link #excludeErrors}). It also handles the logic for some options such
 * as {@link ParseOptions#recordCallStack} and {@link ParseOptions#trace}.
 *
 * <p>The requirement on {@link #doparse(Parse)} are then that it returns the appropriate truth
 * value and updates {@link Parse#pos} if successful. It's also important that any global state
 * change be recorded in {@link Parse#log} so that it may be undone in case of backtracing.
 *
 * <p>Parser may have a rule name ({@link #rule()}). Those may be auto-generated when the parsers
 * are defined through a {@link Grammar} and the grammar or one of its {@link Grammar.rule} is
 * passed {@link Autumn#parse}.
 *
 * <p>Parsers form a directed graph. Each parser may have child parsers (which must be returned by
 * {@link #children()}), which are the parsers that this parser may call during the execution of its
 * {@link #parse} method. The parser graph can be traversed using a {@link ParserWalker}.
 *
 * <p>This class also supports the visitor pattern, in order to add new functionality specialized
 * by type of parser. See {@link #accept(ParserVisitor)} and {@link ParserVisitor} for more details.
 */
public abstract class Parser
{
    // ---------------------------------------------------------------------------------------------

    private String rule;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to exclude errors (failure to match) from this parser and all its sub-parsers from
     * being used as the furthest error ({@link Parse#error}).
     */
    public boolean excludeErrors = false;

    // ---------------------------------------------------------------------------------------------

    /**
     * The name of the rule this parser is assigned to, if any, or null.
     */
    public final String rule() {
        return rule;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the name of the rule this parser is assigned to.
     * This may be called at most once, or an error will occur.
     */
    public void setRule (String rule)
    {
        if (this.rule != null)
            throw new Error("rule name already set");
        this.rule = rule;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Override this method to implement the parsing logic.
     *
     * <p>Returns true if and only if the parse succeeded.
     *
     * <p>Must increase {@link Parse#pos} to indicate how much input was consumed, if any.
     *
     * <p>If the parse failed and the method return false, {@link #parse} will take care of
     * resetting {@link Parse#pos} to its original value on its own. Similarly, in case of failure
     * {@link #parse} will also undo any side effects registered in  {@link Parse#log}.
     *
     * <p>Never call this directly, but call {@link #parse} instead.
     */
    protected abstract boolean doparse (Parse parse);

    // ---------------------------------------------------------------------------------------------

    /**
     * Attempts to further the parse by matching this parser against the start of the remainder of
     * the input.
     *
     * <p>Returns true if and only if the parse succeeded.
     *
     * <p>Will increase {@link Parse#pos} to indicate how much input was consumed, if any; and only
     * if the parse succeeded.
     *
     * <p>Will register side effects in {@link Parse#log}, if any; and only if the parse succeeded.
     */
    public final boolean parse (Parse parse)
    {
        if (parse.options.trace)
            return tracingParse(parse);

        int pos0 = parse.pos;
        int log0 = parse.log.size();
        int err0 = parse.error;
        String errmsg0 = parse.errorMessage;
        ParserCallStack stk0 = parse.errorCallStack;

        if (parse.options.recordCallStack)
            parse.callStack.push(this, pos0);

        boolean result = doparse(parse);

        if (excludeErrors) {
            parse.error = err0;
            parse.errorMessage = errmsg0;
            parse.errorCallStack = stk0;
        }

        if (result) {
            if (parse.options.recordCallStack)
                parse.callStack.pop();
            return true;
        }

        if (!excludeErrors && parse.error <= pos0) {
            parse.error = pos0;
            //noinspection StringEquality
            if (parse.errorMessage == errmsg0)
                parse.errorMessage = null;
            if (parse.options.recordCallStack)
                parse.errorCallStack = parse.callStack.clone();
        }

        if (parse.options.recordCallStack)
            parse.callStack.pop();

        parse.pos = pos0;

        if (parse.log.size() > log0) // this improves performance
            parse.log.rollback(log0);

        return false;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Implementation of {@link #parse(Parse)} for the tracing case. See {@link ParseOptions#trace}
     * for more info.
     */
    private boolean tracingParse (Parse parse)
    {
        long time0 = System.nanoTime();

        int trace0 = parse.traceTimings.size();
        ParserMetrics metrics
            = parse.parseMetrics.metrics.computeIfAbsent(this, k -> new ParserMetrics(this));
        ++ metrics.invocations;
        ++ metrics.recursiveInvocations;

        long time1 = System.nanoTime();

        int pos0 = parse.pos;
        int log0 = parse.log.size();
        int err0 = parse.error;
        ParserCallStack stk0 = parse.errorCallStack;

        if (parse.options.recordCallStack)
            parse.callStack.push(this, pos0);

        boolean result = doparse(parse);

        if (excludeErrors) {
            parse.error = err0;
            parse.errorCallStack = stk0;
        }

        if (result) {
            if (parse.options.recordCallStack)
                parse.callStack.pop();
        }
        else {
            if (!excludeErrors && parse.error <= pos0) {
                parse.error = pos0;
                if (parse.options.recordCallStack)
                    parse.errorCallStack = parse.callStack.clone();
            }

            if (parse.options.recordCallStack)
                parse.callStack.pop();

            parse.pos = pos0;
            parse.log.rollback(log0);
        }

        long total = System.nanoTime() - time1;

        long overheads = 0; // cumulative overheads time in children
        long children = 0;  // total time spent in children (including overheads)
        int size = parse.traceTimings.size();

        for (int i = trace0; i < size; i += 2) {
            children  += parse.traceTimings.pop();
            overheads += parse.traceTimings.pop();
        }

        metrics.selfTime += total - children;

        if (--metrics.recursiveInvocations == 0)
            metrics.totalTime += total - overheads;

        overheads += System.nanoTime() - time0 - total;
        parse.traceTimings.push(overheads);
        parse.traceTimings.push(System.nanoTime() - time0);

        return result;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Part of the implementation of the visitor pattern.
     *
     * <p><b>Do not override this for custom parsers!</b>
     *
     * <p>Instead, see {@link ParserVisitor}, which includes instructions on how to make custom
     * parsers compatible with existing visitors.
     */
    public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns all the sub-parsers of this parser. Those are the parsers that this parser
     * may call during the execution of its {@link #parse} method.
     */
    public abstract Iterable<Parser> children();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the rule name, if any, otherwise the full string representation of this parser, as
     * per {@link #toStringFull()}.
     */
    @Override public final String toString()
    {
        return rule != null
            ? rule
            : toStringFull();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the full string representation of this parser (i.e. not only its rule name).
     * The ouput may however reference sub-parsers by rule name.
     */
    public abstract String toStringFull();

    // ---------------------------------------------------------------------------------------------
}
