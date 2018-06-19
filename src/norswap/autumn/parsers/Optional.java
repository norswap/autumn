package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;

/**
 * Matches its child if it succeeds, otherwise succeeds without consuming any input.
 */
public final class Optional extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    public Optional (Parser child)
    {
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        child.parse(parse);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        return "optional(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}