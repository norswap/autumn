package norswap.autumn.visitors;

import norswap.autumn.Grammar;
import norswap.autumn.ParseState;
import norswap.autumn.Parser;
import norswap.autumn.ParserVisitor;
import norswap.autumn.ParserWalker;
import norswap.autumn.parsers.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static norswap.utils.Vanilla.map;

/**
 * A visitor that makes a deep copy of the part of the parser graph reachable through a parser.
 *
 * <p>To make a copy of a parser, call {@link #getCopy(Parser)}.
 *
 * <p>The visitor memoizes the copies of parser. Requesting a copy of a parser automatically
 * computes (and caches) copies of all parsers reachable through this parser.
 *
 * <p>To support custom parsers, provide an appropriate overload using {@link ParserVisitor#extend}.
 * Also see {@link ParserVisitor}'s Javadoc.
 *
 * <p>Within the supplied overloads, you can request the copy of a sub-parser by using {@link
 * #getCopy(Parser)}. Once the copy has been created, you should register it using {@link
 * #registerCopy(Parser, Parser)}.
 *
 * <p><b>Important Caveats</b>
 *
 * <p>It is not possible to deep-copy the values captured by a lambda function. As such, it is
 * preferable not to use the grammar instance that the source parser comes from for other parses —
 * though that should be <b>in principle</b> safe if the author of that grammar respect guidelines
 * (e.g., used {@link ParseState}. On the other hand, if lambda functions capture (and do anything
 * meaningful) with parsers from the original grammar, <b>the result will probably be broken</b> as
 * the new grammar now includes references to the old parser graph.
 *
 * <p>Similarly, the copied parsers will also refer to the {@link Grammar#ws} parser of the old
 * grammar. If you want to extend the copy, you might need to grab hold of the old grammar and
 * access/modify these.
 *
 * <p>Also note that the visitor won't actually copy parser that have no children (as they can
 * be shared between grammars without hurdles.
 *
 * <p><b>Applications</b>
 *
 * <p>The main use case of this visitor is to extend it to perform <b>grammar transformations</b>.
 * Indeed, by overriding the {@code visit} method for a given type of parser, you can register
 * a transformation of the original parser instead of a copy!
 */
public final class CopyVisitor extends ParserWalker implements ParserVisitor
{
    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to emit warnings in broken scenarios that we attempt to handle anyway (true by
     * default).
     *
     * <p>Currently only used when a recursion that isn't broken by a {@link LazyParser} is
     * encountered.
     */
    public static boolean emitWarnings = true;

    // ---------------------------------------------------------------------------------------------

    private static final Parser[] witness = new Parser[0];

    // ---------------------------------------------------------------------------------------------

    private static HashOverloads overloads = new HashOverloads(VisitorNullable.class);

    // ---------------------------------------------------------------------------------------------

    @Override public Overloads overloads() {
        return overloads;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A map from parser of the original grammar to parsers in the copied grammar. Avoid accessing
     * and modifying directly, rather use the {@link #getCopy(Parser)} and {@link
     * #registerCopy(Parser, Parser)}.
     */
    public Map<Parser, Parser> copies = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy for the given parser. Uses a previously registered copy if available.
     */
    public Parser getCopy (Parser parser)
    {
        Parser copy = copies.get(parser);
        if (copy != null)
            return copy;

        walk(parser);
        return copies.get(parser);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Register {@code copy} as a copy of {@code original}.
     */
    public void registerCopy (Parser original, Parser copy) {
        copies.put(original, copy);
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void work (Parser parser, State state)
    {
        switch (state) {
            case RECURSE:
                // Normally not possible, but let's do our best.
                patchRecursion(parser);
                break;
            case AFTER:
                parser.accept(this);
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void patchRecursion (Parser parser)
    {
        if (emitWarnings) {
            System.err.println(
                "Warning: detected recursion during grammar copy. " +
                "This is weird: recursion normally has to be broken with Grammar#lazy " +
                "parsers. We patched it up with a lazy parser for you, but please check " +
                "what is going on in the original grammar.\n\n" + "You can disable " +
                "these warnings by setting CopyVisitor.emitWarnings to false.\n");
            try {
                System.err.println("Recursive parser: " + parser);
            }
            catch (Exception e) {
                // In case a dumb-dumb has infinite recursion in
                // his toString() method as well.
            }
        }

        Supplier<Parser> supplier = new Supplier<Parser>()
        {
            // This whole shebub is necessary so that we avoid holding on to the `copies` map
            // and part of the original parser graph via `parser` — which would be captured
            // by reference in a lambda.

            private Map<Parser, Parser> copies = CopyVisitor.this.copies;
            private Parser originalParser = parser;
            private Parser copy = null;

            @Override public Parser get()
            {
                if (copy != null)
                    return copy;

                copy = copies.get(originalParser);
                copies = null;
                originalParser = null;
                return copy;
            }
        };

        copies.put(parser, new LazyParser(supplier));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void defaultAction (Parser parser)
    {
        if (parser instanceof Cloneable)
        {
            Method m = null;
            try {
                m = parser.getClass().getMethod("clone");
                m.setAccessible(true);
                registerCopy(parser, (Parser) m.invoke(parser));
            }
            catch (ReflectiveOperationException | SecurityException e) {
                // NoSuchMethodException: won't happen
                // IllegalAccessException: won't happen
                // InvocationTargetException: fall through
                // SecurityException: fall through
            }
            finally {
                if (m != null) m.setAccessible(false);
            }
        }

        // we can't deep-copy the parser, so we just refer to it
        registerCopy(parser, parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractChoice parser) {
        defaultAction(parser);
    }

    @Override public void visit (AbstractForwarding parser) {
        defaultAction(parser);
    }

    @Override public void visit (AbstractPrimitive parser) {
        defaultAction(parser);
    }

    @Override public void visit (AbstractWrapper parser) {
        defaultAction(parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Choice parser) {
        registerCopy(parser, new Choice(map(parser.children(), witness, this::getCopy)));
    }

    @Override public void visit (Sequence parser) {
        registerCopy(parser, new Sequence(map(parser.children(), witness, this::getCopy)));
    }

    @Override public void visit (Longest parser) {
        registerCopy(parser, new Longest(map(parser.children(), witness, this::getCopy)));
    }

    // ---------------------------------------------------------------------------------------------

    // These parser don't need copies! (no children)

    @Override public void visit (CharPredicate parser)      { registerCopy(parser, parser); }
    @Override public void visit (ContextPredicate parser)   { registerCopy(parser, parser); }
    @Override public void visit (Empty parser)              { registerCopy(parser, parser); }
    @Override public void visit (Fail parser)               { registerCopy(parser, parser); }
    @Override public void visit (ObjectPredicate parser)    { registerCopy(parser, parser); }
    @Override public void visit (StringChoice parser)       { registerCopy(parser, parser); }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Lookahead parser) {
        registerCopy(parser, new Lookahead(getCopy(parser.child)));
    }

    @Override public void visit (Not parser) {
        registerCopy(parser, new Not(getCopy(parser.child)));
    }

    @Override public void visit (Optional parser) {
        registerCopy(parser, new Optional(getCopy(parser.child)));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Around parser)
    {
        registerCopy(parser, new Around(parser.min, parser.exact, parser.trailing,
            getCopy(parser.around), getCopy(parser.inside)));
    }

    @Override public void visit (Bounded parser)
    {
        registerCopy(parser,
            new Bounded(getCopy(parser.coarse), getCopy(parser.fine), parser.fallback));
    }

    @Override public void visit (Collect parser)
    {
        registerCopy(parser, new Collect(parser.name, getCopy(parser.child),
            parser.lookback, parser.actionOnFail, parser.pop, parser.action));
    }

    @Override public void visit (LazyParser parser)
    {
        registerCopy(parser, new LazyParser(parser::child));
    }

    @Override public void visit (LeftExpression parser)
    {
        Parser[] infixes  = map(parser.infixes,  witness, this::getCopy);
        Parser[] suffixes = map(parser.suffixes, witness, this::getCopy);

        registerCopy(parser, new LeftExpression(
            getCopy(parser.left),
            parser.right != null ? getCopy(parser.right) : null,
            infixes,  parser.infixSteps,
            suffixes, parser.suffixSteps,
            parser.operatorRequired));
    }

    @Override public void visit (RightExpression parser)
    {
        Parser[] infixes  = map(parser.infixes,  witness, this::getCopy);
        Parser[] prefixes = map(parser.prefixes, witness, this::getCopy);

        registerCopy(parser, new RightExpression(
            parser.left != null ? getCopy(parser.left) : null,
            getCopy(parser.right),
            infixes,  parser.infixSteps,
            prefixes, parser.prefixSteps,
            parser.operatorRequired));
    }

    @Override public void visit (Memo parser)
    {
        registerCopy(parser,
            new Memo(getCopy(parser.child), parser.memoizer, parser.contextExtractor));
    }

    @Override public void visit (Repeat parser)
    {
        registerCopy(parser,
            new Repeat(parser. min, parser.exact, getCopy(parser.child)));
    }

    @Override public void visit (StringMatch parser)
    {
        registerCopy(parser,
            new StringMatch(parser.string, getCopy(parser.whitespace)));
    }

    @Override public void visit (TrailingWhitespace parser)
    {
        registerCopy(parser,
            new TrailingWhitespace(getCopy(parser.child), getCopy(parser.whitespace)));
    }

    // ---------------------------------------------------------------------------------------------
}
