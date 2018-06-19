package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;

/**
 * Matches repetitions of its child. See {@link #Repeat}
 */
public final class Repeat extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final int min;

    // ---------------------------------------------------------------------------------------------

    public final boolean exact;

    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    /**
     * This parser will matches at least {@code min} repetitions of {@code child}. If {@code exact}
     * is true, will match exactly {@code min} repetitions. Otherwise, matches as many repetitions
     * as possible.
     */
    public Repeat (int min, boolean exact, Parser child)
    {
        this.min = min;
        this.exact = exact;
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        for (int i = 0; i < min; ++i)
            if (!child.parse(parse))
                return false;
        if (!exact)
            while (child.parse(parse)) ;
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("repeat(");
        b.append(child).append(", ");
        b.append(min);
        if (exact)
            b.append(", exact");
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
