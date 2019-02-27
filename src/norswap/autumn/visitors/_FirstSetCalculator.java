package norswap.autumn.visitors;

import norswap.autumn.Parser;
import java.util.Set;

/**
 * A visitor implementation for built-in parsers meant to uncover the set of parsers that can be
 * recursively invoked by the parser at the same intitial position. That set may contain the
 * original parser itself, but only if it is left-recursive. (However, {@link
 * _LeftRecursionWorker} is a better way to detect left-recursion in a grammar.)
 *
 * <p>This is currently unused but left in as a a potentially useful example.
 *
 * <p>The set of parsers that can be invoked at the same position will be stored in {@link
 * #firsts()}.
 *
 * <p>The visitor can be reused, but {@link #firsts()} should be cleared between uses.
 *
 * <p>See {@link FirstGraphWalker} for more important precisions.
 *
 * <p>An instantiable version is available as {@link FirstSetCalculator}.
 */
public interface _FirstSetCalculator extends FirstGraphWalker
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A set that will all hold all parsers that can be recursively be the original parser at
     * the same initial position.
     */
    Set<Parser> firsts();

    // ---------------------------------------------------------------------------------------------

    @Override default void walk (Parser parser) {
        parser.accept(this);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that {@code parser} may be invoked at the same initial position as the
     * the original parser. Takes care of filling all our data structures accordingly, and to
     * recursively visit the parser (unless a left-recursion is detected).
     */
    @Override default void first (Parser parser)
    {
        if (!firsts().add(parser))
            // left-recursion or already visited in another branch
            return;

        parser.accept(this);
    }

    // ---------------------------------------------------------------------------------------------
}
