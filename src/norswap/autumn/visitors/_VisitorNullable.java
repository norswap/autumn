package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;
import java.util.Set;

/**
 * A visitor for built-in parsers meant to determine whether the parser is "nullable", i.e., whether
 * it can succeed while consuming no input.
 *
 * <p>This result is stored within {@link #result()}, though it is more often retrieved via the
 * return value of {@link #nullable(Parser)}. You should always invoke the parser through this
 * method!
 *
 * <p>Invocations of the visitor <b>must</b> set this value using {@link #set_result(boolean)}, so
 * that the visitor may be reused for different parsers.
 *
 * <p>Invocations of the visitor may recurse, but only via {@link #nullable(Parser)}. That method
 * takes care to avoid infinite recursion by maintaining the set of parser currently being checked.
 *
 * <p>Because of recursion, the set {@link #set_result(boolean)} call must always occur <b>after</b>
 * any recursion (or the result will be overwritten).
 *
 * <p>This visitor is needed by {@link _VisitorNullableRepetition} and {@link _VisitorFirstParsers}.
 *
 * <p>An instantiable version is available as {@link VisitorNullable}.
 */
public interface _VisitorNullable extends ParserVisitor
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
     * A data structure used to record the parser on which we're currently invoking {@link
     * #nullable(Parser)}, to avoid infinite recursion in case the grammar is recursive (which
     * is typically the case).
     */
    Set<Parser> stack();

    // ---------------------------------------------------------------------------------------------

    /**
     * Visit the given parser and return the {@link #result()}.
     */
    default boolean nullable (Parser parser)
    {
        // A recursive invocation is considered non-nullable.
        // This will leave the original invocation to be nullable if it has a nullable invocation,
        // or non-nullable otherwise, which is the correct semantics.

        if (stack().contains(parser)) {
            set_result(false);
            return false;
        }

        stack().add(parser);
        parser.accept(this);
        stack().remove(parser);
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

    @Override default void visit (Fail parser) {
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
        nullable(parser.child);
    }

    @Override default void visit (LeftRecursive parser) {
        nullable(parser.child);
    }

    @Override default void visit (GuardedRecursion parser) {
        nullable(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LazyParser parser) {
        nullable(parser.child());
    }

    @Override default void visit (TokenParser parser) {
        nullable(parser.target());
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
