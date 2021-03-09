import norswap.autumn.AutumnTestFixture;
import norswap.autumn.Grammar;
import norswap.autumn.ParseResult;
import norswap.autumn.ParseState;
import norswap.autumn.actions.ActionContext;
import norswap.autumn.memo.MemoEntry;
import norswap.autumn.memo.MemoTable;
import norswap.autumn.parsers.*;
import norswap.utils.Strings;
import norswap.utils.Vanilla;
import norswap.utils.data.wrappers.Slot;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

import static java.lang.String.format;
import static norswap.utils.Vanilla.list;

public final class TestParsers extends Grammar
{
    // ---------------------------------------------------------------------------------------------

    private rule rule;

    // ---------------------------------------------------------------------------------------------

    private ParseResult result;

    // ---------------------------------------------------------------------------------------------

    private final AutumnTestFixture fixture = new AutumnTestFixture();
    { fixture.bottomClass = this.getClass(); }

    // ---------------------------------------------------------------------------------------------

    @Override public rule root() {
        throw new Error();
    }

    // ==============================================================================================
    // Pre-Defined Rules
    // ==============================================================================================

    private final rule a  = character('a').push($ -> $.str());
    private final rule b  = character('b').push($ -> $.str());
    private final rule aa = str("aa")     .push($ -> $.str());

    // ==============================================================================================
    // Utilities
    // ==============================================================================================

    private void success (String string)
    {
        fixture.rule = rule;
        result = fixture.success(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void success (String string, Object singleStackValue)
    {
        successTop(string, singleStackValue);
        fixture.assertTrue(result.valueStack.size() == 1, 1,
            () -> "Extraneous stuff on the value stack: " + result.valueStack);
    }

    // ---------------------------------------------------------------------------------------------

    private void successTop (String string, Object top)
    {
        fixture.rule = rule;
        result = fixture.successExpect(string, top, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void prefix (String string)
    {
        fixture.rule = rule;
        result = fixture.prefix(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void prefix (String string, int size)
    {
        fixture.rule = rule;
        result = fixture.prefixOfLength(string, size, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void failure (String string)
    {
        fixture.rule = rule;
        result = fixture.failure(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void failure (String string, int position)
    {
        fixture.rule = rule;
        result = fixture.failureAt(string, position, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void assertEquals (Object actual, Object expected) {
        fixture.assertEquals(actual, expected, 1, () -> "");
    }

    // ==============================================================================================
    // START TESTS
    // ==============================================================================================

    @Test public void charPredicate()
    {
        rule = any;
        success("a");
        success("_");
        failure("\0");

        rule = character('a');
        success("a");
        failure("b");

        rule = alpha;
        success("a");
        success("A");
        failure("1");

        rule = alphanum;
        success("a");
        success("1");
        failure("_");

        rule = digit;
        success("1");
        failure("a");

        rule = octal_digit;
        success("0");
        success("7");
        failure("8");
        failure("a");

        rule = hex_digit;
        success("a");
        success("f");
        success("F");
        failure("g");
        failure("G");
        success("1");

        rule = range('a', 'z');
        success("a");
        failure("1");

        rule = set('a', 'b');
        success("a");
        success("b");
        failure("c");

        rule = set("ab");
        success("a");
        success("b");
        failure("c");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void objectPredicate()
    {
        fixture.parser(ObjectPredicate.any());

        fixture.success(list(new Object()));
        fixture.failure(list((Object) null));

        fixture.parser(ObjectPredicate.instance(String.class));

        fixture.success(list(""));
        fixture.failure(list(3));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void optional()
    {
        rule = alpha.opt();
        success("a");
        success("");
        prefix("_", 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void stringMatch()
    {
        rule = str("foo");
        success("foo");
        prefix("foobar", 3);
        failure("bar");

        rule = rule(new StringMatch("foo", new Optional(CharPredicate.single(' '))));
        success("foo ");
        failure("bar");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void sequence()
    {
        rule = seq(character('a'), character('b'), character('c'));
        success("abc");
        failure("bbc", 0);
        failure("aac", 1);
        failure("aba", 2);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void repeat()
    {
        rule = character('a').at_least(0);
        success("");
        success("a");
        success("aaa");
        prefix("", 0);
        prefix("b", 0);
        prefix("aab", 2);

        rule = character('a').at_least(1);
        success("a");
        success("aaa");
        failure("");
        failure("b");
        prefix("aab", 2);

        rule = character('a').at_least(3);
        success("aaa");
        success("aaaa");
        failure("");
        failure("aa", 2);

        rule = character('a').repeat(3);
        success("aaa");
        failure("");
        failure("aa", 2);
        prefix("aaaa", 3);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void around()
    {
        rule = character('a').sep(0, character(','));
        success("");
        success("a");
        success("a,a");
        prefix("a,", 1);
        prefix("b", 0);

        rule = character('a').sep(1, character(','));
        success("a");
        success("a,a");
        failure("");
        prefix("a,", 1);
        prefix("a,b", 1);

        rule = character('a').sep_trailing(0, character(','));
        success("");
        success("a");
        success("a,");
        success("a,a");
        success("a,a,");
        success(",");
        prefix("b", 0);
        prefix("a,b", 2);

        rule = character('a').sep_trailing(1, character(','));
        success("a");
        success("a,");
        success("a,a");
        success("a,a,");
        failure("");
        failure(",");
        prefix("a,b", 2);

        rule = character('a').sep(3, character(','));
        success("a,a,a");
        success("a,a,a,a");
        failure("a,a", 3);
        failure("a,a,", 4);

        rule = character('a').sep_exact(3, character(','));
        success("a,a,a");
        prefix("a,a,a,a", 5);
        failure("a,a", 3);
        failure("a,a,", 4);

        // exact & trailing
        rule = rule(new Around(3, true, true, CharPredicate.single('a'), CharPredicate.single(',')));
        success("a,a,a");
        success("a,a,a,");
        prefix("a,a,a,a", 6);
        failure("a,a", 3);
        failure("a,a,", 4);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void choice()
    {
        rule = choice(character('a'), character('b'));
        success("a");
        success("b");
        failure("");
        failure("c", 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void longest()
    {
        rule = longest(
            seq(character('a'), str("ba").at_least(1)),
            str("ab").at_least(1));
        success("ab");
        success("aba");
        success("abab");
        failure("");
        failure("a", 1);
        prefix("abc", 2);
        prefix("abac", 3);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void lookahead()
    {
        rule = seq(character('a').ahead(), character('a'));
        success("a");
        failure("");

        rule = seq(str("ab").ahead(), character('a'), character('b'), character('c'));
        success("abc");
        failure("ab", 2);
        failure("ac", 0);

        rule = character('a').ahead();
        prefix("a", 0);
        failure("");
        failure("b");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void not()
    {
        rule = seq(character('a').not(), character('b'));
        success("b");
        failure("a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void lazyParser()
    {
        rule = lazy(() -> alpha);
        success("a");
        success("A");
        failure("1");
    }

    // ---------------------------------------------------------------------------------------------

    private Object pairConcat (ActionContext $) {
        return format("(%s,%s)", $.$[0], $.$[1]);
    }

    // ---------------------------------------------------------------------------------------------

    private Object pairConcatSquare (ActionContext $) {
        return format("[%s,%s]", $.$[0], $.$[1]);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void collect()
    {
        rule = a;
        success("a", "a");

        rule = seq(a, character(','), a).push(this::pairConcat);
        success("a,a", "(a,a)");

        rule = seq(a, character(','), a)
            .push(this::pairConcat, PEEK_ONLY);

        success("a,a");
        assertEquals(result.valueStack.size(), 3);
        assertEquals(result.topValue(), "(a,a)");
        assertEquals(result.valueStack.peekBack(1), "a");
        assertEquals(result.valueStack.peekBack(2), "a");

        // string action
        rule = seq(a, character(','), a)
            .push($ -> $.str(), PEEK_ONLY);

        success("a,a");
        assertEquals(result.valueStack.size(), 3);
        assertEquals(result.topValue(), "a,a");
        assertEquals(result.valueStack.peekBack(1), "a");
        assertEquals(result.valueStack.peekBack(2), "a");

        // tests that a push is properly undone
        rule = seq(
            seq(a, character(','), a).push(this::pairConcat),
            fail);
        failure("a,a", 3);
        assertEquals(result.valueStack.size(), 0);

        // tests that pop is properly undone
        rule = seq(
            a,
            seq(empty.collect($ -> $.parse.stack.pop()), fail).opt());

        success("a");
        assertEquals(result.valueStack.size(), 1);
        assertEquals(result.topValue(), "a");

        // test lookback
        rule = seq(
            str("xxx").push($ -> $.str()),
            seq("yyy").push($ -> $.$[0] + "yyy", LOOKBACK(1)));

        success("xxxyyy");
        assertEquals(result.valueStack.size(), 1);
        assertEquals(result.topValue(), "xxxyyy");

        // test stack predicate

        rule = seq().filter($ -> true);
        success("");

        rule = seq().filter($ -> false);
        failure("");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void backtracking()
    {
        rule = choice(seq(a,a), seq(character('a'), b));

        success("ab", "b");

        rule = choice(
            a.at_least(4),
            seq(character('a').at_least(0), b));

        success("aaab", "b");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void memoTableImplem()
    {
        HashMap<Integer, MemoEntry> map = new HashMap<>();
        MemoTable table = new MemoTable(false);
        int N = 1000_000;
        int RANGE = 10_000;
        int NTOKENS = 100;
        int SPAN = 100;
        Random random = new Random();

        for (int i = 0; i < N; ++i)
        {
            int pos = random.nextInt(RANGE);
            MemoEntry e = table.get(null, pos, null);
            AssertJUnit.assertEquals(e, map.get(pos));

            if (e == null) {
                MemoEntry entry = new MemoEntry(
                    true,
                    null,
                    pos,
                    pos + random.nextInt(SPAN),
                    Collections.emptyList(),
                    null);
                table.memoize(entry);
                map.put(pos, entry);
            }
        }
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void memoTable()
    {
        Supplier<Integer> cntval = () -> result.<Slot<Integer>>parseState("counter").x;

        // 1. Check the collect action is only run once.

        Slot<Integer> counter = new Slot<>(0);
        rule amemo = a.collect($ -> ++ counter.x).memo();

        rule = choice(seq(amemo, a), amemo);
        success("a");
        assertEquals(counter.x, 2); // because success runs the parser TWICE!

        counter.x = 0;
        success("aa");
        assertEquals(counter.x, 2);

        // 2. Base case (no memo) using a parse state counter.

        // Note: you must use a parse state: tests perform two repetition of the parse and
        // this skews results otherwise.

        ParseState<Slot<Integer>> ctr = new ParseState<>("counter", () -> new Slot<>(0));

        amemo = a.collect($ -> $.apply(() -> {
             ++ $.data(ctr).x;
            return () -> -- $.data(ctr).x;
        }));
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assertEquals(cntval.get(), 1);
        success("aa");
        assertEquals(cntval.get(), 2);

        // 3. Same with memo: shouldn't change the results.

        amemo = amemo.memo();
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assertEquals(cntval.get(), 1);
        success("aa");
        assertEquals(cntval.get(), 2);

        // 4. This is pretty redundant.

        ParseState<Slot<Integer>> ctr2 = new ParseState<>("counter", () -> new Slot<>(1));

        amemo = a.collect($-> $.apply(() -> {
            $.data(ctr2).x *= 2;
            return () -> $.data(ctr2).x /= 2;
        })).memo();

        rule = choice(seq(amemo, amemo, amemo), seq(a, amemo));

        success("aa");
        assertEquals(cntval.get(), 2);
        success("aaa");
        assertEquals(cntval.get(), 8);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Copy-pasted from {@link #memoTable} but modified to use a MemoCache instead of a MemoTable,
     * and one added test.
     */
    @Test public void memoCache()
    {
        Supplier<Integer> cntval = () -> result.<Slot<Integer>>parseState("counter").x;

        // 1. Check the collect action is only run once.

        Slot<Integer> counter = new Slot<>(0);
        rule amemo = a.collect($ -> ++ counter.x).memo(3);

        rule = choice(seq(amemo, a), amemo);
        success("a");
        assertEquals(counter.x, 2); // because success runs the parser TWICE!

        counter.x = 0;
        success("aa");
        assertEquals(counter.x, 2);

        // 2. Base case (no memo) using a parse state counter.

        // Note: you must use a parse state: tests perform two repetition of the parse and
        // this skews results otherwise.

        ParseState<Slot<Integer>> ctr = new ParseState<>("counter", () -> new Slot<>(0));

        amemo = a.collect($ -> $.apply(() -> {
            ++ $.data(ctr).x;
            return () -> -- $.data(ctr).x;
        }));
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assertEquals(cntval.get(), 1);
        success("aa");
        assertEquals(cntval.get(), 2);

        // 3. Same with memo: shouldn't change the results.

        amemo = amemo.memo(3);
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assertEquals(cntval.get(), 1);
        success("aa");
        assertEquals(cntval.get(), 2);

        // 4. This is pretty redundant.

        ParseState<Slot<Integer>> ctr2 = new ParseState<>("counter", () -> new Slot<>(1));

        amemo = a.collect($ -> $.apply(() -> {
            $.data(ctr2).x *= 2;
            return () -> $.data(ctr2).x /= 2;
        })).memo(3);

        rule = choice(seq(amemo, amemo, amemo), seq(a, amemo));

        success("aa");
        assertEquals(cntval.get(), 2);
        success("aaa");
        assertEquals(cntval.get(), 8);

        // 5. Same but with insufficient entries.

        amemo = a.collect($ -> $.apply(() -> {
            $.data(ctr2).x *= 2;
            return () -> $.data(ctr2).x /= 2;
        })).memo(1);

        rule = choice(seq(amemo, amemo, amemo), seq(amemo, amemo));

        success("aa");
        assertEquals(cntval.get(), 4);
        success("aaa");
        assertEquals(cntval.get(), 8);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testLeftExpression()
    {
        rule = left_expression()
            .left(a)
            .suffix(str("+"), $ -> "(" + $.$[0] + ")+")
            .suffix(str("-"), $ -> "(" + $.$[0] + ")-")
            .right(b)
            .infix(str("*"), $ -> "(" + $.$[0] + ")*" + $.$[1])
            .infix(str("/"), $ -> "(" + $.$[0] + ")/" + $.$[1]);

        success("a");
        success("a+", "(a)+");
        success("a++", "((a)+)+");
        success("a-", "(a)-");
        success("a--", "((a)-)-");
        success("a+-", "((a)+)-");
        success("a-+", "((a)-)+");
        success("a*b", "(a)*b");
        success("a/b", "(a)/b");
        success("a*b*b", "((a)*b)*b");
        success("a/b/b", "((a)/b)/b");
        success("a/b+", "((a)/b)+");
        success("a+-/b/b+-", "((((((a)+)-)/b)/b)+)-");

        failure("aa");
        failure("+a");

        rule = left_expression()
            .left(a)
            .suffix("+", $ -> "(" + $.$[0] + ")+")
            .suffix("-", $ -> "(" + $.$[0] + ")-")
            .right(a)
            .infix("+", $ -> "(" + $.$[0] + ")+" + $.$[1])
            .infix("*", $ -> "(" + $.$[0] + ")*" + $.$[1])
            .infix("/", $ -> "(" + $.$[0] + ")/" + $.$[1]);

        success("a*a", "(a)*a");
        success("a/a", "(a)/a");
        success("a*a*a", "((a)*a)*a");
        success("a/a/a", "((a)/a)/a");
        success("a+a+a", "((a)+a)+a");

        // test no step version
        rule = left_expression()
            .operand(a)
            .infix("+")
            .infix("-", $ -> "(" + Strings.join("", Vanilla.listSlice($.$, 0, -1)) + ")-" + $.get(-1))
            // this coalesces the stack into a single item
            .suffix("=", $ -> Strings.join("", $.$));

        success("a+a=", "aa");
        success("a-a=", "(a)-a");
        success("a+a-a=", "(aa)-a");
        success("a-a+a=", "(a)-aa");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testRightExpression()
    {
        rule = right_expression()
            .right(a)
            .prefix(str("+"), $ -> "+(" + $.$[0] + ")")
            .prefix(str("-"), $ -> "-(" + $.$[0] + ")")
            .left(b)
            .infix(str("*"), $ -> $.$[0] + "*(" + $.$[1] + ")")
            .infix(str("/"), $ -> $.$[0] + "/(" + $.$[1] + ")");

        success("a");
        success("+a", "+(a)");
        success("++a", "+(+(a))");
        success("-a", "-(a)");
        success("--a", "-(-(a))");
        success("+-a", "+(-(a))");
        success("-+a", "-(+(a))");
        success("b*a", "b*(a)");
        success("b/a", "b/(a)");
        success("b*b*a", "b*(b*(a))");
        success("b/b/a", "b/(b/(a))");
        success("b/+a", "b/(+(a))");
        success("+-b/b/+-a", "+(-(b/(b/(+(-(a))))))");

        failure("aa");
        failure("a+");

        rule = right_expression()
            .operand(a)
            .prefix(str("+"), $ -> "+(" + $.$[0] + ")")
            .prefix(str("-"), $ -> "-(" + $.$[0] + ")")
            .infix(str("+"), $ -> $.$[0] + "+(" + $.$[1] + ")")
            .infix(str("*"), $ -> $.$[0] + "*(" + $.$[1] + ")")
            .infix(str("/"), $ -> $.$[0] + "/(" + $.$[1] + ")");

        success("a*a", "a*(a)");
        success("a/a", "a/(a)");
        success("a*a*a", "a*(a*(a))");
        success("a/a/a", "a/(a/(a))");
        success("a+a+a", "a+(a+(a))");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testBounded()
    {
        rule = seq(not('-'), any).at_least(3).as_val("coarse")
            .refine(set("abc").at_least(3).as_val("fine"))
            .exact();
        rule = seq('-', rule, '-');

        successTop("-abcabc-", "fine");
        failure("-ab-", 3);
        failure("-abcabd-", 7);
        failure("-abdabc-", 7);

        rule = seq(not('-'), any).at_least(3).as_val("coarse")
            .refine(set("abc").at_least(3).as_val("fine"))
            .permissive();
        rule = seq('-', rule, '-');

        successTop("-abcabc-", "fine");
        failure("-ab-", 3);
        success("-abcabd-", "coarse");
        success("-abdabc-", "coarse");

        Slot<Boolean> bool = new Slot<>(false);
        rule = seq(not('-'), any).at_least(3).as_val("coarse")
            .refine(set("abc").at_least(3).as_val("fine"))
            .fallback(p -> bool.x);
        rule = seq('-', rule, '-');

        successTop("-abcabc-", "fine");
        failure("-ab-", 3);
        failure("-abcabd-", 7);
        failure("-abdabc-", 7);

        bool.x = true;

        successTop("-abcabc-", "fine");
        failure("-ab-", 3);
        success("-abcabd-", "coarse");
        success("-abdabc-", "coarse");
    }

    // ---------------------------------------------------------------------------------------------
}
