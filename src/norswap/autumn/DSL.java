package norswap.autumn;

import norswap.autumn.actions.*;
import norswap.autumn.memo.*;
import norswap.autumn.parsers.*;
import norswap.utils.NArrays;
import norswap.utils.data.wrappers.Slot;
import norswap.utils.reflection.Subtyping;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class implements a domain specific language (DSL) for creating parsers. It's just
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
 * <p><b>Usage:</b> To use the DSL, create a class (the <b>grammar class</b>) that extends this class
 * (recommended). It's also possible to instantiate this class and to call methods on it.
 *
 * <p><b>Automatic conversion:</b> Most DSL methods take instances of {@code Object} instead of
 * {@link Parser}. Parsers passed like this are simply passed through. Parsers are extracted out
 * of {@link rule} instances, and {@code String} instances are replaced by calling {@link #str}
 * with the string.
 *
 * <p><b>Whitespace handling:</b> set {@link #ws} to skip whitespace after matching certain parser
 * (most importantly, when using {@link #word}).
 */
public class DSL
{
    // =============================================================================================
    // region [Collect Options]
    // =============================================================================================

    /**
     * Type of options to build {@link Collect} parsers, to be passed to {@link rule#collect}.
     * @see #PEEK_ONLY
     * @see #ACTION_ON_FAIL
     * @see #LOOKBACK(int)
     */
    public static class CollectOption {
        private final int lookback;
        private CollectOption(int lookback) {
            this.lookback = lookback;
        }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that the items pushed on the stack by the child parser should not be popped
     * off the stack before calling the action (the items pushed on the stack by the child are
     * still passed as an array to the action, however).
     */
    public static final CollectOption PEEK_ONLY = new CollectOption(0);

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that the {@link Collect} parser should also be run even if its child parser
     * fails (meaning it always succeeds).
     */
    public static final CollectOption ACTION_ON_FAIL = new CollectOption(0);

    // ---------------------------------------------------------------------------------------------

    /**
     * Indicates that the {@link Collect} sparser hould apply the given lookback before calling the
     * action (i.e. pass (and potentially pop) this many more items from the stack (compared to the
     * amount of items pushed by child parser) to the action).
     */
    public static CollectOption LOOKBACK(int lookback)  {
        return new CollectOption(lookback);
    }

    // endregion
    // =============================================================================================
    // region [Public Fields and Constructors]
    // =============================================================================================

    /**
     * The token factory used by the grammar.
     */
    public final Tokens tokens;

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance using the default memoization strategy for tokens (currently: an
     * 8-slot cache).
     */
    public DSL() {
        this.tokens = new Tokens(() -> new MemoCache(8, false));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Creates a new instance using a custom memoization strategy for tokens.
     */
    public DSL (Supplier<Memoizer> tokenMemo) {
        this.tokens = new Tokens(tokenMemo);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Change this to specify the whitespace parser used for {@link #word}, {@link rule#word},
     * {@link rule#token} and used after automatically converted string literals.
     *
     * <p>This parser may succeed or fail if there is no whitespace to be matched. When used,
     * whitespace will always be parsed optionally.
     *
     * <p>null by default, meaning no whitespace will be matched by {@link #word}, {@link rule#word}
     * and automatically converted string literals.
     *
     * <p>Both {@link #word} and {@link rule#word} capture the value of this field when called, so
     * setting the value of this field should be one of the first thing you do in your grammar.
     *
     * <p>If {@link #excludeWhitespaceErrors} is set, its {@link Parser#excludeErrors} field will be
     * automatically set as long as {@link #word(String)} or {@link rule#word()} is called at least
     * once (otherwise you'll have to set it yourself if you use {@code ws} explicitly).
     */
    public rule ws = null;

    // ---------------------------------------------------------------------------------------------

    private Parser ws() {
        if (ws == null)
            return empty.getParser();
        Parser p = ws.getParser();
        if (!p.excludeErrors && excludeWhitespaceErrors)
            p.excludeErrors = true;
        return p;
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to exclude errors inside whitespace ({@link #ws}) from counting against the furthest
     * parse error ({@link Parse#error}). True by default.
     */
    public boolean excludeWhitespaceErrors = true;

    // endregion
    // =============================================================================================
    // region [Conversions]
    // =============================================================================================

    private Parser compile (Object item)
    {
        if (item instanceof rule)
            return ((rule) item).getParser();

        if (item instanceof Parser)
            return (Parser) item;

        if (item instanceof String)
            return new StringMatch((String) item, null);

        if (item instanceof Character)
            return CharPredicate.single((char) item);

        throw new Error("unknown item type " + item.getClass());
    }

    /**
     * Wraps the given parser into a {@link rule}.
     */
    public rule rule (Parser parser) {
        return new rule(parser);
    }

    // endregion
    // =============================================================================================
    // region [Rule Naming]
    // =============================================================================================

    /**
     * Fetches all the fields declared in the class of this object (i.e. {@code this.getClass()}),
     * and for those that are of type {@link rule} or {@link Parser}, sets the rule name to the name
     * of the field, if no rule name has been set already.
     */
    public void makeRuleNames()
    {
        makeRuleNames(this.getClass());
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Fetches all the fields declared in {@code klass}, and for those that are of type {@link rule}
     * or {@link Parser}, sets the rule name to the name of the field, if no rule name has been set
     * already.
     */
    public void makeRuleNames (Class<?> klass)
    {
        makeRuleNames(DSL.class.getFields());
        makeRuleNames(klass.getDeclaredFields());
    }

    // ---------------------------------------------------------------------------------------------

    // Note: supresses warning on `f.isAccessible()` deprecated after Java 8 in favor of
    // `f.canAccess(this)`. Language level 8 with a later JDK will yield a warning while we
    // can't use `canAccess` yet.
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private void makeRuleNames (Field[] fields)
    {
        try {
            for (Field f : fields) {
                if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible())
                    f.setAccessible(true);

                if (Subtyping.check(f.getType(), rule.class)) {
                    rule w = (rule) f.get(this);
                    if (w == null) continue;
                    Parser p = w.getParser();
                    if (p.rule() == null)
                        p.setRule(f.getName());
                }
                else if (Subtyping.check(f.getType(), Parser.class)) {
                    Parser p = (Parser) f.get(this);
                    if (p == null) continue;
                    if (p.rule() == null)
                        p.setRule(f.getName());
                }
            }
        }
        // Should always be a security exception: illegal access prevented by `setAccessible`.
        catch (SecurityException e) {
            throw new RuntimeException(
                "The security policy does not allow Autumn to access private or protected fields "
                    + "in the grammar. Either make all the fields containing grammar rules public, "
                    + "or amend the security policy by granting: "
                    + "permission java.lang.reflect.ReflectPermission \"suppressAccessChecks\";", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // endregion
    // =============================================================================================
    // region [Pre-Defined Rules]
    // =============================================================================================

    /**
     * A parser that always succeeds.
     */
    public rule empty = new rule(new Empty());

    // ---------------------------------------------------------------------------------------------

    /**
     * A parser that always fails.
     */
    public rule fail = new rule(new Fail());

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
    public rule hex_digit = new rule(CharPredicate.hexDigit());

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single octal digit.
     */
    public rule octal_digit = new rule(CharPredicate.octalDigit());

    // ---------------------------------------------------------------------------------------------

    /**
     * A rule that matches zero or more of the usual whitespace characters (spaces, tabs (\t), line
     * return (\n) and carriage feed (\r)). Fit to be assigned to {@link #ws}.
     */
    public rule usual_whitespace = set(" \t\n\r").at_least(0);

    // endregion
    // =============================================================================================
    // region [Simple Parsers]
    // =============================================================================================

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

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link StringMatch} parser with post whitespace matching dependent on {@link
     * #ws}.
     */
    public rule word (String string) {
        return new rule(new StringMatch(string, ws()));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link TokenParser} wrapping a {@link StringMatch} parser with post whitespace
     * matching dependent on {@link #ws}.
     *
     * <p>Less verbose equivalent to {@code word(string).token()}.
     */
    public rule token (String string) {
        return word(string).token();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * A {@link CharPredicate} that matches a single character.
     */
    public rule character (int character) {
        return new rule(CharPredicate.single(character));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link CharPredicate} parser that matches an (inclusive) range of characters.
     */
    public rule range (int start, int end) {
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
    public rule set (int... chars) {
        return new rule(CharPredicate.set(chars));
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
     * Returns an {@link ObjectPredicate} parser with name "opred".
     */
    public rule opred (Predicate<Object> predicate) {
        return new rule(new ObjectPredicate("opred", predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link ContextPredicate} parsed with name "context".
     */
    public rule context (Predicate<Parse> predicate) {
        return new rule (new ContextPredicate("context", predicate));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a negation ({@link Not}) of the parser.
     *
     * <p>Prefer using {@link rule#not()} if the parser has type {@link rule}.
     */
    public rule not (Object parser) {
        return new rule(new Not(compile(parser)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a lookahead version ({@link Lookahead}) of the parser.
     *
     * <p>Prefer using {@link rule#ahead()} if the parser has type {@link rule}.
     */
    public rule ahead (Object parser) {
        return new rule(new Lookahead(compile(parser)));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns an optional version ({@link Optional}) of the parser.
     *
     * <p>Prefer using {@link rule#opt()} if the parser has type {@link rule}.
     */
    public rule opt (Object parser) {
        return new rule(new Optional(compile(parser)));
    }

    // endregion
    // =============================================================================================
    // region [Token Choices]
    // =============================================================================================

    /**
     * Returns a {@link TokenChoice} parser that selects between the passed token parsers or base
     * token parsers. These tokens must have been defined previously (using {@link rule#token()},
     * <b>lazy references won't work.</b>
     */
    public rule token_choice (Object... parsers)
    {
        Parser[] compiledParsers = new Parser[parsers.length];

        for (int i = 0; i < parsers.length; ++i)
        {
            if (parsers[i] instanceof String)
                throw new Error("Token choice requires exact parser reference and does not work "
                    + "with automatic string conversion. String:" + parsers[i]);

            compiledParsers[i] = compile(parsers[i]);
        }

        return new rule(tokens.tokenChoice(compiledParsers));
    }

    // endregion
    // =============================================================================================
    // region [Expression Parsers]
    // =========================================================================================

    /**
     * Returns a {@link LeftExpressionBuilder} that helps build a {@link LeftExpression} parser.
     */
    public LeftExpressionBuilder left_expression() {
        return new LeftExpressionBuilder();
    }

    // -----------------------------------------------------------------------------------------

    /**
     * Returns a {@link RightExpressionBuilder} that helps build a {@link RightExpression}
     * parser.
     */
    public RightExpressionBuilder right_expression() {
        return new RightExpressionBuilder();
    }

    // endregion
    // =============================================================================================
    // region [Lazy, Recursive and Associative Parsers]
    // =============================================================================================

    /**
     * Returns a {@link LazyParser} using the given supplier. Can be used to build recursive
     * parsers (for <b>left-recursive</b> parsers, use {@link #left_expression()} instead!).
     */
    public rule lazy_parser (Supplier<Parser> supplier) {
        return new rule(new LazyParser(supplier));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a {@link LazyParser} using the given supplier. Can be used to build recursive
     * parsers (for <b>left-recursive</b> parsers, use {@link #left_expression()} instead!).
     */
    public rule lazy (Supplier<rule> supplier) {
        return new rule(new LazyParser(() -> supplier.get().getParser()));
    }

    // endregion
    // =============================================================================================

    /**
     * Wraps a {@link Parser} to enable builder-style parser construction.
     *
     * <p>Functionally, this is a parser wrapper, but it is called "rule" to prettify grammar
     * definitions (where each rule is a field declaration whose type is "rule").
     *
     * <p>Extract the parser using {@link #getParser()}.
     */
    public class rule
    {
        // =========================================================================================
        // region [Public Fields, Constructor & General Methods]
        // =========================================================================================

        /**
         * Parser held by the rule builder - this must always be accessed via {@link #getParser()}!
         *
         * <p>It must also never be modified (instead a new rule must be returned), though this
         * is not final for the sake for {@link ExpressionBuilder}, which only knows when to build
         * its parser when its {@code get()} method is called.
         */
        Parser parser;

        // -----------------------------------------------------------------------------------------

        private rule (Parser parser) {
            this.parser = parser;
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns the DSL instance this rule belongs to.
         */
        public DSL dsl() {
            return DSL.this;
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns this wrapper, after setting the name of the parser to the given name. Only works
         * for parsers with a name property: {@link Collect}, {@link CharPredicate} and {@link
         * ObjectPredicate}.
         */
        public rule named (String name)
        {
            Parser parser = getParser();
            /**/ if (parser instanceof Collect)
                ((Collect) parser).name = name;
            else if (parser instanceof CharPredicate)
                ((CharPredicate) parser).name = name;
            else if (parser instanceof ObjectPredicate)
                ((ObjectPredicate) parser).name = name;
            else if (parser instanceof ContextPredicate)
                ((ContextPredicate) parser).name = name;
            else
                throw new Error("Wrapped parser doesn't have a name property: " + this);

            return this;
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns the wrapped parser.
         */
        public Parser getParser () {
            return parser;
        }

        // -----------------------------------------------------------------------------------------

        @Override public String toString() {
            return getParser().toString();
        }

        // endregion
        // =========================================================================================
        // region [Misc Combinators]
        // =========================================================================================

        /**
         * Returns a negation ({@link Not}) of the parser.
         */
        public rule not() {
            return new rule(new Not(getParser()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a lookahead version ({@link Lookahead}) of the parser.
         */
        public rule ahead() {
            return new rule(new Lookahead(getParser()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns an optional version ({@link Optional}) of the parser.
         */
        public rule opt() {
            return new rule(new Optional(getParser()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a repetition ({@link Repeat}) of exactly {@code n} times the parser.
         */
        public rule repeat (int n) {
            return new rule(new Repeat(n, true, getParser()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a repetition ({@link Repeat}) of at least {@code min} times the parser.
         */
        public rule at_least (int min) {
            return new rule(new Repeat(min, false, getParser()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns an {@link Around} parser that matches at least {@code min} repetition
         * of the parser, separated by the {@code separator} parser.
         */
        public rule sep (int min, Object separator) {
            return new rule(new Around(min, false, false, getParser(), compile(separator)));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns an {@link Around} parser that matches exactly {@code n} repetition
         * of the parser, separated by the {@code separator} parser.
         */
        public rule sep_exact (int n, Object separator) {
            return new rule(new Around(n, true, false, getParser(), compile(separator)));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns an {@link Around} parser that matches at least {@code min} repetition of the
         * parser, separated by the {@code separator} parser, and allowing for a trailing separator.
         */
        public rule sep_trailing (int min, Object separator) {
            return new rule(new Around(min, false, true, getParser(), compile(separator)));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a {@link TrailingWhitespace} parser composed of the parser followed by the
         * whitespace parser {@link #ws}.
         */
        public rule word() {
            return new rule(new TrailingWhitespace(getParser(), ws()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a new {@link TokenParser} wrapping the parser, adding it as a possible token
         * kind. The underlying parser will have its {@link Parser#excludeErrors} flag set to true.
         */
        public rule token() {
            return new rule(tokens.tokenParser(getParser()));
        }

        // -----------------------------------------------------------------------------------------

        public BoundedParserBuilder refine(Object fine) {
            return new BoundedParserBuilder(getParser(), compile(fine));
        }

        // endregion
        // =========================================================================================
        // region [`Collect` parsers]
        // =========================================================================================

        private rule collect (String name, StackAction action, CollectOption... options)
        {
            int lookback = Arrays.stream(options).mapToInt(it -> it.lookback).reduce(0, Math::max);
            boolean actionOnFail = NArrays.contains(options, ACTION_ON_FAIL);
            boolean peekOnly = NArrays.contains(options, PEEK_ONLY);

            return new rule (
                new Collect(name, getParser(), lookback, actionOnFail, !peekOnly, action));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a {@link Collect} parser wrapping the parser, performing a
         * {@link StackAction generic stack action}.
         */
        public rule collect (StackConsumer action, CollectOption... options) {
            return collect("collect", action, options);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a {@link Collect} parser wrapping the parser, performing a {@link StackPush stack
         * push action}: the return value of the action is pushed onto {@link Parse#stack the value
         * stack}.
         */
        public rule push (StackPush action, CollectOption... options) {
            return collect("push", action, options);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a {@link Collect} parser wrapping the parser, performing a {@link StackPredicate
         * stack predicate action}: if the wrapped parser succeed, the predicate is called and the
         * boolean it returns indicate whether the collect parser succeeds or fails.
         */
        public rule filter (StackPredicate pred, CollectOption... options) {
            return collect("filter", pred, options);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a {@link Collect} parser wrapping the parser. The action consists of pushing a
         * list of all collected items onto the stack, casted to the type denoted by {@code klass}.
         */
        public <T> rule as_list (Class<T> klass, CollectOption... options) {
            return collect("as_list",
                (StackPush) $ -> $.<T>$list(),
                options);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a {@link Collect} parser wrapping the parser. The action consists of pushing a
         * copy of the array of all collected items onto the stack, with a type matching
         * the {@code witness} array.
         */
        public <T> rule as_array (T[] witness, CollectOption... options) {
            return collect("as_list",
                (StackPush) $ -> $.$array(witness),
                options);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a peek-only {@link Collect} parser wrapping the parser. The returned parser
         * pushes true or false on the stack depending on whether the underlying parser succeeds or
         * fails. The returned parser always succeeds.
         */
        public rule as_bool()
        {
            return new rule(new Collect("as_bool", new Optional(getParser()), 0, true, false,
                (StackPush) $ -> $.success()));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a peek-only {@link Collect} parser wrapping the parser. The returned parser
         * pushes the supplied value on the stack if the underlying parser is successful.
         */
        public rule as_val (Object value)
        {
            return new rule(new Collect("as_val", getParser(), 0, false, false,
                (StackPush) $ -> value));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a peek-only {@link Collect} parser wrapping the parser. The returned parser
         * pushes null on the stack if and only if the underlying parser fails. The returned parser
         * always succeeds.
         */
        public rule or_push_null()
        {
            return new rule(new Collect("or_push_null", getParser(), 0, true, false,
                (StackConsumer) $ -> { if (!$.success()) $.push(null); }));
        }

        // endregion
        // =========================================================================================
        // region [Memoization]
        // =========================================================================================

        /**
         * Returns a new {@link Memo} parser wrapping the parser. The parse results will be memoized
         * in a {@link MemoTable}.
         */
        public rule memo() {
            return memo((Function<Parse, Object>) null);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a new context-sensitive {@link Memo} parser wrapping the parser. The parse
         * results will be memoized in a {@link MemoTable}. {@code extractor} will be used to
         * extract and compare the relevant context (see {@link Memo} for details).
         */
        public rule memo (Function<Parse, Object> extractor)
        {
            ParseState<Memoizer> memoizer
                = new ParseState<>(new Slot<>(getParser()), () -> new MemoTable(false));

            return new rule(new Memo(getParser(), memoizer, extractor));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a new {@link Memo} parser wrapping the parser. The parse results will be memoized
         * in a {@link MemoCache} with {@code n} slots (must be strictly positive).
         */
        public rule memo (int n) {
            return memo(n, null);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a new context-sensitive {@link Memo} parser wrapping the parser. The parse
         * results will be memoized in a {@link MemoCache} with {@code n} slots (must be strictly
         * positive). {@code extractor} will be used to extract and compare the relevant context
         * (see {@link Memo} for details).
         */
        public rule memo (int n, Function<Parse, Object> extractor)
        {
            if (n <= 0) throw new IllegalArgumentException
                ("A memo cache must have a strictly positive number of entries.");

            ParseState<Memoizer> memoizer
                = new ParseState<>(new Slot<>(getParser()), () -> new MemoCache(n, false));

            return new rule(new Memo(getParser(), memoizer, extractor));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a new {@link Memo} wrapping the parser. The parse results will be memoized using
         * the supplied memoizer. This form is useful when you want to share a single memoizer
         * amongst multiple parsers.
         */
        public rule memo (ParseState<Memoizer> memoizer) {
            return new rule(new Memo(getParser(), memoizer, null));
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Returns a new context-sensitive {@link Memo} wrapping the parser. The parse results will
         * be memoized using the supplied memoizer. This form is useful when you want to share a
         * single memoizer amongst multiple parsers. {@code extractor} will be used to extract and
         * compare the relevant context (see {@link Memo} for details).
         */
        public rule memo (ParseState<Memoizer> memoizer, Function<Parse, Object> extractor) {
            return new rule(new Memo(getParser(), memoizer, extractor));
        }

        // endregion
        // =========================================================================================
    }

    // =============================================================================================
    // region [class ExpressionBuilder]

    /**
     * Base class for {@link LeftExpressionBuilder} and {@link RightExpressionBuilder}.
     */
    public abstract class ExpressionBuilder <Self extends ExpressionBuilder<Self>> extends rule
    {
        // -----------------------------------------------------------------------------------------

        final boolean leftAssociative;
        final boolean requireOperator;
        final Parser left;
        final Parser right;
        final Parser[] infixes;
        final StackAction[] infixSteps;
        final Parser[] affixes;
        final StackAction[] affixSteps;

        // -----------------------------------------------------------------------------------------

        ExpressionBuilder (
            boolean leftAssociative, boolean requireOperator,
            Parser left, Parser right,
            Parser[] infixes, StackAction[] infixSteps,
            Parser[] affixes, StackAction[] affixSteps)
        {
            super(null);
            this.leftAssociative = leftAssociative;
            this.left = left;
            this.right = right;
            this.infixes = infixes;
            this.infixSteps = infixSteps;
            this.affixes = affixes;
            this.affixSteps = affixSteps;
            this.requireOperator = requireOperator;
        }

        // -----------------------------------------------------------------------------------------

        ExpressionBuilder (boolean leftAssociative) {
            this(
                leftAssociative,
                false, null, null,
                new Parser[0], new StackAction[0],
                new Parser[0], new StackAction[0]
            );
        }

        // -----------------------------------------------------------------------------------------

        abstract Self copy (
            boolean requireOtherSide,
            Parser right, Parser left,
            Parser[] infixes, StackAction[] infixSteps,
            Parser[] affixes, StackAction[] affixSteps);

        // -----------------------------------------------------------------------------------------

        /**
         * Define the left and right operand.
         */
        public Self operand (Object op)
        {
            if (this.left != null)
                throw new IllegalStateException("Trying to redefine the left operand.");
            if (this.right != null)
                throw new IllegalStateException("Trying to redefine the right operand.");

            Parser operand = compile(op);            return copy(
                requireOperator, operand, operand, infixes, infixSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define an infix operator, along with the corresponding step action.
         */
        public Self infix (rule op, StackPush step)
        {
            Parser[] ops = NArrays.append(this.infixes, op.getParser());
            StackAction[] opSteps = NArrays.append(this.infixSteps, step);
            return copy(requireOperator, right, left, ops, opSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define the left operand.
         */
        public Self left (Object left)
        {
            if (this.left != null)
                throw new IllegalStateException("Trying to redefine the left operand.");

            return copy(
                requireOperator, right, compile(left), infixes, infixSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define the right operand.
         */
        public Self right (Object right)
        {
            if (this.right != null)
                throw new IllegalStateException("Trying to redefine the right operand.");

            return copy(
                requireOperator, compile(right), left, infixes, infixSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        Self affix (rule op, StackAction step)
        {
            Parser[] affixes = NArrays.append(this.affixes, op.getParser());
            StackAction[] affixSteps = NArrays.append(this.affixSteps, step);
            return copy(requireOperator, right, left, infixes, infixSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        Self requireOperator ()
        {
            if (requireOperator)
                throw new IllegalStateException("Specifiying that an operator is required twice.");

            return copy(true, right, left, infixes, infixSteps, affixes, affixSteps);
        }
    }

    // endregion
    // =============================================================================================
    // region [class LeftExpressionBuilder]

    /**
     * Helps build a {@link LeftExpression} parser.
     */
    public final class LeftExpressionBuilder extends ExpressionBuilder<LeftExpressionBuilder>
    {
        LeftExpressionBuilder() {
            super(true);
        }

        // -----------------------------------------------------------------------------------------

        LeftExpressionBuilder (
            boolean leftAssociative,
            Parser left, Parser right,
            Parser[] ops, StackAction[] opSteps,
            Parser[] affixes, StackAction[] affixSteps,
            boolean requireOtherSide)
        {
            super(
                leftAssociative,
                requireOtherSide, left, right,
                ops, opSteps,
                affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        @Override LeftExpressionBuilder copy (
            boolean requireOtherSide,
            Parser right, Parser left,
            Parser[] infixes, StackAction[] infixSteps,
            Parser[] affixes, StackAction[] affixSteps)
        {
            return new LeftExpressionBuilder(
                leftAssociative,
                left, right,
                infixes, infixSteps,
                affixes, affixSteps,
                requireOtherSide);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define a suffix operator, along with the corresponding step action.
         */
        public LeftExpressionBuilder suffix (rule op, StackPush step) {
            return affix(op, step);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Specifies that an operator match is required, so a left operand cannot match on its own.
         */
        @Override public LeftExpressionBuilder requireOperator () {
            return super.requireOperator();
        }

        // -----------------------------------------------------------------------------------------

        @Override public Parser getParser ()
        {
            if (parser != null)
                return parser; // get() was called before

            if (left == null)
                throw new IllegalStateException(
                    "No left operand specified for a left-associative expression.");

            if (right == null && infixes.length > 0)
                throw new IllegalStateException(
                    "No right operand specified for a left-associative expression, "
                        + "but operators have been defined.");

            if (requireOperator && infixes.length == 0 && affixes.length == 0)
                throw new IllegalStateException(
                    "Right-side required but no prefix or operator has been defined.");

            return parser = new LeftExpression(
                left, right, infixes, infixSteps, affixes, affixSteps, requireOperator);
        }
    }

    // endregion
    // =============================================================================================
    // region [class RightExpressionBuilder]

    /**
     * Helps build a {@link RightExpression} parser.
     */
    public final class RightExpressionBuilder extends ExpressionBuilder<RightExpressionBuilder>
    {
        RightExpressionBuilder() {
            super(false);
        }

        // -----------------------------------------------------------------------------------------

        RightExpressionBuilder (
            boolean leftAssociative,
            Parser left, Parser right,
            Parser[] ops, StackAction[] opSteps,
            Parser[] affixes, StackAction[] affixSteps,
            boolean requireOtherSide)
        {
            super(
                leftAssociative,
                requireOtherSide, left, right,
                ops, opSteps,
                affixes, affixSteps
            );
        }

        // -----------------------------------------------------------------------------------------

        @Override RightExpressionBuilder copy (
            boolean requireOtherSide, Parser right, Parser left,
            Parser[] infixes, StackAction[] infixSteps,
            Parser[] affixes, StackAction[] affixSteps)
        {
            return new RightExpressionBuilder(
                leftAssociative,
                left, right,
                infixes, infixSteps,
                affixes, affixSteps,
                requireOtherSide);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define a prefix operator, along with the corresponding step action.
         */
        public RightExpressionBuilder prefix (rule op, StackPush step) {
            return affix(op, step);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Specifies that an operator match is required, so a right operand cannot match on its own.
         */
        @Override public RightExpressionBuilder requireOperator () {
            return super.requireOperator();
        }

        // -----------------------------------------------------------------------------------------

        @Override public Parser getParser ()
        {
            if (parser != null)
                return parser; // get() was called before

            if (right == null)
                throw new IllegalStateException(
                    "No right operand specified for a right-associative expression.");

            if (left == null && infixes.length > 0)
                throw new IllegalStateException(
                    "No left operand specified for a right-associative expression, "
                        + "but operators have been defined.");

            if (requireOperator && infixes.length == 0 && affixes.length == 0)
                throw new IllegalStateException(
                    "Left-side required but no prefix or operator has been defined.");

            return parser = new RightExpression(
                left, right, infixes, infixSteps, affixes, affixSteps, requireOperator);
        }
    }

    // endregion
    // =============================================================================================
    // region [class BoundedParserBuilder]
    // =============================================================================================

    public final class BoundedParserBuilder
    {
        // -----------------------------------------------------------------------------------------

        private final Parser coarse;
        private final Parser fine;

        // -----------------------------------------------------------------------------------------

        BoundedParserBuilder (Parser coarse, Parser fine) {
            this.coarse = coarse;
            this.fine = fine;
        }

        // -----------------------------------------------------------------------------------------

        public rule exact() {
            return new rule(new Bounded(coarse, fine, p -> false));
        }

        // -----------------------------------------------------------------------------------------

        public rule permissive() {
            return new rule(new Bounded(coarse, fine, p -> true));
        }

        // -----------------------------------------------------------------------------------------

        public rule fallback (Predicate<Parse> fallback) {
            return new rule(new Bounded(coarse, fine, fallback));
        }
    }

    // endregion
    // =============================================================================================
}
