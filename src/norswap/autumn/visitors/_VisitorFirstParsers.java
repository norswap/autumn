package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;
import java.util.Set;

import static norswap.utils.Vanilla.list;

/**
 * A visitor for built-in parsers that retrieves the FIRST set for the visited parser.
 *
 * <p>The FIRST set for a parser P is the set of parsers that P may (directly) invoke
 * at the same input position as itself.
 *
 * <p>This interface requires the use of a {@link _VisitorNullable} that can handle all supported
 * parsers. If a parser A may call parsers B and C sequentially and NULLABLE(B), then both B and C
 * are in FIRST(A).
 *
 * <p>If you use this parser to traverse the FIRST graph, you must beware of cycles. It is recommend
 * to keep a set of visited parsers to avoid infinite recursion.
 *
 * <p>This visitor is used by {@link WellFormednessChecker} to identify left-recursive cycles.
 *
 * <p>As long as you invoke this visitor only through its {@link #firsts_for(Parser)} method,
 * you may reuse it for multiple parsers.
 *
 * <p>An instantiable version is available at {@link VisitorFirstParsers}.
 */
public interface _VisitorFirstParsers extends ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Provides the {@link _VisitorNullable} necessary for this parser to fill its task.
     */
    _VisitorNullable nullable_visitor ();

    // ---------------------------------------------------------------------------------------------

    /**
     * The set of parsers that may be directly invoked by the visited parser  at the same position
     * as itself.
     */
    Set<Parser> firsts();

    // ---------------------------------------------------------------------------------------------

    /**
     * Replaces the set underlying {@link #firsts()} by a new empty set.
     */
    void renew_firsts();

    // ---------------------------------------------------------------------------------------------

    /**
     * Visit the given parser and return the {@link #firsts()} set.
     */
    default Set<Parser> firsts_for (Parser parser)
    {
        renew_firsts();
        parser.accept(this);
        return firsts();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that the given sequence of parsers may be invoked at the same initial position as
     * the original parser and adds parsers from this sequence to {@link #firsts()}, depending on
     * their nullability.
     */
    default void firsts_add_sequence (Iterable<Parser> parsers)
    {
        for (Parser p: parsers) {
            firsts().add(p);
            if (!nullable_visitor().nullable(p)) break;
        }
    }

    // =============================================================================================

    @Override default void visit (CharPredicate parser) {
        // empty
    }

    @Override default void visit (ObjectPredicate parser) {
        // empty
    }

    @Override default void visit (Empty parser) {
        // empty
    }

    @Override default void visit (Fail parser) {
        // empty
    }

    @Override default void visit (StringMatch parser) {
        // empty
    }

    @Override default void visit (AbstractPrimitive parser) {
        // empty
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Collect parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (GuardedRecursion parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (LeftRecursive parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (Lookahead parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (Not parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (Optional parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (Repeat parser) {
        firsts().add(parser.child);
    }

    @Override default void visit (Memo parser) {
        firsts().add(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LazyParser parser) {
        firsts().add(parser.child());
    }

    @Override default void visit (TokenParser parser) {
        firsts().add(parser.target);
    }

    @Override default void visit (AbstractForwarding parser) {
        firsts().add(parser.forwardee);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Parser parser) {
        // pessimistic assumption
        parser.children().forEach(firsts()::add);
    }

    @Override default void visit (AbstractChoice parser) {
        parser.children().forEach(firsts()::add);
    }

    @Override default void visit (Choice parser) {
        parser.children().forEach(firsts()::add);
    }

    @Override default void visit (Longest parser) {
        parser.children().forEach(firsts()::add);
    }

    @Override default void visit (TokenChoice parser) {
        parser.children().forEach(firsts()::add);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Sequence parser) {
        firsts_add_sequence(parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Around parser) {
        firsts_add_sequence(list(parser.around, parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LeftFold parser) {
        firsts_add_sequence(list(parser.left, parser.operator, parser.right));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (RightFold parser)
    {
        firsts_add_sequence(list(parser.left, parser.operator, parser.right));
        if (!parser.operator_required) firsts().add(parser.right);
    }

    // ---------------------------------------------------------------------------------------------
}
