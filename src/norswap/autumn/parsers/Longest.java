package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.SideEffect;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.utils.Vanilla.pop;

/**
 * Matches the same thing as its longest matching child, or fails if none succeed.
 */
public final class Longest extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Parser[] children;

    // ---------------------------------------------------------------------------------------------

    public List<Parser> children()
    {
        return Collections.unmodifiableList(Arrays.asList(children));
    }

    // ---------------------------------------------------------------------------------------------

    public Longest (Parser... children)
    {
        this.children = children;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        int pos0 = parse.pos;
        int log0 = parse.log.size();

        int max_pos = pos0;
        List<SideEffect> delta = null;

        for (Parser child: children)
        {
            boolean success = child.parse(parse);
            if (success) {
                if (parse.pos > max_pos) {
                    max_pos = parse.pos;
                    delta = parse.delta(log0);
                }

                parse.pos = pos0;
                parse.rollback(log0);
            }
        }

        if (delta == null)
            return false;

        parse.pos = max_pos;
        for (SideEffect effect: delta)
            parse.apply(effect);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("longest(");
        for (Parser child: children)
            b.append(child).append(", ");
        if (children.length > 0)
            pop(b, 2);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
