package norswap.autumn.parsers;

import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.util.ArrayStack;
import java.util.Collections;

/**
 * This parser acts as an escape hatch to enable recursion under a left-associative {@link
 * LeftRecursive} parser.
 *
 * <p>The goal is to enable "middle-recursion", i.e. recursion bounded by some input on both sides
 * (the classical exemple is parenthesized expressions). Middle-recursion is by definition neither
 * left- or right-recursion.
 *
 * <p>In fact, for this parser to be safe and well-defined, the user has to make sure that <b>no</b>
 * invocation of the recursive parser occurring during the invocation of this parser will be left-
 * or righ-recursive!
 */
public final class GuardedRecursion extends Parser
{
    // ---------------------------------------------------------------------------------------------

    public final Parser child;

    // ---------------------------------------------------------------------------------------------

    public GuardedRecursion (Parser child) {
        this.child = child;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected boolean doparse (Parse parse)
    {
        ArrayStack<LeftRecursive.LeftRecursiveState> recursives
            = LeftRecursive.active_left_recursives.data(parse);

        int[] recursions = recursives.stream().mapToInt(it -> it.recursions).toArray();

        recursives.forEach(it -> it.recursions = 0);

        boolean result = child.parse(parse);

        for (int i = 0; i < recursives.size(); i++)
            recursives.get(i).recursions = recursions[i];

        return result;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void accept (ParserVisitor visitor) {
        visitor.visit(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Iterable<Parser> children () {
        return Collections.singletonList(child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public String toStringFull () {
        return "guarded(" + child + ")";
    }

    // ---------------------------------------------------------------------------------------------
}
