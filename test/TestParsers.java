import norswap.autumn.Parse;
import norswap.autumn.Parser;
import norswap.autumn.parsers.*;
import norswap.autumn.parsers.Collect.SimpleAction;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Deque;
import java.util.NoSuchElementException;

import static norswap.utils.Vanilla.list;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public final class TestParsers
{
    // ---------------------------------------------------------------------------------------------

    private Parser parser;
    private Parse parse;

    // ---------------------------------------------------------------------------------------------

    private void success (String string)
    {
        prefix(string, string.length());
    }

    // ---------------------------------------------------------------------------------------------

    private void success (String string, Object top)
    {
        parse = new Parse(string);
        assertTrue(parser.parse(parse));
        assertEquals(parse.pos, string.length());
        assertEquals(parse.stack.size(), 1);
        assertEquals(parse.stack.peek(), top);
    }

    // ---------------------------------------------------------------------------------------------

    private void prefix (String string)
    {
        parse = new Parse(string);
        assertTrue(parser.parse(parse));
    }

    // ---------------------------------------------------------------------------------------------

    private void prefix (String string, int index)
    {
        parse = new Parse(string);
        assertTrue(parser.parse(parse));
        assertEquals(parse.pos, index);
    }

    // ---------------------------------------------------------------------------------------------

    private void failure (String string)
    {
        parse = new Parse(string);
        assertFalse(parser.parse(parse));
    }

    // ---------------------------------------------------------------------------------------------

    private void failure (String string, int index)
    {
        parse = new Parse(string);
        assertFalse(parser.parse(parse));
        assertEquals(parse.error, index);
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
        Parse parse;

        parser = ObjectPredicate.any();

        parse = new Parse(list(new Object()));
        assertTrue(parser.parse(parse));

        parse = new Parse(list((Object) null));
        assertFalse(parser.parse(parse));

        parser = ObjectPredicate.instance(String.class);

        parse = new Parse(list(""));
        assertTrue(parser.parse(parse));

        parse = new Parse(list(3));
        assertFalse(parser.parse(parse));
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void optional()
    {
        parser = new Optional(CharPredicate.alpha());
        success("a");
        success("");
        prefix("_", 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void string_match()
    {
        parser = new StringMatch("foo", null);
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
        parser = new Sequence(
            CharPredicate.single('a'),
            CharPredicate.single('b'),
            CharPredicate.single('c'));
        success("abc");
        failure("bbc", 0);
        failure("aac", 1);
        failure("aba", 2);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void repeat()
    {
        parser = new Repeat(0, false, CharPredicate.single('a'));
        success("");
        success("a");
        success("aaa");
        prefix("", 0);
        prefix("b", 0);
        prefix("aab", 2);

        parser = new Repeat(1, false, CharPredicate.single('a'));
        success("a");
        success("aaa");
        failure("");
        failure("b");
        prefix("aab", 2);

        parser = new Repeat(3, false, CharPredicate.single('a'));
        success("aaa");
        success("aaaa");
        failure("");
        failure("aa", 2);

        parser = new Repeat(3, true, CharPredicate.single('a'));
        success("aaa");
        failure("");
        failure("aa", 2);
        prefix("aaaa", 3);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void around()
    {
        parser = new Around(0, false, false, CharPredicate.single('a'), CharPredicate.single(','));
        success("");
        success("a");
        success("a,a");
        prefix("a,", 1);
        prefix("b", 0);

        parser = new Around(1, false, false, CharPredicate.single('a'), CharPredicate.single(','));
        success("a");
        success("a,a");
        failure("");
        prefix("a,", 1);
        prefix("a,b", 1);

        parser = new Around(0, false, true, CharPredicate.single('a'), CharPredicate.single(','));
        success("");
        success("a");
        success("a,");
        success("a,a");
        success("a,a,");
        prefix(",", 0);
        prefix("b", 0);
        prefix("a,b", 2);

        parser = new Around(1, false, true, CharPredicate.single('a'), CharPredicate.single(','));
        success("a");
        success("a,");
        success("a,a");
        success("a,a,");
        failure("");
        failure(",");
        prefix("a,b", 2);

        parser = new Around(3, false, false, CharPredicate.single('a'), CharPredicate.single(','));
        success("a,a,a");
        success("a,a,a,a");
        failure("a,a", 3);
        failure("a,a,", 4);

        parser = new Around(3, true, false, CharPredicate.single('a'), CharPredicate.single(','));
        success("a,a,a");
        prefix("a,a,a,a", 5);
        failure("a,a", 3);
        failure("a,a,", 4);

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
        parser = new Choice(CharPredicate.single('a'), CharPredicate.single('b'));
        success("a");
        success("b");
        failure("");
        failure("c", 0);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void longest()
    {
        Parser enda = new Sequence(
            CharPredicate.single('a'),
            new Repeat(1, false, new StringMatch("ba", null)));

        Parser endb = new Repeat(1, false, new StringMatch("ab", null));

        parser = new Longest(enda, endb);
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
        parser = new Sequence(
            new Lookahead(CharPredicate.single('a')),
            CharPredicate.single('a'));
        success("a");
        prefix("a", 1);
        failure("");

        parser = new Sequence(
            new Lookahead(new StringMatch("ab", null)),
            CharPredicate.single('a'),
            CharPredicate.single('b'),
            CharPredicate.single('c'));
        success("abc");
        failure("ab", 2);
        failure("ac", 0);

        parser = new Lookahead(CharPredicate.single('a'));
        prefix("a", 0);
        failure("");
        failure("b");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void not()
    {
        parser = new Sequence(new Not(CharPredicate.single('a')), CharPredicate.single('b'));
        success("b");
        failure("a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void lazy_parser()
    {
        Parser alpha = CharPredicate.alpha();
        parser = new LazyParser("-> alpha", () -> alpha);
        success("a");
        success("A");
        failure("1");
    }

    // ---------------------------------------------------------------------------------------------

    private static Parser a =
        new Collect("A", CharPredicate.single('a'), true, false,
            (SimpleAction) (p,xs) -> p.push("a"));

    private static Parser b =
        new Collect("B", CharPredicate.single('b'), true, false,
            (SimpleAction) (p,xs) -> p.push("b"));

    private static Parser aa =
        new Collect("AA", new StringMatch("aa", null), true, false,
            (SimpleAction) (p, xs) -> p.push("aa"));

    // ---------------------------------------------------------------------------------------------

    private static void pair_concat (Parse parse, Object[] items) {
        parse.push("(" + items[0] + "," + items[1] + ")");
    }

    // ---------------------------------------------------------------------------------------------

    private static void pair_concat2 (Parse parse, String string, Object[] items) {
        parse.push("(" + string + ")");
    }

    // ---------------------------------------------------------------------------------------------

    private static void concat (Parse parse, Object[] items) {
        parse.push(Arrays.toString(items));
    }

    // ---------------------------------------------------------------------------------------------

    private static Object peek (Deque<?> deque, int index)
    {
        int i = 0;
        for (Object o: deque) {
            if (i == index) return o;
            ++i;
        }
        throw new NoSuchElementException("at index " + index);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void collect()
    {
        parser = a;
        success("a", "a");

        parser = new Collect("as",
            new Sequence(a, CharPredicate.single(','), a),
            true, false,
            (SimpleAction) TestParsers::pair_concat);
        success("a,a", "(a,a)");

        parser = new Collect("as",
            new Sequence(a, CharPredicate.single(','), a),
            false, false,
            (SimpleAction) TestParsers::pair_concat);
        success("a,a");
        assertEquals(parse.stack.size(), 3);
        assertEquals(parse.stack.peek(), "(a,a)");
        assertEquals(peek(parse.stack, 1), "a");
        assertEquals(peek(parse.stack, 2), "a");

        parser = new Collect("as",
            new Sequence(a, CharPredicate.single(','), a),
            false, false,
            (Collect.StringAction) TestParsers::pair_concat2);
        success("a,a");
        assertEquals(parse.stack.size(), 3);
        assertEquals(parse.stack.peek(), "(a,a)");
        assertEquals(peek(parse.stack, 1), "a");
        assertEquals(peek(parse.stack, 2), "a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void left_assoc()
    {
        parser = new LeftAssoc(
            a, CharPredicate.single(','), a, true,
            (p,xs) -> p.push("(" + xs[0] + "," + xs[1] + ")"));
        success("a,a", "(a,a)");
        success("a,a,a", "((a,a),a)");
        success("a,a,a,a", "(((a,a),a),a)");
        failure("");
        failure("a");

        parser =  new LeftAssoc(
            a, CharPredicate.single(','), a, false,
            (p,xs) -> p.push("(" + xs[0] + "," + xs[1] + ")"));
        success("a", "a");
        success("a,a,a,a", "(((a,a),a),a)");
        failure("");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void backtracking()
    {
        parser = new Choice(
            new Sequence(a, a),
            new Sequence(CharPredicate.single('a'), b));
        success("ab", "b");

        parser = new Choice(
            new Repeat(4, false, a),
            new Sequence(new Repeat(0, false, CharPredicate.single('a')), b));
        success("aaab", "b");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void tokens()
    {
        Tokens tokens = new Tokens(a, b, aa);
        Parser a_  = tokens.token_parser(a);
        Parser b_  = tokens.token_parser(b);
        Parser aa_ = tokens.token_parser(aa);

        parser = new Collect("AABAB", new Sequence(aa_, b_, a_, b_), true, false,
            (SimpleAction) TestParsers::concat);
        success("aabab", "[aa, b, a, b]");

        parser = new Sequence(a_, a_);
        failure("aa");

        parser = tokens.token_choice(a, b, aa);
        success("aa", "aa");
        success("b", "b");
        failure("c");
    }


    // ---------------------------------------------------------------------------------------------
}
