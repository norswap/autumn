package norswap.autumn;

import norswap.autumn.parsers.*;
import norswap.autumn.visitors.WellFormednessChecker;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * A visitor interface for the built-in implementations of {@link Parser}.
 *
 * <p>To write a visitor for the built-in parser implementations, it suffices to implement this
 * interface.
 *
 * <p>To handle custom parsers, you need to provide an adequate visitor action (which we'll an
 * "overload" by anology to all the overloads of the {@code visit} method in this interface).
 * You can do so by calling {@link #extend(Class, Class, BiConsumer)}. The best place to
 * put the {@code extend} call is within a {@code static} initializer within the parser. If you're
 * not the author of the parser, a static initializer within the grammar class is also a good spot.
 * Obviously, it should be called before the visitor can be invoked on the parser.
 *
 * <p>If you interleave multiple visitors, you might get a slight performance increase from
 * overloading {@link #overloads()}. See the Javadoc of that method for more details.
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

    /** Private implementation detail. */
    VisitorExtensions exts = new VisitorExtensions();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the set of custom parser overloads for this visitor. All instances of a given
     * visitor class should return the same {@code Overloads} object.
     *
     * <p>By default, {@link ParserVisitor} manages overloads on its own and it is not necessary
     * to override this method. Overriding this method can make things slightly faster by avoiding
     * an extra hash table lookup — but only when you're actively interleaving multiple visitors.
     */
    default Overloads overloads() {
        return exts.overloads(this.getClass());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Adds a visitor {@code V} overload for parser class {@code P}. By "overload" we simply mean
     * the action to be performed when a parser of the given class is visited by a visitor of class
     * {@code V}.
     *
     * <p>The best place to call this method is within a {@code static} initializer within the
     * parser. If you're not the author of the parser, a static initializer within the grammar class
     * is also a good spot. Obviously, it should be called before the visitor can be invoked on the
     * parser.
     */
    static <V extends ParserVisitor, P extends Parser>
    void extend (Class<V> vclass, Class<P> pclass, BiConsumer<P, V> implem)
    {
        exts.extend(vclass, pclass, implem);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * This overload is called for all custom parsers. If an overload for the parser has been
     * registered via {@link #extend(Class, Class, BiConsumer)}, it will be called, otherwise {@link
     * #defaultAction(Parser)} is called.
     */
    default void visit (Parser parser)
    {
        BiConsumer<Parser, ParserVisitor> action = overloads().get(parser.getClass());

        if (action != null)
            action.accept(parser, this);
        else
            defaultAction(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * When a custom parser is visited and that no corresponding overload has been registered using
     * {@link #extend(Class, Class, BiConsumer)}, this method is called.
     *
     * <p>This should perform some action based on conservative assumptions. If no such thing
     * is conceivable, you should throw a runtime exception instead.
     */
    void defaultAction (Parser parser);

    // ---------------------------------------------------------------------------------------------

    void visit (AbstractChoice parser);
    void visit (AbstractForwarding parser);
    void visit (AbstractPrimitive parser);
    void visit (AbstractWrapper parser);
    void visit (Around parser);
    void visit (Bounded parser);
    void visit (CharPredicate parser);
    void visit (Choice parser);
    void visit (Collect parser);
    void visit (ContextPredicate parser);
    void visit (Empty parser);
    void visit (Fail parser);
    void visit (LazyParser parser);
    void visit (LeftExpression parser);
    void visit (Longest parser);
    void visit (Lookahead parser);
    void visit (Memo parser);
    void visit (Not parser);
    void visit (ObjectPredicate parser);
    void visit (Optional parser);
    void visit (Repeat parser);
    void visit (RightExpression parser);
    void visit (Sequence parser);
    void visit (StringChoice parser);
    void visit (StringMatch parser);
    void visit (TrailingWhitespace parser);

    // ---------------------------------------------------------------------------------------------

    /**
     * This class represents a mapping from custom (not built-in) parser classes to
     * their visit action. It is similar to a {@code visit} overload from {@link ParserVisitor},
     * hence the name.
     *
     * <p>By default, {@link ParserVisitor} manages the overloads on its own and no user
     * intervention is required. See below for more details.
     *
     * <p>In custom parsers, {@link Parser#accept(ParserVisitor)} will call {@link
     * ParserVisitor#visit(Parser)} which will retrieve the visitor's overloads via {@link
     * #overloads()} and finally use the parser's class to determine the correct action.
     *
     * <p>Implementationd discussion: when the class is instantiated, the corresponding visitor
     * class must be given, so that the object may be registered globally. This lets {@link
     * ParserVisitor} manages the overloads on its own by default, but also lets users supply their
     * own {@code Overloads} instance (for optimization purposes) by overriding {@link
     * #overloads()}.
     *
     * @see ParserVisitor
     */
    abstract class Overloads
    {
        /**
         * Instantiate this class for the given visitor class. If the given class already has an
         * {@code Overloads} object, this one will replace it, after copying over the
         * previously-defined overloads.
         */
        public Overloads (Class<? extends ParserVisitor> vclass) {
            synchronized (exts) {
                Overloads ov = exts.overloads(vclass);
                if (ov != null) ov.addTo(this);
                exts.store.put(vclass, this);
            }
        }

        /** Retrieve the overload for the given parser class. */
        protected abstract BiConsumer<Parser, ParserVisitor> get
        (Class<? extends Parser> pclass);

        /** Add a new overload for a parser class. */
        protected abstract void put
        (Class<? extends Parser> pclass, BiConsumer<Parser, ParserVisitor> overload);

        /** Transfer all our overloads to {@code other}. */
        protected abstract void addTo (Overloads other);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * The implementation of {@link Overloads} used by default.
     */
    final class HashOverloads extends Overloads
    {
        private final HashMap<Class<? extends Parser>, BiConsumer<Parser, ParserVisitor>> map
            = new HashMap<>();

        /** See {@link Overloads#Overloads(Class)} */
        public HashOverloads (Class<? extends ParserVisitor> vclass) {
            super(vclass);
        }

        @Override public BiConsumer<Parser, ParserVisitor> get (Class<? extends Parser> pclass) {
            return map.get(pclass);
        }

        @Override public void put
            (Class<? extends Parser> pclass, BiConsumer<Parser, ParserVisitor> overload) {
            map.put(pclass, overload);
        }

        @Override public void addTo (Overloads other) {
            map.forEach(other::put);
        }
    }

    // ---------------------------------------------------------------------------------------------
}