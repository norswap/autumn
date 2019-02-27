package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.HashSet;
import java.util.Set;

/**
 * Instantiable version of {@link _FirstSetCalculator}.
 */
public final class FirstSetCalculator implements _FirstSetCalculator
{
    // ---------------------------------------------------------------------------------------------

    private final NullableVisitor nullable_visitor = new NullableVisitor();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<Parser> firsts = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    @Override public _NullableVisitor nullable_visitor () {
        return nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Set<Parser> firsts () {
        return firsts;
    }

    // ---------------------------------------------------------------------------------------------
}
