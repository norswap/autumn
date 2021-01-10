package norswap.autumn.visitors;

import norswap.autumn.Autumn;
import norswap.autumn.ParseOptions;
import norswap.autumn.Parser;
import norswap.autumn.ParserWalker;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used to check if a grammar is well-formed, i.e. (1) it contains no left-recursive
 * paths that would cause infinite recursion; and (2) it contains no repetitions over (a) nullable
 * parser(s) that would cause infinite looping.
 *
 * <p>This is automatically called when parsing through {@link Autumn#parse}, depending on the
 * {@link ParseOptions#wellFormednessCheck} value.
 *
 * <p>See {@link VisitorNullableRepetition} for more information about nullable repetitions.
 *
 * <p>If violations are found, informations about them are stored in {@link #leftRecursives},
 * {@link #leftrecPaths} and {@link #nullableRepetitions}.
 *
 * <p>Invoke instances of this class through their {@link #wellFormed(Parser)} method, which
 * returns true if the parser graph reachable from the given parser is well-formed.
 *
 * <p>It may happen that a grammar has multiple roots (not all parsers can be reached from the same
 * root), in which case {@link #wellFormed(Parser)} can be invoked once on each root, and the
 * results will be accumulated. The boolean result will also take into account all roots seen so
 * far.
 *
 * <p>Instances of this class cannot otherwise be reused.
 */
public final class WellFormednessChecker extends ParserWalker
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A set of parser such that there is a least one parser per left-recursive path in the grammar.
     */
    public final Set<Parser> leftRecursives = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the list of left-recursive grammar paths through which the parsers of {@link
     * #leftRecursives} were found.
     *
     * <p>These may not  be <b>all</b> the left-recursive paths in the grammar, as a single parser
     * may be part of multiple left-recursive cycles.
     */
    public final Set<List<Parser>> leftrecPaths = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * A set of parser that are nullable repetitions, in the sense of {@link
     * VisitorNullableRepetition}.
     */
    public final Set<Parser> nullableRepetitions = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<Parser> visited = new HashSet<>();

    private final LinkedHashSet<Parser> stack = new LinkedHashSet<>();

    private final VisitorFirstParsers firstsVisitor;

    private final VisitorNullableRepetition nullRepsVisitor;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new well-formedness checker using the given nullable visitor.
     *
     * <p>Since {@link VisitorNullable} memoizes parser nullability, you should reuse an existing
     * instance as much as possible.
     */
    public WellFormednessChecker (VisitorNullable nullableVisitor)
    {
        this.firstsVisitor = new VisitorFirstParsers(nullableVisitor);
        this.nullRepsVisitor = new VisitorNullableRepetition(nullableVisitor);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new well-formedness checker using a freshly constructed given nullable visitor.
     */
    public WellFormednessChecker() {
        this(new VisitorNullable());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true if and only if the parser graph reachable from the given parser is well-formed,
     * and no previous invocation of the method on this instance returned false.
     */
    public boolean wellFormed (Parser parser)
    {
        walk(parser);
        return leftRecursives.isEmpty() && nullableRepetitions.isEmpty();
    }

    // ---------------------------------------------------------------------------------------------

    private void first (Parser parser)
    {
        if (visited.contains(parser))
            return;

        if (!stack.add(parser)) // left-recursion
        {
            // find the recursive parser in the stack (e.g. x y z p a b c p)
            //                                                    ^
            int i = 0;
            for (Parser p: stack) {
                if (p == parser) break;
                ++i;
            }

            // isolate left-recursive loop
            ArrayList<Parser> loop = new ArrayList<>(stack.size() - i);
            int j = 0;
            for (Parser p: stack) {
                if (j++ < i) continue;
                loop.add(p);
            }

            // NOTE(norswap): The LeftRecursive parser was abandonned, but if we did have
            // a parser that could enable safe left-recursion, here's how to handle it:
            //
            // for (Parser p: loop)
            //    if (p instanceof LeftRecursive)
            //        return;

            leftRecursives.add(parser);
            leftrecPaths.add(loop);
            visited.add(parser); // only consider a single loop per left-recursive parser
            return;
        }

        firstsVisitor.firsts(parser).forEach(this::first);

        stack.remove(parser);
        visited.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void work (Parser parser, State state)
    {
        if (state != State.BEFORE) return;
        first(parser);
        if (nullRepsVisitor.nullableRepetition(parser))
            nullableRepetitions.add(parser);
    }

    // ---------------------------------------------------------------------------------------------
}
