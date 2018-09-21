package norswap.autumn;

import norswap.autumn.parsers.*;
import norswap.utils.NArrays;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class implements a DSL (Domain Specific Language) for creating parsers. It's just
 * a nicer API than having to piece together parser constructor.
 *
 * <p>This class features methods that return a {@link Wrapper} object wrapping a parser.
 * methods can be called on this wrapper to create further wrappers. e.g.:
 *
 * <pre>
 * {@code
 * Parser arith = digit().at_least(1).sep(1, choice("+", "-")).get();
 * }
 * </pre>
 *
 * <p><b>Automatic conversion:</b> Most DSL methods take instances of {@code Object} instead of
 * {@link Parser}. Parsers passed like this are simply passed through. Parsers are extracted out
 * of {@link Wrapper} instances, and {@code String} instances are replaced by calling {@link #word}
 * with the string.
 *
 * <p><b>Whitespace handling:</b> set {@link #ws} to skip whitespace after matching certain parser
 * (most importantly, when using {@link #word}.
 *
 * <p>To use the DSL, create a class that extends this class (recommended). It's also possible
 * to instantiate this class and to call methods on it.
 */
public class DSL
{
    // ---------------------------------------------------------------------------------------------
    
    private int anonymous_counter = 0;

    // ---------------------------------------------------------------------------------------------

    private ArrayList<Parser> token_base_parsers = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * Change this to specify the whitespace parser used for {@link #word} and used after
     * automatically converted string literals.
     *
     * <p>This parser <b>must</b> always succeed, meaning it must be able to succeed matching
     * the empty string.
     *
     * <p>null by default, meaning no whitespace will be matched.
     */
    public Parser ws = null;

    // ---------------------------------------------------------------------------------------------

    public final Tokens tokens = new Tokens();

    // ---------------------------------------------------------------------------------------------

    public DSL()
    {
        tokens.parsers = token_base_parsers.toArray(new Parser[0]);
    }

    // ---------------------------------------------------------------------------------------------

    public void build_tokenizer()
    {
        for (Parser parser: token_base_parsers)
            parser.exclude_error = true;
        tokens.parsers = token_base_parsers.toArray(new Parser[0]);
    }

    // ---------------------------------------------------------------------------------------------

    private String anoname() {
        return "<anon" + ++anonymous_counter + ">";
    }
    
    // ---------------------------------------------------------------------------------------------

    private Parser compile (Object item)
    {
        if (item instanceof Wrapper)
            return ((Wrapper) item).get();

        if (item instanceof Parser)
            return (Parser) item;

        if (item instanceof String)
            return new StringMatch((String) item, ws);

        throw new Error("unknown item type " + item.getClass());
    }
    
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Sequence} of the given parsers.
     */
    public Wrapper seq (Object... parsers) {
        return new Wrapper(new Sequence(NArrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Choice} between the given parsers.
     */
    public Wrapper choice (Object... parsers) {
        return new Wrapper(new Choice(NArrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Longest} match choice between the given parsers.
     */
    public Wrapper longest (Object... parsers) {
        return new Wrapper(new Longest(NArrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link StringMatch} parser for the given string.
     */
    public Wrapper str (String string) {
        return new Wrapper(new StringMatch(string, null));
    }

    // ---------------------------------------------------------------------------------------------˜

    /**
     * Returns a {@link StringMatch} parser with post whitespace matching dependent on {@link
     * #ws}.
     */
    public Wrapper word (String string) {
        return new Wrapper(new StringMatch(string, ws));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} parser that matches any character.
     */
    public Wrapper any = new Wrapper(CharPredicate.any());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single ASCII alphabetic character.
     */
    public Wrapper alpha = new Wrapper(CharPredicate.alpha());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single ASCII alpha-numeric character.
     */
    public Wrapper alphanum = new Wrapper(CharPredicate.alphanum());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single decimal digit.
     */
    public Wrapper digit = new Wrapper(CharPredicate.digit());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single hexadecimal digit (for letters, both
     * the lowercase and uppercase forms are allowed).
     */
    public Wrapper hex_digit = new Wrapper(CharPredicate.hex_digit());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single octal digit.
     */
    public Wrapper octal_digit = new Wrapper(CharPredicate.octal_digit());

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches an (inclusive) range of characters.
     */
    public Wrapper range (char start, char end) {
        return new Wrapper(CharPredicate.range(start, end));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches a set of characters.
     */
    public Wrapper set (String string) {
        return new Wrapper(CharPredicate.set(string));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches a set of characters.
     */
    public Wrapper set (char... chars) {
        return new Wrapper(CharPredicate.set(chars));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser with an automatically generated anonymous name.
     */
    public Wrapper cpred (IntPredicate predicate) {
        return new Wrapper(new CharPredicate(anoname(), predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an {@link ObjectPredicate} parser with an automatically generated anonymous name.
     */
    public Wrapper opred (Predicate<Object> predicate) {
        return new Wrapper(new ObjectPredicate(anoname(), predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser} using the given supplier.
     */
    public Wrapper lazy_parser (Supplier<Parser> supplier) {
        return new Wrapper(new LazyParser(supplier));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser} using the given supplier.
     */
    public Wrapper lazy (Supplier<Wrapper> supplier) {
        return new Wrapper(new LazyParser(() -> supplier.get().parser));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that allows left-only matches.
     */
    public Wrapper left (Object left, Object operator, Object right,
                         BiConsumer<Parse, Object[]> step) {
        return new Wrapper(
            new LeftAssoc(compile(left), compile(operator), compile(right), false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that does not allow left-only matches.
     */
    public Wrapper left_full (Object left, Object operator, Object right,
                              BiConsumer<Parse, Object[]> step) {
        return new Wrapper(
            new LeftAssoc(compile(left), compile(operator), compile(right), true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenChoice} parser that select between the passed token parsers or
     * base token parsers.
     */
    public Wrapper token_choice (Object... parsers)
    {
        int[] targets = new int[parsers.length];

        for (int i = 0; i < parsers.length; ++i)
        {
            if (parsers[i] instanceof String)
                throw new Error(
                    "Token choice requires exact parser reference and does not work with automatic string conversion.");

            Parser parser = compile(parsers[i]);

            if (parser instanceof TokenParser)
                parser = token_base_parsers.get(((TokenParser) parser).target_index);

            int j = token_base_parsers.indexOf(parser);

            if (j < 0)
                throw new Error("Unknown base token parser.");

            targets[i] = j;
        }

        return new Wrapper(new TokenChoice(tokens, targets));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Wraps a {@link Parser} to enable DSL-style construction parser construction.
     *
     * <p>Extract the parser using {@link #get()}.
     */
    public final class Wrapper
    {
        private final Parser parser;

        private Wrapper (Parser parser) {
            this.parser = parser;
        }

        /**
         * Returns this wrapper, after setting the name of the parser to the given name. Only works
         * for parsers with a name property: {@link Collect}, {@link CharPredicate} and {@link
         * ObjectPredicate}.
         */
        public Wrapper named (String name)
        {
            /**/ if (parser instanceof Collect)
                ((Collect) parser).name = name;
            else if (parser instanceof CharPredicate)
                ((CharPredicate) parser).name = name;
            else if (parser instanceof ObjectPredicate)
            ((ObjectPredicate) parser).name = name;
            else
                throw new Error("Wrapped parser doesn't have a name property.");

            return this;
        }

        /**
         * Returns the wrapped parser.
         */
        public Parser get() {
            return parser;
        }

        /**
         * Returns a negation ({@link Not}) of the parser.
         */
        public Wrapper not() {
            return new Wrapper(new Not(parser));
        }

        /**
         * Returns a lookahead version ({@link Lookahead}) of the parser.
         */
        public Wrapper ahead() {
            return new Wrapper(new Lookahead(parser));
        }

        /**
         * Returns an optional version ({@link Optional}) of the parser.
         */
        public Wrapper opt() {
            return new Wrapper(new Optional(parser));
        }

        /**
         * Returns a repetition ({@link Repeat}) of exactly {@code n} times the parser.
         */
        public Wrapper repeat (int n) {
            return new Wrapper(new Repeat(n, true, parser));
        }

        /**
         * Returns a repetition ({@link Repeat}) of at least {@code min} times the parser.
         */
        public Wrapper at_least (int min) {
            return new Wrapper(new Repeat(min, false, parser));
        }

        /**
         * Returns an {@link Around} parser that matches at least {@code min} repetition
         * of the parser, separated by the {@code separator} parser.
         */
        public Wrapper sep (int min, Object separator) {
            return new Wrapper(new Around(min, false, false, parser, compile(separator)));
        }

        /**
         * Returns an {@link Around} parser that matches exactly {@code n} repetition
         * of the parser, separated by the {@code separator} parser.
         */
        public Wrapper sep_exact (int n, Object separator) {
            return new Wrapper(new Around(n, true, false, parser, compile(separator)));
        }

        /**
         * Returns an {@link Around} parser that matches at least {@code min} repetition of the
         * parser, separated by the {@code separator} parser, and allowing for a trailing separator.
         */
        public Wrapper sep_trailing (int min, Object separator) {
            return new Wrapper(new Around(min, false, true, parser, compile(separator)));
        }

        /**
         * Returns a {@link LeftAssoc} parser that matches a postfix expression (the right-hand
         * side matches the empty string). Allows left-only matches.
         */
        public Wrapper postfix (Object operator, BiConsumer<Parse, Object[]> step) {
            return new Wrapper(
                new LeftAssoc(parser, compile(operator), new StringMatch("", null), false, step));
        }

        /**
         * Returns a {@link LeftAssoc} parser that matches a postfix expression (the right-hand
         * side matches the empty string). Does not allow left-only matches.
         */
        public Wrapper postfix_full (Object operator, BiConsumer<Parse, Object[]> step) {
            return new Wrapper(
                new LeftAssoc(parser, compile(operator), new StringMatch("", null), true, step));
        }

        /**
         * Returns a {@link Sequence} composed of the parser followed by the whitespace parser
         * {@link #ws}.
         */
        public Wrapper word()
        {
            return new Wrapper(new Sequence(parser, ws));
        }

        /**
         * Returns a new {@link TokenParser} wrapping the parser. The token set must be constructed
         * after all tokens have been declared by calling {@link DSL#build_tokenizer()} in an
         * initializer or constructor.
         */
        public Wrapper token ()
        {
            token_base_parsers.add(parser);
            return new Wrapper(new TokenParser(tokens, token_base_parsers.size() - 1));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser. The returned parser
         * pushes null on the stack if and only if the underlying parser fails. The returned parser
         * always succeeds.
         */
        public Wrapper maybe()
        {
            return new Wrapper(new Collect(anoname(), parser, false, true,
                (Collect.SimpleAction) (p, xs) -> { if (xs == null) p.push(null); }));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser. The returned parser
         * pushes the supplied value on the stack if the underlying parser is successful.
         */
        public Wrapper as_val (Object value)
        {
            return new Wrapper(new Collect(anoname(), parser, false, false,
                (Collect.SimpleAction) (p,xs) -> p.push(value)));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser. The returned parser
         * pushes true or false on the stack depending on whether the underlying parser succeeds or
         * fails. The returned parser always succeeds.
         */
        public Wrapper as_bool()
        {
            return new Wrapper(new Collect(anoname(), new Optional(parser), false, true,
                (Collect.SimpleAction) (p,xs) -> p.push(xs != null)));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper reduce (Collect.SimpleAction action) {
            return new Wrapper(new Collect(anoname(), parser, true, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper reduce_list (Collect.ListAction action) {
            return new Wrapper(new Collect(anoname(), parser, true, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper reduce_str (Collect.StringAction action) {
            return new Wrapper(new Collect(anoname(), parser, true, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper collect (Collect.SimpleAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper collect_list (Collect.ListAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper collect_str (Collect.StringAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper push (PushAction action) {
            return new Wrapper(new Collect(anoname(), parser, true, false, action));
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * This action pushes the return value of its {@link #get} method onto the stack.
     */
    @FunctionalInterface public interface PushAction extends Collect.SimpleAction
    {
        @Override default void apply (Parse parse, Object[] items)
        {
            parse.push(get(parse, items));
        }

        Object get (Parse parse, Object[] items);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a new list wrapping the given array after casting it to to an array of type {@code T}.
     *
     * <p>Use the {@code this.<T>list(array)} form to specify the type {@code T}.
     */
    public <T> List<T> list (Object[] array)
    {
        //noinspection unchecked
        return Arrays.asList((T[]) array);
    }


    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a new empty list of type T.
     */
    public <T> List<T> list ()
    {
        //noinspection unchecked
        return Collections.emptyList();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the given object, casted to type {@code T}.
     *
     * <p>The target type {@code T} can be inferred from the assignment target.
     * e.g. {@code Object x = "hello"; String y = $(x);}
     */
    public <T> T $ (Object object)
    {
        //noinspection unchecked
        return (T) object;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the array item at the given index, casted to type {@code T}.
     *
     * @see #$
     */
    public <T> T $ (Object[] array, int index)
    {
        //noinspection unchecked
        return (T) array[index];
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Fetches all the fields of {@code grammar} (both from its class and subclasses), and for those
     * that are of type {@link Wrapper} or {@link Parser}, sets the rule name to the name of the
     * field, if no rule name has been set already.
     */
    public void make_rule_names (Object grammar)
    {
        try {
            Class<?> klass = grammar.getClass();
            for (Field f : klass.getFields()) {
                if (f.getType().equals(Wrapper.class)) {
                    Wrapper w = (Wrapper) f.get(this);
                    if (w == null) continue;
                    Parser p = w.get();
                    if (p.rule() == null)
                        p.set_rule(f.getName());
                } else if (f.getType().equals(Parser.class)) {
                    Parser p = (Parser) f.get(this);
                    if (p == null) continue;
                    if (p.rule() == null)
                        p.set_rule(f.getName());
                }
            }
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
