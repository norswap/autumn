package norswap.autumn.visitors;

import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.parsers.*;

/**
 * A visitor for built-in parsers that checks if the parser entails a repetition over some nullable
 * parser(s) (see {@link VisitorNullable}). These parsers are of interest because they are able to
 * loop forever, as they keep invoking the same parser that successfully matches no input.
 *
 * <p>In fact, this visitor should be taken as an infinite loop/recursion detector (as long as the
 * infinite loop is caused by lawful semantics, not by an implementation bug).
 *
 * <p>To dermine whether a parser repeats over a nullable parser, call {@link
 * #nullable_repetition(Parser)}.
 *
 * <p>To support custom parsers, provide an appropriate overload using {@link ParserVisitor#extend}.
 * Also see {@link ParserVisitor}'s Javadoc.
 *
 * <p>Within the supplied overload, you can query for the nullability of sub-parsers using
 * {@link #nullable(Parser)}. Within your action, you <b>must</b> set {@link #result}, to indicate
 * whether the parser repeats over some nullable(s) (true) or not (false).
 *
 * <p>This visitor is used by {@link WellFormednessChecker}, which invokes it on all parsers
 * in a parser graph.
 *
 * <p>As long as you invoke this visitor only through its {@link #nullable_repetition(Parser)}
 * (Parser)} method, you may reuse it for multiple parsers.
 */
public final class VisitorNullableRepetition implements ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    private static HashOverloads overloads = new HashOverloads(VisitorNullable.class);

    // ---------------------------------------------------------------------------------------------

    @Override public Overloads overloads() {
        return overloads;
    }
    
    // ---------------------------------------------------------------------------------------------

    public final VisitorNullable nullable_visitor;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether the visited parser is a repetition over some nullable(s).
     */
    public boolean result;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance with the given nullable visitor.
     *
     * <p>Since {@link VisitorNullable} memoizes parser nullability, you should reuse an existing
     * instance as much as possible.
     */
    public VisitorNullableRepetition (VisitorNullable nullable_visitor) {
        this.nullable_visitor = nullable_visitor;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates whether the given parser performs repetitions or looping over some nullable
     * parser(s), causing the parser to potentially loop infinitely.
     */
    public boolean nullable_repetition (Parser parser) {
        parser.accept(this);
        return result;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Shortcut for {@code nullable_visitor.nullable(parser}.
     */
    protected boolean nullable (Parser parser) {
        return nullable_visitor.nullable(parser);
    }

    // =============================================================================================

    @Override public void default_action (Parser parser) {
        // Optimistically assume unhandled custom parsers don't loop over nullable parsers.
        result = false;
    }

    // ---------------------------------------------------------------------------------------------

    // Can't loop over a nullable parser.

    @Override public void visit (AbstractChoice parser)    { result = false; }
    @Override public void visit (AbstractForwarding parser){ result = false; }
    @Override public void visit (AbstractPrimitive parser) { result = false; }
    @Override public void visit (AbstractWrapper parser)   { result = false; }
    @Override public void visit (Bounded parser)           { result = false; }
    @Override public void visit (CharPredicate parser)     { result = false; }
    @Override public void visit (Choice parser)            { result = false; }
    @Override public void visit (Collect parser)           { result = false; }
    @Override public void visit (ContextPredicate parser)  { result = false; }
    @Override public void visit (Empty parser)             { result = false; }
    @Override public void visit (Fail parser)              { result = false; }
    @Override public void visit (GuardedRecursion parser)  { result = false; }
    @Override public void visit (LazyParser parser)        { result = false; }
    @Override public void visit (Longest parser)           { result = false; }
    @Override public void visit (Lookahead parser)         { result = false; }
    @Override public void visit (Memo parser)              { result = false; }
    @Override public void visit (Not parser)               { result = false; }
    @Override public void visit (ObjectPredicate parser)   { result = false; }
    @Override public void visit (Optional parser)          { result = false; }
    @Override public void visit (Sequence parser)          { result = false; }
    @Override public void visit (StringMatch parser)       { result = false; }
    @Override public void visit (TokenChoice parser)       { result = false; }
    @Override public void visit (TokenParser parser)       { result = false; }

    // ---------------------------------------------------------------------------------------------

    // Does not loop: the seed has to grow.
    @Override public void visit (LeftRecursive parser) { result = false; }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Around parser)
    {
        result = !parser.exact && nullable(parser.around) && nullable(parser.inside);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Repeat parser)
    {
        result = !parser.exact && nullable(parser.child);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LeftExpression parser)
    {
        for (Parser suffix: parser.suffixes)
            if (nullable(suffix)) {
                result = true;
                return;
            }

        if (parser.right != null && nullable(parser.right))
            for (Parser infix: parser.infixes)
                if (nullable(infix)) {
                    result = true;
                    return;
                }

        result = false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (RightExpression parser)
    {
        for (Parser prefix: parser.prefixes)
            if (nullable(prefix)) {
                result = true;
                return;
            }

        if (parser.left != null && nullable(parser.left))
            for (Parser infix: parser.infixes)
                if (nullable(infix)) {
                    result = true;
                    return;
                }

        result = false;
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (LeftFold parser)
    {
        result = nullable(parser.operator) && nullable(parser.right);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (RightFold parser)
    {
        result = nullable(parser.left) && nullable(parser.operator);
    }

    // ---------------------------------------------------------------------------------------------
}
