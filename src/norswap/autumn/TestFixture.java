package norswap.autumn;

import java.util.List;

/**
 * Make your test class inherit this class in order to benefit from the various {@code success}
 * assertion methods. Set the {@link #parser} field beforehand!
 *
 * Also see the documentation of other fields for more options.
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
     * Whether to display the erroneous call stack in case of parse error.
     */
    public boolean record_call_stack = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Override test with the equivalent assertion method of your test framework.
     * Must accept null as the message.
     */
    public abstract void assert_true (boolean condition, String msg);

    // ---------------------------------------------------------------------------------------------

    /**
     * Override test with the equivalent assertion method of your test framework.
     * Must accept null as the message.
     */
    public abstract void assert_equals (Object actual, Object expected, String msg);

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition)
    {
        assert_true(condition, null);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected)
    {
        assert_equals(actual, expected, null);
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
            System.out.println("Warning - Index out of bounds: " + e.getMessage());
            return "" + position;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     */
    private void success (Object input)
    {
        parse = make_parse(input, false);
        Throwable thrown = null;
        boolean result = false;

        try { result = parser.parse(parse); }
        catch (Throwable t) { thrown = t; }

        if (result && parse.pos == parse.input_length())
            return;

        // To improve test run times, parse without call stack recording,
        // and reparse only in case of error.
        if (record_call_stack)
        {
            Parse old = parse;
            parse = make_parse(input, true);

            final String parse2 = "Parse with call trace recording ";
            final String maybe  = "\nMaybe you made a parser stateful?";

            try { result = parser.parse(parse); }
            catch (Throwable t)
            {
                assert_true(thrown != null,
                    parse2 + "throws an exception while the initial parse does not."
                    + maybe + "\nException: " + t);
                assert thrown != null;
                assert_equals(t.getClass(), thrown.getClass(),
                    parse2 + "does not throw the same type of exception." + maybe);
            }

            assert_true(!result,
                parse2 + "succeeds while the initial parse fails." + maybe);

            assert_equals(old.error, parse.error,
                parse2 + "and initial parse do not fail at the same position." + maybe);
        }

        LineMap map = input instanceof String
            ? new LineMap((String) input, tab_width, column_start)
            : null;

        if (thrown != null)
        {
            System.out.println("\nException at position " + pos_string(map, parse.pos));
            thrown.printStackTrace(System.out);

            if (record_call_stack) {
                System.out.println("\nParser call stack:");
                for (ParserCallFrame frame: parse.call_stack())
                    System.out.println(
                        "at " + pos_string(map, frame.position) + " in " + frame.parser);
            }
        }
        else // parse failure
        {
            System.out.println("\nFurthest parse error at " + pos_string(map, parse.error));

            if (record_call_stack) {
                System.out.println("Parser call stack:");
                for (ParserCallFrame frame: parse.error_call_stack())
                    System.out.println(
                        "at " + pos_string(map, frame.position) + " in " + frame.parser);
            }
        }

        System.out.println();
        assert_true(false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public void success_expect (Object input, Object value)
    {
        success(input);
        assert_true(parse.stack.size() > 0, "Empty AST stack.");
        assert_equals(parse.stack.peek(), value);
    }

    // ---------------------------------------------------------------------------------------------
}
