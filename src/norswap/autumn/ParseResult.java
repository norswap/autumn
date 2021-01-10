package norswap.autumn;

import norswap.autumn.positions.LineMap;
import norswap.autumn.util.ArrayStack;
import norswap.utils.exceptions.Exceptions;
import java.util.Map;

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
    public final boolean fullMatch;

    // ---------------------------------------------------------------------------------------------

    /**
     * Size of the match (which is also the input position one past the last matched item) if the parse
     * succeeded, or -1 otherwise.
     */
    public final int matchSize;

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
    public final int errorPosition;

    // ---------------------------------------------------------------------------------------------

    /**
     * If the parse ended with an exception, the message for the exception; otherwise
     * the message associated with the furthest error (cf. {@link #errorPosition}, if any.
     * May be null if no message was defined or the parse is a full match.
     */
    public final String errorMessage;

    // ---------------------------------------------------------------------------------------------

    /**
     * The final state of the parse value stack. This will be empty if the parse failed.
     */
    public final ArrayStack<?> valueStack;

    // ---------------------------------------------------------------------------------------------

    /**
     * A map from parse state keys ({@link ParseState#key}) to parse states (the state holder that
     * is a type parameter to an instance of {@link ParseState}) used during the parse.
     *
     * <p>Note that if the parse did not need to read or write the parse state, it will not
     * appear here, even thought the parser might require it for other inputs!
     */
    public final Map<Object, Object> parseStates;

    // ---------------------------------------------------------------------------------------------

    /**
     * A stack of parse invocations (call stack) reported for unsuccessful parses if the {@link
     * ParseOptions#recordCallStack} option was specified (otherwise always null).
     *
     * <p>If the parse ended with an exception, this is the call stack at that point; otherwise if
     * the parse isn't a full match, this is the call stack at the point of the furthest error;
     * otherwise null.
     */
    public final ParserCallStack errorCallStack;

    // ---------------------------------------------------------------------------------------------

    /**
     * Trace results, if the {@link ParseOptions#trace} option was specified, null otherwise.
     */
    public final ParseMetrics parseMetrics;

    // ---------------------------------------------------------------------------------------------

    /**
     * The value at the top of the value stack if the parse was successful and the value stack
     * is non-empty, null otherwise.
     *
     * <p>This methods auto-casts its return value to the target type.
     */
    public <T> T topValue () {
        return valueStack != null
            ? cast(valueStack.peek())
            : null;
    }

    // ---------------------------------------------------------------------------------------------

    ParseResult (
        boolean success,
        boolean fullMatch,
        int matchSize,
        Throwable thrown,
        Parser parser,
        ParseOptions options,
        int errorPosition,
        String errorMessage,
        ArrayStack<?> valueStack,
        Map<Object, Object> parseStates,
        ParserCallStack errorCallStack,
        ParseMetrics parseMetrics)
    {
        this.success = success;
        this.fullMatch = fullMatch;
        this.matchSize = matchSize;
        this.thrown = thrown;
        this.parser = parser;
        this.options = options;
        this.errorPosition = errorPosition;
        this.errorMessage = errorMessage;
        this.valueStack = valueStack;
        this.parseStates = parseStates;
        this.errorCallStack = errorCallStack;
        this.parseMetrics = parseMetrics;

        // Do not make this an assertion, as the parsing failure may provide information as to
        // why this happens.
        if (!success && !valueStack.isEmpty())
            System.err.println("Parse failed, but value stack is not empty: " + valueStack);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the parse state data for the given key, casting it to {@code T}.
     */
    public <T> T parseState (Object key) {
        return cast(parseStates.get(key));
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
     *     <li>Always a terminating newline.</li>
     * </ul>
     *
     * @param map If non-null, used to translate input positions in terms of lines and columns.
     *
     * @param onlyRules If true and a parser call stack should be printed, only parsers which are
     * are grammar rules (i.e. have a non-null {@link Parser#rule()}) will be included in the
     * representation.
     *
     * @param filePath If non-null, appended in front of the input positions position in order for
     * them to be become clickable in IntelliJ (and potentially other editors). This is only useful
     * if a {@code map} is also supplied. Note that in IntelliJ, only absolute paths enable linking
     * to colums in addition to lines.
     */
    public void appendTo (StringBuilder b, LineMap map, boolean onlyRules, String filePath)
    {
        if (fullMatch) {
            b.append("Parse succeeded, consuming the whole input.\n");
            return;
        }

        if (thrown != null)
        {
            b.append("Exception thrown at position ");
            if (filePath != null) b.append(filePath).append(":");
            b.append(LineMap.string(map, errorPosition));

            if (options.recordCallStack) {
                b.append("\n");
                b.append(thrown.getClass());
                b.append(": ");
                b.append(thrown.getMessage());
                b.append("\n\nParser trace:\n");
                errorCallStack.appendTo(b, 1, map, false, filePath);
            }

            b.append("\n\nThrown: ");
            b.append(Exceptions.stringStackTrace(thrown));
            return;
        }

        if (success) {
            b.append("Parse succeeded, consuming up to ");
            if (filePath != null) b.append(filePath).append(":");
            b.append(LineMap.string(map, matchSize));
            b.append(".\n");
        } else {
            b.append("Parse failed.\n");
        }

        b.append("Furthest parse error at ");
        if (filePath != null) b.append(filePath).append(":");
        b.append(LineMap.string(map, errorPosition));
        b.append(".\n");

        if (options.recordCallStack) {
            errorCallStack.appendTo(b, 1, map, onlyRules, filePath);
            b.append("\n");
        } else {
            b.append("For more details, ");
            b.append("rerun the parse with ParseOptions#recordCallStack set to true.\n");
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the results of the parse, as per {@link
     * #appendTo(StringBuilder, LineMap, boolean, String)}.
     *
     * @param map If non-null, used to translate input positions in terms of lines and columns.
     *
     * @param onlyRules If true and a parser call stack should be printed, only parsers which are
     * are grammar rules (i.e. have a non-null {@link Parser#rule()}) will be included in the
     * representation.
     *
     * @param filePath If non-null, appended in front of the input positions position in order for
     * them to be become clickable in IntelliJ (and potentially other editors). This is only useful
     * if a {@code map} is also supplied. Note that in IntelliJ, only absolute paths enable linking
     * to colums in addition to lines.
     */
    public String toString (LineMap map, boolean onlyRules, String filePath)
    {
        StringBuilder b = new StringBuilder();
        appendTo(b, map, onlyRules, filePath);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the results of the parse, as per {@link
     * #appendTo(StringBuilder, LineMap, boolean, String)}.
     *
     * <p>No line map is supplied, so input positions are reported as simple offsets. All parsers
     * will be included (not only rules) and no file name will be included.
     */
    @Override public String toString()
    {
        return toString(null, false, null);
    }

    // ---------------------------------------------------------------------------------------------
}
