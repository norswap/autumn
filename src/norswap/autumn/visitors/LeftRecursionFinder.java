package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserWalker;
import norswap.autumn.parsers.LeftRecursive;
import java.util.List;
import java.util.Set;

/**
 * This class is used to find a set of parsers to be marked as left-recursive (for instance by
 * wrapping them inside a {@link LeftRecursive} parsers) in order to remove unmanaged left-recursion
 * from all parsers reachable from an initial parser (transitively via {@link Parser#children()}).
 *
 * <p>Typically the goal is to detect or eliminate left-recursion from the whole grammar, in which
 * case the root should be passed to {@link #walk(Parser)}. If the grammar has multiple roots,
 * multiple consecutive calls to {@link #walk(Parser)} can be made.
 *
 * <p>The results are obtained through {@link #left_recursive()} and {@link #leftrec_paths()}.
 *
 * <p>This class performs most of its work through a {@link _LeftRecursionWorker}, and if you want
 * this to work with custom parsers in addition to built-in ones, you should provide a proper
 * implementation of that visitor.
 */
public class LeftRecursionFinder extends ParserWalker
{
    // ---------------------------------------------------------------------------------------------

    private final _LeftRecursionWorker worker;

    // ---------------------------------------------------------------------------------------------

    /**
     * Builds a new left-recursion finder using a {@link LeftRecursionWorker}, so supporting
     * built-in parsers only.
     */
    public LeftRecursionFinder() {
        this.worker = new LeftRecursionWorker();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Builds a new left-recursion finder using the given worker, allowing the finder to support
     * custom parsers.
     */
    public LeftRecursionFinder (_LeftRecursionWorker worker) {
        this.worker = worker;
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void work (Parser parser, State state)
    {
        if (state != State.BEFORE) return;
        worker.walk(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a set of parser that, if marked as left-recursive, will remove all unmanaged
     * left-recursion from the grammar.
     */
    public Set<Parser> left_recursive() {
        return worker.left_recursive();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the list of left-recursive grammar paths through which the parsers of {@link
     * #left_recursive()} were found.
     *
     * <p>These may not  be <b>all</b> the left-recursive paths in the grammar. See {@link
     * _LeftRecursionWorker} for a discussion, and use {@link _LeftRecursiveCycleEnumerator} if you
     * need to enumerate all paths.
     */
    public Set<List<Parser>> leftrec_paths() {
        return worker.leftrec_paths();
    }

    // ---------------------------------------------------------------------------------------------
}
