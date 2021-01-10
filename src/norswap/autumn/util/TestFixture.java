package norswap.autumn.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A parent class for test classes that implements a few handy assertion methods.
 *
 * <p>The key point of this compared to the usual frameworks is the ability to trim the stack traces
 * of the thrown assertion errors, as well as other exceptions (see below and {@link
 * #trimStackTrace}). This is fully compatible with TestNG (maybe with JUnit, haven't checked).
 *
 * <p>If you want to trim other stack traces in a similar fashion, use {@link
 * #trimStackTrace(Throwable, int)}.
 *
 * <p>The assertion methods should be pretty obvious and repetitive and so they are not documented
 * individually. Here are a few precisions that apply to them.
 *
 * <p>Whenever an integer {@code peel} parameter is present, it indicates that this many items
 * should be removed from the bottom of the stack trace (most recently called methods) of the thrown
 * assertion error.
 *
 * <p>All assertion methods take care of peeling themselves off (as only the assertion call site
 * is really interesting), so you do not need to account for them in {@code peel}.
 *
 * <p>Also see the documentation of {@link #traceSeparator} and {@link #bottomClass} for
 * further stack trace customization.
 *
 * <p>There are two ways to use the fixture: either make the test class inherit it, or instantiate
 * it and call its methods directly. In the latter case, you should re-assign {@link
 * #bottomClass}.</p>
 *
 * <p>Whenever the assertion message is supplied by a {@link Supplier}, the supplier is only
 * called if the assertion is violated, and only once.
 *
 * <p>In assertion methods names, equals refers to the {@link Object#equals} while "same" refers
 * to identity comparison (I reused the JUnit/TestNG terminology).
 *
 * <p>For equality comparisons, the values being compared are added on a new line after the supplied
 * error message, in the same format that TestNG uses - making it compatible with its plugin
 * (although for some reason the plugin only offer values comparisons when equality constraints are
 * violated, not different constraints). Might be compatible with JUnit as well, but I haven't
 * checked. Pull requests welcome.
 *
 * <p>In a perfect world, this would belong in a separate library (in fact, it is copied from my
 * personal utilities library). However, it is part of the public API of Autumn, and having it
 * here enables having the Javadoc.
 */
public class TestFixture
{
    // ---------------------------------------------------------------------------------------------

    /**
     * A separator to be added at the end of assertion error messages, to separate them from the
     * stack trace of the assertion error itself. Especially handy if the error message
     * ends with indented items itself. Defaults to the empty string.
     */
    public String traceSeparator = "";

    // ---------------------------------------------------------------------------------------------

    /**
     * If this is non-null, everything in a reported stack trace that would appear <b>under</b>
     * (i.e. "called earlier than") the last occurence of this class will be removed.
     *
     * <p>This does not impact further stack trace pruning (at the top) via a {@code peel} parameter.
     *
     * <p>By default, this is initialized to the {@code this.getClass()} as it is customary for test
     * classes to extend {@code TestFixture}. However, if you use the fixture as a stand-alone
     * object, you'll need to change this field, or the stack traces won't show where the assertion
     * occured (as the call site must necessarily appear under calls in {@code TestFixture}).
     */
    public Class<?> bottomClass = this.getClass();

    // ---------------------------------------------------------------------------------------------

    /**
     * Trims the stack trace of the given throwable, removing {@code peel} stack trace elements at
     * the top of the stack trace (the most recently called methods), and removes all stack trace
     * elements under the last occurence of {@link #bottomClass}, if it isn't null.
     */
    public void trimStackTrace (Throwable t, int peel)
    {
        StackTraceElement[] trace = t.getStackTrace();
        int newEnd = trace.length;

        for (int i = trace.length - 1; i >= 0; --i)
            if (trace[i].getClassName().equals(bottomClass.getName())) {
                newEnd = i + 1;
                break;
            }

        t.setStackTrace(Arrays.copyOfRange(trace, peel, newEnd));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Throws an {@link AssertionError} with the given message. Removes itself and {@code peel}
     * additional stack trace elements at the top of the stack trace, and honors the {@link
     * #bottomClass} setting.
     */
    public void throwAssertion (int peel, String msg)
    {
        AssertionError error = new AssertionError(msg + traceSeparator);
        trimStackTrace(error, peel + 1);
        throw error;
    }

    // ---------------------------------------------------------------------------------------------

    public void assertTrue (boolean condition, int peel, Supplier<String> msg)
    {
        if (!condition) throwAssertion(peel + 1, msg.get());
    }

    // ---------------------------------------------------------------------------------------------

    public void assertTrue (boolean condition, Supplier<String> msg)
    {
        if (!condition) throwAssertion(1, msg.get());
    }

    // ---------------------------------------------------------------------------------------------

    public void assertTrue (boolean condition, String msg)
    {
        if (!condition) throwAssertion(1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertTrue (boolean condition)
    {
        if (!condition) throwAssertion(1, "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertEquals (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (!Objects.deepEquals(actual, expected))
            throwAssertion(peel + 1,
                msg.get() + "\nexpected [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertEquals (Object actual, Object expected, Supplier<String> msg)
    {
        assertEquals(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertEquals (Object actual, Object expected, String msg)
    {
        assertEquals(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertEquals (Object actual, Object expected)
    {
        assertEquals(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotEquals (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (Objects.deepEquals(actual, expected))
            throwAssertion(peel + 1,
                msg.get() + "\nexpected not same [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotEquals (Object actual, Object expected, Supplier<String> msg)
    {
        assertNotEquals(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotEquals (Object actual, Object expected, String msg)
    {
        assertNotEquals(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotEquals (Object actual, Object expected)
    {
        assertNotEquals(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertSame (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (actual != expected)
            throwAssertion(peel + 1,
                msg.get() + "\nexpected [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertSame (Object actual, Object expected, Supplier<String> msg)
    {
        assertSame(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertSame (Object actual, Object expected, String msg)
    {
        assertSame(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertSame (Object actual, Object expected)
    {
        assertSame(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotSame (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (actual == expected)
            throwAssertion(peel + 1,
                msg.get() + "\nexpected not same [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotSame (Object actual, Object expected, Supplier<String> msg)
    {
        assertNotSame(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotSame (Object actual, Object expected, String msg)
    {
        assertNotSame(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assertNotSame (Object actual, Object expected)
    {
        assertNotSame(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------
}
