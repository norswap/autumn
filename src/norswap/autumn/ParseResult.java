package norswap.autumn;

import java.util.Deque;
import java.util.Map;

import static norswap.utils.Util.cast;

/**
 * The results obtained from a parse (returned by the {@link Parse#run} methods).
 *
 * <p>This includes amongst other things: whether the parse was successful, matched the whole input,
 * the value stack in case of success, and informations about the furthest error in case the whole
 * input wasn't matched.
 */
public final class ParseResult
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the parse was successful (matched a prefix of the input).
     */
   public final boolean success;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the parse was successful AND matched the whole input.
     */
    public final boolean full_match;

    // ---------------------------------------------------------------------------------------------

    /**
     * Size of the match (which is also the input position one past the last matched item) if the parse
     * succeeded, or -1 otherwise.
     */
    public final int match_size;

    // ---------------------------------------------------------------------------------------------

    /**
     * Exception (really, Throwable) that caused the parse to terminate, or null othwerwise.
     */
    public final Throwable thrown;

    // ---------------------------------------------------------------------------------------------

    /**
     * The root parser used to perform the parse.
     */
    public final Parser parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * The options with which the parse was launched.
     */
    public final ParseOptions options;

    // ---------------------------------------------------------------------------------------------

    /**
     * If the parse ended with an exception, the input position at which this exception occured;
     * otherwise if the parse isn't a full match, the position of the furthest error encountered;
     * otherwise -1.
     */
    public final int error_position;

    // ---------------------------------------------------------------------------------------------

    /**
     * The final state of the parse value stack if the parse was successful, null otherwise.
     */
    public final Deque<?> value_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * A map from parse state keys ({@link ParseState#key}) to parse states (the state holder that
     * is a type parameter to an instance of {@link ParseState}) used during the parse.
     *
     * <p>Note that if the parse did not need to read or write the parse state, it will not
     * appear here, even thought the parser might require it for other inputs!
     */
    public final Map<Object, Object> parse_states;

    // ---------------------------------------------------------------------------------------------

    /**
     * A stack of parse invocations (call stack) reported for unsuccessful parses if the {@link
     * ParseOptions#RECORD_CALL_STACK} option was specified (otherwise always null).
     *
     * <p>If the parse ended with an exception, this is the call stack at that point; otherwise if
     * the parse isn't a full match, this is the call stack at the point of the furthest error;
     * otherwise null.
     */
    public final Deque<ParserCallFrame> error_call_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * Trace results, if the {@link ParseOptions#TRACE} option was specified, null otherwise.
     */
    public final TraceMetrics trace_metrics;

    // ---------------------------------------------------------------------------------------------

    /**
     * The value at the top of the value stack if the parse was successful and the value stack
     * is non-empty, null otherwise.
     *
     * <p>This methods auto-casts its return value to the target type.
     */
    public <T> T top_value() {
        return value_stack != null
            ? cast(value_stack.peek())
            : null;
    }

    // ---------------------------------------------------------------------------------------------

    ParseResult (
        boolean success,
        boolean full_match,
        int match_size,
        Throwable thrown,
        Parser parser,
        ParseOptions options,
        int error_position,
        Deque<?> value_stack,
        Map<Object, Object> parse_states,
        Deque<ParserCallFrame> error_call_stack,
        TraceMetrics trace_metrics)
    {
        this.success = success;
        this.full_match = full_match;
        this.match_size = match_size;
        this.thrown = thrown;
        this.parser = parser;
        this.options = options;
        this.error_position = error_position;
        this.value_stack = value_stack;
        this.parse_states = parse_states;
        this.error_call_stack = error_call_stack;
        this.trace_metrics = trace_metrics;
    }

    // ---------------------------------------------------------------------------------------------

    @Override
    public String toString () {
        return "ParseResult{" +
            "success=" + success +
            ", full_match=" + full_match +
            ", match_size=" + match_size +
            ", exception=" + thrown +
            ", parser=" + parser +
            ", options=" + options +
            ", error_position=" + error_position +
            ", value_stack=" + value_stack +
            ", parse_states=" + parse_states +
            ", error_call_stack=" + error_call_stack +
            ", trace_metrics=" + trace_metrics +
            '}';
    }
}
