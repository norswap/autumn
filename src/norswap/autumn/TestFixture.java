package norswap.autumn;

import norswap.utils.Exceptions;
import norswap.utils.Strings;
import java.util.Collection;
import java.util.List;

/**
 * Make your test class inherit this class in order to benefit from the various {@code success}
 * and {@code failure} assertion methods. Set the {@link #parser} field beforehand!
 *
 * <p>Also see the documentation of other fields for more options, and the documentation of
 * the parent class {@link norswap.utils.TestFixture}.
 *
 * <p>In particular, whenever an integer {@code peel} parameter is present, it indicates that this
 * many items should be removed from the bottom of the stack trace (most recently called methods) of
 * the thrown assertion error.
 *
 * <p>All assertion methods take care of peeling themselves off (as only the assertion call site
 * is really interesting), so you do not need to account for them in {@code peel}.
 */
public abstract class TestFixture extends norswap.utils.TestFixture
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The parser being currently tested.
     */
    public Parser parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * Current parse. Set by the various {@code success} methods.
     */
    protected Parse parse;

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

    private Parse make_parse (Object input, boolean record_call_stack)
    {
        if (input instanceof String)
            return new Parse((String) input, record_call_stack, false);
        if (input instanceof List)
            return new Parse((List<?>) input, record_call_stack, false);
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
    private void parse_status (StringBuilder b, boolean result, Parse parse, Throwable t)
    {
        assert !result || t == null;

        if (result && parse.pos == parse.input_length()) {
            b.append("Parse succeeded, consuming the whole input.\n");
            return;
        }

        LineMap map = parse.string != null
            ? new LineMap(parse.string, tab_width, column_start)
            : null;

        if (t != null)
        {
            b.append("Exception thrown at position ");
            b.append(LineMap.string(map, parse.pos));

            if (parse.record_call_stack) {
                b.append("\n");
                b.append(t.getClass());
                b.append(": ");
                b.append(t.getMessage());
                b.append("\n\nParser trace:\n");
                append_parser_call_stack(b, map, parse.call_stack());
            }

            b.append("\n\nThrown: ");
            b.append(Exceptions.string_stack_trace(t));

            return;
        }

        if (result)
            b   .append("Parse succeeded, consuming up to ")
                .append(LineMap.string(map, parse.pos))
                .append(".\n");
        else
            b   .append("Parse failed.\n");

        b   .append("Furthest parse error at ")
            .append(LineMap.string(map, parse.error))
            .append(".\n");

        if (parse.record_call_stack)
            append_parser_call_stack(b, map, parse.error_call_stack());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * cf. {@link #parse_status(StringBuilder, boolean, Parse, Throwable)}
     */
    public String parse_status (boolean result, Parse parse, Throwable t)
    {
        StringBuilder b = new StringBuilder();
        parse_status(b, result, parse, t);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string starting with head, then outlining the outcome of the two supplied
     * parses, as per {@link #parse_status(StringBuilder, boolean, Parse, Throwable)}.
     */
    public String compared_status (
        String head,
        boolean result1, Parse parse1, Throwable t1,
        boolean result2, Parse parse2, Throwable t2)
    {
        StringBuilder b = new StringBuilder(head);
        b.append(" Maybe you made a parser stateful?\n\n");

        b.append("### Initial Parse ###\n\n");
        parse_status(b, result1, parse1, t1);

        b.append("\n\n"); // empty line.

        b.append("### Second Parse ###\n\n");
        parse_status(b, result2, parse2, t2);

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     *
     * <p>Actually invokes {@link #parser} twice, as a way to catch non-determinism in the
     * parsing process (often caused by improper state handling).
     */
    public void success (Object input, int peel)
    {
        parse = make_parse(input, record_call_stack);
        Throwable thrown = null;
        boolean result = false;

        try { result = parser.parse(parse); }
        catch (Throwable t) { thrown = t; }

        boolean r1 = result;
        Throwable thrown1 = thrown;
        Parse old = parse;
        parse = make_parse(input, record_call_stack || !result);
        thrown = null;

        boolean result2 = false;
        try { result2 = parser.parse(parse); }
        catch (Throwable t)
        {
            thrown = t;

            assert_true(thrown1 != null, peel + 1, () -> compared_status(
                "Second parse throws an exception while the initial parse does not.",
                r1, old, thrown1, false, parse, t));

            assert thrown1 != null;
            assert_equals(t.getClass(), thrown1.getClass(), peel + 1, () -> compared_status(
                "Second parse does not throw the same type of exception as the initial parse.",
                false, old, thrown1, false, parse, t));
        }

        boolean r2 = result2;
        Throwable thrown2 = thrown;

        assert_true(thrown1 == null || thrown2 != null, peel + 1, () -> compared_status(
            "Second parse does not throw an exception while the initial parse does.",
            false, old, thrown1, r2, parse, null));

        assert_equals(r2, r1, peel + 1, () -> compared_status(
            "Second parse does not have the same success as the initial parse.",
            r1, old, null, r2, parse, null));

        if (result)
            assert_equals(parse.pos, old.pos, peel + 1, () -> compared_status(
                "Second parse and initial parse do not consume the same amount of input.",
                true, old, null, true, parse, null));
        else
            assert_equals(parse.error, old.error, peel + 1, () -> compared_status(
                "Second parse and initial parse do not fail at the same position.",
                false, old, thrown1, false, parse, thrown2));

        // At this point we have ascertained that the two parses should be equivalent.
        // It's impossible to be sure, however, and so we base everything upon the second one,
        // so that we are at least consistent.

        if (thrown2 != null)
            assert_true(false, peel + 1, () -> parse_status(false, parse, thrown2));

        else if (!result2)
            assert_true(false, peel + 1, () -> parse_status(false, parse, null));

        else if (parse.pos != parse.input_length())
            assert_true(false, peel + 1, () -> parse_status(true, parse, null));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     */
    public void success (Object input)
    {
        success(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public void success_expect (Object input, Object value, int peel)
    {
        success(input, peel + 1);
        assert_true(parse.stack.size() > 0, peel + 1,
            () -> "Empty AST stack.");
        assert_equals(parse.peek(), value, peel + 1,
            () -> "The top of the AST stack did not match the expected value.");
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
    public void failure (Object input, int peel)
    {
        parse = make_parse(input, false);
        boolean result = parser.parse(parse);

        assert_true(!result || parse.pos != parse.input_length(), peel + 1,
            () -> "Parse succeeded when it was expected to fail.");
        assert_true(result || parse.pos == 0, peel + 1,
            () -> "Parse failed but the input position wasn't reset to 0.");
        assert_true(result || parse.error != -1, peel + 1,
            () -> "No parse error was reported.");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input.
     */
    public void failure (Object input)
    {
        failure(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public void failure_at (Object input, int error, int peel)
    {
        failure(input, peel + 1);
        assert_equals(parse.error, error, peel + 1,
            () -> "The furthest parse error didn't occur at the expected location.");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public void failure_at (Object input, int error)
    {
        failure_at(input, error, 1);
    }

    // ---------------------------------------------------------------------------------------------
}
