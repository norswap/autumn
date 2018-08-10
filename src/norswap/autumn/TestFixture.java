package norswap.autumn;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Make your test class inherit this class in order to benefit from the various {@code success}
 * assertion methods. Set the {@link #parser} field beforehand!
 *
 * <p>Also see the documentation of other fields for more options.
 */
public abstract class TestFixture
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
     * Whether to display the erroneous parser call stack in case of parse error.
     * Defaults to true.
     */
    public boolean record_call_stack = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * A separator to be added at the end of assertion error messages, to separate them from the
     * stack trace of the assertion error itself. Especially handy if the error message
     * ends with indented items itself. Default to the empty string.
     */
    public String trace_separator = "------";

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to remove stack trace elements pertaining to the test runner (basically anything
     * above the test class) from the assertion errors' stack traces. Defaults to true.
     */
    public boolean peel_test_runner = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Trims the stack trace of the given throwable, removing {@code peel} stack trace
     * elements at the top of the stack trace (the most recently called methods),
     * and removes all stack trace elements under the last occurence of the class whose full
     * name (the dot-separated "binary name") is equal to {@code bottom_class}, if it isn't null.
     */
    public void trim_stack_trace (Throwable t, int peel, String bottom_class)
    {
        StackTraceElement[] trace = t.getStackTrace();
        int new_end = trace.length;

        for (int i = trace.length - 1; i >= 0; --i)
            if (trace[i].getClassName().equals(bottom_class)) {
                new_end = i + 1;
                break;
            }

        t.setStackTrace(Arrays.copyOfRange(trace, peel, new_end));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Throws an {@link AssertionError} with the given message. Removes itself and {@code peel}
     * additional stack trace elements at the top of the stack trace, and honors the {@link
     * #peel_test_runner} setting.
     */
    public void throw_assertion (int peel, String msg)
    {
        AssertionError error = new AssertionError(msg + trace_separator);
        trim_stack_trace(error, peel + 1, peel_test_runner ? this.getClass().getName() : null);
        throw error;
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition, int peel, Supplier<String> msg)
    {
        if (!condition) throw_assertion(peel + 1, msg.get());
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition, Supplier<String> msg)
    {
        if (!condition) throw_assertion(1, msg.get());
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition, String msg)
    {
        if (!condition) throw_assertion(1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition)
    {
        if (!condition) throw_assertion(1, "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (!Objects.deepEquals(actual, expected))
            throw_assertion(peel + 1,
                msg.get() + "\nexpected [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected, Supplier<String> msg)
    {
        assert_equals(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected, String msg)
    {
        assert_equals(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected)
    {
        assert_equals(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    private Parse make_parse (Object input, boolean record_call_stack)
    {
        if (input instanceof String)
            return Parse.of((String) input, record_call_stack);
        if (input instanceof List)
            return Parse.of((List<?>) input, record_call_stack);
        throw new Error();
    }

    // ---------------------------------------------------------------------------------------------

    private String pos_string (LineMap map, int position)
    {
        try {
            return map != null
                ? map.position_from(position).toString()
                : "" + position;
        }
        catch (IndexOutOfBoundsException e) {
            return "" + position + " (out of bounds)";
        }
    }

    // ---------------------------------------------------------------------------------------------

    // TODO move into norswap-utils
    private String getStackTrace (Throwable t)
    {
        StringWriter trace = new StringWriter();
        PrintWriter w = new PrintWriter(trace);
        t.printStackTrace(w);
        w.close();
        return trace.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Appends a nicely formatted string representation of the parser call stack to the given
     * string builder, indented with one tab. The appended content always ends with a newline;
     */
    public void append_parser_call_stack
        (StringBuilder b, LineMap map, Collection<ParserCallFrame> stack)
    {
        for (ParserCallFrame frame: stack)
            b   .append("\tat ")
                .append(pos_string(map, frame.position))
                .append(" in ")
                .append(frame.parser)
                .append("\n");

        if (stack.isEmpty())
            b.append("\n");
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string containing the stack trace of the given exception (as per {@link
     * Throwable#printStackTrace)}, then — if the parse has stack trace recording on — a string
     * representation of the parser stack trace at the exception site (as per {@link
     * #append_parser_call_stack}). If both are present, they are separated by an empty line. The
     * returned string always ends with a newline.
     */
    public String double_stack_trace (Parse parse, Throwable t)
    {
        StringBuilder b = new StringBuilder();

        LineMap map = parse.string != null
            ? new LineMap(parse.string, tab_width, column_start)
            : null;

        if (peel_test_runner)
            trim_stack_trace(t, 0, this.getClass().getName());

        b   .append("Exception thrown at position ")
            .append(pos_string(map, parse.pos))
            .append("\n\nThrown: ")
            .append(getStackTrace(t));

        if (!parse.record_call_stack)
            return b.toString();

        b   .append("\n")
            .append("Parser call stack: \n");

        append_parser_call_stack(b, map, parse.call_stack());

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     *
     * If the parse has stack trace recording on, returns a string containing the parser stack trace
     * at the furthest error position (as per {@link #append_parser_call_stack}). Otherwise, the
     * string will only contain the header that indicates the furthest error position. In any case,
     * the returned string ends with a newline.
     */
    public String error_parser_trace (Parse parse)
    {
        StringBuilder b = new StringBuilder();

        LineMap map = parse.string != null
            ? new LineMap(parse.string, tab_width, column_start)
            : null;

        b   .append("Furthest parse error at ")
            .append(pos_string(map, parse.error))
            .append("\n");

        if (!parse.record_call_stack)
            return b.toString();

        append_parser_call_stack(b, map, parse.error_call_stack());

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     */
    public void success (Object input, int peel)
    {
        parse = make_parse(input, false);
        Throwable thrown = null;
        boolean result = false;

        try { result = parser.parse(parse); }
        catch (Throwable t) { thrown = t; }

        if (result && parse.pos == parse.input_length())
            return;

        Parse old = parse;

        // To improve test run times, parse without call stack recording,
        // and reparse only in case of error.
        if (record_call_stack)
        {
            Throwable thrown2 = null;
            parse = make_parse(input, true);

            final String parse2 = "Parse with call trace recording ";
            final String maybe  = " Maybe you made a parser stateful?";

            result = false;
            try { result = parser.parse(parse); }
            catch (Throwable t)
            {
                assert_true(thrown != null, peel + 1, () ->
                    parse2 + "throws an exception while the initial parse does not."
                    + maybe + "\n"+ double_stack_trace(parse, t));

                assert thrown != null;

                assert_equals(t.getClass(), thrown.getClass(), peel + 1, () ->
                    parse2 + "does not throw the same type of exception as the initial parse."
                    + maybe);

                thrown2 = t;
            }

            final Throwable fthrown = thrown;
            //noinspection ConstantConditions
            assert_true(thrown == null && thrown2 == null || thrown != null && thrown2 != null,
                peel + 1, () ->
                    parse2 + " does not throw an exception while the initial parse does."
                    + maybe + "\n"+ double_stack_trace(old, fthrown));

            assert_true(!result, peel + 1, () ->
                parse2 + "succeeds while the initial parse fails." + maybe);

            assert_equals(parse.error, old.error, peel + 1, () ->
                parse2 + "and initial parse do not fail at the same position." + maybe);
        }

        final Throwable fthrown = thrown;
        if (thrown != null)
            assert_true(false, peel + 1, () -> double_stack_trace(parse, fthrown));
        else // parse error
            assert_true(false, peel + 1, () -> error_parser_trace(parse));
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
        assert_equals(parse.stack.peek(), value,
            "The top of the AST stack did not match the expected value.");
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
        assert_true(parse.error != -1, peel + 1,
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
