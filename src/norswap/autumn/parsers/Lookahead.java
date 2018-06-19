package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;

/**
 * Succeeds if its child succeed, but does not advance the input position (all other side effects
 * of the child are retained).
 */
public final class Lookahead extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    public Lookahead (Parser child)
    {
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;
        if (child.parse(parse)) {
            parse.pos = pos0;
            return true;
        }
        // parse.pos has already been reset
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        return "lookahead(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
