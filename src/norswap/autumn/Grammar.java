package norswap.autumn;

import norswap.autumn.actions.*;
import norswap.autumn.memo.*;
import norswap.autumn.parsers.*;
import norswap.utils.NArrays;
import norswap.utils.data.wrappers.Slot;
import norswap.utils.reflection.Subtyping;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class is meant to be subclasses to create grammars: a collection of parsers that together
 * define a language.
 *
 * <p>Mostly, this class is a convenient that supplies a domain specific language (DSL) for
 * creating parsers. It is possible to construct {@link Parser parsers} without it, but the DSL
 * is vastly more convenient.
 *
 * <p>What this class provides:
 * <ul>
 *     <li>The abstract {@link #root()}</li> method to define the parser's entry point.</li>
 *     <li>Many methods ("combinators") returning {@link rule} objects wrapping a parser. More on
 *     this below.</li>
 *     <li>Pre-defined instances of {@link rule}.</li>
 *     <li>The {@link #ws} field to define the whitespace parser, which is then automatically used by
*      various methods, including {@link #word(String) and {@link rule#word()}}.</li>
 *     <li>An easy mechanism to define reserved words and identifiers (which are not allowed to be
 *     identical to reserved words) in your language. Specify {@link #id_part}, then use {@link
 *     #reserved(String)} to specify reserved words and {@link #identifier(Object)} to specify your
 *     identifier rule.</li>
 *     <li>It automatically sets the {@link Parser#rule()} of parsers based on the name of the field
 *     they are assigned to. This is done at most once, when {@link Autumn#parse} is called with the
 *     grammar or one of its {@link rule}. This can be disabled through {@link #makeRuleNames}.</li>
 * </ul>
 *
 * <p>Some more details regarding combinators. These methods return an instance of {@link rule}, on
 * which further method can be called to create create composite parsers, e.g.:
 *
 * <pre>
 * {@code
 * rule arith = digit.at_least(1).sep(1, choice("+", "-"));
 * }
 * </pre>
 *
 * <p>Many of these combinators accept one (or multiple) {@code Object} argument, typically called
 * {@code parser}. These arguments can be provided as instances of {@link rule}, {@link Parser},
 * {@link String} (automatically converted into a {@link StringMatch}) or {@link Character}
 * (automatically converted into a {@link CharPredicate}).
 */
public abstract class Grammar
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
    // region [Public Fields & Root Definition]
    // =============================================================================================

    /**
     * Change this to specify the whitespace parser used for {@link #word} and {@link rule#word}.
     *
     * <p>This parser may succeed or fail if there is no whitespace to be matched. When used,
     * whitespace will always be parsed optionally.
     *
     * <p>null by default, meaning no whitespace will be matched by {@link #word} and {@link
     * rule#word}.
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

    /**
     * Set this to specify a parser that is able to match any single character that may occur
     * within an identifier of your language.
     *
     * <p>You must specify this if you want to use {@link #reserved(String)} or {@link
     * #identifier(Object)}.
     */
    public rule id_part = null;

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
     * An array list collecting all reserved words defined using {@link #reserved(String)}, and used
     * by {@link #identifier(Object)}.
     */
    public final ArrayList<String> reservedWords = new ArrayList<>();

    // ---------------------------------------------------------------------------------------------

    /**
     * This lazy rule is used by parser returned by {@link #identifier(Object)}.
     */
    public rule any_reserved_word = lazy(() -> {
            if (id_part == null)
                throw new Error("Trying to use any_reserved_word without having defined " +
                    "Grammar#id_part, which should match any single character that can occur " +
                    "within identifiers.");

            // Reserved words are tried in order. If a a prefix of another reserved word happens
            // before it, this will cause the rule to fail.
            //
            // To avoid this, we preserve the original ordering, excepted that we swap reserved
            // words with their prefixes when the happen before.
            //
            // The point of preserving the original ordering is to allow the user to optimize
            // the parser by specifying frequently-used reserved words first if he so desires
            // (though the impact should be minimal, as valid identifiers will still need to
            // make their way through the whole list).

            TreeSet<String> set = new TreeSet<>();
            ArrayList<String> noPrefixCopy = new ArrayList<>();
            for (String word: reservedWords) {
                set.add(word);
                String lower;
                while ((lower = set.lower(word)) != null && word.startsWith(lower)) {
                    noPrefixCopy.set(noPrefixCopy.indexOf(lower), word);
                    word = lower;
                }
                noPrefixCopy.add(word);
            }

            return seq(new StringChoice(noPrefixCopy.toArray(new String[0])), id_part.not());
        });

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to exclude errors inside whitespace ({@link #ws}) from counting against the furthest
     * parse error ({@link Parse#error}). True by default.
     */
    public boolean excludeWhitespaceErrors = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether to assign names to parsers (based on the field names)
     * when calling {@link Autumn#parse} with a grammar or with a rule.
     */
    public boolean makeRuleNames = true;

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether rule names have already been assigned to parser via {@link #makeRuleNames()} if the
     * field {@link #makeRuleNames} is true.
     */
    private boolean ruleNamesMade = false;

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the main entry point into the grammar. Used when calling {@link Autumn#parse(Grammar,
     * String, ParseOptions)} as well as its {@link Autumn#parse(Grammar.rule, List, ParseOptions)
     * List} version.
     */
    public abstract rule root();

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
     * Fetches all the fields declared in the class of this object (i.e. {@code this.getClass()})
     * and all of its ancestors (including the {@link Grammar} class) and for those that are of type
     * {@link rule} or {@link Parser}, sets the rule name to the name of the field, if no rule name
     * has been set already.
     *
     * <p>This only does anything if the {@link #makeRuleNames} is true (which it is by default).
     * It also remembers wether the names have been assigned and does not do duplicate work.
     *
     * <p>This is called automatically by {@link Autumn#parse} when called with a grammar or with
     * a rule.
     */
    void makeRuleNames()
    {
        if (makeRuleNames && !ruleNamesMade) {
            Class<?> klass = this.getClass();
            while (!klass.equals(Grammar.class)) {
                makeRuleNames(klass.getDeclaredFields());
                klass = klass.getSuperclass();
            }
            makeRuleNames(Grammar.class.getFields());
            ruleNamesMade = true;
        }
    }

    // ---------------------------------------------------------------------------------------------

    // Note: supresses warning on `f.isAccessible()` deprecated after Java 8 in favor of
    // `f.canAccess(this)`. Language level 8 with a later JDK will yield a warning while we
    // can't use `canAccess` yet.
    @SuppressWarnings({"deprecation", "RedundantSuppression"})
    private void makeRuleNames (Field[] fields)
    {
        try {
            for (Field f: fields) {
                boolean madeAccessible = false;
                if (!Modifier.isPublic(f.getModifiers()) && !f.isAccessible())
                    f.setAccessible(madeAccessible = true);

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
                if (madeAccessible)
                    f.setAccessible(false);
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
     * Returns a parser for a reserved word in the language, which is equivalent to
     * {@code seq(string, id_part.not()).word()} and additionally registers the reserved word in
     * {@link #reservedWords}, for use in {@link #identifier(Object)}.
     *
     * <p>For this to work, you must have defined {@link #id_part} to a parser that matches
     * any single character that may occur within an identifier.
     */
    public rule reserved (String string) {
        if (id_part == null)
            throw new Error("Trying to create a reserved word without having defined " +
                "Grammar#id_part, which should match any single character that can occur " +
                "within identifiers.");
        reservedWords.add(string);
        return seq(string, id_part.not()).word();
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Returns a parser wrapping {@code parser} (which should match an identifier of the language),
     * and ensures that it will never match the same as a reserved word created via {@link
     * #reserved(String)}.
     *
     * <p>The returned parser is equivalent to {@code seq(any_reserved_word.not(), parser).word()}.
     *
     * <p>For this to work, you must have defined {@link #id_part} to a parser that matches
     * any single character that may occur within an identifier.
     */
    public rule identifier (Object parser) {
        if (id_part == null)
            throw new Error("Trying to create an identifier without having defined " +
                "Grammar#id_part, which should match any single character that can occur " +
                "within identifiers.");

        return seq(any_reserved_word.not(), parser).word();
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
         * Returns the Grammar instance this rule belongs to.
         */
        public Grammar grammar() {
            return Grammar.this;
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
         * Returns a {@link BoundedParserBuilder} that helps build a {@link Bounded} parser.
         *
         * <p>This parser matches using the current parser, but the reruns the {@code fine} parser
         * over the matched input.
         */
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

    private final static StackConsumer PUSHBACK = $ -> $.pushAll($.$);

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

            Parser operand = compile(op);
            return copy(
                requireOperator, operand, operand, infixes, infixSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        private Self _infix (Object op, StackAction action)
        {
            Parser[] ops = NArrays.append(this.infixes, compile(op));
            StackAction[] opSteps = NArrays.append(this.infixSteps, action);
            return copy(requireOperator, right, left, ops, opSteps, affixes, affixSteps);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define an infix operator which leaves the stack untouched.
         */
        public Self infix (Object op) {
            return _infix(op, PUSHBACK);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define an infix operator, along with the corresponding step action.
         */
        public Self infix (Object op, StackPush step) {
            return _infix(op, step);
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

        Self affix (Object op, StackAction step)
        {
            Parser[] affixes = NArrays.append(this.affixes, compile(op));
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
        public LeftExpressionBuilder suffix (Object op, StackPush step) {
            return affix(op, step);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define a suffix operator which leaves the stack untouched.
         */
        public LeftExpressionBuilder suffix (Object op) {
            return affix(op, PUSHBACK);
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
        public RightExpressionBuilder prefix (Object op, StackPush step) {
            return affix(op, step);
        }

        // -----------------------------------------------------------------------------------------

        /**
         * Define a prefix operator which leaves the stack untouched.
         */
        public RightExpressionBuilder prefix (Object op) {
            return affix(op, PUSHBACK);
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
