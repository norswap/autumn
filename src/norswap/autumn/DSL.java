package norswap.autumn;

import norswap.autumn.parsers.*;
import norswap.utils.NArrays;
import norswap.utils.Slot;
import norswap.utils.Util;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class implements a DSL (Domain Specific Language) for creating parsers. It's just
 * a nicer API than having to piece together parser constructors.
 *
 * <p>This class features methods that return a {@link rule} object wrapping a parser.
 * Methods can be called on this wrapper to create further wrappers. e.g.:
 *
 * <pre>
 * {@code
 * Parser arith = digit().at_least(1).sep(1, choice("+", "-")).get();
 * }
 * </pre>
 *
 * <p><b>Automatic conversion:</b> Most DSL methods take instances of {@code Object} instead of
 * {@link Parser}. Parsers passed like this are simply passed through. Parsers are extracted out
 * of {@link rule} instances, and {@code String} instances are replaced by calling {@link #str}
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

    private final ArrayList<Parser> token_base_parsers = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    private final Tokens tokens = new Tokens();

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

    public DSL()
    {
        tokens.parsers = token_base_parsers.toArray(new Parser[0]);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Finalizes the tokenization parsers that were created via {@link rule#token()}, by fielding
     * an internal {@link Tokens} instance.
     *
     * <p>Must be called after all calls to {@link rule#token()}. When inheriting this class,
     * this will typically be either in an initializer that appears after all the calls, or in a
     * constructor.
     */
    public void build_tokenizer()
    {
        for (Parser parser: token_base_parsers)
            parser.exclude_error = true;
        tokens.parsers = token_base_parsers.toArray(new Parser[0]);
    }
    
    // ---------------------------------------------------------------------------------------------

    private Parser compile (Object item)
    {
        if (item instanceof rule)
            return ((rule) item).get();

        if (item instanceof Parser)
            return (Parser) item;

        if (item instanceof String)
            return new StringMatch((String) item, null);

        throw new Error("unknown item type " + item.getClass());
    }
    
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Sequence} of the given parsers.
     */
    public rule seq (Object... parsers) {
        return new rule(new Sequence(NArrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Choice} between the given parsers.
     */
    public rule choice (Object... parsers) {
        return new rule(new Choice(NArrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link Longest} match choice between the given parsers.
     */
    public rule longest (Object... parsers) {
        return new rule(new Longest(NArrays.map(parsers, new Parser[0], this::compile)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link StringMatch} parser for the given string.
     */
    public rule str (String string) {
        return new rule(new StringMatch(string, null));
    }

    // ---------------------------------------------------------------------------------------------˜

    /**
     * Returns a {@link StringMatch} parser with post whitespace matching dependent on {@link
     * #ws}.
     */
    public rule word (String string) {
        return new rule(new StringMatch(string, ws));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A parser that always succeeds.
     */
    public rule empty = new rule(new Empty());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} parser that matches any character.
     */
    public rule any = new rule(CharPredicate.any());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single ASCII alphabetic character.
     */
    public rule alpha = new rule(CharPredicate.alpha());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single ASCII alpha-numeric character.
     */
    public rule alphanum = new rule(CharPredicate.alphanum());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single decimal digit.
     */
    public rule digit = new rule(CharPredicate.digit());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single hexadecimal digit (for letters, both
     * the lowercase and uppercase forms are allowed).
     */
    public rule hex_digit = new rule(CharPredicate.hex_digit());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single octal digit.
     */
    public rule octal_digit = new rule(CharPredicate.octal_digit());

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches an (inclusive) range of characters.
     */
    public rule range (char start, char end) {
        return new rule(CharPredicate.range(start, end));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches a set of characters.
     */
    public rule set (String string) {
        return new rule(CharPredicate.set(string));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches a set of characters.
     */
    public rule set (char... chars) {
        return new rule(CharPredicate.set(chars));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser with the given name.
     */
    public rule cpred (String name, IntPredicate predicate) {
        return new rule(new CharPredicate(name, predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser with name "cpred".
     */
    public rule cpred (IntPredicate predicate) {
        return new rule(new CharPredicate("cpred", predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an {@link ObjectPredicate} parser with the given name.
     */
    public rule opred (String name, Predicate<Object> predicate) {
        return new rule(new ObjectPredicate(name, predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an {@link ObjectPredicate} parser with name "opred".
     */
    public rule opred (Predicate<Object> predicate) {
        return new rule(new ObjectPredicate("opred", predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser} using the given supplier.
     */
    public rule lazy_parser (Supplier<Parser> supplier) {
        return new rule(new LazyParser(supplier));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser} using the given supplier.
     */
    public rule lazy (Supplier<rule> supplier) {
        return new rule(new LazyParser(() -> supplier.get().parser));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the parser returned by {@code f}, which takes as parameter a {@link LazyParser} able
     * to recursively invoke the parser {@code f} will return, but *not* in left-position.
     */
    public rule recursive_parser (Function<rule, Parser> f)
    {
        Slot<Parser> slot = new Slot<>();
        slot.x = f.apply(new rule(new LazyParser(() -> slot.x)));
        return new rule(slot.x);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the parser returned by {@code f}, which takes as parameter a {@link LazyParser} able
     * to recursively invoke the parser {@code f} will return, but *not* in left position.
     */
    public rule recursive (Function<rule, rule> f)
    {
        return recursive_parser(r -> f.apply(r).get());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the parser returned by {@code f}, which takes as parameter a {@link LazyParser} able
     * to recursively invoke the parser {@code f} will return, including in left position.
     */
    public rule left_recursive_parser (Function<rule, Parser> f) {
        return recursive_parser(r -> new LeftRecursive(f.apply(r)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the parser returned by {@code f}, which takes as parameter a {@link LazyParser} able
     * to recursively invoke the parser {@code f} will return, including in left position.
     */
    public rule left_recursive (Function<rule, rule> f) {
        return recursive_parser(r -> new LeftRecursive(f.apply(r).get()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that allows left-only matches.
     */
    public rule left (Object left, Object operator, Object right, StackAction.Push step) {
        return new rule(
            new LeftAssoc(compile(left), compile(operator), compile(right), false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that allows left-only matches, and with no step
     * action performed.
     */
    public rule left (Object left, Object operator, Object right) {
        return new rule(
            new LeftAssoc(compile(left), compile(operator), compile(right), false, null));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that allows left-only matches, with the same
     * operand on both sides.
     */
    public rule left (Object operand, Object operator, StackAction.Push step) {
        Parser coperand = compile(operand);
        return new rule(new LeftAssoc(coperand, compile(operator), coperand, false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that allows left-only matches, with the same operand
     * on both sides and with no step action performed.
     */
    public rule left (Object operand, Object operator) {
        Parser coperand = compile(operand);
        return new rule(new LeftAssoc(coperand, compile(operator), coperand, false, null));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that does not allow left-only matches.
     */
    public rule left_full (Object left, Object operator, Object right, StackAction.Push step) {
        return new rule(
            new LeftAssoc(compile(left), compile(operator), compile(right), true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that does not allow left-only matches, and with
     * no step action performed.
     */
    public rule left_full (Object left, Object operator, Object right) {
        return new rule(
            new LeftAssoc(compile(left), compile(operator), compile(right), true, null));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that does not allow left-only matches, with the same
     * operand on both sides.
     */
    public rule left_full (Object operand, Object operator, StackAction.Push step) {
        Parser coperand = compile(operand);
        return new rule(new LeftAssoc(coperand, compile(operator), coperand, true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that does not allow left-only matches, with the same
     * operand on both sides and with no step action performed.
     */
    public rule left_full (Object operand, Object operator) {
        Parser coperand = compile(operand);
        return new rule(new LeftAssoc(coperand, compile(operator), coperand, true, null));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link RightAssoc} parser that allows left-only matches.
     */
    public rule right (Object left, Object operator, Object right, StackAction.Push step) {
        return new rule(
            new RightAssoc(compile(left), compile(operator), compile(right), false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that allows left-only matches, with the same
     * operand on both sides.
     */
    public rule right (Object operand, Object operator, StackAction.Push step) {
        Parser coperand = compile(operand);
        return new rule(new RightAssoc(coperand, compile(operator), coperand, false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link RightAssoc} parser that does not allow left-only matches.
     */
    public rule right_full (Object left, Object operator, Object right, StackAction.Push step) {
        return new rule(
            new RightAssoc(compile(left), compile(operator), compile(right), true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link RightAssoc} parser that does not allow left-only matches, with the same
     * operand on both sides.
     */
    public rule right_full (Object operand, Object operator, StackAction.Push step) {
        Parser coperand = compile(operand);
        return new rule(new RightAssoc(coperand, compile(operator), coperand, true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that matches a postfix expression (the right-hand
     * side matches nothing). Allows left-only matches.
     */
    public rule postfix (Object operand, Object operator, StackAction.Push step) {
        return new rule(
            new LeftAssoc(compile(operand), compile(operator), empty.get(), false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LeftAssoc} parser that matches a postfix expression (the right-hand
     * side matches nothing). Does not allow left-only matches.
     */
    public rule postfix_full (Object operand, Object operator, StackAction.Push step) {
        return new rule(
            new LeftAssoc(compile(operand), compile(operator), empty.get(), true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link RightAssoc} parser that matches a prefix expression (the left-hand
     * side matches nothing). Allows right-only matches.
     */
    public rule prefix (Object operator, Object operand, StackAction.Push step) {
        return new rule(
            new RightAssoc(empty.get(), compile(operand), compile(operator), false, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link RightAssoc} parser that matches a prefix expression (the left-hand
     * side matches nothing). Does not allow right-only matches.
     */
    public rule prefix_full (Object operator, Object operand, StackAction.Push.Push step) {
        return new rule(
            new RightAssoc(empty.get(), compile(operand), compile(operator), true, step));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenChoice} parser that select between the passed token parsers or
     * base token parsers.
     */
    public rule token_choice (Object... parsers)
    {
        int[] targets = new int[parsers.length];

        for (int i = 0; i < parsers.length; ++i)
        {
            if (parsers[i] instanceof String)
                throw new Error("Token choice requires exact parser reference and does not work "
                    + "with automatic string conversion. String:" + parsers[i]);

            Parser parser = compile(parsers[i]);

            if (parser instanceof TokenParser)
                parser = token_base_parsers.get(((TokenParser) parser).target_index);

            int j = token_base_parsers.indexOf(parser);

            if (j < 0)
                throw new Error("Unknown base token parser: " + parser);

            targets[i] = j;
        }

        return new rule(new TokenChoice(tokens, targets));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Wraps a {@link Parser} to enable DSL-style construction parser construction.
     *
     * <p>Functionally, this is a parser wrapper, but it is called "rule" to prettify grammar
     * definitions (where each rule is a field declaration whose type is "rule").
     *
     * <p>Extract the parser using {@link #get()}.
     */
    public final class rule
    {
        private final Parser parser;
        private final int lookback;
        private final boolean peek_only;
        private final boolean collect_on_fail;

        private rule (Parser parser) {
            this.parser = parser;
            this.lookback = 0;
            this.peek_only = false;
            this.collect_on_fail = false;
        }

        private rule (Parser parser, int lookback, boolean peek_only, boolean collect_on_fail) {
            this.parser = parser;
            this.lookback = lookback;
            this.peek_only = peek_only;
            this.collect_on_fail = collect_on_fail;
        }

        private rule make (Parser parser)
        {
            if (lookback != 0)
                throw new IllegalStateException("You're trying to create a new rule wrapper from "
                    + "a rule wrapper on which you defined a lookback, without specifying a "
                    + "corresponding collect action. Wrapper holds: " + this);

            if (peek_only)
                throw new IllegalStateException("You're trying to create a new rule wrapper from "
                    + "a rule wrapper on which you defined the peek_only property, without "
                    + "specifying a corresponding collect action. Wrapper holds: " + this);

            return new rule(parser);
        }

        /**
         * Returns this wrapper, after setting the name of the parser to the given name. Only works
         * for parsers with a name property: {@link Collect}, {@link CharPredicate} and {@link
         * ObjectPredicate}.
         */
        public rule named (String name)
        {
            /**/ if (parser instanceof Collect)
                ((Collect) parser).name = name;
            else if (parser instanceof CharPredicate)
                ((CharPredicate) parser).name = name;
            else if (parser instanceof ObjectPredicate)
            ((ObjectPredicate) parser).name = name;
            else
                throw new Error("Wrapped parser doesn't have a name property: " + this);

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
        public rule not() {
            return make(new Not(parser));
        }

        /**
         * Returns a lookahead version ({@link Lookahead}) of the parser.
         */
        public rule ahead() {
            return make(new Lookahead(parser));
        }

        /**
         * Returns an optional version ({@link Optional}) of the parser.
         */
        public rule opt() {
            return make(new Optional(parser));
        }

        /**
         * Returns a repetition ({@link Repeat}) of exactly {@code n} times the parser.
         */
        public rule repeat (int n) {
            return make(new Repeat(n, true, parser));
        }

        /**
         * Returns a repetition ({@link Repeat}) of at least {@code min} times the parser.
         */
        public rule at_least (int min) {
            return make(new Repeat(min, false, parser));
        }

        /**
         * Returns an {@link Around} parser that matches at least {@code min} repetition
         * of the parser, separated by the {@code separator} parser.
         */
        public rule sep (int min, Object separator) {
            return make(new Around(min, false, false, parser, compile(separator)));
        }

        /**
         * Returns an {@link Around} parser that matches exactly {@code n} repetition
         * of the parser, separated by the {@code separator} parser.
         */
        public rule sep_exact (int n, Object separator) {
            return make(new Around(n, true, false, parser, compile(separator)));
        }

        /**
         * Returns an {@link Around} parser that matches at least {@code min} repetition of the
         * parser, separated by the {@code separator} parser, and allowing for a trailing separator.
         */
        public rule sep_trailing (int min, Object separator) {
            return make(new Around(min, false, true, parser, compile(separator)));
        }

        /**
         * Returns a {@link LeftRecursive} parser that wraps the parser, whose left-recursion must
         * be reached through a {@link LazyParser} reference to the parser returned by this
         * method.
         */
        public rule left_recursive()
        {
            return make(new LeftRecursive(parser));
        }

        /**
         * Returns a {@link Sequence} composed of the parser followed by the whitespace parser
         * {@link #ws}.
         */
        public rule word()
        {
            return make(new Sequence(parser, ws));
        }

        /**
         * Returns a new {@link TokenParser} wrapping the parser. The token set must be constructed
         * after all tokens have been declared by calling {@link DSL#build_tokenizer()} in an
         * initializer or constructor.
         */
        public rule token()
        {
            token_base_parsers.add(parser);
            return make(new TokenParser(tokens, token_base_parsers.size() - 1));
        }

        /**
         * Returns a peek-only {@link Collect} parser wrapping the parser. The returned parser
         * pushes null on the stack if and only if the underlying parser fails. The returned parser
         * always succeeds.
         *
         * <p>The collect flags {@link #lookback(int)}, {@link #peek_only()} and {@link
         * #collect_on_fail()} may not be set when calling this method.
         */
        public rule maybe()
        {
            return make(new Collect("maybe", parser, 0, true, false,
                (p, xs) -> { if (xs == null) p.push(null); }));
        }

        /**
         * Returns a peek-only {@link Collect} parser wrapping the parser. The returned parser
         * pushes the supplied value on the stack if the underlying parser is successful.
         *
         * <p>The collect flags {@link #lookback(int)}, {@link #peek_only()} and {@link
         * #collect_on_fail()} may not be set when calling this method.
         */
        public rule as_val (Object value)
        {
            return make(new Collect("as_val", parser, 0, false, false,
                (StackAction.Push) (p,xs) -> value));
        }

        /**
         * Returns a peek-only {@link Collect} parser wrapping the parser. The returned parser
         * pushes true or false on the stack depending on whether the underlying parser succeeds or
         * fails. The returned parser always succeeds.
         *
         * <p>The collect flags {@link #lookback(int)}, {@link #peek_only()} and {@link
         * #collect_on_fail()} may not be set when calling this method.
         */
        public rule as_bool()
        {
            return make(new Collect("as_bool", new Optional(parser), 0, true, false,
                (StackAction.Push) (p,xs) -> xs != null));
        }

        /**
         * Pre-defines the {@link Collect#lookback} lookback parameter for a {@link Collect} parser.
         * Once this parameter is set, the only parser that this rule wrapper can be used to build
         * is a {@link Collect} parser.
         */
        public rule lookback (int lookback)
        {
            if (this.lookback != 0) throw new IllegalStateException(
                "Trying to redefine the lookback on rule wrapper holding: " + this);

            return new rule(this.parser, lookback, this.peek_only, this.collect_on_fail);
        }

        /**
         * Pre-defines the {@link Collect#pop} parameter for a {@link Collect} parser to be
         * false. Once this parameter is set, the only parser that this rule wrapper can be used to
         * build is a {@link Collect} parser.
         */
        public rule peek_only()
        {
            if (peek_only) throw new IllegalStateException(
                "Attempting to set the peek_only property twice on rule wrapper holding: " + this);

            return new rule(this.parser, lookback, true, this.collect_on_fail);
        }

        /**
         * Pre-defines the {@link Collect#action_on_fail} parameter for a {@link Collect} parser to
         * be true. Once this parameter is set, the only parser that this rule wrapper can be used
         * to build is a {@link Collect} parser.
         */
        public rule collect_on_fail()
        {
            if (collect_on_fail) throw new IllegalStateException(
                    "Attempting to set the collect_on_fail property twice on rule wrapper holding: "
                    + this);

            return new rule(this.parser, lookback, this.peek_only, true);
        }

        /**
         * Returns a {@link Collect} parser wrapping the parser. By default: has no lookback, pops
         * the items off the stack on success and does nothing in case of failure. Can be modified
         * by {@link #peek_only()}, {@link #lookback(int)} and {@link #collect_on_fail()}.
         */
        public rule collect (StackAction action) {
            return new rule(new Collect("collect", parser, lookback, collect_on_fail,
                !peek_only, action));
        }

        /**
         * Returns a {@link Collect} parser wrapping the parser. By default: has no lookback, pops
         * the items off the stack on success and does nothing in case of failure. Can be modified
         * by {@link #peek_only()}, {@link #lookback(int)} and {@link #collect_on_fail()}.
         */
        public rule push (StackAction.Push action) {
            return new rule(new Collect("push", parser, lookback, collect_on_fail,
                !peek_only, action));
        }

        /**
         * Returns a {@link Collect} parser wrapping the parser. The action consists of pushing a
         * list of all collected items onto the stack, casted to the type denoted by {@code klass}.
         * By default: has no lookback, pops the items off the stack on success and does nothing in
         * case of failure. Can be modified by {@link #peek_only()}, {@link #lookback(int)} and
         * {@link #collect_on_fail()}.
         */
        public <T> rule as_list(Class<T> klass) {
            return new rule(new Collect("as_list", parser, lookback, collect_on_fail, !peek_only,
                (StackAction.Push) (p,xs) -> Arrays.asList(Util.<T[]>cast(xs))));
        }

        @Override public String toString() {
            return parser.toString();
        }
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
     * Returns a new list wrapping the slice {@code [start, length[} of {@code array} after casting
     * it to to an array of type {@code T}.
     *
     * <p>Use the {@code this.<T>list(array)} form to specify the type {@code T}.
     */
    public <T> List<T> list (int start, Object[] array)
    {
        //noinspection unchecked
        return Arrays.asList(Arrays.copyOfRange((T[]) array, start, array.length));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a new list wrapping the slice {@code [start, end[} of {@code array} after casting it
     * to to an array of type {@code T}.
     *
     * <p>Use the {@code this.<T>list(array)} form to specify the type {@code T}.
     */
    public <T> List<T> list (int start, int end, Object[] array)
    {
        //noinspection unchecked
        return Arrays.asList(Arrays.copyOfRange((T[]) array, start, end));
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
     * that are of type {@link rule} or {@link Parser}, sets the rule name to the name of the
     * field, if no rule name has been set already.
     */
    public void make_rule_names (Object grammar)
    {
        make_rule_names(DSL.class.getFields());
        make_rule_names(grammar.getClass().getDeclaredFields());
    }

    // ---------------------------------------------------------------------------------------------

    private void make_rule_names (Field[] fields)
    {
        try {
            for (Field f : fields) {
                if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible())
                    f.setAccessible(true);

                if (f.getType().equals(rule.class)) {
                    rule w = (rule) f.get(this);
                    if (w == null) continue;
                    Parser p = w.get();
                    if (p.rule() == null)
                        p.set_rule(f.getName());
                }
                else if (f.getType().equals(Parser.class)) {
                    Parser p = (Parser) f.get(this);
                    if (p == null) continue;
                    if (p.rule() == null)
                        p.set_rule(f.getName());
                }
            }
        }
        // Should always be a security exception: illegal access prevented by `setAccessible`.
        catch (SecurityException e) {
            throw new RuntimeException(
                "The security policy does not allow Autumn to access private or protect fields "
                    + "in the grammar. Either make all the fields containing grammar rules public, "
                    + "or amend the security policy by granting: "
                    + "permission java.lang.reflect.ReflectPermission \"suppressAccessChecks\";", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------------------------------------------------------------------------------------
}
