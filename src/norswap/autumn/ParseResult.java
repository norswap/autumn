package norswap.autumn;

import norswap.autumn.util.ArrayStack;
import norswap.utils.Exceptions;
import java.util.Map;

import static norswap.autumn.ParseOptions.RECORD_CALL_STACK;
import static norswap.utils.Util.cast;

/**
 * The results obtained from a parse, returned by one of the {@link Autumn} {@code .run} methods.
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
    public final ArrayStack<?> value_stack;

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
    public final ParserCallStack error_call_stack;

    // ---------------------------------------------------------------------------------------------

    /**
     * Trace results, if the {@link ParseOptions#TRACE} option was specified, null otherwise.
     */
    public final ParseMetrics parse_metrics;

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
        ArrayStack<?> value_stack,
        Map<Object, Object> parse_states,
        ParserCallStack error_call_stack,
        ParseMetrics parse_metrics)
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
        this.parse_metrics = parse_metrics;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Appends a string representing the results of the parse to {@code b}.
     *
     * <p>This includes:</p>
     * <ul>
     *     <li>Whether the parse succeeded or failed.</li>
     *     <li>If the parser succeeded, whether it consumed the whole input or not.</li>
     *     <li>If the parse threw an exception, its stack trace, as well as the parser trace
     *     at the point of the exception, if available.</li>
     *     <li>Otherwise, if the parse failed or did not consume the whole input, the parse trace at
     *     the point of the furthest error, if available.</li>
     * </ul>
     *
     * <p>If {@code map} is non-null, it is used to translate the input position in terms of
     * lines and columns.
     */
    public void append_to (StringBuilder b, LineMap map)
    {
        if (full_match) {
            b.append("Parse succeeded, consuming the whole input.\n");
            return;
        }

        if (thrown != null)
        {
            b.append("Exception thrown at position ");
            b.append(LineMap.string(map, error_position));

            if (options.has(RECORD_CALL_STACK)) {
                b.append("\n");
                b.append(thrown.getClass());
                b.append(": ");
                b.append(thrown.getMessage());
                b.append("\n\nParser trace:\n");
                error_call_stack.append_to(b, 1, map);
            }

            b.append("\n\nThrown: ");
            b.append(Exceptions.string_stack_trace(thrown));

            return;
        }

        if (success)
            b   .append("Parse succeeded, consuming up to ")
                .append(LineMap.string(map, match_size))
                .append(".\n");
        else
            b   .append("Parse failed.\n");

        b   .append("Furthest parse error at ")
            .append(LineMap.string(map, error_position))
            .append(".\n");

        if (options.has(RECORD_CALL_STACK))
            error_call_stack.append_to(b, 1, map);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the results of the parse, as per {@link
     * #append_to(StringBuilder, LineMap)}.
     *
     * <p>If {@code map} is non-null, it is used to translate the input position in terms of
     * lines and columns.
     */
    public String toString (LineMap map)
    {
        StringBuilder b = new StringBuilder();
        append_to(b, map);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the results of the parse, as per {@link
     * #append_to(StringBuilder, LineMap)}.
     *
     * <p>No line map is supplied, so input positions are reported as simple offsets.
     */
    @Override public String toString()
    {
        return toString(null);
    }

    // ---------------------------------------------------------------------------------------------
}
