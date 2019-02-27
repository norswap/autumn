package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.parsers.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A visitor implementation for built-in parsers that finds all left-recursive cycles that include
 * a specific parser.
 *
 * <p>The list of uncovered cycles will be stored in {@link #left_recursive_cycles()}.
 *
 * <p>Because it focuses on finding the left-recursive cycles including only a single parser, this
 * visitor is not the most efficient way to find left-recursive cycles in a whole grammar.
 * In order to ensure that your grammar is safe with regard to left-recursion, use a
 * {@link _LeftRecursionWorker} instead. See the <b>efficiency notes</b> below for more
 * technical details.
 *
 * <p>The visitor can be reused, but {@link #left_recursive_cycles()} should be cleared between
 * uses.
 *
 * <p>See {@link FirstGraphWalker} for more important precisions.
 *
 * <p>An instantiable version is available as {@link LeftRecursiveCycleEnumerator}.
 *
 * <b><u>Efficiency Notes</u></b>
 *
 * We consider the directed graph defined by the FIRST relationship: the pair of parsers (a, b) is
 * in FIRST if a may invoke b at the same input position it was invoked at. A left-recursive
 * parser cycle is simply a cycle in this graph.
 *
 * <p>Finding <b>all</b> simple cycles (no edge or vertex repetition) in a graph is already
 * computationally non trivial. Johnson's algorithm, the classical approach, has O((v+e)(c+1))
 * complexity — where v is the number of vertices, e the number of edges and c the number of simple
 * cycles, which in a complete graph may grow faster than 2^v (fortunately, the graph defined by
 * invocation at the same position is much sparser than a complete graph). However, using this
 * approach would require pre-computing a real graph representation (with adjacency matrices or
 * lists).
 *
 * <p>Fortunately, finding whether the graph has cycles (without having to list all cycles) is
 * considerably easier at O(v) complexity. It can also be shown that one can compute a set of
 * vertices to remove from the graph in order to remove <b>all</b> cycles in O(v). In the context of
 * Autumn grammar, "removing a vertex" would be wrapping it in a {@link LeftRecursive} parser.
 * {@link _LeftRecursionWorker} performs this calculation for you.
 *
 * <p>As for this visitor, it essentially inspects performs depth-first traversal of the graph
 * starting at the parser under consideration. The complexity is hard to characterize because
 * whole arborescences will be explored multiple times by virtue of being reached through different
 * paths in the graph. The effective complexity will depend on how dense/sparse the graph is.
 *
 * <p>Nevertheless, this approach is useful in order to list all the cycles involving a specific
 * parser. As such it can be used as a complement to {@link LeftRecursiveCycleEnumerator} since we
 * expect to find few left-recursive cycles in practice. It's also relatively tractable
 * because the left-recursive graph is much sparser than a complete graph, and grammars are tiny
 * by modern standards.
 */
public interface _LeftRecursiveCycleEnumerator extends FirstGraphWalker
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A set that will all hold all left-recursive cycles that include the original parser. The
     * original parser will always be the first parser in each list. Parsers held within a specific
     * list are distinct from one another.
     */
    Set<List<Parser>> left_recursive_cycles();

    // ---------------------------------------------------------------------------------------------

    /**
     * Implementation! Provide storage but do not use.
     *
     * <p>Stores a trace of parsers that can be transitively invoked at the same input position.
     */
    LinkedHashSet<Parser> stack();

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that {@code parser} may be invoked at the same initial position as the
     * the original parser. Takes care of filling all our data structures accordingly, and to
     * recursively visit the parser (unless a left-recursion is detected).
     */
    default void first (Parser parser)
    {
        if (!stack().add(parser)) // left-recursion
        {
            Parser first = stack().iterator().next();
            if (parser == first)
                left_recursive_cycles().add(new ArrayList<>(stack()));
            return;
        }

        parser.accept(this);
        stack().remove(parser);
    }

    // ---------------------------------------------------------------------------------------------
}
