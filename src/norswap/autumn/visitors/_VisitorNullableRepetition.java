package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;

/**
 * A visitor for built-in parsers that checks if the parser entails a repetition over some nullable
 * parser(s) (see {@link _VisitorNullable}). These parsers are of interest because they are able to
 * loop forever, as they keep invoking the same parser that successfully matches no input.
 *
 * <p>In fact, this visitor should be taken as an infinite loop/recursion detector (as long as the
 * infinite loop is caused by lawful semantics, not by an implementation bug).
 *
 * <p>The result is stored within {@link #result()}, though it is more often retrieved via the
 * return value of {@link #nullable_repetition(Parser)}.
 *
 * <p>Invocations of the visitor <b>must</b> set this value using {@link #set_result(boolean)}, so
 * that the visitor may be reused for different parsers.
 *
 * <p>This visitor is used by {@link WellFormednessChecker}, which invokes it on all parsers
 * in a parser graph.
 *
 * <p>An instantiable version is available as {@link VisitorNullableRepetition}.
 */
public interface _VisitorNullableRepetition extends ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Provides the necessary {@link _VisitorNullable} to check if child parsers are nullable.
     */
    _VisitorNullable nullable_visitor ();

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the visited parser is a repetition over some nullable(s).
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
    default boolean nullable_repetition (Parser parser) {
        parser.accept(this);
        return result();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Shortcut for {@code nullable_visitor().nullable(parser}.
     */
    default boolean nullable (Parser parser) {
        return nullable_visitor().nullable(parser);
    }

    // ---------------------------------------------------------------------------------------------

    // Can't loop over a nullable parser.

    @Override default void visit (AbstractChoice parser)    { set_result(false); }
    @Override default void visit (AbstractForwarding parser){ set_result(false); }
    @Override default void visit (AbstractPrimitive parser) { set_result(false); }
    @Override default void visit (CharPredicate parser)     { set_result(false); }
    @Override default void visit (Choice parser)            { set_result(false); }
    @Override default void visit (Collect parser)           { set_result(false); }
    @Override default void visit (Empty parser)             { set_result(false); }
    @Override default void visit (Fail parser)              { set_result(false); }
    @Override default void visit (GuardedRecursion parser)  { set_result(false); }
    @Override default void visit (LazyParser parser)        { set_result(false); }
    @Override default void visit (Longest parser)           { set_result(false); }
    @Override default void visit (Lookahead parser)         { set_result(false); }
    @Override default void visit (Memo parser)              { set_result(false); }
    @Override default void visit (Not parser)               { set_result(false); }
    @Override default void visit (ObjectPredicate parser)   { set_result(false); }
    @Override default void visit (Optional parser)          { set_result(false); }
    @Override default void visit (Sequence parser)          { set_result(false); }
    @Override default void visit (StringMatch parser)       { set_result(false); }
    @Override default void visit (TokenChoice parser)       { set_result(false); }
    @Override default void visit (TokenParser parser)       { set_result(false); }

    // ---------------------------------------------------------------------------------------------

    // Does not loop: the seed has to grow.
    @Override default void visit (LeftRecursive parser) { set_result(false); }

    // ---------------------------------------------------------------------------------------------

    // Assume unhandled custom parsers don't loop over nullable parsers.
    @Override default void visit (Parser parser) { set_result(false); }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Around parser)
    {
        set_result(!parser.exact && nullable(parser.around) && nullable(parser.inside));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (Repeat parser)
    {
        set_result(!parser.exact && nullable(parser.child));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (LeftFold parser)
    {
        set_result(nullable(parser.operator) && nullable(parser.right));
    }

    // ---------------------------------------------------------------------------------------------

    @Override default void visit (RightFold parser)
    {
        set_result(nullable(parser.operator) && nullable(parser.left));
    }

    // ---------------------------------------------------------------------------------------------
}
