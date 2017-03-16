package norswap.whimsy.test
import norswap.autumn.Grammar
import norswap.autumn.test.*
import norswap.utils.lines
import norswap.whimsy.Attribute
import norswap.whimsy.Node
import norswap.whimsy.Reactor
import norswap.whimsy.ReactorError
import org.testng.Assert.*
import org.testng.ITestResult
import org.testng.annotations.AfterMethod

/**
 *  A superclass for test class that wish to test the behaviour of a reactor.
 *
 *  Since it is often easier to go through a parse step to get a tree of nodes, this class extends
 *  [GrammarFixture] and redefines.
 */
abstract class GrammarReactorFixture: GrammarFixture()
{
    // ---------------------------------------------------------------------------------------------

    override val g: Grammar = EmptyGrammar()

    // ---------------------------------------------------------------------------------------------

    private var reactor = Reactor()

    // ---------------------------------------------------------------------------------------------

    var testing_for_error = false

    // ---------------------------------------------------------------------------------------------

    /**
     * Initializes the reactor with rules.
     */
    abstract fun Reactor.init()

    // ---------------------------------------------------------------------------------------------

    fun parse (input: String): Node
    {
        success(input)
        val root = g.stack.peek() as Node
        reactor = Reactor()
        reactor.init()
        reactor.visit_root(root)
        reactor.derive()
        return root
    }

    // ---------------------------------------------------------------------------------------------

    fun attr (input: String, name: String, value: Any?)
    {
        testing_for_error = false
        val root = parse(input)
        assertEquals(root.raw(name), value)
    }

    // ---------------------------------------------------------------------------------------------

    fun root_error (input: String, name: String, klass: Class<out ReactorError>)
    {
        testing_for_error = true
        val root = parse(input)
        success(input)
        val errors = reactor.errors()
        assertEquals(errors.size, 1)
        assertTrue(errors[0].affected.contains(Attribute(root, name)))
        assertEquals(errors[0]::class.java, klass)
    }

    // ---------------------------------------------------------------------------------------------

    @AfterMethod
    fun diagnostic () // (result: ITestResult)
    {
        if (testing_for_error) return
        val errors = reactor.errors()
        if (errors.isEmpty()) return
        println(errors.lines())
    }

    // ---------------------------------------------------------------------------------------------
}