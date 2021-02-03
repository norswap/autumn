package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.ParserWalker;
import norswap.autumn.parsers.*;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static norswap.utils.Vanilla.list;

/**
 * A visitor that retrieves the FIRST set for the visited parser.
 *
 * <p>The FIRST set for a parser P is the set of parsers that P may (directly) invoke
 * at the same input position as itself.
 *
 * <p>To determine the FIRST set of a parser, call call {@link #firsts(Parser)}.
 *
 * <p>This interface requires the use of a {@link VisitorNullable} that can handle all supported
 * parsers. If a parser A may call parsers B and C sequentially and NULLABLE(B), then both B and C
 * are in FIRST(A).
 *
 * <p>To support custom parsers, provide an appropriate overload using {@link
 * ParserVisitor#extend}. Also see {@link ParserVisitor}'s Javadoc.
 *
 * <p>Within the supplied overloads, you can query for the nullability of sub-parsers using {@link
 * #nullable(Parser)}. If you determine that a sub-parser is part of the FIRST set, you should add
 * it to {@link #firsts}. The method {@link #firstsAddSequence(Iterable)} is also handy.
 *
 * <p>If you use this parser to traverse the FIRST graph, you must beware of cycles. It is recommend
 * to keep a set of visited parsers to avoid infinite recursion, or to use this visitor to filter a
 * parser traversal using {@link ParserWalker}.
 *
 * <p>This visitor is used by {@link WellFormednessChecker} to identify left-recursive cycles.
 *
 * <p>As long as you invoke this visitor only through its {@link #firsts(Parser)} method, you may
 * reuse it for multiple parsers.
 */
public final class VisitorFirstParsers implements ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    private static final HashOverloads overloads = new HashOverloads(VisitorFirstParsers.class);

    // ---------------------------------------------------------------------------------------------

    @Override public Overloads overloads() {
        return overloads;
    }

    // ---------------------------------------------------------------------------------------------

    public final VisitorNullable nullableVisitor;

    // ---------------------------------------------------------------------------------------------

    public Set<Parser> firsts = new HashSet<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance with the given nullable visitor.
     *
     * <p>Since {@link VisitorNullable} memoizes parser nullability, you should reuse an existing
     * instance as much as possible.
     */
    public VisitorFirstParsers (VisitorNullable nullableVisitor) {
        this.nullableVisitor = nullableVisitor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the FIRST set for {@code parser}: the set of direct sub-parsers of {@code parser}
     * that may be invoked at the same input position as {@code parser}.
     */
    public Set<Parser> firsts (Parser parser)
    {
        firsts = new HashSet<>();
        parser.accept(this);
        return firsts;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that the given sequence of parsers may be invoked at the same initial position as
     * the original parser and adds parsers from this sequence to {@link #firsts}, depending on
     * their nullability.
     */
    public void firstsAddSequence (Iterable<Parser> parsers)
    {
        for (Parser p: parsers) {
            firsts.add(p);
            if (!nullable(p)) break;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Shortcut for {@code nullableVisitor.nullable(parser}.
     */
    public boolean nullable (Parser parser) {
        return nullableVisitor.nullable(parser);
    }

    // =============================================================================================

    @Override public void defaultAction (Parser parser) {
        // pessimistic assumption
        parser.children().forEach(firsts::add);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (CharPredicate parser) {
        // empty
    }

    @Override public void visit (ContextPredicate parser) {
        // empty
    }

    @Override public void visit (ObjectPredicate parser) {
        // empty
    }

    @Override public void visit (Empty parser) {
        // empty
    }

    @Override public void visit (Fail parser) {
        // empty
    }

    @Override public void visit (AbstractPrimitive parser) {
        // empty
    }

    @Override public void visit (StringChoice parser) {
        // empty
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Collect parser) {
        firsts.add(parser.child);
    }

    @Override public void visit (Lookahead parser) {
        firsts.add(parser.child);
    }

    @Override public void visit (Not parser) {
        firsts.add(parser.child);
    }

    @Override public void visit (Optional parser) {
        firsts.add(parser.child);
    }

    @Override public void visit (Repeat parser) {
        firsts.add(parser.child);
    }

    @Override public void visit (Memo parser) {
        firsts.add(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LazyParser parser) {
        firsts.add(parser.child());
    }

    @Override public void visit (AbstractForwarding parser) {
        firsts.add(parser.forwardee);
    }

    @Override public void visit (AbstractWrapper parser) {
        firsts.add(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractChoice parser) {
        firsts.addAll(parser.children());
    }

    @Override public void visit (Bounded parser) {
        firsts.addAll(parser.children());
    }

    @Override public void visit (Choice parser) {
        firsts.addAll(parser.children());
    }

    @Override public void visit (Longest parser) {
        firsts.addAll(parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Sequence parser) {
        firstsAddSequence(parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Around parser) {
        firstsAddSequence(list(parser.around, parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (TrailingWhitespace parser) {
        if (parser.whitespace != null)
            firstsAddSequence(list(parser.child, parser.whitespace));
        else
            firsts.add(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LeftExpression parser)
    {
        firsts.add(parser.left);

        if (!nullable(parser.left))
            return;

        firsts.addAll(list(parser.suffixes));
        firsts.addAll(list(parser.infixes));

        if (Stream.of(parser.infixes).anyMatch(this::nullable))
            firsts.add(parser.right);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (RightExpression parser)
    {
        if (parser.left != null) firsts.add(parser.left);
        firsts.addAll(list(parser.prefixes));

        boolean rightAdded = false;

        if (!parser.operatorRequired)
            firsts.add(parser.right);

        if (parser.left != null && nullable(parser.left))
            firsts.addAll(list(parser.infixes));

        // NOTE: We do not check for a nullable prefix, nor for nullable left + one nullable infix,
        // as that is a nullable repetition violation, and will be caught as such.
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (StringMatch parser) {
        if (parser.string.length() == 0 && parser.whitespace != null)
            firsts.add(parser.whitespace);
    }

    // ---------------------------------------------------------------------------------------------

}
