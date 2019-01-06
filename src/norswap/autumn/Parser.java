package norswap.autumn;

import java.util.ArrayDeque;

public abstract class Parser
{
    // ---------------------------------------------------------------------------------------------

    protected String rule;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to exclude errors (failure to match) from this parser and all its sub-parsers from
     * being used as the furthest error ({@link Parse#error}).
     *
     * <p>Avoid setting this flag on the root parser, as someone might use {@link Parse#error}
     * to determine if a parse was successful or not.
     */
    public boolean exclude_error = false;

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
    public void set_rule (String rule)
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
     * <p>Must increase {@link Parse#pos} to indicate how much input was consumed, if any; and only
     * if the parse succeeded.
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
        if (parse.trace)
            return tracing_parse(parse);

        int pos0 = parse.pos;
        int log0 = parse.log.size();
        int err0 = parse.error;
        ArrayDeque<ParserCallFrame> stk0 = parse.error_call_stack;

        if (parse.record_call_stack)
            parse.call_stack.push(new ParserCallFrame(this, pos0));

        boolean result = doparse(parse);

        if (exclude_error) {
            parse.error = err0;
            parse.error_call_stack = stk0;
        }

        if (result) {
            if (parse.record_call_stack)
                parse.call_stack.pop();
            return true;
        }

        if (!exclude_error && parse.error <= pos0) {
            parse.error = pos0;
            if (parse.record_call_stack)
                parse.error_call_stack = parse.call_stack.clone();
        }

        if (parse.record_call_stack)
            parse.call_stack.pop();

        parse.pos = pos0;
        parse.rollback(log0);
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Implementation of {@link #parse(Parse)} for the tracing case. See {@link Parse#trace} for
     * more info.
     */
    final boolean tracing_parse (Parse parse)
    {
        int trace0 = parse.trace_timings.size();
        ParserMetrics metrics
            = parse.trace_metrics.computeIfAbsent(this, k -> new ParserMetrics(this));
        ++ metrics.invocations;
        ++ metrics.recursive_invocations;
        long time0 = System.nanoTime();

        int pos0 = parse.pos;
        int log0 = parse.log.size();
        int err0 = parse.error;
        ArrayDeque<ParserCallFrame> stk0 = parse.error_call_stack;

        if (parse.record_call_stack)
            parse.call_stack.push(new ParserCallFrame(this, pos0));

        boolean result = doparse(parse);

        if (exclude_error) {
            parse.error = err0;
            parse.error_call_stack = stk0;
        }

        if (result) {
            if (parse.record_call_stack)
                parse.call_stack.pop();
        }
        else {
            if (!exclude_error && parse.error <= pos0) {
                parse.error = pos0;
                if (parse.record_call_stack)
                    parse.error_call_stack = parse.call_stack.clone();
            }

            if (parse.record_call_stack)
                parse.call_stack.pop();

            parse.pos = pos0;
            parse.rollback(log0);
        }

        long total = System.nanoTime() - time0;

        long children = 0;
        int size = parse.trace_timings.size();
        for (int i = trace0; i < size; ++i)
            children += parse.trace_timings.pop();
        metrics.self_time += total - children;
        if (--metrics.recursive_invocations == 0)
            metrics.total_time += total;
        parse.trace_timings.push(total);

        return result;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Part of the implementation of the visitor pattern.
     *
     * <p>When creating a custom parser named {@code X}, you must create a new interface — typically
     * called {@code XVisitor} — that implements a {@code visit(X)} method. The implementation of
     * the present method should be (ignore the first underscore):
     *
     * <pre>
     * {@code
     * _ @Override void accept (ParserVisitor visitor) {
     *     ((XVisitor) visitor).visit(this);
     * }
     * }
     * </pre>
     *
     * <p>To walk a whole parser tree with {@link #walk}, you should supply an object that
     * implements {@link ParserVisitor} as well as the appropriate visitor interface for every type
     * of custom parser used in the parser tree (e.g. {@code XVisitor}).
     */
    public abstract void accept (ParserVisitor visitor);

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns all the sub-parsers of this parser. Those are the parsers that this parser
     * may call when running its {@link #parse} method.
     */
    public abstract Iterable<Parser> children();

    // ---------------------------------------------------------------------------------------------

    /**
     * Walks the parser tree, calling {@code pre} on this parser, before recursively calling
     * this method on its children (as per {@link #children}) and finally calling {@code post} on
     * this parser.
     */
    public final void walk (ParserVisitor pre, ParserVisitor post)
    {
        if (pre != null)
            this.accept(pre);

        for (Parser child: children())
            child.walk(pre, post);

        if (post != null)
            this.accept(post);
    }

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
