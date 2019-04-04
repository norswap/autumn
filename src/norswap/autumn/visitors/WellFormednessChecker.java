package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
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
 * <p>See {@link _VisitorNullableRepetition} for more information about nullable repetitions.
 *
 * <p>If violations are found, informations about them are stored in {@link #left_recursives},
 * {@link #leftrec_paths} and {@link #nullable_repetitions}.
 *
 * <p>Invoke instances of this class through their {@link #well_formed(Parser)} method, which
 * returns true if the parser graph reachable from the given parser is well-formed.
 *
 * <p>It may happen that a grammar has multiple roots (not all parsers can be reached from the same
 * root), in which case {@link #well_formed(Parser)} can be invoked once on each root, and the
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
    public final Set<Parser> left_recursives = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the list of left-recursive grammar paths through which the parsers of {@link
     * #left_recursives} were found.
     *
     * <p>These may not  be <b>all</b> the left-recursive paths in the grammar, as a single parser
     * may be part of multiple left-recursive cycles.
     */
    public final Set<List<Parser>> leftrec_paths = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * A set of parser that are nullable repetitions, in the sense of {@link
     * _VisitorNullableRepetition}.
     */
    public final Set<Parser> nullable_repetitions = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    private final HashSet<Parser> visited = new HashSet<>();

    private final LinkedHashSet<Parser> stack = new LinkedHashSet<>();

    private final _VisitorFirstParsers firsts_visitor;

    private final _VisitorNullableRepetition null_reps_visitor;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new well-formedness checker using the given visitors.
     *
     * <p>If you use custom parsers that can't be handled by the visitors' default overload ({@link
     * ParserVisitor#visit(Parser)}, you must use this constructor and supply visitors that handle
     * the custom parsers used in the grammar.
     */
    public WellFormednessChecker
        (_VisitorFirstParsers firsts_visitor, _VisitorNullableRepetition null_reps_visitor)
    {
        this.firsts_visitor = firsts_visitor;
        this.null_reps_visitor = null_reps_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new well-formedness checker using visitors for the built-in parsers.
     *
     * <p>Use the other constructor if your grammar contains custom parsers that can't be handled
     * via the visitors' default overload ({@link ParserVisitor#visit(Parser)}.
     */
    public WellFormednessChecker () {
        this(new VisitorFirstParsers(), new VisitorNullableRepetition());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true if and only if the parser graph reachable from the given parser is well-formed,
     * and no previous invocation of the method on this instance returned false.
     */
    public boolean well_formed (Parser parser)
    {
        walk(parser);
        return left_recursives.isEmpty() && nullable_repetitions.isEmpty();
    }

    // ---------------------------------------------------------------------------------------------

    private void first (Parser parser)
    {
        if (visited.contains(parser))
            return;

        if (!stack.add(parser)) // left-recursion
        {
            left_recursives.add(parser);
            visited.add(parser);

            // find the recursive parser in the stack
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

            leftrec_paths.add(loop);
            return;
        }

        firsts_visitor.firsts_for(parser).forEach(this::first);

        stack.remove(parser);
        visited.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void work (Parser parser, State state)
    {
        if (state != State.BEFORE) return;
        first(parser);
        if (null_reps_visitor.nullable_repetition(parser))
            nullable_repetitions.add(parser);
    }

    // ---------------------------------------------------------------------------------------------
}
