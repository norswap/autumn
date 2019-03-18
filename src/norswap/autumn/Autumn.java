package norswap.autumn;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * This class holds the {@code run} methods, which are the entry points to start a parse.
 */
public final class Autumn
{
    // ---------------------------------------------------------------------------------------------

    private Autumn () {}

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with {@code parser} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (Parser parser, String string, ParseOptions options)
    {
        requireNonNull(parser, "Parser cannot be null.");
        requireNonNull(string, "Input string cannot be null.");
        requireNonNull(options, "Parse options cannot be null.");
        return Parse.run(parser, string, null, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with {@code parser} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (Parser parser, List<?> list, ParseOptions options)
    {
        requireNonNull(parser, "Parser cannot be null.");
        requireNonNull(list, "Input list cannot be null.");
        requireNonNull(options, "Parse options cannot be null.");
        return Parse.run(parser, null, list, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with {@code rule} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (DSL.rule rule, String string, ParseOptions options)
    {
        requireNonNull(rule, "Rule cannot be null.");
        return parse(rule.get(), string, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with {@code rule} and the given parse options.
     *
     * <p>Use {@code ParseOptions.get()} to get a default set of options.
     */
    public static ParseResult parse (DSL.rule rule, List<?> list, ParseOptions options)
    {
        requireNonNull(rule, "Rule cannot be null.");
        return parse(rule.get(), list, options);
    }

    // ---------------------------------------------------------------------------------------------
}