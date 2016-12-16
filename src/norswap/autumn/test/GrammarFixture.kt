package norswap.autumn.test
import norswap.autumn.*
import org.testng.Assert.*
import org.testng.Reporter

// =================================================================================================

/**
 * A superclass for test class that wish to test the behaviour of a parser.
 *
 * The test class must define [g], the grammar that will be associated with it.
 *
 * The test class can use the [top_fun] and [top_val] methods to define the parser to be tested.
 * Afterward, it calls predicate methods (methods starting with `success` or with `failure`).
 * These methods trigger the parse of the input they have received, and verify that the expected
 * results are observed.
 */
abstract class GrammarFixture
{
    // ---------------------------------------------------------------------------------------------

    var top: Parser = { true }

    // ---------------------------------------------------------------------------------------------

    /**
     * The grammar used for parsing in predicate methods.
     * This grammar is reset at the beginning of each predicate method.
     */
    abstract val g: Grammar

    // ---------------------------------------------------------------------------------------------

    /**
     * Whether it is allowed for the grammar to report issues.
     * If false, success predicate methods will fail if the grammar reports any issue.
     */
    private var allow_issues = false

    // ---------------------------------------------------------------------------------------------

    /**
     * Specify the parser to run in predicate methods. The code for [f] is treated as the right
     * side of a field assignment in a grammar (the grammar becomes the receiver).
     */
    inline fun top_val (f: Grammar.() -> Parser)
    {
        top = g.f()
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Specify the parser to run when in predicate methods. The code for [f] is treated as the body
     * of a method definition in a grammar (the grammar becomes the receiver).
     */
    inline fun top_fun (crossinline f: Grammar.() -> Boolean)
    {
        top = { g.f() }
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should succeed on the provided input.
     */
    fun success (input: String)
    {
        g.reset()

        try {
            assertTrue(g.parse(ParseInput(input), false, top))
        }
        catch (e: AssertionError)
        {
            val failure = g.failure
            val position = g.offsetToString(g.fail_pos)

            Reporter.log("\nfailure at ($position): " + failure?.invoke(), true)

            if (failure is UncaughtException)
                failure.e.printStackTrace()

            if (failure is CaughtException)
                failure.e.printStackTrace()

            throw e
        }

        assertEquals(g.pos, input.length)
        if (!allow_issues) assertTrue(g.issues.isEmpty())
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should succeed on the provided input. Additionally the top of the
     * stack should match [value].
     */
    fun success_expect (input: String, value: Any?)
    {
        success(input)
        assertEquals(g.stack.peek(), value)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should succeed on the provided input, and that
     * the grammar should report a single issue matching [issue].
     */
    fun success_issue (input: String, issue: String)
    {
        allow_issues = true
        success(input)
        assertEquals(g.issues.size, 1)
        assertEquals(g.issues[0](), issue)
        allow_issues = false
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should succeed on the provided input, and that
     * the grammar should report a single issue matching [issue]. Additionally the top of the
     * stack should match [value].
     */
    fun success_issue (input: String, value: Any, issue: String)
    {
        allow_issues = true
        success_expect(input, value)
        assertEquals(g.issues.size, 1)
        assertEquals(g.issues[0](), issue)
        allow_issues = false
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should fail on the provided input.
     */
    fun failure (input: String)
    {
        g.reset()
        assertFalse(g.parse(ParseInput(input), false, top))
        assertEquals(g.pos, 0)
        assertNotEquals(g.fail_pos, -1)
        assertNotNull(g.failure)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should fail on the provided input, at the provided position.
     */
    fun failure_at (input: String, err_pos: Int)
    {
        failure(input)
        assertEquals(g.fail_pos, err_pos)
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should fail on the provided input, at the provided position,
     * with the provided failure.
     */
    fun failure_at (input: String, err_pos: Int, failure: Failure)
    {
        failure(input)
        assertEquals(g.fail_pos, err_pos)
        assertEquals(g.failure!!(), failure())
    }

    // ---------------------------------------------------------------------------------------------

    /**
     * Asserts that the top parser should fail on the provided input, at the provided position,
     * with a failure indicating that the string [missed] could not be matched.
     */
    fun failure_expect (input: String, err_pos: Int, missed: String)
    {
        failure_at(input, err_pos, NoString(missed))
    }
}

// =================================================================================================

/**
 * A grammar whose root always succeeds consuming no input.
 */
class EmptyGrammar: Grammar()
{
    override fun root() = true
}

// =================================================================================================

/**
 * A [GrammarFixture] with an empty grammar.
 */
abstract class EmptyGrammarFixture: GrammarFixture()
{
    override val g = EmptyGrammar()
}

// =================================================================================================
