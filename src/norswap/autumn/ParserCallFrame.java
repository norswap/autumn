package norswap.autumn;

/**
 * Represents a parser invocation at a certain input position.
 */
public final class ParserCallFrame
{
    // ---------------------------------------------------------------------------------------------

    public final Parser parser;

    // ---------------------------------------------------------------------------------------------

    public final int position;

    // ---------------------------------------------------------------------------------------------

    ParserCallFrame (Parser parser, int position)
    {
        this.parser = parser;
        this.position = position;
    }

    // ---------------------------------------------------------------------------------------------
}
