package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Instantiable version of {@link _LeftRecursiveCycleEnumerator}.
 */
public final class LeftRecursiveCycleEnumerator implements _LeftRecursiveCycleEnumerator
{
    // ---------------------------------------------------------------------------------------------

    private final NullableVisitor nullable_visitor = new NullableVisitor();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<List<Parser>> left_recursive_cycles = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final LinkedHashSet<Parser> stack = new LinkedHashSet<>();

    // ---------------------------------------------------------------------------------------------

    @Override public _NullableVisitor nullable_visitor () {
        return nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override  public Set<List<Parser>> left_recursive_cycles () {
        return left_recursive_cycles;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public LinkedHashSet<Parser> stack () {
        return stack;
    }

    // ---------------------------------------------------------------------------------------------
}
