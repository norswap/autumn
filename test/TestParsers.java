import norswap.autumn.Autumn;
import norswap.autumn.DSL;
import norswap.autumn.Parse;
import norswap.autumn.ParseResult;
import norswap.autumn.Parser;
import norswap.autumn.StackAction;
import norswap.autumn.TestFixture;
import norswap.autumn.parsers.*;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public final class TestParsers extends DSL
{
    // ---------------------------------------------------------------------------------------------

    private Parser parser;

    // ---------------------------------------------------------------------------------------------

    private ParseResult result;

    // ---------------------------------------------------------------------------------------------

    private TestFixture fixture = new TestFixture(); {
        // TODO fix this upstream
        fixture.peel_test_runner = false;
    }

    // ---------------------------------------------------------------------------------------------

    private void success (String string)
    {
        fixture.parser = parser;
        result = fixture.success(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void success (String string, Object top)
    {
        fixture.parser = parser;
        result = fixture.success_expect(string, top, 1);
        fixture.assert_true(result.value_stack.size() == 1, 1,
            () -> "Extraneous stuff on the value stack: " + result.value_stack);
    }

    // ---------------------------------------------------------------------------------------------

    private void prefix (String string)
    {
        fixture.parser = parser;
        result = fixture.prefix(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void prefix (String string, int size)
    {
        fixture.parser = parser;
        result = fixture.prefix_of_length(string, size, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void failure (String string)
    {
        fixture.parser = parser;
        result = fixture.failure(string, 1);
    }

    // ---------------------------------------------------------------------------------------------

    private void failure (String string, int position)
    {
        fixture.parser = parser;
        result = fixture.failure_at(string, position, 1);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void char_predicate()
    {
        parser = CharPredicate.any();
        success("a");
        success("_");
        failure("\0");

        parser = CharPredicate.single('a');
        success("a");
        failure("b");

        parser = CharPredicate.alpha();
        success("a");
        success("A");
        failure("1");
        
        parser = CharPredicate.alphanum();
        success("a");
        success("1");
        failure("_");

        parser = CharPredicate.digit();
        success("1");
        failure("a");

        parser = CharPredicate.octal_digit();
        success("0");
        success("7");
        failure("8");
        failure("a");

        parser = CharPredicate.hex_digit();
        success("a");
        success("f");
        success("F");
        failure("g");
        failure("G");
        success("1");

        parser = CharPredicate.range('a', 'z');
        success("a");
        failure("1");

        parser = CharPredicate.set('a', 'b');
        success("a");
        success("b");
        failure("c");

        parser = CharPredicate.set("ab");
        success("a");
        success("b");
        failure("c");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void object_predicate()
    {
        parser = ObjectPredicate.any();

        assertTrue(Autumn.run(parser, list(new Object()), null).full_match);
        assertFalse(Autumn.run(parser, list((Object) null), null).success);

        parser = ObjectPredicate.instance(String.class);

        assertTrue(Autumn.run(parser, list(""), null).full_match);
        assertFalse(Autumn.run(parser, list(3), null).success);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void optional()
    {
        parser = alpha.opt().get();
        success("a");
        success("");
        prefix("_", 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void string_match()
    {
        parser = str("foo").get();
        success("foo");
        prefix("foobar", 3);
        failure("bar");

        parser = new StringMatch("foo", new Optional(CharPredicate.single(' ')));
        success("foo");
        success("foo ");
        failure("bar");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void sequence()
    {
        parser = seq(character('a'), character('b'), character('c')).get();
        success("abc");
        failure("bbc", 0);
        failure("aac", 1);
        failure("aba", 2);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void repeat()
    {
        parser = character('a').at_least(0).get();
        success("");
        success("a");
        success("aaa");
        prefix("", 0);
        prefix("b", 0);
        prefix("aab", 2);

        parser = character('a').at_least(1).get();
        success("a");
        success("aaa");
        failure("");
        failure("b");
        prefix("aab", 2);

        parser = character('a').at_least(3).get();
        success("aaa");
        success("aaaa");
        failure("");
        failure("aa", 2);

        parser = character('a').repeat(3).get();
        success("aaa");
        failure("");
        failure("aa", 2);
        prefix("aaaa", 3);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void around()
    {
        parser = character('a').sep(0, character(',')).get();
        success("");
        success("a");
        success("a,a");
        prefix("a,", 1);
        prefix("b", 0);

        parser = character('a').sep(1, character(',')).get();
        success("a");
        success("a,a");
        failure("");
        prefix("a,", 1);
        prefix("a,b", 1);

        parser = character('a').sep_trailing(0, character(',')).get();
        success("");
        success("a");
        success("a,");
        success("a,a");
        success("a,a,");
        success(",");
        prefix("b", 0);
        prefix("a,b", 2);

        parser = character('a').sep_trailing(1, character(',')).get();
        success("a");
        success("a,");
        success("a,a");
        success("a,a,");
        failure("");
        failure(",");
        prefix("a,b", 2);

        parser = character('a').sep(3, character(',')).get();
        success("a,a,a");
        success("a,a,a,a");
        failure("a,a", 3);
        failure("a,a,", 4);

        parser = character('a').sep_exact(3, character(',')).get();
        success("a,a,a");
        prefix("a,a,a,a", 5);
        failure("a,a", 3);
        failure("a,a,", 4);

        // exact & trailing
        parser = new Around(3, true, true, CharPredicate.single('a'), CharPredicate.single(','));
        success("a,a,a");
        success("a,a,a,");
        prefix("a,a,a,a", 6);
        failure("a,a", 3);
        failure("a,a,", 4);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void choice()
    {
        parser = choice(character('a'), character('b')).get();
        success("a");
        success("b");
        failure("");
        failure("c", 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void longest()
    {
        parser = longest(
            seq(character('a'), str("ba").at_least(1)),
            str("ab").at_least(1)).get();
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
        parser = seq(character('a').ahead(), character('a')).get();
        success("a");
        failure("");

        parser = seq(str("ab").ahead(), character('a'), character('b'), character('c')).get();
        success("abc");
        failure("ab", 2);
        failure("ac", 0);

        parser = character('a').ahead().get();
        prefix("a", 0);
        failure("");
        failure("b");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void not()
    {
        parser = seq(character('a').not(), character('b')).get();
        success("b");
        failure("a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void lazy_parser()
    {
        parser = new LazyParser(() -> alpha.get());
        success("a");
        success("A");
        failure("1");
    }

    // ---------------------------------------------------------------------------------------------

    private Parser a  = character('a').push_match().get();
    private Parser b  = character('b').push_match().get();
    private Parser aa = str("aa")     .push_match().get();

    // ---------------------------------------------------------------------------------------------

    private Object pair_concat (Parse parse, Object[] items) {
        return "(" + items[0] + "," + items[1] + ")";
    }

    // ---------------------------------------------------------------------------------------------

    private Object pair_concat_square (Parse parse, Object[] items) {
        return "[" + items[0] + "," + items[1] + "]";
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void collect()
    {
        parser = a;
        success("a", "a");

        parser = seq(a, character(','), a).push(this::pair_concat).get();
        success("a,a", "(a,a)");

        parser = seq(a, character(','), a)
            .peek_only()
            .push(this::pair_concat).get();

        success("a,a");
        assertEquals(result.value_stack.size(), 3);
        assertEquals(result.top_value(), "(a,a)");
        assertEquals(result.value_stack.peek_back(1), "a");
        assertEquals(result.value_stack.peek_back(2), "a");

        // string action
        parser = seq(a, character(','), a)
            .peek_only()
            .collect((StackAction.WithString) (p,str,xs) -> p.stack.push(str)).get();

        success("a,a");
        assertEquals(result.value_stack.size(), 3);
        assertEquals(result.top_value(), "a,a");
        assertEquals(result.value_stack.peek_back(1), "a");
        assertEquals(result.value_stack.peek_back(2), "a");

        // tests that a push is properly undone
        parser = seq(
            seq(a, character(','), a).push(this::pair_concat),
            fail).get();
        failure("a,a", 3);
        assertEquals(result.value_stack.size(), 0);

        // tests that pop is properly undone
        parser = seq(
            a,
            seq(empty.collect((p,xs) -> p.stack.pop()), fail).opt()
        ).get();

        success("a");
        assertEquals(result.value_stack.size(), 1);
        assertEquals(result.top_value(), "a");

        // test lookback
        parser = seq(
            str("xxx").push_match(),
            seq("yyy").lookback(1).push((p,xs) -> xs[0] + "yyy")
        ).get();

        success("xxxyyy");
        assertEquals(result.value_stack.size(), 1);
        assertEquals(result.top_value(), "xxxyyy");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void left_assoc()
    {
        parser = left_full(b, character(','), a, this::pair_concat).get();

        success("b,a", "(b,a)");
        success("b,a,a", "((b,a),a)");
        success("b,a,a,a", "(((b,a),a),a)");
        failure("");
        failure("b");
        failure("a");

        parser = left(b, character(','), a, this::pair_concat).get();

        success("b", "b");
        success("b,a,a,a", "(((b,a),a),a)");
        failure("");
        failure("a");

        // check that side-effects from an operator are properly undone
        parser = seq(left(b, a, b, (p,xs) -> "bab"), a).push(this::pair_concat).get();

        success("baba", "(bab,a)");
        success("ba", "(b,a)");
        failure("bab");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void right_assoc()
    {
        parser = right_full(a, character(','), b, this::pair_concat).get();

        success("a,b", "(a,b)");
        success("a,a,b", "(a,(a,b))");
        success("a,a,a,b", "(a,(a,(a,b)))");
        failure("");
        failure("b");
        failure("a");

        parser = right(a, character(','), b, this::pair_concat).get();

        success("b", "b");
        success("a,a,a,b", "(a,(a,(a,b)))");
        failure("");
        failure("a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void backtracking()
    {
        parser = choice(seq(a,a), seq(character('a'), b)).get();

        success("ab", "b");

        parser = choice(
            rule(a).at_least(4),
            seq(character('a').at_least(0), b)).get();

        success("aaab", "b");
    }

    // ---------------------------------------------------------------------------------------------

    // Test for the token cache.
    // Commented, as this API is not supposed to be publicly accessible.
    // Last run March 03 2019

//    @Test public void token_cache()
//    {
//        HashMap<Integer, TokenResult> map = new HashMap<>();
//        TokenCache cache = new TokenCache();
//        int N = 1000_000;
//        int RANGE = 10_000;
//        int NTOKENS = 100;
//        int SPAN = 100;
//        Random random = new Random();
//
//        for (int i = 0; i < N; ++i)
//        {
//            int pos = random.nextInt(RANGE);
//            TokenResult r = cache.get(pos);
//            assertEquals(r, map.get(pos));
//
//            if (r == null) {
//                TokenResult res = new TokenResult(
//                    random.nextInt(NTOKENS), pos, pos + random.nextInt(SPAN),
//                    Collections.emptyList());
//                cache.put(pos, res);
//                map.put(pos, res);
//            }
//        }
//    }

    // ---------------------------------------------------------------------------------------------

    @Test public void tokens()
    {
        Tokens tokens = new Tokens(a, b, aa);
        Parser a_  = tokens.token_parser(a);
        Parser b_  = tokens.token_parser(b);
        Parser aa_ = tokens.token_parser(aa);

        parser = seq(aa_, b_, a_, b_).push((p,xs) -> Arrays.toString(xs)).get();
        success("aabab", "[aa, b, a, b]");

        parser = seq(a_, a_).get();
        failure("aa");

        parser = tokens.token_choice(a, b, aa);
        success("aa", "aa");
        success("b", "b");
        failure("c");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void left_recursive()
    {
        // simple left-recursion
        // A -> Aa | a
        parser = left_recursive(A -> choice(
            seq(A, a).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "((a,a),a)");
        success("aaaa", "(((a,a),a),a)");
        failure("b", 0);
        failure("", 0);

        // nested left-recursion
        // B -> Bb | A
        Parser old = parser;
        parser = left_recursive(B -> choice(
            seq(B, b).push(this::pair_concat),
            old)).get();

        success("ab", "(a,b)");
        success("aaab", "(((a,a),a),b)");
        success("abbb", "(((a,b),b),b)");
        success("aaabbb", "(((((a,a),a),b),b),b)");
        failure("b", 0);

        // simple left- and right-recursion (right-associative)
        // A -> AA | a
        parser = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        failure("b", 0);
        failure("", 0);

        // simple left- and right-recursion (left-associative)
        // A -> AA | a
        parser = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "((a,a),a)");
        success("aaaa", "(((a,a),a),a)");
        failure("b", 0);
        failure("", 0);

        // left- and right-recursion + right-recursion (right-associative)
        // A -> AA | bA | a
        parser = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            seq(b, A).push(this::pair_concat),
            a)).get();

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
        parser = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            seq(b, A).push(this::pair_concat),
            a)).get();

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
        parser = left_recursive(A -> choice(
            seq(a, A).push(this::pair_concat),
            seq(A, a).push(this::pair_concat_square),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        failure("b", 0);
        failure("", 0);

        // separated left- and right-recursion (right first) (left-associative)
        // A -> aA | Aa | a
        parser = left_recursive_left_assoc(A -> choice(
            seq(a, A).push(this::pair_concat),
            seq(A, a).push(this::pair_concat_square),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        prefix("aaa", 2);
        prefix("aaaa", 2);
        failure("b", 0);
        failure("", 0);

        // separated left- and right-recursion (left first) (right-associative)
        // A -> Aa | aA | a
        parser = left_recursive(A -> choice(
            seq(A, a).push(this::pair_concat_square),
            seq(a, A).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "(a,(a,a))");
        success("aaaa", "(a,(a,(a,a)))");
        failure("b", 0);
        failure("", 0);

        // separated left- and right-recursion (left first) (left-associative)
        // A -> Aa | aA | a
        parser = left_recursive_left_assoc(A -> choice(
            seq(A, a).push(this::pair_concat_square),
            seq(a, A).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aa", "(a,a)");
        success("aaa", "[(a,a),a]");
        success("aaaa", "[[(a,a),a],a]");
        failure("b", 0);
        failure("", 0);

        // nested left- and right-recursion (both right-associative)
        // B -> BB | A | b
        // A -> AA | a
        parser = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            a)).get();
        parser = left_recursive(B -> choice(
            seq(B, B).push(this::pair_concat),
            parser,
            b)).get();

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
        parser = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            a)).get();
        parser = left_recursive_left_assoc(B -> choice(
            seq(B, B).push(this::pair_concat),
            parser,
            b)).get();

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
        parser = left_recursive_left_assoc(A -> choice(
            seq(A, A).push(this::pair_concat),
            a)).get();
        parser = left_recursive(B -> choice(
            seq(B, B).push(this::pair_concat),
            parser,
            b)).get();

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
        parser = left_recursive(A -> choice(
            seq(A, A).push(this::pair_concat),
            a)).get();
        parser = left_recursive_left_assoc(B -> choice(
            seq(B, B).push(this::pair_concat),
            parser,
            b)).get();

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
        parser = left_recursive(A -> choice(
            seq(A, str("("), A, str(")")).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("a(a)", "(a,a)");
        success("a(a)(a)", "((a,a),a)");
        success("a(a(a))", "(a,(a,a))");
        success("a(a(a))(a(a))", "((a,(a,a)),(a,a))");
        failure("", 0);
        prefix("aa", 1);

        // guarded recursion
        // A -> A(guarded[A]) | a
        parser = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A.guarded(), str(")")).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("a(a)", "(a,a)");
        success("a(a)(a)", "((a,a),a)");
        success("a(a(a))", "(a,(a,a))");
        success("a(a(a))(a(a))", "((a,(a,a)),(a,a))");
        failure("", 0);
        prefix("aa", 1);

        // left- right- and middle-recursion (no guard)
        // A -> A(A)A | AA | a

        parser = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A, str(")"), A)
                .push((p,xs) -> "(" + xs[0] + "," + xs[1] + "," + xs[2] + ")"),
            seq(A, A).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aaa", "((a,a),a)");
        success("a(a)a", "(a,a,a)");
        success("aa(a)a", "((a,a),a,a)");
        success("a(a)aa", "((a,a,a),a)");
        failure("");
        prefix("a(aa)a", 1);

        // left- right- and middle-recursion (with guard)
        // A -> A(guarded[A])A | AA | a

        parser = left_recursive_left_assoc(A -> choice(
            seq(A, str("("), A.guarded(), str(")"), A)
                .push((p,xs) -> "(" + xs[0] + "," + xs[1] + "," + xs[2] + ")"),
            seq(A, A).push(this::pair_concat),
            a)).get();

        success("a", "a");
        success("aaa", "((a,a),a)");
        success("a(a)a", "(a,a,a)");
        success("aa(a)a", "((a,a),a,a)");
        success("a(a)aa", "((a,a,a),a)");
        success("a(aaa)aa", "((a,((a,a),a),a),a)");
        failure("");
    }

    // ---------------------------------------------------------------------------------------------
}
