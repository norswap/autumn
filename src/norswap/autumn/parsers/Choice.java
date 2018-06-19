package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.utils.Vanilla.pop;

/**
 * Matches the same thing as its first matching child, or fails if none succeed.
 */
public final class Choice extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Parser[] children;

    // ---------------------------------------------------------------------------------------------

    public List<Parser> children()
    {
        return Collections.unmodifiableList(Arrays.asList(children));
    }

    // ---------------------------------------------------------------------------------------------

    public Choice (Parser... children)
    {
        this.children = children;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public boolean doparse (Parse parse)
    {
        for (Parser child: children)
            if (child.parse(parse))
                return true;
        return false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder();
        b.append("choice(");
        for (Parser child: children)
            b.append(child).append(", ");
        if (children.length > 0)
            pop(b, 2);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
