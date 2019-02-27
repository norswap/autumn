package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;

/**
 * A visitor implementation for built-in parsers meant to determine whether the parser is
 * "nullable", i.e., whether it can succeed while consuming no input.
 *
 * <p>This result is stored within {@link #result()}. Extension of this visitor for custom parsers
 * must set this value using {@link #set_result(boolean)}.
 *
 * <p>This visitor is needed by {@link FirstGraphWalker} and its various extensions.
 *
 * <p>The visitor can be reused, as any invocation of the visitor <b>must</b> overwrite the result.
 *
 * <p>An instantiable version is available as {@link NullableVisitor}.
 */
public interface _NullableVisitor extends ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the visited parser is nullable.
     */
    boolean result();

    // ---------------------------------------------------------------------------------------------

    /**
     * Set the result for the visited parser.
     */
    void set_result (boolean value);

    // ---------------------------------------------------------------------------------------------

    /**
     * Visit the given parser and return the {@link #result()}.
     */
    default boolean nullable (Parser parser) {
        parser.accept(this);
        return result();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the result to whether at least one parser in {@code parsers} is nullable.
     */
    default void one_nullable (Iterable<Parser> parsers)
    {
        for (Parser parser: parsers) {
            if (nullable(parser)) {
                set_result(true);
                return;
            }
        }
        set_result(false);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Sets the result to whether all parsers in {@code parsers} are nullable.
     */
    default void all_nullable (Iterable<Parser> parsers)
    {
        for (Parser parser: parsers) {
            if (!nullable(parser)) {
                set_result(false);
                return;
            }
        }
        set_result(true);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Parser parser)
    {
        // default action: be overly conservative
        set_result(true);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (CharPredicate parser) {
        set_result(false);
    }

    @Override default void visit (ObjectPredicate parser) {
        set_result(false);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Lookahead parser) {
        set_result(true);
    }

    @Override default void visit (Not parser) {
        set_result(true);
    }

    @Override default void visit (Optional parser) {
        set_result(true);
    }

    @Override default void visit (Empty parser) {
        set_result(true);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Collect parser) {
        parser.child.accept(this);
    }

    @Override default void visit (LeftRecursive parser) {
        parser.child.accept(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LazyParser parser) {
        parser.child().accept(this);
    }

    @Override default void visit (TokenParser parser) {
        parser.target().accept(this);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Choice parser) {
        one_nullable(parser.children());
    }

    @Override default void visit (Longest parser) {
        one_nullable(parser.children());
    }

    @Override default void visit (TokenChoice parser) {
        one_nullable(parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Sequence parser) {
        all_nullable(parser.children());
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (StringMatch parser) {
        set_result(parser.string.equals(""));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Repeat parser) {
        set_result(parser.min == 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Around parser) {
        set_result(
               parser.min == 0
            || parser.min == 1 && nullable(parser.around)
            || nullable(parser.around) && nullable(parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LeftAssoc parser) {
        set_result(
               !parser.operator_required && nullable(parser.left)
            || nullable(parser.left) && nullable(parser.operator) && nullable(parser.right));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (RightAssoc parser) {
        set_result(
               !parser.operator_required && nullable(parser.right)
            || nullable(parser.left) && nullable(parser.operator) && nullable(parser.right));
    }

    // ---------------------------------------------------------------------------------------------
}
