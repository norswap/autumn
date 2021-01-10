package norswap.autumn;

import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Implementations of this class can be used to walk over the parser graph by calling its
 * {@link #walk(Parser)} method.
 *
 * <p>The {@link #walk(Parser)} calls {@link #work(Parser, State)} on all parsers transitively
 * reachable through the original parser (using {@link Parser#children()}. The method is called at
 * least twice on each reachable parser: once before walking the children (with state {@link
 * State#BEFORE}, once after (with state {@link State#AFTER}.
 *
 * <p>{@link #work(Parser, State)} may also be called with state {@link State#RECURSE}, whenever a
 * recursion on a parser is encountered, or with state {@link State#VISITED} if a parser that has
 * already been visited is encountered again. Not that when a method is called with {@link
 * State#RECURSE}, it is <b>not</b> called immediately again with {@link State#VISITED}.
 *
 * <p>If you need to specialize what the work method does to specific kind of parsers, consider
 * using a {@link ParserVisitor}.
 */
public abstract class ParserWalker
{
    // ---------------------------------------------------------------------------------------------

    /**
     * See {@link ParserWalker}.
     */
    public enum State {
        BEFORE,
        AFTER,
        RECURSE,
        VISITED
    }

    // ---------------------------------------------------------------------------------------------

    private HashSet<Parser> visited = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private LinkedHashSet<Parser> stack = new LinkedHashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * The entry point of the walker.
     */
    public final void walk (Parser parser)
    {
        if (!stack.add(parser)) {
            work(parser, State.RECURSE);
            return;
        }

        if (!visited.add(parser)) {
            work(parser, State.VISITED);
            return;
        }

        work(parser, State.BEFORE);

        for (Parser child: parser.children())
            walk(child);

        work(parser, State.AFTER);
        stack.remove(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the indicated parser has been visited yet.
     */
    public boolean visited (Parser parser) {
        return visited.contains(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the indicated parser is in the path currently being visited.
     */
    public boolean inPath (Parser parser) {
        return stack.contains(parser);
    }

    // ---------------------------------------------------------------------------------------------

    protected abstract void work (Parser parser, State state);

    // ---------------------------------------------------------------------------------------------
}
