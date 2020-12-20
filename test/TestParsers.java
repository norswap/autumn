import norswap.autumn.DSL;
import norswap.autumn.ParseResult;
import norswap.autumn.ParseState;
import norswap.autumn.TestFixture;
import norswap.autumn.memo.MemoEntry;
import norswap.autumn.memo.MemoTable;
import norswap.autumn.parsers.*;
import norswap.utils.Slot;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Supplier;

import static org.testng.AssertJUnit.assertEquals;

public final class TestParsers extends DSL
{
    // ---------------------------------------------------------------------------------------------

    private rule rule;

    // ---------------------------------------------------------------------------------------------

    private ParseResult result;

    // ---------------------------------------------------------------------------------------------

    private final TestFixture fixture = new TestFixture();
    { fixture.bottom_class = this.getClass(); }

    // ==============================================================================================
    // Pre-Defined Rules
    // ==============================================================================================

    private final rule a  = character('a').push_string_match();
    private final rule b  = character('b').push_string_match();
    private final rule aa = str("aa")     .push_string_match();

    // ==============================================================================================
    // Utilities
    // ==============================================================================================

    private void success (String string)
    {
        fixture.rule = rule;
        result = fixture.success(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void success (String string, Object single_stack_value)
    {
        success_top(string, single_stack_value);
        fixture.assert_true(result.value_stack.size() == 1, 1,
            () -> "Extraneous stuff on the value stack: " + result.value_stack);
    }

    // ---------------------------------------------------------------------------------------------

    private void success_top (String string, Object top)
    {
        fixture.rule = rule;
        result = fixture.success_expect(string, top, 1);
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
        result = fixture.prefix_of_length(string, size, 1);
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
        result = fixture.failure_at(string, position, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void assert_equals (Object actual, Object expected) {
        fixture.assert_equals(actual, expected, 1, () -> "");
    }

    // ==============================================================================================
    // START TESTS
    // ==============================================================================================

    @Test public void char_predicate()
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

    @Test public void object_predicate()
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

    @Test public void string_match()
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

    @Test public void lazy_parser()
    {
        rule = lazy(() -> alpha);
        success("a");
        success("A");
        failure("1");
    }

    // ---------------------------------------------------------------------------------------------

    private Object pair_concat (Object[] items) {
        return "(" + items[0] + "," + items[1] + ")";
    }

    // ---------------------------------------------------------------------------------------------

    private Object pair_concat_square (Object[] items) {
        return "[" + items[0] + "," + items[1] + "]";
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void collect()
    {
        rule = a;
        success("a", "a");

        rule = seq(a, character(','), a).push(this::pair_concat);
        success("a,a", "(a,a)");

        rule = seq(a, character(','), a)
            .push(this::pair_concat, PEEK_ONLY);

        success("a,a");
        assert_equals(result.value_stack.size(), 3);
        assert_equals(result.top_value(), "(a,a)");
        assert_equals(result.value_stack.peek_back(1), "a");
        assert_equals(result.value_stack.peek_back(2), "a");

        // string action
        rule = seq(a, character(','), a)
            .push((p,$,s) -> s.get(p.string), PEEK_ONLY);

        success("a,a");
        assert_equals(result.value_stack.size(), 3);
        assert_equals(result.top_value(), "a,a");
        assert_equals(result.value_stack.peek_back(1), "a");
        assert_equals(result.value_stack.peek_back(2), "a");

        // tests that a push is properly undone
        rule = seq(
            seq(a, character(','), a).push(this::pair_concat),
            fail);
        failure("a,a", 3);
        assert_equals(result.value_stack.size(), 0);

        // tests that pop is properly undone
        rule = seq(
            a,
            seq(empty.collect((p,xs,p0,s0) -> p.stack.pop()), fail).opt());

        success("a");
        assert_equals(result.value_stack.size(), 1);
        assert_equals(result.top_value(), "a");

        // test lookback
        rule = seq(
            str("xxx").push_string_match(),
            seq("yyy").push(xs -> xs[0] + "yyy", LOOKBACK(1)));

        success("xxxyyy");
        assert_equals(result.value_stack.size(), 1);
        assert_equals(result.top_value(), "xxxyyy");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void left_fold()
    {
        rule = left_fold_full(b, character(','), a, this::pair_concat);

        success("b,a", "(b,a)");
        success("b,a,a", "((b,a),a)");
        success("b,a,a,a", "(((b,a),a),a)");
        failure("");
        failure("b");
        failure("a");

        rule = left_fold(b, character(','), a, this::pair_concat);

        success("b", "b");
        success("b,a,a,a", "(((b,a),a),a)");
        failure("");
        failure("a");

        // check that side-effects from an operator are properly undone
        rule = seq(left_fold(b, a, b, xs -> "bab"), a).push(this::pair_concat);

        success("baba", "(bab,a)");
        success("ba", "(b,a)");
        failure("bab");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void right_fold()
    {
        rule = right_fold_full(a, character(','), b, this::pair_concat);

        success("a,b", "(a,b)");
        success("a,a,b", "(a,(a,b))");
        success("a,a,a,b", "(a,(a,(a,b)))");
        failure("");
        failure("b");
        failure("a");

        rule = right_fold(a, character(','), b, this::pair_concat);

        success("b", "b");
        success("a,a,a,b", "(a,(a,(a,b)))");
        failure("");
        failure("a");
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

    @Test public void memo_table_implem()
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
            assertEquals(e, map.get(pos));

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

    @Test public void tokens()
    {
        // Note: this pollutes the DSL state with these tokens, but it's okay since this
        // fonctionality is only tested in this method.
        rule a_  = a.token();
        rule b_  = b.token();
        rule aa_ = aa.token();

        rule = seq(aa_, b_, a_, b_).push(Arrays::toString);
        success("aabab", "[aa, b, a, b]");

        rule = seq(a_, a_);
        failure("aa");

        rule = token_choice(a, b, aa);
        success("aa", "aa");
        success("b", "b");
        failure("c");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void left_recursive()
    {
        // simple left-recursion
        // A -> Aa | a
        rule = left_recursive(A -> choice(
            seq(A, a).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "((a,a),a)");
        success("aaaa", "(((a,a),a),a)");
        failure("b", 0);
        failure("", 0);

        // nested left-recursion
        // B -> Bb | A
        rule old = rule;
        rule = left_recursive(B -> choice(
            seq(B, b).push(this::pair_concat),
            old));

        success("ab", "(a,b)");
        success("aaab", "(((a,a),a),b)");
        success("abbb", "(((a,b),b),b)");
        success("aaabbb", "(((((a,a),a),b),b),b)");
        failure("b", 0);

        // simple left- and right-recursion (right-associative)
        // A -> AA | a
        rule = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        failure("b", 0);
        failure("", 0);

        // simple left- and right-recursion (left-associative)
        // A -> AA | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "((a,a),a)");
        success("aaaa", "(((a,a),a),a)");
        failure("b", 0);
        failure("", 0);

        // left- and right-recursion + right-recursion (right-associative)
        // A -> AA | bA | a
        rule = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            seq(b, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        success("ba", "(b,a)");
        success("baa", "(b,(a,a))");
        success("bba", "(b,(b,a))");
        success("bbaa", "(b,(b,(a,a)))");
        failure("b", 1);
        failure("", 0);

        // left- and right-recursion + right-recursion (left-associative)
        // A -> AA | bA | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            seq(b, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "((a,a),a)");
        success("aaaa", "(((a,a),a),a)");
        success("ba", "(b,a)");
        success("baa", "((b,a),a)");
        failure("b", 1);
        failure("", 0);
        failure("bba", 2);
        failure("bbaa", 2);

        // separated left- and right-recursion (right first) (right-associative)
        // A -> aA | Aa | a
        rule = left_recursive(A -> choice(
            seq(a, A).push(this::pair_concat),
            seq(A, a).push(this::pair_concat_square),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        failure("b", 0);
        failure("", 0);

        // separated left- and right-recursion (right first) (left-associative)
        // A -> aA | Aa | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(a, A).push(this::pair_concat),
            seq(A, a).push(this::pair_concat_square),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        prefix("aaa", 2);
        prefix("aaaa", 2);
        failure("b", 0);
        failure("", 0);

        // separated left- and right-recursion (left first) (right-associative)
        // A -> Aa | aA | a
        rule = left_recursive(A -> choice(
            seq(A, a).push(this::pair_concat_square),
            seq(a, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        failure("b", 0);
        failure("", 0);

        // separated left- and right-recursion (left first) (left-associative)
        // A -> Aa | aA | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, a).push(this::pair_concat_square),
            seq(a, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "[(a,a),a]");
        success("aaaa", "[[(a,a),a],a]");
        failure("b", 0);
        failure("", 0);

        // nested left- and right-recursion (both right-associative)
        // B -> BB | A | b
        // A -> AA | a
        rule = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            a));
        rule = left_recursive(B -> choice(
            seq(B, B).push(this::pair_concat),
            rule,
            b));

        success("b", "b");
        success("a", "a");
        success("bb", "(b,b)");
        success("bbbb", "(b,(b,(b,b)))");
        success("aa", "(a,a)");
        success("aaaa", "(a,(a,(a,a)))");
        success("baaabbb", "(b,((a,(a,a)),(b,(b,b))))");
        success("aab", "((a,a),b)");
        failure("", 0);

        // nested left- and right-recursion (both left-associative)
        // B -> BB | A | b
        // A -> AA | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            a));
        rule = left_recursive_left_assoc(B -> choice(
            seq(B, B).push(this::pair_concat),
            rule,
            b));

        success("b", "b");
        success("a", "a");
        success("bb", "(b,b)");
        success("bbbb", "(((b,b),b),b)");
        success("aa", "(a,a)");
        success("aaaa", "(((a,a),a),a)");
        success("baaabb", "(((b,((a,a),a)),b),b)");
        failure("", 0);

        // nested left- and right-recursion (left-assoc in right-assoc)
        // B -> BB | A | b
        // A -> AA | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            a));
        rule = left_recursive(B -> choice(
            seq(B, B).push(this::pair_concat),
            rule,
            b));

        success("b", "b");
        success("a", "a");
        success("bb", "(b,b)");
        success("bbbb", "(b,(b,(b,b)))");
        success("aa", "(a,a)");
        success("aaaa", "(((a,a),a),a)");
        success("baaabbb", "(b,(((a,a),a),(b,(b,b))))");
        failure("", 0);

        // nested left- and right-recursion (right-assoc in left-assoc)
        // B -> BB | A | b
        // A -> AA | a
        rule = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            a));
        rule = left_recursive_left_assoc(B -> choice(
            seq(B, B).push(this::pair_concat),
            rule,
            b));

        success("b", "b");
        success("a", "a");
        success("bb", "(b,b)");
        success("bbbb", "(((b,b),b),b)");
        success("aa", "(a,a)");
        success("aaaa", "(a,(a,(a,a)))");
        success("baaabb", "(((b,(a,(a,a))),b),b)");
        failure("", 0);

        // example where left-recursion can occur in a right-recursion
        // A -> A(A) | a
        rule = left_recursive(A -> choice(
            seq(A, str("("), A, str(")")).push(this::pair_concat),
            a));

        success("a", "a");
        success("a(a)", "(a,a)");
        success("a(a)(a)", "((a,a),a)");
        success("a(a(a))", "(a,(a,a))");
        success("a(a(a))(a(a))", "((a,(a,a)),(a,a))");
        failure("", 0);
        prefix("aa", 1);

        // example where left-recursion can occur in a right-recursion
        // A -> A(A) | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A, str(")")).push(this::pair_concat),
            a));

        success("a", "a");
        success("a(a)", "(a,a)");
        success("a(a)(a)", "((a,a),a)");
        failure("a(a(a))");
        failure("", 0);
        prefix("aa", 1);

        // example where B left-recursion can occur in a A right-recursion
        // A -> AA | B | a
        // B -> BB | b
        rule B1 = left_recursive_left_assoc(B -> choice(
            seq(B, B),
            str("b")));
        rule = left_recursive_left_assoc(A -> choice(
           seq(A, A),
            B1,
            str("a")));

        success("a");
        success("b");
        success("aa");
        success("aaa");
        success("bb");
        success("bbb");
        success("bba");
        success("abb");

        // example with mutual recursion
        // A -> AB | a
        // B -> Bb | bA
        rule B2 = left_recursive_left_assoc(B -> choice(
            seq(B, str("b")),
            seq(str("b"), lazy(() -> rule))));
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, B2),
            str("a")));

        success("aba");
        failure("abaa"); // A should right-recurse through B, which is forbidden

        // guarded recursion
        // A -> A(guarded[A]) | a
        rule = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A.guarded(), str(")")).push(this::pair_concat),
            a));

        success("a", "a");
        success("a(a)", "(a,a)");
        success("a(a)(a)", "((a,a),a)");
        success("a(a(a))", "(a,(a,a))");
        success("a(a(a))(a(a))", "((a,(a,a)),(a,a))");
        failure("", 0);
        prefix("aa", 1);

        // left- right- and middle-recursion (no guard)
        // A -> A(A)A | AA | a

        rule = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A, str(")"), A)
                .push(xs -> "(" + xs[0] + "," + xs[1] + "," + xs[2] + ")"),
            seq(A, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aaa", "((a,a),a)");
        success("a(a)a", "(a,a,a)");
        success("aa(a)a", "((a,a),a,a)");
        success("a(a)aa", "((a,a,a),a)");
        failure("");
        prefix("a(aa)a", 1);

        // left- right- and middle-recursion (with guard)
        // A -> A(guarded[A])A | AA | a

        rule = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A.guarded(), str(")"), A)
                .push(xs -> "(" + xs[0] + "," + xs[1] + "," + xs[2] + ")"),
            seq(A, A).push(this::pair_concat),
            a));

        success("a", "a");
        success("aaa", "((a,a),a)");
        success("a(a)a", "(a,a,a)");
        success("aa(a)a", "((a,a),a,a)");
        success("a(a)aa", "((a,a,a),a)");
        success("a(aaa)aa", "((a,((a,a),a),a),a)");
        failure("");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void memo_table()
    {
        Supplier<Integer> cntval = () -> result.<Slot<Integer>>parse_state("counter").x;

        // 1. Check the collect action is only run once.

        Slot<Integer> counter = new Slot<>(0);
        rule amemo = a.collect((p,xs,p0,s0) -> ++ counter.x).memo();

        rule = choice(seq(amemo, a), amemo);
        success("a");
        assert_equals(counter.x, 2); // because success runs the parser TWICE!

        counter.x = 0;
        success("aa");
        assert_equals(counter.x, 2);

        // 2. Base case (no memo) using a parse state counter.

        // Note: you must use a parse state: tests perform two repetition of the parse and
        // this skews results otherwise.

        ParseState<Slot<Integer>> ctr = new ParseState<>("counter", () -> new Slot<>(0));

        amemo = a.collect((p,xs,p0,s0) -> p.log.apply(() -> {
             ++ ctr.data(p).x;
            return () -> -- ctr.data(p).x;
        }));
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assert_equals(cntval.get(), 1);
        success("aa");
        assert_equals(cntval.get(), 2);

        // 3. Same with memo: shouldn't change the results.

        amemo = amemo.memo();
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assert_equals(cntval.get(), 1);
        success("aa");
        assert_equals(cntval.get(), 2);

        // 4. This is pretty redundant.

        ParseState<Slot<Integer>> ctr2 = new ParseState<>("counter", () -> new Slot<>(1));

        amemo = a.collect((p,xs,p0,s0) -> p.log.apply(() -> {
            ctr2.data(p).x *= 2;
            return () -> ctr2.data(p).x /= 2;
        })).memo();

        rule = choice(seq(amemo, amemo, amemo), seq(a, amemo));

        success("aa");
        assert_equals(cntval.get(), 2);
        success("aaa");
        assert_equals(cntval.get(), 8);
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Copy-pasted from {@link #memo_table} but modified to use a MemoCache instead of a MemoTable,
     * and one added test.
     */
    @Test public void memo_cache()
    {
        Supplier<Integer> cntval = () -> result.<Slot<Integer>>parse_state("counter").x;

        // 1. Check the collect action is only run once.

        Slot<Integer> counter = new Slot<>(0);
        rule amemo = a.collect((p,xs,p0,s0) -> ++ counter.x).memo(3);

        rule = choice(seq(amemo, a), amemo);
        success("a");
        assert_equals(counter.x, 2); // because success runs the parser TWICE!

        counter.x = 0;
        success("aa");
        assert_equals(counter.x, 2);

        // 2. Base case (no memo) using a parse state counter.

        // Note: you must use a parse state: tests perform two repetition of the parse and
        // this skews results otherwise.

        ParseState<Slot<Integer>> ctr = new ParseState<>("counter", () -> new Slot<>(0));

        amemo = a.collect((p,xs,p0,s0) -> p.log.apply(() -> {
            ++ ctr.data(p).x;
            return () -> -- ctr.data(p).x;
        }));
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assert_equals(cntval.get(), 1);
        success("aa");
        assert_equals(cntval.get(), 2);

        // 3. Same with memo: shouldn't change the results.

        amemo = amemo.memo(3);
        rule = choice(seq(amemo, amemo), amemo);

        success("a");
        assert_equals(cntval.get(), 1);
        success("aa");
        assert_equals(cntval.get(), 2);

        // 4. This is pretty redundant.

        ParseState<Slot<Integer>> ctr2 = new ParseState<>("counter", () -> new Slot<>(1));

        amemo = a.collect((p,xs,p0,s0) -> p.log.apply(() -> {
            ctr2.data(p).x *= 2;
            return () -> ctr2.data(p).x /= 2;
        })).memo(3);

        rule = choice(seq(amemo, amemo, amemo), seq(a, amemo));

        success("aa");
        assert_equals(cntval.get(), 2);
        success("aaa");
        assert_equals(cntval.get(), 8);

        // 5. Same but with insufficient entries.

        amemo = a.collect((p,xs,p0,s0) -> p.log.apply(() -> {
            ctr2.data(p).x *= 2;
            return () -> ctr2.data(p).x /= 2;
        })).memo(1);

        rule = choice(seq(amemo, amemo, amemo), seq(amemo, amemo));

        success("aa");
        assert_equals(cntval.get(), 4);
        success("aaa");
        assert_equals(cntval.get(), 8);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_left_expression()
    {
        rule = left_expression()
            .left(a)
            .suffix(str("+"), xs -> "(" + xs[0] + ")+")
            .suffix(str("-"), xs -> "(" + xs[0] + ")-")
            .right(b)
            .infix(str("*"), xs -> "(" + xs[0] + ")*" + xs[1])
            .infix(str("/"), xs -> "(" + xs[0] + ")/" + xs[1])
            .get();

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
            .suffix(str("+"), xs -> "(" + xs[0] + ")+")
            .suffix(str("-"), xs -> "(" + xs[0] + ")-")
            .right(a)
            .infix(str("+"), xs -> "(" + xs[0] + ")+" + xs[1])
            .infix(str("*"), xs -> "(" + xs[0] + ")*" + xs[1])
            .infix(str("/"), xs -> "(" + xs[0] + ")/" + xs[1])
            .get();

        success("a*a", "(a)*a");
        success("a/a", "(a)/a");
        success("a*a*a", "((a)*a)*a");
        success("a/a/a", "((a)/a)/a");
        success("a+a+a", "((a)+a)+a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_right_expression()
    {
        rule = right_expression()
            ._maybe_slow_right(a)
            .prefix(str("+"), xs -> "+(" + xs[0] + ")")
            .prefix(str("-"), xs -> "-(" + xs[0] + ")")
            ._maybe_slow_left(b)
            .infix(str("*"), xs -> xs[0] + "*(" + xs[1] + ")")
            .infix(str("/"), xs -> xs[0] + "/(" + xs[1] + ")")
            .get();

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
            .prefix(str("+"), xs -> "+(" + xs[0] + ")")
            .prefix(str("-"), xs -> "-(" + xs[0] + ")")
            .infix(str("+"), xs -> xs[0] + "+(" + xs[1] + ")")
            .infix(str("*"), xs -> xs[0] + "*(" + xs[1] + ")")
            .infix(str("/"), xs -> xs[0] + "/(" + xs[1] + ")")
            .get();

        success("a*a", "a*(a)");
        success("a/a", "a/(a)");
        success("a*a*a", "a*(a*(a))");
        success("a/a/a", "a/(a/(a))");
        success("a+a+a", "a+(a+(a))");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_bounded()
    {
        rule = seq(not('-'), any).at_least(3).as_val("coarse")
            .refine(set("abc").at_least(3).as_val("fine"))
            .exact();
        rule = seq('-', rule, '-');

        success_top("-abcabc-", "fine");
        failure("-ab-", 3);
        failure("-abcabd-", 7);
        failure("-abdabc-", 7);

        rule = seq(not('-'), any).at_least(3).as_val("coarse")
            .refine(set("abc").at_least(3).as_val("fine"))
            .permissive();
        rule = seq('-', rule, '-');

        success_top("-abcabc-", "fine");
        failure("-ab-", 3);
        success("-abcabd-", "coarse");
        success("-abdabc-", "coarse");

        Slot<Boolean> bool = new Slot<>(false);
        rule = seq(not('-'), any).at_least(3).as_val("coarse")
            .refine(set("abc").at_least(3).as_val("fine"))
            .fallback(p -> bool.x);
        rule = seq('-', rule, '-');

        success_top("-abcabc-", "fine");
        failure("-ab-", 3);
        failure("-abcabd-", 7);
        failure("-abdabc-", 7);

        bool.x = true;

        success_top("-abcabc-", "fine");
        failure("-ab-", 3);
        success("-abcabd-", "coarse");
        success("-abdabc-", "coarse");
    }

    // ---------------------------------------------------------------------------------------------
}
