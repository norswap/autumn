package norswap.autumn.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A parent class for test classes that implements a few handy assertion methods.
 *
 * <p>The key point of this compared to the usual frameworks is the ability to trim the stack traces
 * of the thrown assertion errors, as well as other exceptions (see below and {@link
 * #trim_stack_trace}). This is fully compatible with TestNG (maybe with JUnit, haven't checked).
 *
 * <p>If you want to trim other stack traces in a similar fashion, use {@link
 * #trim_stack_trace(Throwable, int)}.
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
 * <p>Also see the documentation of {@link #trace_separator} and {@link #bottom_class} for
 * further stack trace customization.
 *
 * <p>There are two ways to use the fixture: either make the test class inherit it, or instantiate
 * it and call its methods directly. In the latter case, you should re-assign {@link
 * #bottom_class}.</p>
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
    public String trace_separator = "";

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
    public Class<?> bottom_class = this.getClass();

    // ---------------------------------------------------------------------------------------------

    /**
     * Trims the stack trace of the given throwable, removing {@code peel} stack trace elements at
     * the top of the stack trace (the most recently called methods), and removes all stack trace
     * elements under the last occurence of {@link #bottom_class}, if it isn't null.
     */
    public void trim_stack_trace (Throwable t, int peel)
    {
        StackTraceElement[] trace = t.getStackTrace();
        int new_end = trace.length;

        for (int i = trace.length - 1; i >= 0; --i)
            if (trace[i].getClassName().equals(bottom_class.getName())) {
                new_end = i + 1;
                break;
            }

        t.setStackTrace(Arrays.copyOfRange(trace, peel, new_end));
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Throws an {@link AssertionError} with the given message. Removes itself and {@code peel}
     * additional stack trace elements at the top of the stack trace, and honors the {@link
     * #bottom_class} setting.
     */
    public void throw_assertion (int peel, String msg)
    {
        AssertionError error = new AssertionError(msg + trace_separator);
        trim_stack_trace(error, peel + 1);
        throw error;
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition, int peel, Supplier<String> msg)
    {
        if (!condition) throw_assertion(peel + 1, msg.get());
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition, Supplier<String> msg)
    {
        if (!condition) throw_assertion(1, msg.get());
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition, String msg)
    {
        if (!condition) throw_assertion(1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_true (boolean condition)
    {
        if (!condition) throw_assertion(1, "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (!Objects.deepEquals(actual, expected))
            throw_assertion(peel + 1,
                msg.get() + "\nexpected [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected, Supplier<String> msg)
    {
        assert_equals(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected, String msg)
    {
        assert_equals(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_equals (Object actual, Object expected)
    {
        assert_equals(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_equals (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (Objects.deepEquals(actual, expected))
            throw_assertion(peel + 1,
                msg.get() + "\nexpected not same [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_equals (Object actual, Object expected, Supplier<String> msg)
    {
        assert_not_equals(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_equals (Object actual, Object expected, String msg)
    {
        assert_not_equals(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_equals (Object actual, Object expected)
    {
        assert_not_equals(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_same (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (actual != expected)
            throw_assertion(peel + 1,
                msg.get() + "\nexpected [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_same (Object actual, Object expected, Supplier<String> msg)
    {
        assert_same(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_same (Object actual, Object expected, String msg)
    {
        assert_same(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_same (Object actual, Object expected)
    {
        assert_same(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_same (Object actual, Object expected, int peel, Supplier<String> msg)
    {
        if (actual == expected)
            throw_assertion(peel + 1,
                msg.get() + "\nexpected not same [" + expected + "] but found [" + actual + "]");
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_same (Object actual, Object expected, Supplier<String> msg)
    {
        assert_not_same(actual, expected, 1, msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_same (Object actual, Object expected, String msg)
    {
        assert_not_same(actual, expected, 1, () -> msg);
    }

    // ---------------------------------------------------------------------------------------------

    public void assert_not_same (Object actual, Object expected)
    {
        assert_not_same(actual, expected, 1, () -> "");
    }

    // ---------------------------------------------------------------------------------------------
}
