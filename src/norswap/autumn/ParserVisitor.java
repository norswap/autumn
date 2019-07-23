package norswap.autumn;

import norswap.autumn.parsers.*;
import norswap.autumn.visitors.VisitorNullable;
import norswap.autumn.visitors.WellFormednessChecker;
import java.util.function.BiConsumer;

/**
 * A visitor interface for the built-in implementations of {@link Parser}.
 *
 * <p>To write a visitor for the built-in parser implementations, it suffices to implement this
 * interface. The built-in parsers already override {@link Parser#accept(ParserVisitor)} to call
 * {@code visitor.visit(this);} —  which will select the right overload for the specific parser.
 *
 * <p>In general, it might be desirable to extend the visitor to handle custom parsers. To do so,
 * visitor implementations should provide a means to register new {@code visit} implementations to
 * be dynamically dispatched on the basis of the parser's {@link Class} object. See for instance the
 * documentation of {@link VisitorNullable} and its {@link VisitorNullable#extension(Class,
 * BiConsumer)} method. In Autumn's built-in visitors, such extension mechanisms are backed by a
 * static instance of {@code HashMap<Class<? extends Parser>, BiConsumer<Parser,
 * MyVisitorImplementation>>}.
 *
 * <p>Finally, note that the visitor interface <b>only</b> allows specialization of behaviour based
 * on the parser type. It does not perform any kind of grammar traversal on your behalf. Of course,
 * for some visitors, such a traversal might be a part of the specialized functionality, but {@code
 * ParseVisitor} offers no support for this. However, {@link ParserWalker} has the logic to traverse
 * the grammar (which is essentially a directed parser graph whose edges are given by {@link
 * Parser#children()}. As an example of how visitors and walkers can work in tandem, see {@link
 * WellFormednessChecker} and its implementation.
 */
public interface ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Catch-all overload, in case someone doesn't want to implement a visitor interface for a
     * custom parser. If no catch-all is conceivable for your visitor, this should throw a runtime
     * exception.
     */
    void visit (Parser parser);

    // ---------------------------------------------------------------------------------------------

    void visit (AbstractChoice parser);
    void visit (AbstractForwarding parser);
    void visit (AbstractPrimitive parser);
    void visit (AbstractWrapper parser);
    void visit (Around parser);
    void visit (CharPredicate parser);
    void visit (Choice parser);
    void visit (Collect parser);
    void visit (ContextPredicate parser);
    void visit (Empty parser);
    void visit (Fail parser);
    void visit (GuardedRecursion parser);
    void visit (LazyParser parser);
    void visit (LeftExpression parser);
    void visit (LeftFold parser);
    void visit (LeftRecursive parser);
    void visit (Longest parser);
    void visit (Lookahead parser);
    void visit (Memo parser);
    void visit (Not parser);
    void visit (ObjectPredicate parser);
    void visit (Optional parser);
    void visit (Repeat parser);
    void visit (RightExpression parser);
    void visit (RightFold parser);
    void visit (Sequence parser);
    void visit (StringMatch parser);
    void visit (TokenChoice parser);
    void visit (TokenParser parser);

    // ---------------------------------------------------------------------------------------------
}