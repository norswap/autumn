package norswap.autumn.visitors;

import norswap.autumn.DSL;
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
 * <p>To make a copy of a parser, call {@link #get_copy(Parser)}.
 *
 * <p>The visitor memoizes the copies of parser. Requesting a copy of a parser automatically
 * computes (and caches) copies of all parsers reachable through this parser.
 *
 * <p>To support custom parsers, provide an appropriate overload using {@link ParserVisitor#extend}.
 * Also see {@link ParserVisitor}'s Javadoc.
 *
 * <p>Within the supplied overloads, you can request the copy of a sub-parser by using {@link
 * #get_copy(Parser)}. Once the copy has been created, you should register it using {@link
 * #register_copy(Parser, Parser)}.
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
 * <p>Similarly, the copied parsers will also refer to the {@link Tokens} instance of the old
 * grammar, as well as its {@link DSL#ws} rule. If you want to extend the copy, you might need
 * to grab hold of the old grammar and access/modify these.
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
    public static boolean emit_warnings = true;

    // ---------------------------------------------------------------------------------------------

    private static final Parser[] witness = new Parser[0];

    // ---------------------------------------------------------------------------------------------

    private static HashOverloads overloads = new HashOverloads(VisitorNullable.class);

    // ---------------------------------------------------------------------------------------------

    @Override public Overloads overloads () {
        return overloads;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A map from parser of the original grammar to parsers in the copied grammar. Avoid accessing
     * and modifying directly, rather use the {@link #get_copy(Parser)} and {@link
     * #register_copy(Parser, Parser)}.
     */
    public Map<Parser, Parser> copies = new HashMap<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a copy for the given parser. Uses a previously registered copy if available.
     */
    public Parser get_copy (Parser parser)
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
    public void register_copy (Parser original, Parser copy) {
        copies.put(original, copy);
    }

    // ---------------------------------------------------------------------------------------------

    @Override protected void work (Parser parser, State state)
    {
        switch (state) {
            case RECURSE:
                // Normally not possible, but let's do our best.
                patch_recursion(parser);
                break;
            case AFTER:
                parser.accept(this);
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------

    private void patch_recursion (Parser parser)
    {
        if (emit_warnings) {
            System.err.println(
                "Warning: detected recursion during grammar copy. " +
                "This is weird: recursion normally has to be broken with DSL#lazy " +
                "parsers. We patched it up with a lazy parser for you, but please check " +
                "what is going on in the original grammar.\n\n" + "You can disable " +
                "these warnings by setting CopyVisitor.emit_warnings to false.\n");
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
            private Parser original_parser = parser;
            private Parser copy = null;

            @Override public Parser get ()
            {
                if (copy != null)
                    return copy;

                copy = copies.get(original_parser);
                copies = null;
                original_parser = null;
                return copy;
            }
        };

        copies.put(parser, new LazyParser(supplier));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void default_action (Parser parser)
    {
        if (parser instanceof Cloneable)
        {
            Method m = null;
            try {
                m = parser.getClass().getMethod("clone");
                m.setAccessible(true);
                register_copy(parser, (Parser) m.invoke(parser));
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
        register_copy(parser, parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (AbstractChoice parser) {
        default_action(parser);
    }

    @Override public void visit (AbstractForwarding parser) {
        default_action(parser);
    }

    @Override public void visit (AbstractPrimitive parser) {
        default_action(parser);
    }

    @Override public void visit (AbstractWrapper parser) {
        default_action(parser);
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Choice parser) {
        register_copy(parser, new Choice(map(parser.children(), witness, this::get_copy)));
    }

    @Override public void visit (Sequence parser) {
        register_copy(parser, new Sequence(map(parser.children(), witness, this::get_copy)));
    }

    @Override public void visit (Longest parser) {
        register_copy(parser, new Longest(map(parser.children(), witness, this::get_copy)));
    }

    // ---------------------------------------------------------------------------------------------

    // These parser don't need copies! (no children)

    @Override public void visit (CharPredicate parser)      { register_copy(parser, parser); }
    @Override public void visit (ContextPredicate parser)   { register_copy(parser, parser); }
    @Override public void visit (Empty parser)              { register_copy(parser, parser); }
    @Override public void visit (Fail parser)               { register_copy(parser, parser); }
    @Override public void visit (ObjectPredicate parser)    { register_copy(parser, parser); }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Lookahead parser) {
        register_copy(parser, new Lookahead(get_copy(parser.child)));
    }

    @Override public void visit (Not parser) {
        register_copy(parser, new Not(get_copy(parser.child)));
    }

    @Override public void visit (Optional parser) {
        register_copy(parser, new Optional(get_copy(parser.child)));
    }

    @Override public void visit (GuardedRecursion parser) {
        register_copy(parser, new GuardedRecursion(get_copy(parser.child)));
    }

    // ---------------------------------------------------------------------------------------------

    @Override public void visit (Around parser)
    {
        register_copy(parser, new Around(parser.min, parser.exact, parser.trailing,
            get_copy(parser.around), get_copy(parser.inside)));
    }

    @Override public void visit (Bounded parser)
    {
        register_copy(parser,
            new Bounded(get_copy(parser.coarse), get_copy(parser.fine), parser.fallback));
    }

    @Override public void visit (Collect parser)
    {
        register_copy(parser, new Collect(parser.name, get_copy(parser.child),
            parser.lookback, parser.action_on_fail, parser.pop, parser.action));
    }

    @Override public void visit (LazyParser parser)
    {
        register_copy(parser, new LazyParser(parser::child));
    }

    @Override public void visit (LeftExpression parser)
    {
        Parser[] infixes  = map(parser.infixes,  witness, this::get_copy);
        Parser[] suffixes = map(parser.suffixes, witness, this::get_copy);

        register_copy(parser, new LeftExpression(
            get_copy(parser.left),
            parser.right != null ? get_copy(parser.right) : null,
            infixes,  parser.infix_steps,
            suffixes, parser.suffix_steps,
            parser.operator_required));
    }

    @Override public void visit (RightExpression parser)
    {
        Parser[] infixes  = map(parser.infixes,  witness, this::get_copy);
        Parser[] prefixes = map(parser.prefixes, witness, this::get_copy);

        register_copy(parser, new RightExpression(
            parser.left != null ? get_copy(parser.left) : null,
            get_copy(parser.right),
            infixes,  parser.infix_steps,
            prefixes, parser.prefix_steps,
            parser.operator_required));
    }

    @Override public void visit (LeftRecursive parser)
    {
        register_copy(parser,
            new LeftRecursive(get_copy(parser.child), parser.left_associative));
    }

    @Override public void visit (Memo parser)
    {
        register_copy(parser,
            new Memo(get_copy(parser.child), parser.memoizer, parser.context_extractor));
    }

    @Override public void visit (Repeat parser)
    {
        register_copy(parser,
            new Repeat(parser. min, parser.exact, get_copy(parser.child)));
    }

    @Override public void visit (TokenChoice parser)
    {
        register_copy(parser,
            new TokenChoice(parser.tokens, map(parser.targets, witness, this::get_copy)));
    }

    @Override public void visit (TokenParser parser)
    {
        register_copy(parser,
            new TokenParser(parser.tokens, get_copy(parser.target)));
    }

    @Override public void visit (StringMatch parser)
    {
        register_copy(parser,
            new StringMatch(parser.string, get_copy(parser.whitespace)));
    }


    @Override public void visit (TrailingWhitespace parser)
    {
        register_copy(parser,
            new TrailingWhitespace(get_copy(parser.child), get_copy(parser.whitespace)));
    }

    // ---------------------------------------------------------------------------------------------
}
