package norswap.autumn;

import norswap.autumn.parsers.*;
import norswap.utils.Arrays;
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
 * <p>To use the DSL, create a class that extends this class (recommended). It's also possible
 * to instantiate this class and to call methods on it.
 */
public class DSL
{
    // ---------------------------------------------------------------------------------------------
    
    private int anonymous_counter = 0;
    
    // ---------------------------------------------------------------------------------------------

    /**
     * Override this to specify the whitespace parser used for {@link #word} and used after
     * automatically converted string literals.
     *
     * <p>null by default, meaning no whitespace will be matched.
     */
    public Parser whitespace() {
        return null;
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
            return new StringMatch((String) item, whitespace());

        throw new Error("unknown item type " + item.getClass());
    }
    
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Sequence} of the given parsers.
     */
    public Wrapper seq (Object... parsers) {
        return new Wrapper(new Sequence(Arrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Choice} between the given parsers.
     */
    public Wrapper choice (Object... parsers) {
        return new Wrapper(new Choice(Arrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Longest} match choice between the given parsers.
     */
    public Wrapper longest (Object... parsers) {
        return new Wrapper(new Longest(Arrays.map(parsers, new Parser[0], this::compile)));
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
     * #whitespace()}.
     */
    public Wrapper word (String string) {
        return new Wrapper(new StringMatch(string, whitespace()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches any character.
     */
    public Wrapper any() {
        return new Wrapper(CharPredicate.any());
    }

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
     * Returns a {@link CharPredicate} that matches a single ASCII alphabetic character.
     */
    public Wrapper alpha() {
        return new Wrapper(CharPredicate.alpha());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} that matches a single ASCII alpha-numeric character.
     */
    public Wrapper alphanum() {
        return new Wrapper(CharPredicate.alphanum());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} that matches a single decimal digit.
     */
    public Wrapper digit() {
        return new Wrapper(CharPredicate.digit());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} that matches a single hexadecimal digit (for letters, both
     * the lowercase and uppercase forms are allowed).
     */
    public Wrapper hex_digit() {
        return new Wrapper(CharPredicate.hex_digit());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} that matches a single octal digit.
     */
    public Wrapper octal_digit() {
        return new Wrapper(CharPredicate.octal_digit());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser.
     */
    public Wrapper cpred (String name, IntPredicate predicate) {
        return new Wrapper(new CharPredicate(name, predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser with an automatically generated anonymous name.
     */
    public Wrapper cpred (IntPredicate predicate) {
        return cpred(anoname(), predicate);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an {@link ObjectPredicate} parser.
     */
    public Wrapper opred (String name, Predicate<Object> predicate) {
        return new Wrapper(new ObjectPredicate(name, predicate));
    }
    
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an {@link ObjectPredicate} parser with an automatically generated anonymous name.
     */
    public Wrapper opred (Predicate<Object> predicate) {
        return opred(anoname(), predicate);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser}.
     */
    public Wrapper lazy (String name, Supplier<Parser> supplier) {
        return new Wrapper(new LazyParser(name, supplier));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser} with an automatically generated anonymous name.
     */
    public Wrapper lazy (Supplier<Parser> supplier) {
        return lazy(anoname(), supplier);
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
         * Returns a reducing {@link Collect} parser wrapping the parser.
         */
        public Wrapper reduce (String name, Collect.SimpleAction action) {
            return new Wrapper(new Collect(name, parser, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser.
         */
        public Wrapper reduce_list (String name, Collect.ListAction action) {
            return new Wrapper(new Collect(name, parser, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser.
         */
        public Wrapper reduce_str (String name, Collect.StringAction action) {
            return new Wrapper(new Collect(name, parser, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser.
         */
        public Wrapper collect (String name, Collect.SimpleAction action) {
            return new Wrapper(new Collect(name, parser, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser.
         */
        public Wrapper collect_list (String name, Collect.ListAction action) {
            return new Wrapper(new Collect(name, parser, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser.
         */
        public Wrapper collect_str (String name, Collect.StringAction action) {
            return new Wrapper(new Collect(name, parser, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper reduce (Collect.SimpleAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper reduce_list (Collect.ListAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, action));
        }

        /**
         * Returns a reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper reduce_str (Collect.StringAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper collect (Collect.SimpleAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper collect_list (Collect.ListAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, action));
        }

        /**
         * Returns a non-reducing {@link Collect} parser wrapping the parser, with an automatically
         * generated anonymous name.
         */
        public Wrapper collect_str (Collect.StringAction action) {
            return new Wrapper(new Collect(anoname(), parser, false, action));
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
         * parser, separated by the {@code separator} parser, and allowing for a trailing separator
         * if at least one repetition is matched.
         */
        public Wrapper sep_trailing (int min, Object separator) {
            return new Wrapper(new Around(min, false, true, parser, compile(separator)));
        }
    }

    // ---------------------------------------------------------------------------------------------
}
