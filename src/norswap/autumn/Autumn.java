package norswap.autumn;

import java.util.List;

/**
 * This class holds the {@code run} methods, which are the entry points to start a parse.
 */
public final class Autumn
{
    // ---------------------------------------------------------------------------------------------

    private Autumn () {}

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with {@code parser} and the given parse options (uses {@link
     * ParseOptions#DEFAULT_PARSE_OPTIONS} if null).
     */
    public static ParseResult run (Parser parser, String string, ParseOptions options) {
        return Parse.run(parser, string, null, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with {@code parser} and the given parse options (uses {@link
     * ParseOptions#DEFAULT_PARSE_OPTIONS} if null).
     */
    public static ParseResult run (Parser parser, List<?> list, ParseOptions options) {
        return Parse.run(parser, null, list, options);
    }

    // ---------------------------------------------------------------------------------------------
}