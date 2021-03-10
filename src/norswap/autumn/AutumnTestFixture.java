package norswap.autumn;

import norswap.autumn.positions.LineMap;
import norswap.autumn.positions.LineMapString;
import norswap.autumn.positions.LineMapTokens;
import norswap.autumn.positions.Token;
import norswap.utils.TestFixture;
import java.util.List;
import java.util.function.Function;

import static norswap.utils.Util.cast;

/**
 * Make your test class inherit this class in order to benefit from its various {@code success},
 * {@code prefix} and {@code failure} assertion methods. Set the {@link #rule} field or call {@link
 * #parser} beforehand!
 *
 * <p>You can also instantiate this class and directly call its methods. This is handy when you want
 * your tests to inherit another class (such as {@link Grammar}). For an example of this, see {@code
 * test/TestParsers.java} in Autumn's source. In this case, you should re-assign {@link
 * #bottomClass}.
 *
 * <p>All parser assertion methods (variants with names starting by {@code success}, {@code prefix}
 * and {@code failure}) do actually run the parsers twice, as a way to catch non-determinism in the
 * parsing process (often caused by improper state handling). This can be disabled by setting {@link
 * #runTwice} to false.
 *
 * <p>You can specify the options for these parses by setting {@link #options}.
 *
 * <p>Also see the fields' documentation for more options, and the documentation of the parent class
 * {@link TestFixture}.
 *
 * <p>In particular, whenever an integer {@code peel} parameter is present, it indicates that this
 * many items should be removed from the bottom of the stack trace (outermost/earliest method calls)
 * of the thrown assertion error.
 *
 * <p>All assertion methods take care of peeling themselves off (as only the assertion call site
 * is really interesting), so you do not need to account for them in {@code peel}.
 */
@SuppressWarnings("UnusedReturnValue")
public class AutumnTestFixture extends TestFixture
{
    // ---------------------------------------------------------------------------------------------

    /**
     * The parser being currently tested.
     */
    private Parser parser;

    // ---------------------------------------------------------------------------------------------

    /**
     * The rule being currently tested. Set this or call {@link #parser} before calling any test
     * method.
     */
    public Grammar.rule rule;

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets a {@link Parser} to be tested, if you'd rather specify that than a {@link Grammar.rule}
     * via {@link #rule}.
     */
    public void parser (Parser parser) {
        this.rule = null;
        this.parser = parser;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Set this field to specify the options that should be used by a parse. If null, options will
     * be constructed automatically (they won't be assigned to this field). This field is used for
     * both parses that run during each test. Overrides {@link #recordCallStack} and {@link
     * #wellFormednessChecks}.
     */
    public ParseOptions options;

    // ---------------------------------------------------------------------------------------------

    /**
     * Set this field in order to add this as a name in front of positions in printed output
     * (cf. {@link LineMap#name()}. In particular it's used when constructed a {@link LineMap}
     * automatically, and has no effect if {@link #map} is non-{@code null}.
     *
     * <p>If you assign this, its value will be reset to {@code null} after calling any public
     * method in the class (in order to avoid confusing bugs and/or memory leaks).
     */
    public String inputName;

    // ---------------------------------------------------------------------------------------------

    /**
     * If this is set to a non-{@code null} value, it will be used to translate the input string
     * into a list of tokens. {@code null} by default.
     */
    public Function<String, List<?>> lexer = null;

    // ---------------------------------------------------------------------------------------------

    /**
     * LineMap used to provide file positions during diagnostics.
     *
     * <p>If {@code null}, this will be constructed automatically if possible: if the input is a
     * string and no {@link #lexer} is supplied, or if the lexer generates tokens that extend {@link
     * Token} (inferred from looking at the first token). The {@link #inputName} field will be
     * used for the input name if non-{@code null}.
     *
     * <p>If you assign this (in order to reuse an existing line map), its value will be reset to
     * {@code null} after calling any public method in the class (in order to avoid confusing bugs
     * and/or memory leaks).
     */
    public LineMap map = null;

    // ---------------------------------------------------------------------------------------------

    /**
     * First column index. 1 by default, you can change this to 0 if required.
     */
    public int columnStart = 1;

    // ---------------------------------------------------------------------------------------------

    /**
     * Visual tab width. 4 by default, you can change this if required.
     */
    public int tabWidth = 4;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the parse should be run twice, in order to check for parser non-determinism (usually
     * due to state mishandling). True by default.
     */
    public boolean runTwice = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to always record the parser call stack of the tested parsers. Defaults to true. If
     * set to false, the call stack will be recorded only on the second parser call, if the first
     * call failed. The only point of setting this to false is to speed up your tests.
     *
     * <p>Overriden by {@link #options} (whose value for call stack recording will be used for both
     * parses).
     */
    public boolean recordCallStack = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to perform a well-formedness check at the start of the first parse. Defaults to
     * true. Setting this to false can speed up your tests considerably (~2x).
     *
     * <p>Overriden by {@link #options} (whose value for well-formedness checking will be used for
     * both parses).
     */
    public boolean wellFormednessChecks = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * If set to true, only parsers which are are grammar rules (i.e. have a non-null {@link
     * Parser#rule()}) will be included in the string representation of parser call stacks.
     */
    public boolean onlyRulesInCallStacks = false;

    // ---------------------------------------------------------------------------------------------

    public AutumnTestFixture () {
        traceSeparator = "\n------";
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Make sure every public method calls this before returning, but not before it has finished
     * using the variables.
     */
    private void clearLocals() {
        this.map = null;
        this.inputName = null;
    }

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    private ParseResult parse (Object input)
    {
        if (rule != null)
            parser = rule.getParser();

        ParseOptions options = this.options != null
            ? this.options
            : ParseOptions
                .recordCallStack(recordCallStack)
                .wellFormednessCheck(wellFormednessChecks)
                .get();

        return input instanceof String
            ? rule != null
                ? Autumn.parse(rule, (String) input, options)
                : Autumn.parse(parser, (String) input, options)
            : rule != null
                ? Autumn.parse(rule, (List<?>) input, options)
                : Autumn.parse(parser, (List<?>) input, options);
    }

    // ---------------------------------------------------------------------------------------------

    private ParseResult run (Object input, boolean recordCallStack)
    {
        if (input instanceof List)
            return parse(input);

        if (!(input instanceof String))
            throw new IllegalArgumentException("invalid parse input type: " + input.getClass());

        String inputName = this.inputName != null ? this.inputName : "<test>";

        if (lexer == null) {
            this.map = new LineMapString(inputName, (String) input);
            return parse(input);
        }

        // String input + lexer

        List<?> tokens = lexer.apply((String) input);

        if (tokens.size() > 0 && tokens.get(0) instanceof Token)
            this.map = new LineMapTokens(inputName, (String) input, cast(tokens));

        return parse(tokens);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string starting with {@code msgHead}, then outlining the outcome of the two
     * supplied parses, as per {@link ParseResult#appendTo(StringBuilder, LineMap, boolean)}.
     */
    public String comparedStatus (String msgHead, LineMap map, ParseResult r1, ParseResult r2)
    {
        StringBuilder b = new StringBuilder(msgHead);
        b.append(" Maybe you made a parser stateful?\n\n");

        b.append("### Initial Parse ###\n\n");
        r1.appendTo(b, map, onlyRulesInCallStacks);

        b.append("\n\n"); // empty line.

        b.append("### Second Parse ###\n\n");
        r2.appendTo(b, map, onlyRulesInCallStacks);

        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    private ParseResult prefixInternal (Object input, int peel)
    {
        ParseResult r1 = run(input, recordCallStack);

        if (!runTwice) {
            assertTrue(r1.success, peel + 1,
                () -> r1.toString(map, onlyRulesInCallStacks));
            return r1;
        }

        ParseResult r2 = run(input, recordCallStack || !r1.success);

        assertTrue(r2.thrown == null || r1.thrown != null, peel + 1, () -> comparedStatus(
            "Second parse throws an exception while the initial parse does not.",
            map, r1, r2));

        assertTrue(r1.thrown == null || r2.thrown != null, peel + 1, () -> comparedStatus(
            "Second parse does not throw an exception while the initial parse does.",
            map, r1, r2));

        if (r1.thrown != null && r2.thrown != null)
            assertEquals(r1.thrown.getClass(), r2.thrown.getClass(), peel + 1,
                () -> comparedStatus(
                    "Second parse does not throw the same type of exception as the initial parse.",
                    map, r1, r2));

        assertEquals(r2.success, r1.success, peel + 1, () -> comparedStatus(
            "Second parse does not have the same success as the initial parse.",
            map, r1, r2));

        if (r1.success)
            assertEquals(r2.matchSize, r1.matchSize, peel + 1, () -> comparedStatus(
                "Second parse and initial parse do not consume the same amount of input.",
                map, r1, r2));
        else
            assertEquals(r2.errorOffset, r1.errorOffset, peel + 1, () -> comparedStatus(
                "Second parse and initial parse do not fail at the same position.",
                map, r1, r2));

        // At this point we have ascertained that the two parses should be equivalent.
        // It's impossible to be sure, however, and so we base everything upon the first one,
        // so that we are at least consistent.

        assertTrue(r1.success, peel + 1,
            () -> r1.toString(map, onlyRulesInCallStacks));

        return r1;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input.
     */
    public ParseResult prefix (Object input, int peel)
    {
        ParseResult result = prefixInternal(input, peel);
        clearLocals();
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input.
     */
    public ParseResult prefix (Object input) {
        ParseResult result = prefixInternal(input, 1);
        clearLocals();
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input, and that the
     * top of the stack is equal to {@code value}.
     */
    public ParseResult prefixExpect (Object input, Object value, int peel)
    {
        ParseResult r = prefixInternal(input, peel + 1);
        assertTrue(r.valueStack.size() > 0, peel + 1,
            () -> "Empty AST stack.");
        assertTrue(r.valueStack.size() == 1, peel + 1,
            () -> "Expect a single item on the stack, but got multiple: " + r.valueStack);
        assertEquals(r.topValue(), value, peel + 1,
            () -> "The top of the AST stack did not match the expected value.");
        clearLocals();
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input, and that the
     * top of the stack is equal to {@code value}.
     */
    public ParseResult prefixExpect (Object input, Object value) {
        return prefixExpect(input, value, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching a prefix of the given input with the given
     * length.
     */
    public ParseResult prefixOfLength (Object input, int length, int peel)
    {
        ParseResult r = prefixInternal(input, peel + 1);
        assertTrue(r.matchSize == length, peel + 1,
            () -> r.toString(map, onlyRulesInCallStacks));
        clearLocals();
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input.
     */
    public ParseResult success (Object input, int peel)
    {
        ParseResult r = prefixInternal(input, peel + 1);
        assertTrue(r.fullMatch, peel + 1,
            () -> r.toString(map, onlyRulesInCallStacks));
        clearLocals();
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input.
     */
    public ParseResult success (Object input) {
        return success(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public ParseResult successExpect (Object input, Object value, int peel)
    {
        ParseResult r = success(input, peel + 1);
        assertTrue(r.valueStack.size() > 0, peel + 1,
            () -> "Empty AST stack.");
        assertEquals(r.valueStack.peek(), value, peel + 1,
            () -> "The top of the AST stack did not match the expected value.");
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser succeeds matching all of the given input, and that
     * the top of the stack is equal to {@code value}.
     */
    public ParseResult successExpect (Object input, Object value) {
        return successExpect(input, value, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input.
     */
    public ParseResult failure (Object input, int peel)
    {
        ParseResult r = run(input, recordCallStack);

        assertTrue(!r.fullMatch, peel + 1,
            () -> "Parse succeeded when it was expected to fail.");

        clearLocals();
        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input.
     */
    public ParseResult failure (Object input) {
        return failure(input, 1);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public ParseResult failureAt (Object input, int errorPosition, int peel)
    {
        ParseResult r = failure(input, peel + 1);

        assertEquals(r.errorOffset, errorPosition, peel + 1,
            () -> "The furthest parse error didn't occur at the expected location.");

        return r;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the rule or parser fails to match all of the given input, and additionally
     * that the furthest error occurs at the given input position.
     */
    public ParseResult failureAt (Object input, int error) {
        return failureAt(input, error, 1);
    }

    // ---------------------------------------------------------------------------------------------
}
