package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.ParserWalker;
import norswap.autumn.parsers.*;
import java.util.HashSet;

import static norswap.utils.Vanilla.list;

/**
 * A visitor for built-in parsers meant to determine whether the parser is "nullable", i.e., whether
 * it can succeed while consuming no input.
 *
 * <p>To dermine whether a parser is nullable, call {@link #nullable(Parser)}.
 *
 * <p>The class memoizes the nullability of parsers. Requesting the nullability of a parser
 * automatically computes the nullability of all parsers reachable through this parser, as
 * the nullability computation is highly recursive.
 *
 * <p>To support custom parsers, provide an appropriate overload using {@link ParserVisitor#extend}.
 * Also see {@link ParserVisitor}'s Javadoc.
 *
 * <p>Within the supplied overload, you can query for the nullability of sub-parsers using
 * {@link #nullable(Parser)}, and if you determine that the parser is nullable, you should add it to
 * the set via one the method whose name start with {@code add} (e.g. {@link #add_nullable(Parser)}.
 *
 * <p>This visitor is used by {@link VisitorNullableRepetition} and {@link VisitorFirstParsers}.
 */
public final class VisitorNullable extends ParserWalker implements ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    private static HashOverloads overloads = new HashOverloads(VisitorNullable.class);

    // ---------------------------------------------------------------------------------------------

    @Override public Overloads overloads() {
        return overloads;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The set of nullable parsers found so far. Avoid modifying directly, rather use of of the
     * methods starting with {@code add}.
     */
    public HashSet<Parser> nullables = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    @Override protected void work (Parser parser, State state)
    {
        // A recursive invocation is considered non-nullable.
        // This will leave the original invocation to be nullable if it has a nullable invocation,
        // or non-nullable otherwise, which is the correct semantics.

        if (state == State.AFTER)
            parser.accept(this);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns true iff {@code parser} is nullable: it can succeed while consuming no input.
     */
    public boolean nullable (Parser parser)
    {
        if (visited(parser))
            return nullables.contains(parser);

        walk(parser);
        return nullables.contains(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds the given parser to the set of nullable parsers.
     */
    public void add_nullable (Parser parser) {
        nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if {@code other} is nullable.
     */
    public void add_if_nullable (Parser parser, Parser other)
    {
        if (nullable(other))
            nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if {@code cond} is true.
     */
    public void add_if (Parser parser, boolean cond) {
        if (cond) nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if at least one parser amongst
     * {@code others} is nullable.
     */
    public void add_if_one_nullable (Parser parser, Iterable<Parser> others)
    {
        for (Parser other: others)
            if (nullable(parser)) {
                nullables.add(parser);
                return;
            }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if all parsers in {@code others}
     * are nullable.
     */
    public void add_if_all_nullable (Parser parser, Iterable<Parser> others)
    {
        for (Parser other: others)
            if (!nullable(parser))
                return;

        nullables.add(parser);
    }

    // =============================================================================================

    @Override public void default_action (Parser parser) {
        // overly conservative
        nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (CharPredicate parser) {
        // not nullable
    }

    @Override public void visit (ObjectPredicate parser) {
        // not nullable
    }

    @Override public void visit (Fail parser) {
        // not nullable
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (ContextPredicate parser) {
        nullables.add(parser);
    }

    @Override public void visit (Lookahead parser) {
        nullables.add(parser);
    }

    @Override public void visit (Not parser) {
        nullables.add(parser);
    }

    @Override public void visit (Optional parser) {
        nullables.add(parser);
    }

    @Override public void visit (Empty parser) {
        nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Collect parser) {
        add_if_nullable(parser, parser.child);
    }

    @Override public void visit (LeftRecursive parser) {
        add_if_nullable(parser, parser.child);
    }

    @Override public void visit (GuardedRecursion parser) {
        add_if_nullable(parser, parser.child);
    }

    @Override public void visit (Memo parser) {
        add_if_nullable(parser, parser.child);
    }

    @Override public void visit (AbstractWrapper parser) {
        add_if_nullable(parser, parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LazyParser parser) {
        add_if_nullable(parser, parser.child());
    }

    @Override public void visit (TokenParser parser) {
        add_if_nullable(parser, parser.target);
    }

    @Override public void visit (AbstractForwarding parser) {
        add_if_nullable(parser, parser.forwardee);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractChoice parser) {
        add_if_one_nullable(parser, parser.children());
    }

    @Override public void visit (Choice parser) {
        add_if_one_nullable(parser, parser.children());
    }

    @Override public void visit (Longest parser) {
        add_if_one_nullable(parser, parser.children());
    }

    @Override public void visit (TokenChoice parser) {
        add_if_one_nullable(parser, parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Sequence parser) {
        add_if_all_nullable(parser, parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (StringMatch parser) {
        add_if(parser, parser.string.equals(""));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractPrimitive parser) {
        add_if(parser, parser.nullable);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Repeat parser) {
        add_if(parser, parser.min == 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Around parser)
    {
        add_if(parser,
            parser.min == 0
                || parser.min == 1 && nullable(parser.around)
                || nullable(parser.around) && nullable(parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LeftExpression parser)
    {
        if (!nullable(parser.left))
            return; // not nullable if left not nullable

        if (!parser.operator_required) {
            nullables.add(parser); // nullable left and no operator required
            return;
        }

        if (nullable(parser.right)) {
            add_if_one_nullable(parser, list(parser.infixes));
            return;
        }

        add_if_one_nullable(parser, list(parser.suffixes));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (RightExpression parser)
    {
        if (!nullable(parser.right))
            return; // not nullable if right not nullable

        if (!parser.operator_required) {
            nullables.add(parser); // nullable right and no operator required
            return;
        }

        if (nullable(parser.left)) {
            add_if_one_nullable(parser, list(parser.infixes));
            return;
        }

        add_if_one_nullable(parser, list(parser.prefixes));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LeftFold parser) {
        add_if(parser,
            !parser.operator_required && nullable(parser.left)
            || nullable(parser.left) && nullable(parser.operator) && nullable(parser.right));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (RightFold parser) {
        add_if(parser,
            !parser.operator_required && nullable(parser.right)
            || nullable(parser.left) && nullable(parser.operator) && nullable(parser.right));
    }

    // ---------------------------------------------------------------------------------------------
}
