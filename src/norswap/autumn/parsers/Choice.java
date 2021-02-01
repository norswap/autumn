package norswap.autumn.parsers;

import norswap.autumn.Grammar;
import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static norswap.utils.Strings.joinArray;

/**
 * Matches the same thing as its first matching child, or fails if none succeed.
 *
 * <p>Build with {@link Grammar#choice(Object...)}
 */
public final class Choice extends Parser
{
    // ---------------------------------------------------------------------------------------------

    private final Parser[] children;

    // ---------------------------------------------------------------------------------------------

    @Override public List<Parser> children() {
        return Collections.unmodifiableList(Arrays.asList(children));
    }

    // ---------------------------------------------------------------------------------------------

    public Choice (Parser... children) {
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

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull()
    {
        StringBuilder b = new StringBuilder("choice(");
        joinArray(b, ", ", children);
        b.append(")");
        return b.toString();
    }

    // ---------------------------------------------------------------------------------------------
}
