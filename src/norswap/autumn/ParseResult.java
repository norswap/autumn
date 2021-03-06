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
     * If the parse ended with an exception, the input offset at which this exception occured;
     * otherwise if the parse isn't a full match, the position of the furthest error encountered;
     * otherwise -1.
     */
    public final int errorOffset;

    // ---------------------------------------------------------------------------------------------

    /**
     * If the parse ended with an exception, the message for the exception; otherwise the message
     * associated with the furthest error (cf. {@link #errorOffset}, if any. May be null if no
     * message was defined or the parse is a full match.
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
    public <T> T topValue() {
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
        int errorOffset,
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
        this.errorOffset = errorOffset;
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
     * Append user-facing failure information to {@code b}.
     * <p> {@code map} and {@code filePath} are allowed to be null.
     */
    private void appendUserErrorMessage (StringBuilder b, LineMap map)
    {
        String name = map == null ? null : map.name();
        if (success) {
            b.append("Parse succeeded without consuming full input, up to ");
            if (name != null) b.append(name).append(":");
            b.append(LineMap.string(map, matchSize));
            b.append(".\n");
        } else {
            b.append("Parse failed.\n");
        }

        b.append("Furthest parse error at ");
        if (name != null) b.append(name).append(":");
        b.append(LineMap.string(map, errorOffset));
        b.append(".\n");
        if (map != null) b.append(map.lineSnippet(map.positionFrom(errorOffset)));
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
     */
    public void appendTo (StringBuilder b, LineMap map, boolean onlyRules)
    {
        if (fullMatch) {
            b.append("Parse succeeded, consuming the whole input.\n");
            return;
        }

        String name = map == null ? null : map.name();

        if (thrown != null)
        {
            b.append("Exception thrown at position ");
            if (name != null) b.append(name).append(":");
            b.append(LineMap.string(map, errorOffset));

            if (options.recordCallStack) {
                b.append("\n");
                b.append(thrown.getClass());
                b.append(": ");
                b.append(thrown.getMessage());
                b.append("\n\nParser trace:\n");
                errorCallStack.appendTo(b, 1, map, false, name);
            }

            b.append("\n\nThrown: ");
            b.append(Exceptions.stringStackTrace(thrown));
            return;
        }

        appendUserErrorMessage(b, map);

        if (options.recordCallStack) {
            errorCallStack.appendTo(b, 1, map, onlyRules, name);
            b.append("\n");
        } else {
            b.append("For more details, ");
            b.append("rerun the parse with ParseOptions#recordCallStack set to true.\n");
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the results of the parse, as per {@link
     * #appendTo(StringBuilder, LineMap, boolean)}.
     *
     * @param map If non-null, used to translate input positions in terms of lines and columns.
     *
     * @param onlyRules If true and a parser call stack should be printed, only parsers which are
     * are grammar rules (i.e. have a non-null {@link Parser#rule()}) will be included in the
     * representation.
     */
    public String toString (LineMap map, boolean onlyRules)
    {
        StringBuilder b = new StringBuilder();
        appendTo(b, map, onlyRules);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string representation of the results of the parse, as per {@link
     * #appendTo(StringBuilder, LineMap, boolean)}.
     *
     * <p>No line map is supplied, so input positions are reported as simple offsets. All parsers
     * will be included (not only rules) and no file name will be included.
     */
    @Override public String toString() {
        return toString(null, false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a string containing a parse error message containing only user-facing information
     * (no rule names, stack trace, etc).
     *
     * <p>This method must not be called (and will throw an exception) if called on a full match
     * result ({@link #fullMatch}) or when an exception was thrown ({@link #thrown}{@code != null}).
     *
     * <p>The message will contain the position of the furthest parse error. This position will be
     * in line:column format if {@code map} is non-null, in which case a code snippet of the
     * location is also shown. Otherwise, a simple character offset is used.
     */
    public String userErrorString (LineMap map)
    {
        if (fullMatch)
            throw new IllegalStateException(
                "calling ParseResult#userErrorString on a successful parse");
        if (thrown != null)
            throw new IllegalStateException(
                "calling ParseResult#userErrorString on a parser where an exception was thrown");

        StringBuilder b = new StringBuilder();
        appendUserErrorMessage(b, map);
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
