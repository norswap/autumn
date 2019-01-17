package norswap.autumn;

import norswap.utils.Exceptions;
import norswap.utils.Strings;
import java.util.Collection;
import java.util.List;

import static norswap.autumn.ParseOptions.*;

/**
 * Make your test class inherit this class in order to benefit from its various {@code success}
 * and {@code failure} assertion methods. Set the {@link #parser} field beforehand!
 *
 * <p>Also see the documentation of other fields for more options, and the documentation of
 * the parent class {@link norswap.utils.TestFixture}.
 *
 * <p>In particular, whenever an integer {@code peel} parameter is present, it indicates that this
 * many items should be removed from the bottom of the stack trace (outermost/earliest method calls)
 * of the thrown assertion error.
 *
 * <p>All assertion methods take care of peeling themselves off (as only the assertion call site
 * is really interesting), so you do not need to account for them in {@code peel}.
 */
@SuppressWarnings("UnusedReturnValue")
public abstract class TestFixture extends norswap.utils.TestFixture
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The parser being currently tested.
     */
    public Parser parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * First column index. 1 by default, you can change this to 0 if required.
     */
    public int column_start = 1;

    // ---------------------------------------------------------------------------------------------

    /**
     * Visual tab width. 4 by default, you can change this if required.
     */
    public int tab_width = 4;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to always record the parser call stack of the tested parsers. Defaults to true. If
     * set to false, the call stack will be recorded only on the second parser call, if the first
     * call failed. The only point of setting this to false is to speed up your tests.
     */
    public boolean record_call_stack = true;

    // ---------------------------------------------------------------------------------------------

    public TestFixture()
    {
        trace_separator = "\n------";
    }

    // ---------------------------------------------------------------------------------------------

    private ParseResult run (Object input, boolean record_call_stack)
    {
        ParseOptions options = parse_options(IF(record_call_stack, RECORD_CALL_STACK));

        if (input instanceof String)
            return Autumn.run(parser, (String) input, options);
        if (input instanceof List)
            return Autumn.run(parser, (List<?>) input, options);
        throw new Error();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Appends a nicely formatted string representation of the parser call stack to the given
     * string builder, indented with one tab. The appended content never ends with a newline.
     * <p>
     * The line map can be null for object-based parses, or if no (row, column) position
     * translation is required.
     */
    public void append_parser_call_stack
        (StringBuilder b, LineMap map, Collection<ParserCallFrame> stack)
    {
        for (ParserCallFrame frame: stack)
            b   .append("\tat ")
                .append(LineMap.string(map, frame.position))
                .append(" in ")
                .append(frame.parser)
                .append("\n");

        if (!stack.isEmpty())
            Strings.pop(b, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Appends a string representing the outcome of the parse.
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
     */
    private void parse_status (StringBuilder b, ParseResult r, Object input)
    {
        if (r.full_match) {
            b.append("Parse succeeded, consuming the whole input.\n");
            return;
        }

        LineMap map = input instanceof String
            ? new LineMap((String) input, tab_width, column_start)
            : null;

        if (r.thrown != null)
        {
            b.append("Exception thrown at position ");
            b.append(LineMap.string(map, r.error_position));

            if (r.options.has(RECORD_CALL_STACK)) {
                b.append("\n");
                b.append(r.thrown.getClass());
                b.append(": ");
                b.append(r.thrown.getMessage());
                b.append("\n\nParser trace:\n");
                append_parser_call_stack(b, map, r.error_call_stack);
            }

            b.append("\n\nThrown: ");
            b.append(Exceptions.string_stack_trace(r.thrown));

            return;
        }

        if (r.success)
            b   .append("Parse succeeded, consuming up to ")
                .append(LineMap.string(map, r.match_size))
                .append(".\n");
        else
            b   .append("Parse failed.\n");

        b   .append("Furthest parse error at ")
            .append(LineMap.string(map, r.error_position))
            .append(".\n");

        if (r.options.has(RECORD_CALL_STACK))
            append_parser_call_stack(b, map, r.error_call_stack);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * cf. {@link #parse_status(StringBuilder, ParseResult, Object)}
     */
    public String parse_status (ParseResult r, Object input)
    {
        StringBuilder b = new StringBuilder();
        parse_status(b, r, input);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string starting with head, then outlining the outcome of the two supplied
     * parses, as per {@link #parse_status(StringBuilder, ParseResult, Object)}.
     */
    public String compared_status (String msg_head, Object input, ParseResult r1, ParseResult r2)
    {
        StringBuilder b = new StringBuilder(msg_head);
        b.append(" Maybe you made a parser stateful?\n\n");

        b.append("### Initial Parse ###\n\n");
        parse_status(b, r1, input);

        b.append("\n\n"); // empty line.

        b.append("### Second Parse ###\n\n");
        parse_status(b, r2, input);

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     *
     * <p>Actually invokes {@link #parser} twice, as a way to catch non-determinism in the
     * parsing process (often caused by improper state handling).
     */
    public ParseResult success (Object input, int peel)
    {
        ParseResult r1 = run(input, record_call_stack);
        ParseResult r2 = run(input, record_call_stack || !r1.success);

        assert_true(r2.thrown == null || r1.thrown != null, peel + 1, () -> compared_status(
            "Second parse throws an exception while the initial parse does not.",
            input, r1, r2));

        assert_true(r1.thrown == null || r2.thrown != null, peel + 1, () -> compared_status(
            "Second parse does not throw an exception while the initial parse does.",
            input, r1, r2));

        if (r1.thrown != null && r2.thrown != null)
            assert_equals(r1.thrown.getClass(), r2.thrown.getClass(), peel + 1,
            () -> compared_status(
                "Second parse does not throw the same type of exception as the initial parse.",
                input, r1, r2));

        assert_equals(r2.success, r1.success, peel + 1, () -> compared_status(
            "Second parse does not have the same success as the initial parse.",
            input, r1, r2));

        if (r1.success)
            assert_equals(r2.match_size, r1.match_size, peel + 1, () -> compared_status(
                "Second parse and initial parse do not consume the same amount of input.",
                input, r1, r2));
        else
            assert_equals(r2.error_position, r1.error_position, peel + 1, () -> compared_status(
                "Second parse and initial parse do not fail at the same position.",
                input, r1, r2));

        // At this point we have ascertained that the two parses should be equivalent.
        // It's impossible to be sure, however, and so we base everything upon the first one,
        // so that we are at least consistent.

        assert_true(r1.full_match, peel + 1, () -> parse_status(r1, input));
        return r1;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     */
    public ParseResult success (Object input)
    {
        return success(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public ParseResult success_expect (Object input, Object value, int peel)
    {
        ParseResult r = success(input, peel + 1);
        assert_true(r.value_stack.size() > 0, peel + 1,
            () -> "Empty AST stack.");
        assert_equals(r.value_stack.peek(), value, peel + 1,
            () -> "The top of the AST stack did not match the expected value.");
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public void success_expect (Object input, Object value)
    {
        success_expect(input, value, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input.
     */
    public ParseResult failure (Object input, int peel)
    {
        ParseResult r = run(input, record_call_stack);

        assert_true(!r.full_match, peel + 1,
            () -> "Parse succeeded when it was expected to fail.");
        assert_true(r.success || r.thrown == null && r.error_position != -1, peel + 1,
            () -> "No exception nor parse error was reported.");

        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input.
     */
    public ParseResult failure (Object input)
    {
        return failure(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public ParseResult failure_at (Object input, int error_position, int peel)
    {
        ParseResult r = failure(input, peel + 1);

        assert_equals(r.error_position, error_position, peel + 1,
            () -> "The furthest parse error didn't occur at the expected location.");

        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public ParseResult failure_at (Object input, int error)
    {
        return failure_at(input, error, 1);
    }

    // ---------------------------------------------------------------------------------------------
}
