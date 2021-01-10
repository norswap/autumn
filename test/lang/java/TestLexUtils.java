package lang.java;

import norswap.utils.exceptions.Exceptional;
import org.testng.Assert;
import org.testng.annotations.Test;

import static norswap.lang.java.LexUtils.*;
import static org.testng.Assert.assertTrue;

public final class TestLexUtils
{
    // ---------------------------------------------------------------------------------------------

    private static void assertEquals (Exceptional<Number> result, double value)
    {
        assertTrue(result.isValue());
        Assert.assertEquals(result.get().doubleValue(), value);
    }

    // ---------------------------------------------------------------------------------------------

    private static void assertEquals (Exceptional<Number> result, long value)
    {
        assertTrue(result.isValue());
        Assert.assertEquals(result.get().longValue(), value);
    }

    // ---------------------------------------------------------------------------------------------

    private static void assertEquals (Exceptional<String> result, String value)
    {
        assertTrue(result.isValue());
        Assert.assertEquals(result.get(), value);
    }

    // ---------------------------------------------------------------------------------------------
    
    private static <T> void assertProblem (Exceptional<T> result, String msg)
    {
        assertTrue(result.isException());
        Assert.assertEquals(result.exception().getMessage(), msg);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testParseFloating()
    {
        assertEquals(parse_floating("42."),           42.);
        assertEquals(parse_floating(".42"),           .42);
        assertEquals(parse_floating("4_2."),          4_2.);
        assertEquals(parse_floating(".4__2"),         .4__2);
        assertEquals(parse_floating("42f"),           42f);
        assertEquals(parse_floating("42d"),           42d);
        assertEquals(parse_floating(".42f"),          .42f);
        assertEquals(parse_floating("42e42"),         42e42);
        assertEquals(parse_floating("42e+4_2"),       42e+4_2);
        assertEquals(parse_floating(".42e42"),        .42e42);
        assertEquals(parse_floating(".42e38f"),       .42e38f);
        assertEquals(parse_floating("42.42e+24f"),    42.42e+24f);
        assertEquals(parse_floating("42.e-42F"),      42.e-42F);
        assertEquals(parse_floating("42e42"),         42e42);
        assertEquals(parse_floating("42e+4_2"),       42e+4_2);
        assertEquals(parse_floating("0x8p0"),         0x8p0);
        assertEquals(parse_floating("0x8p8"),         0x8p8);
        assertEquals(parse_floating("0x8p0_8"),       0x8p0_8);
        assertEquals(parse_floating("0x8.8p0"),       0x8.8p0);
        assertEquals(parse_floating("0x8.8p0d"),      0x8.8p0d);
        assertEquals(parse_floating("0x8p0f"),        0x8p0f);

        assertEquals(parse_floating("0f"),             0f);
        assertEquals(parse_floating("0d"),             0.0);
        assertEquals(parse_floating("0e999999999f"),   0f);
        assertEquals(parse_floating("0e999999999"),    0.0);
        assertEquals(parse_floating("0x0p0f"),         0f);
        assertEquals(parse_floating("0x0p0"),          0.0);
        assertEquals(parse_floating("0x0p999999999f"), 0f);
        assertEquals(parse_floating("0x0p999999999"),  0.0);
        
        assertProblem(parse_floating(".42e-48f"),
                "Float literal is too small.");
        assertProblem(parse_floating("42.42e+42f"),
                "Float literal is too big.");
        assertProblem(parse_floating("0.1e-999"),
                "Double literal is too small.");
        assertProblem(parse_floating("42e999"),
                "Double literal is too big.");

        assertProblem(parse_floating("0x42p-999f"),
                "Float literal is too small.");
        assertProblem(parse_floating("0x42p999f"),
                "Float literal is too big.");
        assertProblem(parse_floating("0x42p-9999"),
                "Double literal is too small.");
        assertProblem(parse_floating("0x42p9999"),
                "Double literal is too big.");
    }

    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings({"OctalInteger", "LongLiteralEndingWithLowercaseL"})
    @Test public void testParseInteger()
    {
        assertEquals(parse_integer("0"),         0);
        assertEquals(parse_integer("0L"),        0L);
        assertEquals(parse_integer("42"),        42);
        assertEquals(parse_integer("4_2"),       4_2);
        assertEquals(parse_integer("42l"),       42l);
        assertEquals(parse_integer("4_2L"),      4_2L);
        assertEquals(parse_integer("0x8"),       0x8);
        assertEquals(parse_integer("0x1_8"),     0x1_8);
        assertEquals(parse_integer("0111"),      0111);
        assertEquals(parse_integer("0111L"),     0111L);
        assertEquals(parse_integer("01_1__1"),   01_1__1);
        assertEquals(parse_integer("0B111"),     0B111);
        assertEquals(parse_integer("0b1_0__1L"), 0b1_0__1L);

        assertProblem(parse_integer("9999999999"),
            "Integer literal is too big.");
        assertProblem(parse_integer("9999999999999999999L"),
            "Long literal is too big.");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testUnescape()
    {
        assertEquals(unescape("\\t"),                  "\t");
        assertEquals(unescape("\\\\"),                 "\\");
        assertEquals(unescape("\\u07FF"),              "\u07FF");
        assertEquals(unescape("\\177"),                "\177");
        assertEquals(unescape("aa\\tbb"),              "aa\tbb");
        assertEquals(unescape("aa\\\\bb"),             "aa\\bb");
        assertEquals(unescape("aa\\u07FFbb"),          "aa\u07FFbb");
        assertEquals(unescape("aa\\177bb"),            "aa\177bb");
        assertEquals(unescape("\\t\\\\\\u07FF\\177"),  "\t\\\u07FF\177");
        assertEquals(unescape("\\777"),                "\777");

        assertProblem(unescape("\\u07"),       "Illegal hex escape in string.");
        assertProblem(unescape("\\u07xxx"),    "Illegal hex escape in string.");
        assertProblem(unescape("\\m"),         "Illegal escape in string.");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testEscape()
    {
        Assert.assertEquals(escape("\t"),              "\\t");
        Assert.assertEquals(escape("\\"),              "\\\\");
        Assert.assertEquals(escape("\u0006"),          "\\u0006");
        Assert.assertEquals(escape("\6"),              "\\u0006");
        Assert.assertEquals(escape("aa\tbb"),          "aa\\tbb");
        Assert.assertEquals(escape("aa\\bb"),          "aa\\\\bb");
        Assert.assertEquals(escape("aa\u0006bb"),      "aa\\u0006bb");
        Assert.assertEquals(escape("aa\6bb"),          "aa\\u0006bb");
        Assert.assertEquals(escape("\t\\\u0006\6"),    "\\t\\\\\\u0006\\u0006");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testTrimLeadingWhitespace()
    {
        Assert.assertEquals(trim_leading_whitespace(""),                               "");
        Assert.assertEquals(trim_leading_whitespace(" \t\n"),                          "");
        Assert.assertEquals(trim_leading_whitespace("// xxx"),                         "");
        Assert.assertEquals(trim_leading_whitespace("  // xxx\n\n "),                  "");
        Assert.assertEquals(trim_leading_whitespace("/* xxx */"),                      "");
        Assert.assertEquals(trim_leading_whitespace("/* /* xxx */"),                   "");
        Assert.assertEquals(trim_leading_whitespace("  /* xxx */\n\n "),               "");
        Assert.assertEquals(trim_leading_whitespace("  /* xxx */ // xx \n// ooo\n "),  "");

        Assert.assertEquals(trim_leading_whitespace("a"),                              "a");
        Assert.assertEquals(trim_leading_whitespace(" \t\na"),                         "a");
        Assert.assertEquals(trim_leading_whitespace("  // xxx\n\n a"),                 "a");
        Assert.assertEquals(trim_leading_whitespace("/* xxx */a"),                     "a");
        Assert.assertEquals(trim_leading_whitespace("/* /* xxx */a"),                  "a");
        Assert.assertEquals(trim_leading_whitespace("  /* xxx */\n\n a"),              "a");
        Assert.assertEquals(trim_leading_whitespace("  /* xxx */ // xx \n// ooo\n a"), "a");
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testTrimTrailingWhitespace()
    {
        Assert.assertEquals(trim_trailing_whitespace(""),                               "");
        Assert.assertEquals(trim_trailing_whitespace(" \t\n"),                          "");
        Assert.assertEquals(trim_trailing_whitespace("// xxx"),                         "");
        Assert.assertEquals(trim_trailing_whitespace("  // xxx\n\n "),                  "");
        Assert.assertEquals(trim_trailing_whitespace("/* xxx */"),                      "");
        Assert.assertEquals(trim_trailing_whitespace("/* /* xxx */"),                   "");
        Assert.assertEquals(trim_trailing_whitespace("  /* xxx */\n\n "),               "");
        Assert.assertEquals(trim_trailing_whitespace("  /* xxx */ // xx \n// ooo\n "),  "");

        Assert.assertEquals(trim_trailing_whitespace("a"),                               "a");
        Assert.assertEquals(trim_trailing_whitespace("a \t\n"),                          "a");
        Assert.assertEquals(trim_trailing_whitespace("a// xxx"),                         "a");
        Assert.assertEquals(trim_trailing_whitespace("a  // xxx\n\n "),                  "a");
        Assert.assertEquals(trim_trailing_whitespace("a/* xxx */"),                      "a");
        Assert.assertEquals(trim_trailing_whitespace("a/* /* xxx */"),                   "a");
        Assert.assertEquals(trim_trailing_whitespace("a  /* xxx */\n\n "),               "a");
        Assert.assertEquals(trim_trailing_whitespace("a  /* xxx */ // xx \n// ooo\n "),  "a");
        Assert.assertEquals(trim_trailing_whitespace("a\n//comment\n\t"),                "a");
    }

    // ---------------------------------------------------------------------------------------------
}
