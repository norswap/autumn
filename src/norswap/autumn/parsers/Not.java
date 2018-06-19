package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;

/**
 * Succeeds only if its child fails.
 */
public final class Not extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    public Not (Parser child)
    {
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int error0 = parse.error;
        // if the child matches, #parse will undo its side effects
        boolean success = !child.parse(parse);
        // negated parsers should not count towards the furthest error
        parse.error = error0;
        return success;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        return "not(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
