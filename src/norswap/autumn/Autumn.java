package norswap.autumn;

import java.util.List;

/**
 * This class holds the {@link #run} methods which are the entry point to start a parse.
 */
public final class Autumn
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code string} with {@code parser} and the given parse options (uses {@link
     * ParseOptions#≠DEFAULT_PARSE_OPTIONS} if null).
     */
    public static ParseResult run (Parser parser, String string, ParseOptions options) {
        return Parse.run(parser, string, null, options);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Parses {@code list} with {@code parser} and the given parse options (uses {@link
     * ParseOptions#≠DEFAULT_PARSE_OPTIONS} if null).
     */
    public static ParseResult run (Parser parser, List<?> list, ParseOptions options) {
        return Parse.run(parser, null, list, options);
    }

    // ---------------------------------------------------------------------------------------------
}