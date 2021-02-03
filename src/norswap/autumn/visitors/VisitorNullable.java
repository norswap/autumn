package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.ParserWalker;
import norswap.autumn.parsers.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static norswap.utils.Vanilla.list;

/**
 * A visitor meant to determine whether the parser is "nullable", i.e., whether it can succeed while
 * consuming no input.
 *
 * <p>To dermine whether a parser is nullable, call {@link #nullable(Parser)}.
 *
 * <p>The visitor memoizes the nullability of parsers. Requesting the nullability of a parser
 * automatically computes the nullability of all parsers reachable through this parser, as
 * the nullability computation is highly recursive.
 *
 * <p>To support custom parsers, provide an appropriate overload using {@link ParserVisitor#extend}.
 * Also see {@link ParserVisitor}'s Javadoc.
 *
 * <p>Within the supplied overloads, you can query for the nullability of sub-parsers using {@link
 * #nullable(Parser)}, and if you determine that the parent parser is nullable, you should add it to
 * the set via one the method whose name start with {@code add} (e.g. {@link #addNullable(Parser)}.
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
     * The set of nullable parsers found so far. Avoid modifying directly, rather use of the
     * methods starting with {@code add}.
     */
    public Set<Parser> nullables = new HashSet<>();

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
     * Reuses the previously computed nullability if available.
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
    public void addNullable (Parser parser) {
        nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if {@code other} is nullable.
     */
    public void addIfNullable (Parser parser, Parser other)
    {
        if (nullable(other))
            nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if {@code cond} is true.
     */
    public void addIf (Parser parser, boolean cond) {
        if (cond) nullables.add(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if at least one parser amongst
     * {@code others} is nullable.
     */
    public void addIfOneNullable (Parser parser, Iterable<Parser> others)
    {
        for (Parser other: others)
            if (nullable(other)) {
                nullables.add(parser);
                return;
            }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds {@code parser} to the set of nullable parsers, but only if all parsers in {@code others}
     * are nullable.
     */
    public void addIfAllNullable (Parser parser, Iterable<Parser> others)
    {
        for (Parser other: others)
            if (!nullable(other))
                return;

        nullables.add(parser);
    }

    // =============================================================================================

    @Override public void defaultAction (Parser parser) {
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
        addIfNullable(parser, parser.child);
    }

    @Override public void visit (Memo parser) {
        addIfNullable(parser, parser.child);
    }

    @Override public void visit (AbstractWrapper parser) {
        addIfNullable(parser, parser.child);
    }

    @Override public void visit (TrailingWhitespace parser) {
        addIfNullable(parser, parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Bounded parser) {
        addIfNullable(parser, parser.coarse);
    }

    @Override public void visit (LazyParser parser) {
        addIfNullable(parser, parser.child());
    }

    @Override public void visit (AbstractForwarding parser) {
        addIfNullable(parser, parser.forwardee);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractChoice parser) {
        addIfOneNullable(parser, parser.children());
    }

    @Override public void visit (Choice parser) {
        addIfOneNullable(parser, parser.children());
    }

    @Override public void visit (Longest parser) {
        addIfOneNullable(parser, parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Sequence parser) {
        addIfAllNullable(parser, parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (StringMatch parser) {
        addIf(parser, parser.string.equals(""));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (StringChoice parser) {
        addIf(parser, Arrays.stream(parser.strings).anyMatch(str -> str.length() == 0));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractPrimitive parser) {
        addIf(parser, parser.nullable);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Repeat parser) {
        addIf(parser, parser.min == 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Around parser)
    {
        addIf(parser,
            parser.min == 0
                || parser.min == 1 && nullable(parser.around)
                || nullable(parser.around) && nullable(parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LeftExpression parser)
    {
        if (!nullable(parser.left))
            return; // not nullable if left not nullable

        if (!parser.operatorRequired) {
            nullables.add(parser); // nullable left and no operator required
            return;
        }

        if (parser.right != null && nullable(parser.right)) {
            addIfOneNullable(parser, list(parser.infixes));
            return;
        }

        addIfOneNullable(parser, list(parser.suffixes));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (RightExpression parser)
    {
        if (!nullable(parser.right))
            return; // not nullable if right not nullable

        if (!parser.operatorRequired) {
            nullables.add(parser); // nullable right and no operator required
            return;
        }

        if (parser.left != null && nullable(parser.left)) {
            addIfOneNullable(parser, list(parser.infixes));
            return;
        }

        addIfOneNullable(parser, list(parser.prefixes));
    }

    // ---------------------------------------------------------------------------------------------
}
