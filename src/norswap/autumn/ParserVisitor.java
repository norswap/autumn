package norswap.autumn;

import norswap.autumn.parsers.*;
import norswap.autumn.visitors.VisitorFirstParsers;
import norswap.autumn.visitors.WellFormednessChecker;

/**
 * A visitor interface for the built-in implementations of {@link Parser}.
 *
 * <p>To write a visitor for the built-in parser implementations, it suffices to implement this
 * interface. The built-in parsers already override {@link Parser#accept(ParserVisitor)} to call
 * {@code visitor.visit(this);} —  which will select the right overload for the specific parser.
 *
 * <p><b>Note:</b>** The documentation below is a brief explanation of how to use the visitor
 * pattern in Autumn. A more gentle and expanded version is available in the user manual:
 * https://github.com/norswap/autumn4/blob/master/doc/B6-parser-visitors-walkers.md</p>
 *
 * <p>To handle custom parsers (say {@code MyParser}), things are slightly more complex. You must
 * first create a sub-interface extending {@code ParserVisitor} (say {@code MyParserVisitor}),
 * adding a method {@code void visit(MyParser parser);}. Next, {@code MyParser} must override {@link
 * Parser#accept(ParserVisitor)} to call {@code ((MyParserVisitor)visitor).visit(this);}. Now all
 * visitors that implement {@code MyParserVisitor} will work with all built-in parsers as well as
 * {@code MyParser}.
 *
 * <p>This is not the end of the story, as you may want to compose visitors for multiple parsers.
 * The trick is to actually implement {@code ParserVisitor} and its sub-interfaces as interfaces,
 * then to compose these implementations with a single class.
 *
 * <p>As an example, consider this hierarchy:
 * <pre>
 * {@code
 * interface ParserVisitor
 *  + interface MyParserVisitor     extends ParserVisitor
 *  + interface _VisitorA           extends ParserVisitor
 *     + class VisitorA             extends _VisitorA
 *  + interface _VisitorB           extends ParserVisitor
 *     + class VisitorB             extends _VisitorB
 *
 * interface _VisitorMyParserA      extends _VisitorA, MyParserVisitor
 *  + final class VisitorMyParserA  extends VisitorA implements _VisitorMyParserA
 *
 * interface _VisitorMyParserB      extends _VisitorB, MyParserVisitor
 *  + final class VisitorMyParserB  extends VisitorA implements _VisitorMyParserB
 * }
 * </pre>
 *
 * <p>Within it, we have an interface to handle our custom parser {@code MyParser}: {@code
 * MyParserVisitor}. We also have two visitor implementations that do not support {@code MyParser}:
 * {@code _VisitorA} and {@code _VisitorB}, which might be coming from some library. Note that those
 * are interfaces, precisely so that they can be composed.
 *
 * <p>{@code VisitorA} and {@code VisitorB} are instantiable versions of these implementations.
 * Typically, these classes will be almost empty: the only thing you must do is to supply storage for
 * the state of the visitors. (e.g. storage for {@link VisitorFirstParsers#firsts()}).
 *
 * <p>Note that it's a convention to use an underscore (_) in front of the name of the
 * "implementation interfaces" and to use the same name without the underscore for the corresponding
 * instantiable version. Another convention we use is to have visitors' names (both interfaces and
 * classes) start with "Visitor".
 *
 * <p>Now, if you want to use {@code _VisitorA} with {@code MyParserVisitor}, you need to compose
 * them and supply an {@code void accept(MyParser)} overload. This is what {@code _VisitorMyParserA}
 * does, and similarly with {@code _VisitorMyParserB} for {@code _VisitorB}. We also supply
 * the corresponding instantiable classes. Note that if you're not going to expose your code as a
 * library, you can skip the interface step ({@code _VisitorMyParserA}) and immediately implement
 * {@code _VisitorA} and {@code MyParserVisitor} in a class!
 *
 * <p>Note that we make {@code _VisitorMyParserA} extends {@code VisitorA}: this simply avoids
 * duplicating the storage definitions already made in {@code VisitorA}.
 *
 * <p>These composition hierarchies can even get hairier, but everything will keep working as long
 * as each method is only overriden in a single interface of the hiearchy.
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
    void visit (LeftFold parser);
    void visit (LeftRecursive parser);
    void visit (Longest parser);
    void visit (Lookahead parser);
    void visit (Memo parser);
    void visit (Not parser);
    void visit (ObjectPredicate parser);
    void visit (Optional parser);
    void visit (Repeat parser);
    void visit (RightFold parser);
    void visit (Sequence parser);
    void visit (StringMatch parser);
    void visit (TokenChoice parser);
    void visit (TokenParser parser);

    // ---------------------------------------------------------------------------------------------
}