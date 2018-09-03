package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Arrays;

/**
 * Matches an alternating sequence of its {@code around} and {@code inside} children, where the
 * first and last item of the sequence is always {@code around}. See {@link #Around}.
 */
public final class Around extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final int min;

    // ---------------------------------------------------------------------------------------------

    public final boolean exact;

    // ---------------------------------------------------------------------------------------------

    public final boolean trailing;

    // ---------------------------------------------------------------------------------------------

    public final Parser around;

    // ---------------------------------------------------------------------------------------------

    public final Parser inside;

    // ---------------------------------------------------------------------------------------------

    private final Parser inside_then_around;

    // ---------------------------------------------------------------------------------------------

    /**
     * This parser will matches at least {@code min} repetitions of {@code around}, separated by
     * matches for {@code inside}. If {@code exact} is true, will match exactly {@code min}
     * repetitions. Otherwise, matches as many repetitions as possible. If {@code trailing} is true,
     * allows an optional trailing match for {@code inside}.
     */
    public Around (int min, boolean exact, boolean trailing, Parser around, Parser inside)
    {
        this.min = min;
        this.exact = exact;
        this.trailing = trailing;
        this.around = around;
        this.inside = inside;
        this.inside_then_around = new Sequence(inside, around);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        if (!around.parse(parse)) {
            if (min == 0 && trailing)
                inside.parse(parse);
            return min == 0;
        }
        for (int i = 0; i < min - 1; ++i)
            if (!inside_then_around.parse(parse))
                return false;
        if (!exact)
            while (inside_then_around.parse(parse)) ;
        if (trailing)
            inside.parse(parse);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children() {
        return Arrays.asList(around, inside);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("around(");
        b.append(around).append(", ");
        b.append(inside).append(", ");
        b.append(min);
        if (exact)
            b.append(", exact");
        if (trailing)
            b.append(", trailing");
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
