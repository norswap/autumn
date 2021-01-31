package norswap.autumn;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * This class holds the {@code run} methods, which are the entry points to start a parse.
 */
public final class Autumn
{
    // ---------------------------------------------------------------------------------------------

    private Autumn() {}

    // ---------------------------------------------------------------------------------------------

    private static final String warning =
        "Stack overflow during parse. Maybe your grammar is not well-formed " +
        "(contains left-recursion or repetition over nullable parsers)? " +
        "Re-run the parse with options ParseOptions#wellFormednessCheck or " +
        " ParseOptions#wellFormednessChecker to verify.";

    // ---------------------------------------------------------------------------------------------

    private static class PotentiallyMalformedGrammarError extends Error
    {
        PotentiallyMalformedGrammarError (StackOverflowError e) {
            // no stack trace for this error
            super(warning, e, true, false);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with {@code parser} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     *
     * <p>Not truly deprecated, but you should generally prefer calling an overload that takes
     * a {@link Grammar} or a {@link Grammar.rule}.
     */
    @Deprecated
    public static ParseResult parse (Parser parser, String string, ParseOptions options)
    {
        requireNonNull(parser,  "Parser cannot be null.");
        requireNonNull(string,  "Input string cannot be null.");
        requireNonNull(options, "Parse options cannot be null.");

        try {
            return Parse.run(parser, string, null, options);
        } catch (StackOverflowError e) {
            throw new PotentiallyMalformedGrammarError(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with {@code parser} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     *
     * <p>Not truly deprecated, but you should generally prefer calling an overload that takes
     * a {@link Grammar} or a {@link Grammar.rule}.
     */
    @Deprecated
    public static ParseResult parse (Parser parser, List<?> list, ParseOptions options)
    {
        requireNonNull(parser,  "Parser cannot be null.");
        requireNonNull(list,    "Input list cannot be null.");
        requireNonNull(options, "Parse options cannot be null.");
        try {
            return Parse.run(parser, null, list, options);
        } catch (StackOverflowError e) {
            throw new PotentiallyMalformedGrammarError(e);
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with {@code rule} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (Grammar.rule rule, String string, ParseOptions options)
    {
        requireNonNull(rule, "Rule cannot be null.");
        rule.grammar().makeRuleNames();
        return parse(rule.getParser(), string, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with {@code rule} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (Grammar.rule rule, List<?> list, ParseOptions options)
    {
        requireNonNull(rule, "Rule cannot be null.");
        rule.grammar().makeRuleNames();
        return parse(rule.getParser(), list, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with the root rule of {@code grammar} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (Grammar grammar, String string, ParseOptions options)
    {
        requireNonNull(grammar, "Grammar cannot be null.");
        return parse(grammar.root(), string, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with the root rule of {@code grammar} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (Grammar grammar, List<?> list, ParseOptions options)
    {
        requireNonNull(grammar, "Grammar cannot be null.");
        return parse(grammar.root(), list, options);
    }

    // ---------------------------------------------------------------------------------------------
}