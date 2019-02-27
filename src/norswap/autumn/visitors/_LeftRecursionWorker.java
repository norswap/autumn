package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A visitor for built-in parsers that is able to isolate a set of parsers that should be marked as
 * left-recursive in order to prevent unmanaged left-recursion in the part of the FIRST graph (see
 * {@link FirstGraphWalker} reachable from the parser originally supplied to the visitor.
 *
 * <p>This parser is mostly meant to be used by {@link LeftRecursionFinder}. What it does on its
 * own is not incredibly coherent as a unit of work since it finds all left-recursion reachable
 * through itself in the FIRST graph, not only those that involve the original parser.
 *
 * <p>The set of parsers to mark as left-recursive is available via {@link #left_recursive()}. The
 * paths through which these nodes where found are made available via {@link #leftrec_paths()}. Note
 * that these may not be all the left-recursive paths in the grammar, as marking a single node as
 * left-recursive might cause many paths to be resolved! If you care about all the paths, you can
 * obtain them via {@link _LeftRecursiveCycleEnumerator}. See the "Efficiency Notes" section of the
 * {@link _LeftRecursiveCycleEnumerator} documentation for more information.
 *
 * <p>This visitor can be reused but {@link #cleanup()} should be called between uses. It can also
 * be reused without cleanup, in which case it will build up a set of left-recursive nodes and paths
 * reachable through multiple initial parsers (notably, {@link LeftRecursionFinder} does this).
 *
 * <p>See {@link FirstGraphWalker} for more important precisions.
 *
 * <p>An instantiable version is available as {@link LeftRecursionWorker}.
 */
public interface _LeftRecursionWorker extends FirstGraphWalker
{
    // ---------------------------------------------------------------------------------------------

    Set<Parser> visited();

    // ---------------------------------------------------------------------------------------------

    Set<Parser> left_recursive();

    // ---------------------------------------------------------------------------------------------

    Set<List<Parser>> leftrec_paths();

    // ---------------------------------------------------------------------------------------------

    LinkedHashSet<Parser> stack();

    // ---------------------------------------------------------------------------------------------

    /**
     * Empties the visitor's data structure so that it can be reused.
     */
    default void cleanup()
    {
        visited().clear();
        left_recursive().clear();
        leftrec_paths().clear();
        stack().clear();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that {@code parser} may be invoked at the same initial position as the
     * the original parser. Takes care of filling all our data structures accordingly, and to
     * recursively visit the parser (unless a left-recursion is detected).
     */
    default void first (Parser parser)
    {
        if (visited().contains(parser))
            return;

        if (!stack().add(parser)) // left-recursion
        {
            left_recursive().add(parser);
            visited().add(parser);

            // find the recursive parser in the stack
            int i = 0;
            for (Parser p: stack()) {
                if (p == parser) break;
                ++i;
            }

            // isolate left-recursive loop
            ArrayList<Parser> loop = new ArrayList<>(stack().size() - i);
            int j = 0;
            for (Parser p: stack()) {
                if (j++ < i) continue;
                loop.add(p);
            }

            leftrec_paths().add(loop);
            return;
        }

        parser.accept(this);
        stack().remove(parser);
        visited().add(parser);
    }

    // ---------------------------------------------------------------------------------------------
}
