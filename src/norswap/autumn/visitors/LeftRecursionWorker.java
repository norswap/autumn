package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Instantiable version of {@link _LeftRecursionWorker}.
 */
public class LeftRecursionWorker implements _LeftRecursionWorker
{
    // ---------------------------------------------------------------------------------------------

    private final NullableVisitor nullable_visitor = new NullableVisitor();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<Parser> visited = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<Parser> left_recursive = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<List<Parser>> leftrec_paths = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final LinkedHashSet<Parser> stack = new LinkedHashSet<>();

    // ---------------------------------------------------------------------------------------------

    @Override public _NullableVisitor nullable_visitor () {
        return nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Set<Parser> visited () {
        return visited;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Set<Parser> left_recursive () {
        return left_recursive;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public Set<List<Parser>> leftrec_paths () {
        return leftrec_paths;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public LinkedHashSet<Parser> stack () {
        return stack;
    }

    // ---------------------------------------------------------------------------------------------
}
