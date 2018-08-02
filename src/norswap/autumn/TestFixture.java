package norswap.autumn;

import norswap.lang.java.ast.Literal;
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
    public boolean record_call_stack = false;

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

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     */
    public void success (String input)
    {
        parse = Parse.of(input);
        boolean result = parser.parse(parse);
        if (result && parse.pos == input.length()) return;

        // To improve routine test run times, parse without call stack recording,
        // and reparse only in case of error.
        if (record_call_stack)
        {
            Parse old = parse;
            parse = Parse.of(input, true);

            assert_true(!parser.parse(parse),
                "Parse with call trace recording succeeds while the initial parse fails. " +
                "Maybe you made a parser stateful?");

            assert_equals(old.error, parse.error,
                "Parse with call trace recording and initial parse do not fail at the same position. " +
                "Maybe you made a parser stateful?");
        }

        LineMap map = new LineMap(input, tab_width, column_start);
        LineMap.Position pos = map.position_from(parse.error);
        System.out.println("Furthest parse error at " + pos);

        if (record_call_stack)
            for (Parser parser: parse.call_stack())
                System.out.println("in " + parser);

        System.out.println();

        assert_true(false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input.
     */
    public void success (List<?> input)
    {
        parse = Parse.of(input);
        boolean result = parser.parse(parse);
        if (result && parse.pos == input.size()) return;

        // To improve routine test run times, parse without call stack recording,
        // and reparse only in case of error.
        if (record_call_stack)
        {
            Parse old = parse;
            parse = Parse.of(input, true);

            assert_true(!parser.parse(parse),
                "Parse with call trace recording succeeds while the initial parse fails. " +
                    "Maybe you made a parser stateful?");

            assert_equals(old.error, parse.error,
                "Parse with call trace recording and initial parse do not fail at the same position. " +
                    "Maybe you made a parser stateful?");
        }

        System.out.println("Furthest parse error at " + parse.error);

        if (record_call_stack)
            for (Parser parser: parse.call_stack())
                System.out.println("in " + parser);

        System.out.println();

        assert_true(false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public void success_expect (String input, Object value)
    {
        success(input);
        assert_true(parse.stack.size() > 0, "Empty AST stack.");
        assert_equals(parse.stack.peek(), value);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that {@link #parser} succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public void success_expect (List<?> input, Object value)
    {
        success(input);
        assert_true(parse.stack.size() > 0, "Empty AST stack.");
        assert_equals(parse.stack.peek(), value);
    }

    // ---------------------------------------------------------------------------------------------
}
