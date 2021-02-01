import norswap.autumn.Autumn;
import norswap.autumn.Grammar;
import norswap.autumn.MalformedGrammarError;
import norswap.autumn.ParseOptions;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

public class TestWellFormedness extends Grammar
{
    // ---------------------------------------------------------------------------------------------

    @Override public rule root() {
        throw new Error();
    }

    // ---------------------------------------------------------------------------------------------

    public rule leftRecursive = choice(
        seq(lazy(() -> this.leftRecursive), "a"),
        "a");

    public rule emptyStringRepetition = str("").at_least(1);

    public rule optRepetition = opt("a").at_least(0);

    public rule nullableRepetitionRepetition = str("a").at_least(0).at_least(0);

    public rule anonymousLeftRecursive = choice(
        seq(lazy(() -> this.anonymousLeftRecursive), "a"),
        "a");

    // ---------------------------------------------------------------------------------------------

    private void assertThrown(rule rule, String name) {
        boolean thrown = false;
        try {
            Autumn.parse(rule, "aaa", ParseOptions.get());
        } catch (MalformedGrammarError e) {
            thrown = true;
        }
        assertTrue("MalformedGrammarError not thrown for " + name, thrown);
    }

    // ---------------------------------------------------------------------------------------------

    @Test public void testLeftRecursion() {
        assertThrown(leftRecursive, "left-recursive parser");
    }

    @Test public void testAnonymousLeftRecursive() {
        // This used to fail because of infinite recursion in Parse#toString.
        assertThrown(anonymousLeftRecursive, "left-recursive parser");
    }

    @Test public void testEmptyStringRepetition() {
        assertThrown(emptyStringRepetition, "empty string repetition");
    }

    @Test public void testOptReptition() {
        assertThrown(optRepetition, "repetition of optional parser");
    }

    @Test public void testNullableReptitionRepetition() {
        assertThrown(nullableRepetitionRepetition, "repetition of nullable repetition");
    }

    // ---------------------------------------------------------------------------------------------
}
