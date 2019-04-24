package norswap.autumn;

import java.util.List;

/**
 * Make your test class inherit this class in order to benefit from its various {@code success}
 * and {@code failure} assertion methods. Set the {@link #parser} field beforehand!
 *
 * <p>You can also instantiate this class and directly call its methods. This is handy when you want
 * your tests to inherit another class (such as {@link DSL}). For an example of this, see {@code
 * test/TestParsers.java} in Autumn's source. In this case, you should re-assign {@link
 * #bottom_class}.
 *
 * <p>All parser assertion methods (variants with names starting by {@code success}, {@code prefix}
 * and {@code failure}) do actually run the parsers twice, as a way to catch non-determinism in the
 * parsing process (often caused by improper state handling).
 *
 * <p>Also see the fields' documentation for more options, and the documentation of the parent class
 * {@link norswap.autumn.util.TestFixture}.
 *
 * <p>In particular, whenever an integer {@code peel} parameter is present, it indicates that this
 * many items should be removed from the bottom of the stack trace (outermost/earliest method calls)
 * of the thrown assertion error.
 *
 * <p>All assertion methods take care of peeling themselves off (as only the assertion call site
 * is really interesting), so you do not need to account for them in {@code peel}.
 */
@SuppressWarnings("UnusedReturnValue")
public class TestFixture extends norswap.autumn.util.TestFixture
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The parser being currently tested.
     */
    private Parser parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule being currently tested.
     */
    public DSL.rule rule;

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets a {@link Parser} to be tested, if you'd rather specify that than a {@link DSL.rule}
     * via {@link #rule}.
     */
    public void parser (Parser parser) {
        this.rule = null;
        this.parser = parser;
    }

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
        if (rule != null)
            parser = rule.get();

        ParseOptions options = record_call_stack
            ? ParseOptions.record_call_stack().get()
            : ParseOptions.get();

        if (input instanceof String)
            return Autumn.parse(parser, (String) input, options);
        if (input instanceof List)
            return Autumn.parse(parser, (List<?>) input, options);
        throw new Error();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string starting with {@code msg_head}, then outlining the outcome of the two
     * supplied parses, as per {@link ParseResult#append_to(StringBuilder, LineMap)}.
     */
    public String compared_status (String msg_head, LineMap map, ParseResult r1, ParseResult r2)
    {
        StringBuilder b = new StringBuilder(msg_head);
        b.append(" Maybe you made a parser stateful?\n\n");

        b.append("### Initial Parse ###\n\n");
        r1.append_to(b, map);

        b.append("\n\n"); // empty line.

        b.append("### Second Parse ###\n\n");
        r2.append_to(b, map);

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private ParseResult prefix (Object input, LineMap map, int peel)
    {
        ParseResult r1 = run(input, record_call_stack);
        ParseResult r2 = run(input, record_call_stack || !r1.success);

        assert_true(r2.thrown == null || r1.thrown != null, peel + 1, () -> compared_status(
            "Second parse throws an exception while the initial parse does not.",
            map, r1, r2));

        assert_true(r1.thrown == null || r2.thrown != null, peel + 1, () -> compared_status(
            "Second parse does not throw an exception while the initial parse does.",
            map, r1, r2));

        if (r1.thrown != null && r2.thrown != null)
            assert_equals(r1.thrown.getClass(), r2.thrown.getClass(), peel + 1,
                () -> compared_status(
                    "Second parse does not throw the same type of exception as the initial parse.",
                    map, r1, r2));

        assert_equals(r2.success, r1.success, peel + 1, () -> compared_status(
            "Second parse does not have the same success as the initial parse.",
            map, r1, r2));

        if (r1.success)
            assert_equals(r2.match_size, r1.match_size, peel + 1, () -> compared_status(
                "Second parse and initial parse do not consume the same amount of input.",
                map, r1, r2));
        else
            assert_equals(r2.error_position, r1.error_position, peel + 1, () -> compared_status(
                "Second parse and initial parse do not fail at the same position.",
                map, r1, r2));

        // At this point we have ascertained that the two parses should be equivalent.
        // It's impossible to be sure, however, and so we base everything upon the first one,
        // so that we are at least consistent.

        assert_true(r1.success, peel + 1, () -> r1.toString(map));
        return r1;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input.
     */
    public ParseResult prefix (Object input, int peel)
    {
        LineMap map = input instanceof String ? new LineMap((String) input) : null;
        return prefix(input, map, peel + 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input.
     */
    public ParseResult prefix (Object input) {
        return prefix(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input, and that the
     * top of the stack is equal to {@code value}.
     */
    public ParseResult prefix_expect (Object input, Object value, int peel)
    {
        LineMap map = input instanceof String ? new LineMap((String) input) : null;
        ParseResult r = prefix(input, map, peel + 1);
        assert_true(r.value_stack.size() > 0, peel + 1,
            () -> "Empty AST stack.");
        assert_equals(r.value_stack.peek(), value, peel + 1,
            () -> "The top of the AST stack did not match the expected value.");
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input, and that the
     * top of the stack is equal to {@code value}.
     */
    public ParseResult prefix_expect (Object input, Object value) {
        return prefix_expect(input, value, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input with the given
     * length.
     */
    public ParseResult prefix_of_length (Object input, int length, int peel)
    {
        LineMap map = input instanceof String ? new LineMap((String) input) : null;
        ParseResult r = prefix(input, map, peel + 1);
        assert_true(r.match_size == length, peel + 1, () -> r.toString(map));
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input.
     */
    public ParseResult success (Object input, int peel)
    {
        LineMap map = input instanceof String ? new LineMap((String) input) : null;
        ParseResult r = prefix(input, map, peel + 1);
        assert_true(r.full_match, peel + 1, () -> r.toString(map));
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input.
     */
    public ParseResult success (Object input)
    {
        return success(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input, and that
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
     * Asserts that the rule or parser succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public ParseResult success_expect (Object input, Object value)
    {
        return success_expect(input, value, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input.
     */
    public ParseResult failure (Object input, int peel)
    {
        ParseResult r = run(input, record_call_stack);

        assert_true(!r.full_match, peel + 1,
            () -> "Parse succeeded when it was expected to fail.");

        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input.
     */
    public ParseResult failure (Object input)
    {
        return failure(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input, and additionally
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
     * Asserts that the rule or parser fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public ParseResult failure_at (Object input, int error)
    {
        return failure_at(input, error, 1);
    }

    // ---------------------------------------------------------------------------------------------
}
