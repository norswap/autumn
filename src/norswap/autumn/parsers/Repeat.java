package norswap.autumn.parsers;

import norswap.autumn.Grammar.rule;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Collections;

/**
 * Matches repetitions of its child. See {@link #Repeat} for more details.
 *
 * <p>Build with {@link rule#at_least(int)} or {@link rule#repeat(int)}.
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

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Collections.singletonList(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull() {
        return String.format("repeat(%s, %d%s)", child, min, exact ? ", exact" : "");
    }

    // ---------------------------------------------------------------------------------------------
}
