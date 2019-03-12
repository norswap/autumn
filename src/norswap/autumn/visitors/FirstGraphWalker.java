package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;

import static norswap.utils.Vanilla.list;

/**
 * A visitor for built-in parsers that helps walk the graph induced by the FIRST relationship: the
 * pair of parsers (a, b) is in FIRST if a may invoke b at the same input position it was invoked
 * at.
 *
 * <p><b>IMPORTANT: Call this visitor through its {@link #walk(Parser)} method.</b>
 *
 * <p>Basically, the visitor overrides the {@code visit} methods for built-in parsers so that they
 * call the {@link #first(Parser)} method for each of their sub-parsers that maybe be invoked at
 * the same position.
 *
 * <p>It's up to extensions of this interface to implement {@link #first(Parser)} to perform the
 * actual walk. At a minimum it should call {@code parser.accept(this)}, and should avoid infinite
 * recursion by detection recursion and/or maintaining a set of already visited nodes.
 *
 * <p>This interface requires the use of a {@link _NullableVisitor} that can handle all supported
 * parsers. If a parser A may call parsers B and C sequentially, and FIRST(A, B), and NULLABLE(A),
 * then FIRST(A, C) is also true.
 *
 * <p>If extending this visitor (or one of its extensions) for custom parsers, you should usually
 * leave its data structures alone, but call {@link #first(Parser)} to indicate that a parser may be
 * invoked at the initial position. {@link #first(Parser)} will take care of the recursive logic.
 * You'll also need to extend {@link _NullableVisitor} for your custom parsers.
 *
 * <p>Built-in extensions of this class include {@link _FirstSetCalculator}, which computes the
 * closure over the FIRST relationship; {@link _LeftRecursiveCycleEnumerator} which finds all cycles
 * in the graph, which correspond to left-recursive paths in the grammar; and TODO (detector)
 */
public interface FirstGraphWalker extends ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Provides the {@link _NullableVisitor} necessary for this parser to fill its task.
     */
    _NullableVisitor nullable_visitor ();

    // ---------------------------------------------------------------------------------------------

    /**
     * The entry point for this visitor.
     *
     * <p>The default implementation calls {@link #first(Parser)}, override if that is not
     * the desired behaviour.
     */
    default void walk (Parser parser) {
        first(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that {@code parser} may be invoked at the same initial position as the the original
     * parser. Must take care of filling data structures of interest accordingly, and to recursively
     * visit the parser while avoiding infinite recursion.
     */
    void first (Parser parser);

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that the given sequence of parsers may be invoked at the same initial position
     * as the original parser and calls {@link #first(Parser)} on items as needed, depending on
     * their nullability.
     */
    default void first_sequence (Iterable<Parser> parsers)
    {
        for (Parser p: parsers) {
            first(p);
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

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Collect parser) {
        first(parser.child);
    }

    @Override default void visit (GuardedRecursion parser) {
        first(parser.child);
    }

    @Override default void visit (LeftRecursive parser) {
        first(parser.child);
    }

    @Override default void visit (Lookahead parser) {
        first(parser.child);
    }

    @Override default void visit (Not parser) {
        first(parser.child);
    }

    @Override default void visit (Optional parser) {
        first(parser.child);
    }

    @Override default void visit (Repeat parser) {
        first(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LazyParser parser) {
        first(parser.child());
    }

    @Override default void visit (TokenParser parser) {
        first(parser.target());
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Parser parser) {
        parser.children().forEach(this::first);
    }

    @Override default void visit (Choice parser) {
        parser.children().forEach(this::first);
    }

    @Override default void visit (Longest parser) {
        parser.children().forEach(this::first);
    }

    @Override default void visit (TokenChoice parser) {
        parser.children().forEach(this::first);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Sequence parser) {
        first_sequence(parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Around parser) {
        first_sequence(list(parser.around, parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LeftAssoc parser) {
        first_sequence(list(parser.left, parser.operator, parser.right));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (RightAssoc parser)
    {
        first_sequence(list(parser.left, parser.operator, parser.right));
        if (!parser.operator_required) first(parser.right);
    }

    // ---------------------------------------------------------------------------------------------
}
