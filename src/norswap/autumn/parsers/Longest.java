package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.SideEffect;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.utils.Strings.joinArray;

/**
 * Matches the same thing as its longest matching child, or fails if none succeed.
 * In case of a tie, matches like the earliest longest matching child.
 *
 * <p>Build with {@link Grammar#longest(Object...)}
 */
public final class Longest extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Parser[] children;

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
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

        int maxPos = pos0;
        List<SideEffect> delta = null;

        for (Parser child: children)
        {
            boolean success = child.parse(parse);
            if (success) {
                if (parse.pos > maxPos) {
                    maxPos = parse.pos;
                    delta = parse.log.delta(log0);
                }

                parse.pos = pos0;
                parse.log.rollback(log0);
            }
        }

        if (delta == null)
            return false;

        parse.pos = maxPos;
        parse.log.apply(delta);
        return true;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("longest(");
        joinArray(b, ", ", children);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
