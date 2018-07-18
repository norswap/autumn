package lang.java;

import norswap.utils.Exceptional;
import org.testng.annotations.Test;

import static norswap.lang.java.LexUtils.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public final class TestLexUtils
{
    // ---------------------------------------------------------------------------------------------

    private static void assert_equals (Exceptional<Number> result, double value)
    {
        // TODO utils message supplier
        assertTrue(result.is_value());
        assertEquals(result.get().doubleValue(), value);
    }

    // ---------------------------------------------------------------------------------------------

    private static void assert_equals (Exceptional<Number> result, long value)
    {
        // TODO utils message supplier
        assertTrue(result.is_value());
        assertEquals(result.get().longValue(), value);
    }

    // ---------------------------------------------------------------------------------------------

    private static void assert_equals (Exceptional<String> result, String value)
    {
        // TODO utils message supplier
        assertTrue(result.is_value());
        assertEquals(result.get(), value);
    }

    // ---------------------------------------------------------------------------------------------
    
    private static <T> void assert_problem (Exceptional<T> result, String msg)
    {
        // TODO utils message supplier
        assertTrue(result.is_exception());
        assertEquals(result.exception().getMessage(), msg);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_parse_floating()
    {
        assert_equals(parse_floating("42."),           42.);
        assert_equals(parse_floating(".42"),           .42);
        assert_equals(parse_floating("4_2."),          4_2.);
        assert_equals(parse_floating(".4__2"),         .4__2);
        assert_equals(parse_floating("42f"),           42f);
        assert_equals(parse_floating("42d"),           42d);
        assert_equals(parse_floating(".42f"),          .42f);
        assert_equals(parse_floating("42e42"),         42e42);
        assert_equals(parse_floating("42e+4_2"),       42e+4_2);
        assert_equals(parse_floating(".42e42"),        .42e42);
        assert_equals(parse_floating(".42e38f"),       .42e38f);
        assert_equals(parse_floating("42.42e+24f"),    42.42e+24f);
        assert_equals(parse_floating("42.e-42F"),      42.e-42F);
        assert_equals(parse_floating("42e42"),         42e42);
        assert_equals(parse_floating("42e+4_2"),       42e+4_2);
        assert_equals(parse_floating("0x8p0"),         0x8p0);
        assert_equals(parse_floating("0x8p8"),         0x8p8);
        assert_equals(parse_floating("0x8p0_8"),       0x8p0_8);
        assert_equals(parse_floating("0x8.8p0"),       0x8.8p0);
        assert_equals(parse_floating("0x8.8p0d"),      0x8.8p0d);
        assert_equals(parse_floating("0x8p0f"),        0x8p0f);

        assert_equals(parse_floating("0f"),             0f);
        assert_equals(parse_floating("0d"),             0.0);
        assert_equals(parse_floating("0e999999999f"),   0f);
        assert_equals(parse_floating("0e999999999"),    0.0);
        assert_equals(parse_floating("0x0p0f"),         0f);
        assert_equals(parse_floating("0x0p0"),          0.0);
        assert_equals(parse_floating("0x0p999999999f"), 0f);
        assert_equals(parse_floating("0x0p999999999"),  0.0);
        
        assert_problem(parse_floating(".42e-48f"),
                "Float literal is too small.");
        assert_problem(parse_floating("42.42e+42f"),
                "Float literal is too big.");
        assert_problem(parse_floating("0.1e-999"),
                "Double literal is too small.");
        assert_problem(parse_floating("42e999"),
                "Double literal is too big.");

        assert_problem(parse_floating("0x42p-999f"),
                "Float literal is too small.");
        assert_problem(parse_floating("0x42p999f"),
                "Float literal is too big.");
        assert_problem(parse_floating("0x42p-9999"),
                "Double literal is too small.");
        assert_problem(parse_floating("0x42p9999"),
                "Double literal is too big.");
    }

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings({"OctalInteger", "LongLiteralEndingWithLowercaseL"})
    @Test public void test_parse_integer()
    {
        assert_equals(parse_integer("0"),         0);
        assert_equals(parse_integer("0L"),        0L);
        assert_equals(parse_integer("42"),        42);
        assert_equals(parse_integer("4_2"),       4_2);
        assert_equals(parse_integer("42l"),       42l);
        assert_equals(parse_integer("4_2L"),      4_2L);
        assert_equals(parse_integer("0x8"),       0x8);
        assert_equals(parse_integer("0x1_8"),     0x1_8);
        assert_equals(parse_integer("0111"),      0111);
        assert_equals(parse_integer("0111L"),     0111L);
        assert_equals(parse_integer("01_1__1"),   01_1__1);
        assert_equals(parse_integer("0B111"),     0B111);
        assert_equals(parse_integer("0b1_0__1L"), 0b1_0__1L);

        assert_problem(parse_integer("9999999999"),
            "Integer literal is too big.");
        assert_problem(parse_integer("9999999999999999999L"),
            "Long literal is too big.");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_unescape()
    {
        assert_equals(unescape("\\t"),                  "\t");
        assert_equals(unescape("\\\\"),                 "\\");
        assert_equals(unescape("\\u07FF"),              "\u07FF");
        assert_equals(unescape("\\177"),                "\177");
        assert_equals(unescape("aa\\tbb"),              "aa\tbb");
        assert_equals(unescape("aa\\\\bb"),             "aa\\bb");
        assert_equals(unescape("aa\\u07FFbb"),          "aa\u07FFbb");
        assert_equals(unescape("aa\\177bb"),            "aa\177bb");
        assert_equals(unescape("\\t\\\\\\u07FF\\177"),  "\t\\\u07FF\177");
        assert_equals(unescape("\\777"),                "\777");

        assert_problem(unescape("\\u07"),       "Illegal hex escape in string.");
        assert_problem(unescape("\\u07xxx"),    "Illegal hex escape in string.");
        assert_problem(unescape("\\m"),         "Illegal escape in string.");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_escape()
    {
        assertEquals(escape("\t"),              "\\t");
        assertEquals(escape("\\"),              "\\\\");
        assertEquals(escape("\u0006"),          "\\u0006");
        assertEquals(escape("\6"),              "\\u0006");
        assertEquals(escape("aa\tbb"),          "aa\\tbb");
        assertEquals(escape("aa\\bb"),          "aa\\\\bb");
        assertEquals(escape("aa\u0006bb"),      "aa\\u0006bb");
        assertEquals(escape("aa\6bb"),          "aa\\u0006bb");
        assertEquals(escape("\t\\\u0006\6"),    "\\t\\\\\\u0006\\u0006");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_trim_leading_whitespace()
    {
        assertEquals(trim_leading_whitespace(""),                               "");
        assertEquals(trim_leading_whitespace(" \t\n"),                          "");
        assertEquals(trim_leading_whitespace("// xxx"),                         "");
        assertEquals(trim_leading_whitespace("  // xxx\n\n "),                  "");
        assertEquals(trim_leading_whitespace("/* xxx */"),                      "");
        assertEquals(trim_leading_whitespace("/* /* xxx */"),                   "");
        assertEquals(trim_leading_whitespace("  /* xxx */\n\n "),               "");
        assertEquals(trim_leading_whitespace("  /* xxx */ // xx \n// ooo\n "),  "");

        assertEquals(trim_leading_whitespace("a"),                              "a");
        assertEquals(trim_leading_whitespace(" \t\na"),                         "a");
        assertEquals(trim_leading_whitespace("  // xxx\n\n a"),                 "a");
        assertEquals(trim_leading_whitespace("/* xxx */a"),                     "a");
        assertEquals(trim_leading_whitespace("/* /* xxx */a"),                  "a");
        assertEquals(trim_leading_whitespace("  /* xxx */\n\n a"),              "a");
        assertEquals(trim_leading_whitespace("  /* xxx */ // xx \n// ooo\n a"), "a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void test_trim_trailing_whitespace()
    {
        assertEquals(trim_trailing_whitespace(""),                               "");
        assertEquals(trim_trailing_whitespace(" \t\n"),                          "");
        assertEquals(trim_trailing_whitespace("// xxx"),                         "");
        assertEquals(trim_trailing_whitespace("  // xxx\n\n "),                  "");
        assertEquals(trim_trailing_whitespace("/* xxx */"),                      "");
        assertEquals(trim_trailing_whitespace("/* /* xxx */"),                   "");
        assertEquals(trim_trailing_whitespace("  /* xxx */\n\n "),               "");
        assertEquals(trim_trailing_whitespace("  /* xxx */ // xx \n// ooo\n "),  "");

        assertEquals(trim_trailing_whitespace("a"),                               "a");
        assertEquals(trim_trailing_whitespace("a \t\n"),                          "a");
        assertEquals(trim_trailing_whitespace("a// xxx"),                         "a");
        assertEquals(trim_trailing_whitespace("a  // xxx\n\n "),                  "a");
        assertEquals(trim_trailing_whitespace("a/* xxx */"),                      "a");
        assertEquals(trim_trailing_whitespace("a/* /* xxx */"),                   "a");
        assertEquals(trim_trailing_whitespace("a  /* xxx */\n\n "),               "a");
        assertEquals(trim_trailing_whitespace("a  /* xxx */ // xx \n// ooo\n "),  "a");
    }

    // ---------------------------------------------------------------------------------------------
}
